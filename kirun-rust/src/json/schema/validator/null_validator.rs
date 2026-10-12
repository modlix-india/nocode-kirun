//! Port of kirun-js `json/schema/validator/NullValidator.ts`.

use crate::json::schema::validator::schema_validator::SchemaPath;
use crate::json::schema::Schema;
use crate::util::primitive::js_to_string;
use crate::{KIRunError, Result, Value};

pub struct NullValidator;

impl NullValidator {
    /// JS `NullValidator.validate(parents, schema, element)`.
    pub fn validate(parents: &[Schema], schema: &Schema, element: Value) -> Result<Value> {
        Self::validate_in(parents, schema, element)
    }

    pub(crate) fn validate_in<P: SchemaPath + ?Sized>(parents: &P, _schema: &Schema, element: Value) -> Result<Value> {
        if element.is_null() {
            return Ok(element);
        }
        Err(KIRunError::schema_validation(
            parents.schema_path(),
            format!("Expected a null but found {}", js_to_string(&element)),
        ))
    }
}
