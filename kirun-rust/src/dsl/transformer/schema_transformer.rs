//! Port of kirun-js `dsl/transformer/SchemaTransformer.ts`. `type` may be a scalar, a
//! single-entry array or a union array (the kirun-py fix): only a single type is "simple".

use crate::util::primitive::js_stringify;
use crate::{Result, Value};

const PRIMITIVE_TYPES: [&str; 9] = ["INTEGER", "LONG", "FLOAT", "DOUBLE", "STRING", "BOOLEAN", "NULL", "ANY", "OBJECT"];

pub struct SchemaTransformer;

impl SchemaTransformer {
    /// JS `transform(schemaSpec)`: DSL schema text (or a JSON object) → schema JSON.
    pub fn transform(schema_spec: &Value) -> Result<Value> {
        let Value::String(spec) = schema_spec else {
            return Ok(schema_spec.clone());
        };
        let spec = spec.trim();
        if let Some(inner) = spec.strip_prefix("ARRAY OF ") {
            return Ok(serde_json::json!({
                "type": "ARRAY",
                "items": Self::transform(&Value::String(inner.trim().to_string()))?,
            }));
        }
        if PRIMITIVE_TYPES.contains(&spec) {
            return Ok(serde_json::json!({ "type": spec }));
        }
        // Unknown schema specification: treated as STRING.
        Ok(serde_json::json!({ "type": "STRING" }))
    }

    /// The schema's type when it has exactly one (`"STRING"` or `["STRING"]`).
    fn sole_type(schema: &serde_json::Map<String, Value>) -> Option<&str> {
        match schema.get("type")? {
            Value::String(s) => Some(s),
            Value::Array(a) if a.len() == 1 => a[0].as_str(),
            _ => None,
        }
    }

    /// Schema JSON → DSL text (`STRING`, `ARRAY OF INTEGER`, or JSON for anything not simple).
    pub fn to_text(schema: &Value) -> String {
        let Value::Object(obj) = schema else {
            return crate::util::primitive::js_to_string(schema);
        };
        let sole = Self::sole_type(obj);
        // Only a bare `{type, items}` shortens to ARRAY OF; anything else (e.g. a stored
        // `version`) prints as JSON, like kirun-js, so the round trip keeps it.
        if sole == Some("ARRAY") && obj.len() == 2 {
            if let Some(items) = obj.get("items").filter(|i| !i.is_null()) {
                return format!("ARRAY OF {}", Self::to_text(items));
            }
        }
        if let Some(t) = sole.filter(|t| PRIMITIVE_TYPES.contains(t)) {
            if obj.len() == 1 {
                return t.to_string();
            }
        }
        js_stringify(schema)
    }

    pub fn is_simple_schema(schema: &Value) -> bool {
        let Value::Object(obj) = schema else {
            return false;
        };
        let sole = Self::sole_type(obj);
        if sole.is_some_and(|t| PRIMITIVE_TYPES.contains(&t)) && obj.len() == 1 {
            return true;
        }
        if sole == Some("ARRAY") {
            if let Some(items) = obj.get("items").filter(|i| !i.is_null()) {
                return Self::is_simple_schema(items);
            }
        }
        false
    }
}
