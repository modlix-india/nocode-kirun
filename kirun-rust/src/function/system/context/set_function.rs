//! Port of kirun-js `function/system/context/SetFunction.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::r#type::{SchemaType, TypeUtil};
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::expression::tokenextractor::token_value_extractor::split_path;
use crate::runtime::expression::ExpressionEvaluator;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::null_check::is_null_value;
use crate::util::primitive::js_to_string;
use crate::util::string::string_formatter::StringFormatter;
use crate::util::string::string_util::StringUtil;
use crate::{KIRunError, Map, Result, Value};

const NAME: &str = "name";
const VALUE: &str = "value";

struct Segment {
    value: String,
    is_array: bool,
}

pub struct SetFunction {
    signature: FunctionSignature,
}

impl SetFunction {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Set")
                .set_namespace(Namespaces::SYSTEM_CTX)
                .set_parameters(Map::from([
                    Parameter::of_entry(
                        NAME,
                        Schema::new().set_name(NAME).set_type(TypeUtil::of(&[SchemaType::String])).set_min_length(1),
                        false,
                    ),
                    Parameter::of_entry(VALUE, Schema::of_any(VALUE), false),
                ]))
                .set_events(Map::from([Event::output_event_map_entry(Map::new())])),
        }
    }

    /// Evaluate any dynamic expressions in path parts
    /// (e.g. `Context.a[Steps.loop.index]` where the index is dynamic).
    fn evaluate_dynamic_parts(&self, parts: &[String], context: &FunctionExecutionParameters) -> Vec<String> {
        parts.iter().map(|part| self.evaluate_bracket_expressions(part, context)).collect()
    }

    /// `arr[Steps.loop.index]` → `arr[0]` when `Steps.loop.index` evaluates to 0.
    fn evaluate_bracket_expressions(&self, part: &str, context: &FunctionExecutionParameters) -> String {
        let chars: Vec<char> = part.chars().collect();
        let mut result = String::new();
        let mut i = 0;

        while i < chars.len() {
            if chars[i] == '[' {
                result.push('[');
                i += 1;

                let mut bracket_content = String::new();
                let mut depth = 1;
                let mut in_quote = false;
                let mut quote_char = '\0';

                while i < chars.len() && depth > 0 {
                    let ch = chars[i];

                    if in_quote {
                        if ch == quote_char && (i == 0 || chars[i - 1] != '\\') {
                            in_quote = false;
                        }
                        bracket_content.push(ch);
                    } else if ch == '"' || ch == '\'' {
                        in_quote = true;
                        quote_char = ch;
                        bracket_content.push(ch);
                    } else if ch == '[' {
                        depth += 1;
                        bracket_content.push(ch);
                    } else if ch == ']' {
                        depth -= 1;
                        if depth > 0 {
                            bracket_content.push(ch);
                        }
                    } else {
                        bracket_content.push(ch);
                    }
                    i += 1;
                }

                let is_static = is_integer(&bracket_content)
                    || is_quoted(&bracket_content, '"')
                    || is_quoted(&bracket_content, '\'');
                if is_static {
                    result.push_str(&bracket_content);
                    result.push(']');
                } else {
                    match ExpressionEvaluator::new(&bracket_content).evaluate(context.get_values_map()) {
                        Ok(v) => {
                            result.push_str(&js_to_string(&v));
                            result.push(']');
                        }
                        Err(_) => {
                            // If evaluation fails, keep the original.
                            result.push_str(&bracket_content);
                            result.push(']');
                        }
                    }
                }
            } else {
                result.push(chars[i]);
                i += 1;
            }
        }

        result
    }

    fn modify_context_with_parts(
        &self,
        context: &FunctionExecutionParameters,
        key: &str,
        value: Value,
        parts: &[String],
    ) -> Result<FunctionOutput> {
        // parts[0] is "Context", parts[1] is the context element name.
        if parts.len() < 2 {
            return Err(KIRunError::runtime(StringFormatter::format("Context path '$' is too short", &[key])));
        }

        let first_segment_parts = parse_bracket_segments(&parts[1]);
        let context_key = first_segment_parts[0].clone();

        let not_found = || {
            KIRunError::runtime(StringFormatter::format(
                "Context doesn't have any element with name '$' ",
                &[&context_key],
            ))
        };

        let ctx = context.get_context().ok_or_else(not_found)?;
        let mut ctx = ctx.write();
        let ce = ctx.get_mut(&context_key).ok_or_else(not_found)?;

        // Just "Context.a" with no further path.
        if parts.len() == 2 && first_segment_parts.len() == 1 {
            ce.element = value;
            return Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]));
        }

        if is_null_value(&ce.element) {
            let next_is_array = if first_segment_parts.len() > 1 {
                is_array_index(&first_segment_parts[1])
            } else if parts.len() > 2 {
                is_array_access(&parts[2])
            } else {
                false
            };
            ce.element = if next_is_array { Value::Array(vec![]) } else { Value::Object(Default::default()) };
        }

        let mut all_segments: Vec<Segment> = Vec::new();
        for seg in first_segment_parts.iter().skip(1) {
            all_segments.push(Segment { value: strip_quotes(seg), is_array: is_array_index(seg) });
        }
        for part in parts.iter().skip(2) {
            for seg in parse_bracket_segments(part) {
                all_segments.push(Segment { value: strip_quotes(&seg), is_array: is_array_index(&seg) });
            }
        }

        let mut el: &mut Value = &mut ce.element;

        // Navigate to the parent of the final element.
        for i in 0..all_segments.len().saturating_sub(1) {
            let segment = &all_segments[i];
            let next_is_array = all_segments[i + 1].is_array;
            el = if segment.is_array {
                get_data_from_array(el, &segment.value, next_is_array)?
            } else {
                get_data_from_object(el, &segment.value, next_is_array)?
            };
        }

        if let Some(last) = all_segments.last() {
            if last.is_array {
                put_data_in_array(el, &last.value, value)?;
            } else {
                put_data_in_object(el, &last.value, value)?;
            }
        }

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}

