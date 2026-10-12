//! Port of kirun-js `runtime/expression/tokenextractor/TokenValueExtractor.ts`.

use std::borrow::Cow;
use std::sync::Arc;

use crate::runtime::expression::exception::expression_evaluation_exception;
use crate::runtime::expression::js;
use crate::util::string::string_formatter::StringFormatter;
use crate::{KIRunError, Map, Result, Value};

/// Prefix (`Arguments.`, `Steps.`, `Context.`, `Page.` ...) → extractor.
pub type ValuesMap = Map<Arc<dyn TokenValueExtractor>>;

/// JS `TokenValueExtractor`. JS hands extractors the values map via `setValuesMap` before
/// evaluating (so `a[Steps.x.y]` can resolve the inner path); Rust passes it explicitly.
pub trait TokenValueExtractor: Send + Sync {
    fn get_prefix(&self) -> &str;

    fn get_store(&self) -> Value;

    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value>;

    /// JS `getValue(token)`.
    fn get_value(&self, token: &str) -> Result<Value> {
        self.get_value_with(token, None)
    }

    /// JS `getValue(token)` after `setValuesMap(valuesMap)`.
    fn get_value_with(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        get_value_base(self, token, values_map)
    }
}

/// The shared body of JS `TokenValueExtractor.getValue` (prefix check, `.__index`).
pub fn get_value_base<T: TokenValueExtractor + ?Sized>(
    extractor: &T,
    token: &str,
    values_map: Option<&ValuesMap>,
) -> Result<Value> {
    let token = normalize_root_bracket(token);
    let token: &str = &token;
    let prefix = extractor.get_prefix();

    if !token.starts_with(prefix) {
        return Err(KIRunError::runtime(StringFormatter::format(
            "Token $ doesn't start with $",
            &[&token, prefix],
        )));
    }

    if let Some(parent_part) = token.strip_suffix(".__index") {
        let parent_value = extractor.get_value_internal(parent_part, values_map)?;

        if let Some(v) = parent_value.as_object().and_then(|o| o.get("__index")) {
            if !v.is_null() {
                return Ok(v.clone());
            }
        }
        if parent_part.ends_with(']') {
            let open = parent_part.rfind('[').map_or(0, |i| i + 1);
            let index_string = &parent_part[open..parent_part.len() - 1];
            return Ok(match js::parse_int(index_string) {
                Some(n) => js::num(n),
                None => Value::String(index_string.to_string()),
            });
        }
        let dot = parent_part.rfind('.').map_or(0, |i| i + 1);
        return Ok(Value::String(parent_part[dot..].to_string()));
    }

    extractor.get_value_internal(token, values_map)
}

/// JS `TokenValueExtractor.normalizeRootBracket`: `Parent[0]` becomes `Parent.[0]`.
/// Borrows the token when it needs no change (the common case).
pub fn normalize_root_bracket(token: &str) -> Cow<'_, str> {
    let Some(bracket) = token.find('[') else { return Cow::Borrowed(token) };
    if bracket == 0 {
        return Cow::Borrowed(token);
    }
    if let Some(dot) = token.find('.') {
        if dot < bracket {
            return Cow::Borrowed(token);
        }
    }
    let head = &token[..bracket];
    let mut chars = head.chars();
    let first_ok = chars.next().is_some_and(|c| c.is_ascii_alphabetic() || c == '_' || c == '$');
    if !first_ok || !chars.all(|c| c.is_ascii_alphanumeric() || c == '_' || c == '$') {
        return Cow::Borrowed(token);
    }
    Cow::Owned(format!("{head}.{}", &token[bracket..]))
}

/// JS `TokenValueExtractor.splitPath`: splits on dots outside brackets, keeping `..` ranges.
pub fn split_path(token: &str) -> Vec<String> {
    split_path_ref(token).into_iter().map(String::from).collect()
}

