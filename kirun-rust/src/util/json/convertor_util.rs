//! Port of kirun-js `util/json/ConvertorUtil.ts`.

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::validator::schema_validator::SchemaPath;
use crate::util::primitive::js_to_string;
use crate::{KIRunError, Result, Value};

pub struct ConvertorUtil;

impl ConvertorUtil {
    /// JS `handleUnConvertibleValue` (default value `null`).
    pub fn handle_un_convertible_value<P: SchemaPath + ?Sized>(
        parents: &P,
        mode: Option<ConversionMode>,
        element: &Value,
        error_message: &str,
    ) -> Result<Value> {
        Self::handle_un_convertible_value_with_default(parents, mode, element, Value::Null, error_message)
    }

    /// JS `handleUnConvertibleValueWithDefault`: STRICT fails with a `SchemaConversion` error,
    /// LENIENT gives `null`, USE_DEFAULT the default and SKIP the element unchanged.
    pub fn handle_un_convertible_value_with_default<P: SchemaPath + ?Sized>(
        parents: &P,
        mode: Option<ConversionMode>,
        element: &Value,
        default_value: Value,
        error_message: &str,
    ) -> Result<Value> {
        let mode = mode.unwrap_or(ConversionMode::Strict);
        match mode {
            ConversionMode::Strict => Err(KIRunError::SchemaConversion {
                schema_path: parents.schema_path(),
                source_value: js_to_string(element),
                message: error_message.to_string(),
                mode: Some(mode.name().to_string()),
                causes: Vec::new(),
            }),
            ConversionMode::Lenient => Ok(Value::Null),
            ConversionMode::UseDefault => Ok(default_value),
            ConversionMode::Skip => Ok(element.clone()),
        }
    }
}
