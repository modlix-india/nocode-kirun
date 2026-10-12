//! Port of kirun-js `json/schema/convertor/BooleanConvertor.ts`.

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::validator::schema_validator::SchemaPath;
use crate::json::schema::Schema;
use crate::util::json::convertor_util::ConvertorUtil;
use crate::{Result, Value};

pub struct BooleanConvertor;

impl BooleanConvertor {
    /// JS `BooleanConvertor.convert(parents, schema, mode, element)`. Unconvertible values in STRICT mode
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
                "Expected a boolean but found null",
            );
        }

        match Self::get_boolean_primitive(element) {
            Some(b) => Ok(Value::Bool(b)),
            None => ConvertorUtil::handle_un_convertible_value_with_default(
                parents,
                Some(mode),
                element,
                Self::get_default(schema),
                "Unable to convert to boolean",
            ),
        }
    }

    fn get_boolean_primitive(element: &Value) -> Option<bool> {
        match element {
            Value::Bool(b) => Some(*b),
            Value::String(s) => Self::handle_string_value(s),
            Value::Number(n) => Self::handle_number_value(n.as_f64()?),
            _ => None,
        }
    }

    fn handle_string_value(value: &str) -> Option<bool> {
        match value.to_lowercase().trim() {
            "true" | "t" | "yes" | "y" | "1" => Some(true),
            "false" | "f" | "no" | "n" | "0" => Some(false),
            _ => None,
        }
    }

    fn handle_number_value(value: f64) -> Option<bool> {
        if value == 0.0 || value == 1.0 {
            Some(value == 1.0)
        } else {
            None
        }
    }

    fn get_default(schema: &Schema) -> Value {
        match schema.get_default_value() {
            Some(v) if !v.is_null() => v.clone(),
            _ => Value::Bool(false),
        }
    }
}
