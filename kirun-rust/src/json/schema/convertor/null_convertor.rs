//! Port of kirun-js `json/schema/convertor/NullConvertor.ts`.

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::validator::schema_validator::SchemaPath;
use crate::json::schema::Schema;
use crate::util::json::convertor_util::ConvertorUtil;
use crate::{Result, Value};

pub struct NullConvertor;

impl NullConvertor {
    /// JS `NullConvertor.convert(parents, schema, mode, element)`. Unconvertible values in STRICT mode
    /// fail with `KIRunError::SchemaConversion`.
    pub fn convert(parents: &[Schema], schema: &Schema, mode: ConversionMode, element: &Value) -> Result<Value> {
        Self::convert_in(parents, schema, mode, element)
    }

    pub(crate) fn convert_in<P: SchemaPath + ?Sized>(
        parents: &P,
        _schema: &Schema,
        mode: ConversionMode,
        element: &Value,
    ) -> Result<Value> {
        if element.is_null() {
            return Ok(Value::Null);
        }

        if let Value::String(s) = element {
            if s.to_lowercase() == "null" {
                return Ok(Value::Null);
            }
        }

        ConvertorUtil::handle_un_convertible_value_with_default(
            parents,
            Some(mode),
            element,
            Value::Null,
            "Unable to convert to null",
        )
    }
}
