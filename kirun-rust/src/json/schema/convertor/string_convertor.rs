//! Port of kirun-js `json/schema/convertor/StringConvertor.ts`.

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::validator::schema_validator::SchemaPath;
use crate::json::schema::Schema;
use crate::util::json::convertor_util::ConvertorUtil;
use crate::util::primitive::js_to_string;
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

        // JS `element ?? JSON.stringify(...)` keeps the element itself (never null here), then
        // calls `toString()` on it: objects become "[object Object]", arrays join with ",".
        let value = js_to_string(element);
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
