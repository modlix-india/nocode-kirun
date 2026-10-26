//! Port of kirun-js `json/schema/convertor/StringConvertor.ts`, printing values as kirun-java's does.

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::validator::schema_validator::SchemaPath;
use crate::json::schema::Schema;
use crate::util::json::convertor_util::ConvertorUtil;
use crate::{Result, Value};

pub struct StringConvertor;

impl StringConvertor {
    /// JS `StringConvertor.convert(parents, schema, mode, element)`. Unconvertible values in STRICT mode
    /// fail with `KIRunError::SchemaConversion`.
    pub fn convert(parents: &[Schema], schema: &Schema, mode: ConversionMode, element: &Value) -> Result<Value> {
        Self::convert_in(parents, schema, mode, element)
    }

    pub(crate) fn convert_in<P: SchemaPath + ?Sized>(
        parents: &P,
        schema: &Schema,
        mode: ConversionMode,
        element: &Value,
    ) -> Result<Value> {
        if element.is_null() {
            return ConvertorUtil::handle_un_convertible_value_with_default(
                parents,
                Some(mode),
                element,
                Self::get_default(schema),
                "Expected a string but found null",
            );
        }

        // kirun-java: a primitive's `getAsString()` (a number as written), anything else as JSON
        // (kirun-js prints "[object Object]" and joins arrays with ",").
        let value = match element {
            Value::String(s) => s.clone(),
            other => serde_json::to_string(other).unwrap_or_default(),
        };
        Ok(Value::String(Self::get_converted_string(value, mode)))
    }

    fn get_converted_string(value: String, mode: ConversionMode) -> String {
        if mode == ConversionMode::Strict {
            return value;
        }
        value.trim().to_string()
    }

    fn get_default(schema: &Schema) -> Value {
        schema.get_default_value().cloned().unwrap_or(Value::Null)
    }
}
