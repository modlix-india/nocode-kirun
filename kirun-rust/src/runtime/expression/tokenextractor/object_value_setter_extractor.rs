//! Port of kirun-js `runtime/expression/tokenextractor/ObjectValueSetterExtractor.ts`.

use super::token_value_extractor::{retrieve_element_from, split_path, TokenValueExtractor, ValuesMap};
use crate::runtime::expression::js;
use crate::runtime::expression::operation::Operation;
use crate::util::duplicate::duplicate;
use crate::util::string::string_formatter::StringFormatter;
use crate::{KIRunError, Result, Value};

pub struct ObjectValueSetterExtractor {
    store: Value,
    prefix: String,
}

fn runtime_err(format: &str, param: &str) -> KIRunError {
    KIRunError::runtime(StringFormatter::format(format, &[param]))
}

fn empty_for(next_op: Operation) -> Value {
    if next_op == Operation::OBJECT_OPERATOR {
        Value::Object(serde_json::Map::new())
    } else {
        Value::Array(Vec::new())
    }
}

/// JS `isArrayIndex`: `/^-?\d+$/`.
fn is_array_index(segment: &str) -> bool {
    let b = segment.strip_prefix('-').unwrap_or(segment);
    !b.is_empty() && b.chars().all(|c| c.is_ascii_digit())
}

fn strip_quotes(segment: &str) -> String {
    let quoted = segment.len() >= 2
        && ((segment.starts_with('"') && segment.ends_with('"'))
            || (segment.starts_with('\'') && segment.ends_with('\'')));
    if quoted {
        segment[1..segment.len() - 1].to_string()
    } else {
        segment.to_string()
    }
}

fn get_op_for_segment(segment: &str) -> Operation {
    if is_array_index(segment) || segment.starts_with('[') {
        Operation::ARRAY_OPERATOR
    } else {
        Operation::OBJECT_OPERATOR
    }
}

/// JS `parseBracketSegments`: `addresses[0]` -> `["addresses", "0"]`.
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

fn array_index(mem: &str) -> Result<usize> {
    let index = js::parse_int(mem).ok_or_else(|| runtime_err("Expected an array index but found $", mem))?;
    if index < 0.0 {
        return Err(runtime_err("Array index is out of bound - $", mem));
    }
    Ok(index as usize)
}

fn get_data_from_array<'a>(el: &'a mut Value, mem: &str, next_op: Operation) -> Result<&'a mut Value> {
    let Value::Array(arr) = el else {
        return Err(runtime_err("Expected an array but found $", &js::to_js_string(el)));
    };
    let index = array_index(mem)?;
    if arr.len() <= index {
        arr.resize(index + 1, Value::Null);
    }
    if arr[index].is_null() {
        arr[index] = empty_for(next_op);
    }
    Ok(&mut arr[index])
}

fn get_data_from_object<'a>(el: &'a mut Value, mem: &str, next_op: Operation) -> Result<&'a mut Value> {
    let Value::Object(obj) = el else {
        return Err(runtime_err("Expected an object but found $", &js::to_js_string(el)));
    };
    let je = obj.entry(mem.to_string()).or_insert(Value::Null);
    if je.is_null() {
        *je = empty_for(next_op);
    }
    Ok(je)
}

fn step<'a>(el: &'a mut Value, segment: &str, next_op: Operation) -> Result<&'a mut Value> {
    if is_array_index(segment) && el.is_array() {
        get_data_from_array(el, segment, next_op)
    } else {
        get_data_from_object(el, &strip_quotes(segment), next_op)
    }
}

fn put_data_in_array(el: &mut Value, mem: &str, value: Value, overwrite: bool, delete_on_null: bool) -> Result<()> {
    let Value::Array(arr) = el else {
        return Err(runtime_err("Expected an array but found $", &js::to_js_string(el)));
    };
    let index = array_index(mem)?;
    if overwrite || arr.get(index).map_or(true, Value::is_null) {
        if delete_on_null && value.is_null() {
            if index < arr.len() {
                arr.remove(index);
            }
        } else {
            if arr.len() <= index {
                arr.resize(index + 1, Value::Null);
            }
            arr[index] = value;
        }
    }
    Ok(())
}

fn put_data_in_object(el: &mut Value, mem: &str, value: Value, overwrite: bool, delete_on_null: bool) -> Result<()> {
    let Value::Object(obj) = el else {
        return Err(runtime_err("Expected an object but found $", &js::to_js_string(el)));
    };
    if overwrite || obj.get(mem).map_or(true, Value::is_null) {
        if delete_on_null && value.is_null() {
            obj.shift_remove(mem);
        } else {
            obj.insert(mem.to_string(), value);
        }
    }
    Ok(())
}

impl ObjectValueSetterExtractor {
    pub fn new(store: Value, prefix: impl Into<String>) -> Self {
        Self { store, prefix: prefix.into() }
    }

    pub fn set_store(&mut self, store: Value) -> &mut Self {
        self.store = store;
        self
    }

    /// JS `setValue(token, value, overwrite = true, deleteOnNull = false)`.
    pub fn set_value(&mut self, token: &str, value: Value, overwrite: bool, delete_on_null: bool) -> Result<()> {
        self.store = duplicate(&self.store);
        self.modify_store(token, value, overwrite, delete_on_null)
    }

    fn modify_store(&mut self, string_token: &str, value: Value, overwrite: bool, delete_on_null: bool) -> Result<()> {
        let parts = split_path(string_token);

        if parts.len() < 2 {
            return Err(runtime_err("Invalid path: $", string_token));
        }

        let mut el: &mut Value = &mut self.store;

        for i in 1..parts.len() - 1 {
            let segments = parse_bracket_segments(&parts[i]);
            for j in 0..segments.len() {
                let next_op = if j == segments.len() - 1 {
                    get_op_for_segment(if i == parts.len() - 2 { &parts[parts.len() - 1] } else { &parts[i + 1] })
                } else if is_array_index(&segments[j + 1]) {
                    Operation::ARRAY_OPERATOR
                } else {
                    Operation::OBJECT_OPERATOR
                };
                el = step(el, &segments[j], next_op)?;
            }
        }

        let final_segments = parse_bracket_segments(&parts[parts.len() - 1]);
        for j in 0..final_segments.len() - 1 {
            let next_op = if is_array_index(&final_segments[j + 1]) {
                Operation::ARRAY_OPERATOR
            } else {
                Operation::OBJECT_OPERATOR
            };
            el = step(el, &final_segments[j], next_op)?;
        }

        let last_segment = &final_segments[final_segments.len() - 1];
        if is_array_index(last_segment) && el.is_array() {
            put_data_in_array(el, last_segment, value, overwrite, delete_on_null)
        } else {
            put_data_in_object(el, &strip_quotes(last_segment), value, overwrite, delete_on_null)
        }
    }
}

impl TokenValueExtractor for ObjectValueSetterExtractor {
    fn get_prefix(&self) -> &str {
        &self.prefix
    }

    fn get_store(&self) -> Value {
        self.store.clone()
    }

    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        let parts = split_path(token);
        retrieve_element_from(token, &parts, 1, &self.store, values_map)
    }
}
