//! Port of kirun-js `json/schema/validator/NullValidator.ts`; the value in the message is JSON, as
//! kirun-java prints it.

use crate::json::schema::validator::schema_validator::{java_json, SchemaPath};
use crate::json::schema::Schema;
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
            format!("Expected a null but found {}", java_json(&element)),
        ))
    }
}
