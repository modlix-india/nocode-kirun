//! kirun-java tells a value that is not there (Java `null`) from a JSON `null`
//! (`JsonNull`); kirun-rust, like kirun-js, has one `Value::Null` for both. Where the difference
//! shows (a `System.GenerateEvent` result that is Java `null` fails the run with Reactor's
//! `NullPointerException` "t2"), this module answers whether kirun-java's evaluator would have
//! returned Java `null` for an expression that kirun-rust evaluated to `Value::Null`.
//!
//! kirun-java returns Java `null` when the expression is a plain path (possibly in
//! parentheses) whose value is missing: a key the object does not have, an index outside the
//! array or string, or anything under a missing or null value. A path ending on a key whose
//! value is JSON `null`, and every expression with an operator (`??`, `?:`, `+`, ...), give
//! `JsonNull`. Measured against kirun-java 4.9.2.

use std::sync::LazyLock;

use regex::Regex;

use crate::runtime::expression::js;
use crate::runtime::expression::tokenextractor::token_value_extractor::ValuesMap;
use crate::runtime::expression::ExpressionEvaluator;
use crate::Value;

/// `Prefix.part.part[index]["key"]`: a path with no operator in it.
static PLAIN_PATH: LazyLock<Regex> = LazyLock::new(|| {
    Regex::new(r#"^[A-Za-z_$][A-Za-z0-9_$]*(\.[A-Za-z0-9_$]+|\[(-?\d+|"[^"\[\]]*"|'[^'\[\]]*')\])+$"#).unwrap()
});

/// Whether kirun-java evaluates `expression` to Java `null` (missing), given that kirun-rust
/// evaluated it to `Value::Null`.
///
/// kirun-java's extractors differ at the top of their stores: `Steps.<step>.<event>` of a step or
/// event not (yet) there, and `Context.<name>` of an unknown name, are JSON `null`; a missing
/// argument, or a missing key in an event's result, is Java `null`.
pub fn is_missing(expression: &str, values_map: &ValuesMap) -> bool {
    let path = strip_parentheses(expression.trim());
    if !PLAIN_PATH.is_match(path) {
        return false;
    }
    let Some(dot) = path.find('.') else { return false };
    let prefix = &path[..=dot];
    let Some(extractor) = values_map.get(prefix) else { return false };

    let parts = crate::runtime::expression::tokenextractor::token_value_extractor::split_path_ref(path);
    match prefix {
        "Steps." => {
            let store = extractor.get_store();
            let event = parts.get(1).and_then(|step| store.get(*step)).and_then(|e| parts.get(2).and_then(|n| e.get(*n)));
            if parts.len() < 4 || event.is_none() {
                return false;
            }
        }
        "Context." if parts.len() == 2 && !parts[1].contains('[') => return false,
        _ => {}
    }

    let (parent, last) = split_last(path);
    let parent_value = if parent.contains('.') || parent.contains('[') {
        ExpressionEvaluator::new(parent).evaluate(values_map).unwrap_or(Value::Null)
    } else {
        extractor.get_store()
    };
    !has_member(&parent_value, last)
}

/// `(((x)))` → `x`, only when the outer parentheses enclose the whole expression.
fn strip_parentheses(mut s: &str) -> &str {
    while s.starts_with('(') && s.ends_with(')') && encloses(s) {
        s = s[1..s.len() - 1].trim();
    }
    s
}

fn encloses(s: &str) -> bool {
    let mut depth = 0;
    for (i, c) in s.char_indices() {
        match c {
            '(' => depth += 1,
            ')' => {
                depth -= 1;
                if depth == 0 && i != s.len() - 1 {
                    return false;
                }
            }
            _ => {}
        }
    }
    true
}

/// `a.b.c` → (`a.b`, `c`); `a.b[0]` → (`a.b`, `[0]`).
fn split_last(path: &str) -> (&str, &str) {
    if path.ends_with(']') {
        if let Some(open) = path.rfind('[') {
            return (&path[..open], &path[open..]);
        }
    }
    match path.rfind('.') {
        Some(dot) => (&path[..dot], &path[dot + 1..]),
        None => (path, ""),
    }
}

/// kirun-java `TokenValueExtractor.resolveSegmentFast`: whether `segment` reads something
/// (possibly JSON `null`) out of `parent`.
fn has_member(parent: &Value, segment: &str) -> bool {
    let key = segment.strip_prefix('[').and_then(|s| s.strip_suffix(']')).unwrap_or(segment);
    let unquoted = key
        .strip_prefix('"')
        .and_then(|k| k.strip_suffix('"'))
        .or_else(|| key.strip_prefix('\'').and_then(|k| k.strip_suffix('\'')));
    if key == "length" && !parent.is_null() {
        return true;
    }
    match parent {
        Value::Object(o) => o.contains_key(unquoted.unwrap_or(key)),
        Value::Array(a) => index_in(key, a.len()),
        Value::String(s) => index_in(key, js::str_len(s)),
        _ => false,
    }
}

fn index_in(key: &str, len: usize) -> bool {
    match key.parse::<i64>() {
        Ok(i) => {
            let actual = if i < 0 { len as i64 + i } else { i };
            actual >= 0 && (actual as usize) < len
        }
        Err(_) => false,
    }
}

#[cfg(test)]
mod tests {
    use std::sync::Arc;

    use serde_json::json;

    use super::*;
    use crate::runtime::expression::tokenextractor::token_value_extractor::TokenValueExtractor;
    use crate::runtime::tokenextractor::arguments_token_value_extractor::ArgumentsTokenValueExtractor;
    use crate::Map;

    fn values(args: Value) -> ValuesMap {
        let store: Map<Value> = args.as_object().unwrap().iter().map(|(k, v)| (k.clone(), v.clone())).collect();
        let ate = ArgumentsTokenValueExtractor::new(store);
        let mut vm: ValuesMap = Map::new();
        vm.insert(ate.get_prefix().to_string(), Arc::new(ate));
        vm
    }

    #[test]
    fn missing_paths_are_java_null() {
        let vm = values(json!({ "a": { "b": null, "arr": [], "nul": [null], "s": "abc" }, "n": null }));
        assert!(is_missing("Arguments.x", &vm));
        assert!(is_missing("Arguments.a.c", &vm));
        assert!(is_missing("Arguments.x.y", &vm));
        assert!(is_missing("Arguments.a.b.c", &vm), "under a JSON null");
        assert!(is_missing("Arguments.a.arr[0]", &vm));
        assert!(is_missing("Arguments.a.s[5]", &vm));
        assert!(is_missing("( Arguments.x )", &vm));
        assert!(is_missing("Arguments.a[\"zz\"]", &vm));
        assert!(is_missing("Arguments.x.length", &vm));
    }

    #[test]
    fn steps_and_context_tops_are_json_null() {
        use crate::runtime::tokenextractor::output_map_token_value_extractor::OutputMapTokenValueExtractor;
        let mut steps: crate::Map<crate::Map<crate::Map<Value>>> = Map::new();
        let mut result = Map::new();
        result.insert("result".to_string(), json!({ "content": [] }));
        let mut events = Map::new();
        events.insert("output".to_string(), result);
        steps.insert("read".to_string(), events);
        let ome = OutputMapTokenValueExtractor::new(crate::shared(steps));
        let mut vm: ValuesMap = Map::new();
        vm.insert(ome.get_prefix().to_string(), Arc::new(ome));
        assert!(!is_missing("Steps.other.output.result", &vm), "step not run yet");
        assert!(!is_missing("Steps.read.error.data", &vm), "event not raised");
        assert!(is_missing("Steps.read.output.nope", &vm), "key not in the result");
        assert!(is_missing("Steps.read.output.result.content[0]", &vm));
        assert!(!is_missing("Steps.read.output.result.content", &vm));
    }

    #[test]
    fn present_nulls_and_operators_are_json_null() {
        let vm = values(json!({ "a": { "b": null, "nul": [null] }, "n": null }));
        assert!(!is_missing("Arguments.n", &vm));
        assert!(!is_missing("Arguments.a.b", &vm));
        assert!(!is_missing("Arguments.a.nul[0]", &vm));
        assert!(!is_missing("Arguments.a.nul[-1]", &vm));
        assert!(!is_missing("Arguments.a['b']", &vm));
        assert!(!is_missing("Arguments.x ?? Arguments.y", &vm));
        assert!(!is_missing("(Arguments.x) + (Arguments.y)", &vm));
        assert!(!is_missing("null", &vm));
        assert!(!is_missing("Nope.x", &vm), "no extractor: a literal");
        assert!(!is_missing("Arguments", &vm));
    }
}
