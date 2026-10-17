//! Port of kirun-js `json/schema/validator/BooleanValidator.ts`, with kirun-java's message for a
//! value that is not a boolean (`<value> is not a boolean`, the value as JSON).

use crate::json::schema::validator::schema_validator::{java_json, SchemaPath};
use crate::json::schema::Schema;
use crate::{KIRunError, Result, Value};

pub struct BooleanValidator;

impl BooleanValidator {
    /// JS `BooleanValidator.validate(parents, schema, element)`.
    pub fn validate(parents: &[Schema], schema: &Schema, element: Value) -> Result<Value> {
        Self::validate_in(parents, schema, element)
    }

    pub(crate) fn validate_in<P: SchemaPath + ?Sized>(parents: &P, _schema: &Schema, element: Value) -> Result<Value> {
        match element {
            Value::Null => Err(KIRunError::schema_validation(parents.schema_path(), "Expected a boolean but found null")),
            Value::Bool(_) => Ok(element),
            _ => Err(KIRunError::schema_validation(
                parents.schema_path(),
                format!("{} is not a boolean", java_json(&element)),
            )),
        }
    }
}