impl Default for SetFunction {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for SetFunction {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let args = context.get_arguments().cloned().unwrap_or_default();
        let key = match args.get(NAME) {
            Some(Value::String(s)) => Some(s.clone()),
            Some(Value::Null) | None => None,
            Some(v) => Some(js_to_string(v)),
        };

        if StringUtil::is_null_or_blank(key.as_deref()) {
            return Err(KIRunError::runtime("Empty string is not a valid name for the context element"));
        }
        let key = key.unwrap_or_default();

        let value = args.get(VALUE).cloned().unwrap_or(Value::Null);

        let parts = split_path(&key);

        if parts.is_empty() || parts[0] != "Context" {
            return Err(KIRunError::execution(StringFormatter::format(
                "The context path $ is not a valid path in context",
                &[&key],
            )));
        }

        // Evaluate dynamic expressions in the path before taking the context lock (the
        // evaluator reads the context through the values map).
        let evaluated_parts = self.evaluate_dynamic_parts(&parts, context);

        self.modify_context_with_parts(context, &key, value, &evaluated_parts)
    }
}

/// `"arr[0]"` → `["arr", "0"]`, `"obj"` → `["obj"]`.
fn parse_bracket_segments(part: &str) -> Vec<String> {
    let chars: Vec<char> = part.chars().collect();
    let mut segments = Vec::new();
    let mut start = 0;
    let mut i = 0;

    while i < chars.len() {
        if chars[i] == '[' {
            if i > start {
                segments.push(chars[start..i].iter().collect());
            }
            let mut end = i + 1;
            let mut in_quote = false;
            let mut quote_char = '\0';
            while end < chars.len() {
                if in_quote {
                    if chars[end] == quote_char && chars[end - 1] != '\\' {
                        in_quote = false;
                    }
                } else if chars[end] == '"' || chars[end] == '\'' {
                    in_quote = true;
                    quote_char = chars[end];
                } else if chars[end] == ']' {
                    break;
                }
                end += 1;
            }
            segments.push(chars[i + 1..end.min(chars.len())].iter().collect());
            start = end + 1;
            i = start;
        } else {
            i += 1;
        }
    }

    if start < chars.len() {
        segments.push(chars[start..].iter().collect());
    }

    if segments.is_empty() {
        vec![part.to_string()]
    } else {
        segments
    }
}