/// `split_path` returning slices of `token` (no allocation per part). The delimiters are ASCII,
/// so byte positions are always char boundaries.
pub fn split_path_ref(token: &str) -> Vec<&str> {
    let b = token.as_bytes();
    let mut parts = Vec::with_capacity(4);
    let mut start = 0;
    let mut in_bracket = false;

    for i in 0..b.len() {
        match b[i] {
            b'[' => in_bracket = true,
            b']' => in_bracket = false,
            b'.' if !in_bracket && !is_double_dot(b, i) => {
                if i > start {
                    parts.push(&token[start..i]);
                }
                start = i + 1;
            }
            _ => {}
        }
    }

    if start < b.len() {
        parts.push(&token[start..]);
    }
    parts
}

fn is_double_dot(b: &[u8], pos: usize) -> bool {
    (pos > 0 && b[pos - 1] == b'.') || (pos + 1 < b.len() && b[pos + 1] == b'.')
}

fn bracket_segments(segment: &str) -> impl Iterator<Item = &str> {
    segment.split(['[', ']']).map(str::trim).filter(|e| !e.is_empty())
}

fn is_integer_str(s: &str) -> bool {
    let b = s.strip_prefix('-').unwrap_or(s);
    !b.is_empty() && b.chars().all(|c| c.is_ascii_digit())
}

/// JS `TokenValueExtractor.retrieveElementFrom` (protected in JS).
pub fn retrieve_element_from<S: AsRef<str>>(
    token: &str,
    parts: &[S],
    part_number: usize,
    json_element: &Value,
    values_map: Option<&ValuesMap>,
) -> Result<Value> {
    retrieve_element_ref(token, parts, part_number, json_element, values_map).map(Cow::into_owned)
}

/// `retrieve_element_from` that walks the path by reference and clones only the value it
/// ends on (JS follows references; cloning each step made long paths and big roots costly).
pub fn retrieve_element_ref<'a, S: AsRef<str>>(
    token: &str,
    parts: &[S],
    part_number: usize,
    json_element: &'a Value,
    values_map: Option<&ValuesMap>,
) -> Result<Cow<'a, Value>> {
    let mut current: Cow<'a, Value> = Cow::Borrowed(json_element);

    for pn in part_number..parts.len() {
        if current.is_null() {
            return Ok(Cow::Owned(Value::Null));
        }
        for segment in bracket_segments(parts[pn].as_ref()) {
            current = resolve_segment_fast(token, parts, pn, segment, current, values_map)?;
            if current.is_null() {
                return Ok(Cow::Owned(Value::Null));
            }
        }
    }
    Ok(current)
}

/// A child of `current`: borrowed when `current` is, cloned otherwise.
fn project<'a>(current: Cow<'a, Value>, child: impl for<'b> Fn(&'b Value) -> Option<&'b Value>) -> Cow<'a, Value> {
    match current {
        Cow::Borrowed(v) => child(v).map_or(Cow::Owned(Value::Null), Cow::Borrowed),
        Cow::Owned(v) => Cow::Owned(child(&v).cloned().unwrap_or(Value::Null)),
    }
}

/// JS `'length' in obj ? ... : Object.keys(obj).length`.
fn object_length(o: &serde_json::Map<String, Value>) -> Value {
    match o.get("length") {
        Some(Value::Array(_)) | Some(Value::Object(_)) | None => Value::from(o.len()),
        Some(v) => v.clone(),
    }
}

fn index_into(len: usize, idx: i64) -> Option<usize> {
    let actual = if idx < 0 { len as i64 + idx } else { idx };
    if actual >= 0 && (actual as usize) < len {
        Some(actual as usize)
    } else {
        None
    }
}

