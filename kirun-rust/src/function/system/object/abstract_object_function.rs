//! Port of kirun-js `function/system/object/AbstractObjectFunction.ts`.
//!
//! JS uses an abstract base class that only builds the signature; in Rust the object
//! functions call [`AbstractObjectFunction::signature`] and share the key helpers below.

use std::cmp::Ordering;

use crate::json::schema::Schema;
use crate::model::{Event, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::{Map, Value};

const VALUE: &str = "value";
const SOURCE: &str = "source";

pub struct AbstractObjectFunction;

impl AbstractObjectFunction {
    /// JS `constructor(functionName, valueSchema)`.
    pub fn signature(function_name: &str, value_schema: Schema) -> FunctionSignature {
        FunctionSignature::new(function_name)
            .set_namespace(Namespaces::SYSTEM_OBJECT)
            .set_parameters(Map::from([Parameter::of_entry(SOURCE, Schema::of_any(SOURCE), false)]))
            .set_events(Map::from([Event::output_event_map_entry(Map::from([(VALUE.to_string(), value_schema)]))]))
    }
}

/// JS `Object.entries(value)`: own enumerable properties. Arrays and strings expose their
/// indices (strings by UTF-16 unit), other primitives have none.
pub(crate) fn object_entries(value: &Value) -> Vec<(String, Value)> {
    match value {
        Value::Object(m) => m.iter().map(|(k, v)| (k.clone(), v.clone())).collect(),
        Value::Array(a) => a.iter().enumerate().map(|(i, v)| (i.to_string(), v.clone())).collect(),
        Value::String(s) => s
            .encode_utf16()
            .enumerate()
            .map(|(i, u)| (i.to_string(), Value::String(String::from_utf16_lossy(&[u]))))
            .collect(),
        _ => vec![],
    }
}

/// Approximation of JS `a.localeCompare(b)` (ICU root collation): punctuation and symbols
/// before digits before letters, letters compared case-insensitively first, then lowercase
/// before uppercase.
pub(crate) fn locale_compare(a: &str, b: &str) -> Ordering {
    fn class(c: char) -> u8 {
        if c.is_alphabetic() {
            2
        } else if c.is_numeric() {
            1
        } else {
            0
        }
    }
    let primary = |s: &str| -> Vec<(u8, String)> {
        s.chars().map(|c| (class(c), c.to_lowercase().collect::<String>())).collect()
    };
    primary(a).cmp(&primary(b)).then_with(|| {
        // Tertiary: lowercase sorts before uppercase.
        let tertiary = |s: &str| -> Vec<bool> { s.chars().map(char::is_uppercase).collect() };
        tertiary(a).cmp(&tertiary(b))
    })
}