/// JS `/^-?\d+$/`.
fn is_integer(s: &str) -> bool {
    let b = s.strip_prefix('-').unwrap_or(s);
    !b.is_empty() && b.chars().all(|c| c.is_ascii_digit())
}

fn is_quoted(s: &str, q: char) -> bool {
    s.starts_with(q) && s.ends_with(q)
}

fn is_array_index(segment: &str) -> bool {
    is_integer(segment)
}

fn is_array_access(part: &str) -> bool {
    part.starts_with('[') || is_array_index(part)
}

fn strip_quotes(segment: &str) -> String {
    if (segment.starts_with('"') && segment.ends_with('"')) || (segment.starts_with('\'') && segment.ends_with('\'')) {
        // JS `substring(1, length - 1)`; a lone quote gives "".
        let chars: Vec<char> = segment.chars().collect();
        if chars.len() >= 2 {
            return chars[1..chars.len() - 1].iter().collect();
        }
        return String::new();
    }
    segment.to_string()
}

/// JS `parseInt(mem)` followed by the NaN and negative checks.
fn array_index(mem: &str) -> Result<usize> {
    let t = mem.trim_start();
    let (neg, digits) = match t.strip_prefix('-') {
        Some(rest) => (true, rest),
        None => (false, t.strip_prefix('+').unwrap_or(t)),
    };
    let digits: String = digits.chars().take_while(|c| c.is_ascii_digit()).collect();
    if digits.is_empty() {
        return Err(KIRunError::runtime(StringFormatter::format(
            "Expected an array index but found $",
            &[mem],
        )));
    }
    if neg && digits.chars().any(|c| c != '0') {
        return Err(KIRunError::runtime(StringFormatter::format("Array index is out of bound - $", &[mem])));
    }
    digits
        .parse::<usize>()
        .map_err(|_| KIRunError::runtime(StringFormatter::format("Array index is out of bound - $", &[mem])))
}

fn empty_container(next_is_array: bool) -> Value {
    if next_is_array {
        Value::Array(vec![])
    } else {
        Value::Object(Default::default())
    }
}

fn get_data_from_array<'a>(el: &'a mut Value, mem: &str, next_is_array: bool) -> Result<&'a mut Value> {
    let Value::Array(arr) = el else {
        return Err(KIRunError::runtime(StringFormatter::format(
            "Expected an array but found $",
            &[&js_to_string(el)],
        )));
    };
    let index = array_index(mem)?;
    // JS assigning past the end makes a sparse array; holes read back as null here.
    if arr.len() <= index {
        arr.resize(index + 1, Value::Null);
    }
    if is_null_value(&arr[index]) {
        arr[index] = empty_container(next_is_array);
    }
    Ok(&mut arr[index])
}

fn get_data_from_object<'a>(el: &'a mut Value, mem: &str, next_is_array: bool) -> Result<&'a mut Value> {
    let Value::Object(obj) = el else {
        return Err(KIRunError::runtime(StringFormatter::format(
            "Expected an object but found $",
            &[&js_to_string(el)],
        )));
    };
    let je = obj.entry(mem.to_string()).or_insert(Value::Null);
    if is_null_value(je) {
        *je = empty_container(next_is_array);
    }
    Ok(je)
}

fn put_data_in_array(el: &mut Value, mem: &str, value: Value) -> Result<()> {
    let Value::Array(arr) = el else {
        return Err(KIRunError::runtime(StringFormatter::format(
            "Expected an array but found $",
            &[&js_to_string(el)],
        )));
    };
    let index = array_index(mem)?;
    if arr.len() <= index {
        arr.resize(index + 1, Value::Null);
    }
    arr[index] = value;
    Ok(())
}

fn put_data_in_object(el: &mut Value, mem: &str, value: Value) -> Result<()> {
    let Value::Object(obj) = el else {
        return Err(KIRunError::runtime(StringFormatter::format(
            "Expected an object but found $",
            &[&js_to_string(el)],
        )));
    };
    obj.insert(mem.to_string(), value);
    Ok(())
}