fn resolve_segment_fast<'a, S: AsRef<str>>(
    token: &str,
    parts: &[S],
    part_number: usize,
    segment: &str,
    element: Cow<'a, Value>,
    values_map: Option<&ValuesMap>,
) -> Result<Cow<'a, Value>> {
    if element.is_null() {
        return Ok(Cow::Owned(Value::Null));
    }

    if segment.starts_with('"') || segment.starts_with('\'') {
        return resolve_for_each_part_of_token_with_brackets(token, parts, part_number, segment, &element, values_map)
            .map(Cow::Owned);
    }

    match &*element {
        Value::Object(o) => {
            if segment == "length" {
                return Ok(Cow::Owned(object_length(o)));
            }
            return Ok(project(element, |v| v.as_object().and_then(|o| o.get(segment))));
        }
        Value::Array(a) => {
            if segment == "length" {
                return Ok(Cow::Owned(Value::from(a.len())));
            }
            if is_integer_str(segment) {
                let idx = segment.parse::<i64>().ok().and_then(|i| index_into(a.len(), i));
                return Ok(match idx {
                    Some(i) => project(element, |v| v.as_array().and_then(|a| a.get(i))),
                    None => Cow::Owned(Value::Null),
                });
            }
        }
        Value::String(s) => {
            if segment == "length" {
                return Ok(Cow::Owned(Value::from(js::str_len(s))));
            }
            if is_integer_str(segment) {
                return Ok(Cow::Owned(
                    segment
                        .parse::<i64>()
                        .ok()
                        .and_then(|i| index_into(js::str_len(s), i))
                        .and_then(|i| js::char_at(s, i))
                        .map(Value::String)
                        .unwrap_or(Value::Null),
                ));
            }
        }
        _ => {}
    }

    resolve_for_each_part_of_token_with_brackets(token, parts, part_number, segment, &element, values_map)
        .map(Cow::Owned)
}

/// JS `resolveForEachPartOfTokenWithBrackets`.
fn resolve_for_each_part_of_token_with_brackets<S: AsRef<str>>(
    token: &str,
    parts: &[S],
    part_number: usize,
    c_part: &str,
    c_element: &Value,
    values_map: Option<&ValuesMap>,
) -> Result<Value> {
    if c_element.is_null() {
        return Ok(Value::Null);
    }

    if c_part == "length" || c_part == "\"length\"" || c_part == "'length'" {
        return get_length(token, c_element);
    }

    if c_element.is_string() || c_element.is_array() {
        return handle_array_access(token, c_part, c_element, values_map);
    }

    handle_object_access(token, parts, part_number, c_part, c_element)
}

fn get_length(token: &str, c_element: &Value) -> Result<Value> {
    match c_element {
        Value::String(s) => Ok(Value::from(js::str_len(s))),
        Value::Array(a) => Ok(Value::from(a.len())),
        Value::Object(o) => Ok(object_length(o)),
        _ => Err(expression_evaluation_exception(
            token,
            &StringFormatter::format("Length can't be found in token $", &[token]),
        )),
    }
}

