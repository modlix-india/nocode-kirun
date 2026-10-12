//! Port of kirun-js `util/json/ValidatorUtil.ts`.

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::validator::schema_validator::SchemaPath;
use crate::{KIRunError, Result, Value};

pub struct ValidatorUtil;

impl ValidatorUtil {
    /// JS `handleValidationError`: STRICT (the default) fails with a `SchemaValidation` error,
    /// LENIENT gives `null`, USE_DEFAULT the default and SKIP the element unchanged.
    pub fn handle_validation_error<P: SchemaPath + ?Sized>(
        parents: &P,
        mode: Option<ConversionMode>,
        element: &Value,
        default_value: Value,
        error_message: &str,
    ) -> Result<Value> {
        match mode.unwrap_or(ConversionMode::Strict) {
            ConversionMode::Strict => {
                Err(KIRunError::schema_validation(parents.schema_path(), error_message))
            }
            ConversionMode::Lenient => Ok(Value::Null),
            ConversionMode::UseDefault => Ok(default_value),
            ConversionMode::Skip => Ok(element.clone()),
        }
    }
}