fn handle_array_access(token: &str, c_part: &str, c_array: &Value, values_map: Option<&ValuesMap>) -> Result<Value> {
    let len = match c_array {
        Value::String(s) => js::str_len(s),
        Value::Array(a) => a.len(),
        _ => 0,
    };

    if let Some(dot_dot_index) = c_part.find("..") {
        let start_index = &c_part[..dot_dot_index];
        let end_index = &c_part[dot_dot_index + 2..];

        let int_start = if start_index.is_empty() { Some(0.0) } else { js::parse_int(start_index) };
        let int_end = if end_index.is_empty() { Some(len as f64) } else { js::parse_int(end_index) };

        let (Some(mut int_start), Some(mut int_end)) = (int_start, int_end) else { return Ok(Value::Null) };

        if len > 0 {
            while int_start < 0.0 {
                int_start += len as f64;
            }
            while int_end < 0.0 {
                int_end += len as f64;
            }
        } else {
            int_start = int_start.max(0.0);
            int_end = int_end.max(0.0);
        }

        let is_string = c_array.is_string();
        if int_start >= int_end {
            return Ok(if is_string { Value::String(String::new()) } else { Value::Array(vec![]) });
        }

        let (s, e) = (int_start as usize, int_end as usize);
        return Ok(match c_array {
            Value::String(st) => Value::String(js::substring(st, s, e)),
            Value::Array(a) => {
                let e = e.min(a.len());
                let s = s.min(e);
                Value::Array(a[s..e].to_vec())
            }
            _ => Value::Null,
        });
    }

    let mut index = js::parse_int(c_part);

    if index.is_none() {
        if let Some(vm) = values_map {
            if let Some(dot_index) = c_part.find('.').filter(|d| *d > 0) {
                let prefix = &c_part[..=dot_index];
                if let Some(extractor) = vm.get(prefix) {
                    if let Ok(resolved) = extractor.get_value_with(c_part, Some(vm)) {
                        match &resolved {
                            Value::Number(n) => index = n.as_f64(),
                            Value::String(s) => index = js::parse_int(s),
                            _ => {}
                        }
                    }
                }
                if index.is_none() {
                    index = Some(0.0);
                }
            }
        }
    }

    let Some(mut index) = index else {
        return Err(expression_evaluation_exception(
            token,
            &StringFormatter::format("$ is not a number", &[c_part]),
        ));
    };

    if len == 0 {
        return Ok(Value::Null);
    }
    while index < 0.0 {
        index += len as f64;
    }
    if index >= len as f64 {
        return Ok(Value::Null);
    }
    let i = index as usize;
    Ok(match c_array {
        Value::String(s) => js::char_at(s, i).map(Value::String).unwrap_or(Value::Null),
        Value::Array(a) => a[i].clone(),
        _ => Value::Null,
    })
}

fn handle_object_access<S: AsRef<str>>(
    token: &str,
    parts: &[S],
    part_number: usize,
    c_part: &str,
    c_object: &Value,
) -> Result<Value> {
    let mut c_part = c_part;
    if c_part.starts_with('"') || c_part.starts_with('\'') {
        let quote_char = c_part.chars().next().unwrap_or('"');
        if !c_part.ends_with(quote_char) || c_part.chars().count() == 1 {
            return Err(expression_evaluation_exception(
                token,
                &StringFormatter::format("$ is missing a closing quote or empty key found", &[token]),
            ));
        }
        c_part = &c_part[1..c_part.len() - 1];
    }

    check_if_object(token, parts, part_number, c_object)?;

    Ok(match c_object {
        Value::Object(o) => o.get(c_part).cloned().unwrap_or(Value::Null),
        // JS `"str"[key]`: only `length` and indices exist on a string.
        _ => Value::Null,
    })
}

/// JS `checkIfObject` (protected in JS).
pub fn check_if_object<S: AsRef<str>>(token: &str, parts: &[S], part_number: usize, json_element: &Value) -> Result<()> {
    if !(json_element.is_object() || json_element.is_string()) {
        return Err(expression_evaluation_exception(
            token,
            &StringFormatter::format(
                "Unable to retrieve $ from $ in the path $",
                &[parts.get(part_number).map(|p| p.as_ref()).unwrap_or(""), &js::to_js_string(json_element), token],
            ),
        ));
    }
    Ok(())
}

/// The JS idiom shared by the Arguments/Context/internal extractors: `parts = splitPath(token)`,
/// `key = parts[1]`, and a bracket directly on the key (`a[2]`) stays in `parts[1]`.
/// Returns `(key, parts, fromIndex)`.
pub fn key_and_parts(token: &str) -> (String, Vec<String>, usize) {
    let (key, parts, from_index) = key_and_parts_ref(token);
    (key.to_string(), parts.into_iter().map(String::from).collect(), from_index)
}

/// `key_and_parts` borrowing from `token`.
pub fn key_and_parts_ref(token: &str) -> (&str, Vec<&str>, usize) {
    let mut parts = split_path_ref(token);
    let mut key = parts.get(1).copied().unwrap_or("");
    let mut from_index = 2;
    if let Some(b_index) = key.find('[') {
        parts[1] = &key[b_index..];
        key = &key[..b_index];
        from_index = 1;
    }
    (key, parts, from_index)
}
