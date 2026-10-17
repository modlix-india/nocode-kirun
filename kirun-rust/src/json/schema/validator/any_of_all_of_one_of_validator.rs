//! Port of kirun-js `json/schema/validator/AnyOfAllOfOneOfValidator.ts`.

use futures::future::BoxFuture;

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::validator::schema_validator::{SchemaParents, SchemaValidator};
use crate::json::schema::Schema;
use crate::repository::Repository;
use crate::{KIRunError, Result, Value};

pub struct AnyOfAllOfOneOfValidator;

impl AnyOfAllOfOneOfValidator {
    /// JS `AnyOfAllOfOneOfValidator.validate(parents, schema, repository, element)`.
    pub async fn validate(
        parents: &[Schema],
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
    ) -> Result<Value> {
        Self::validate_with(parents, schema, repository, element, false, None).await
    }

    /// JS `AnyOfAllOfOneOfValidator.validate(..., convert, mode)`.
    pub async fn validate_with(
        parents: &[Schema],
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        let mut p = SchemaParents::from_slice(parents);
        Self::validate_in(&mut p, schema, repository, element, convert, mode).await
    }

    /// Checks the first non-empty of oneOf, allOf, anyOf (in that order), like kirun-java.
    pub(crate) fn validate_in<'a>(
        parents: &'a mut SchemaParents,
        schema: &'a Schema,
        repository: Option<&'a dyn Repository<Schema>>,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> BoxFuture<'a, Result<Value>> {
        Box::pin(async move {
            let mut list: Vec<KIRunError> = Vec::new();
            if schema.get_one_of().is_some_and(|v| !v.is_empty()) {
                return Self::one_of(parents, schema, repository, element, &mut list, convert, mode).await;
            } else if schema.get_all_of().is_some_and(|v| !v.is_empty()) {
                return Self::all_of(parents, schema, repository, element, &mut list, convert, mode).await;
            } else if schema.get_any_of().is_some_and(|v| !v.is_empty()) {
                return Self::any_of(parents, schema, repository, element, &mut list, convert, mode).await;
            }
            Ok(element)
        })
    }

    #[allow(clippy::too_many_arguments)]
    async fn any_of(
        parents: &mut SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
        list: &mut Vec<KIRunError>,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        let mut flag = false;
        let mut result = Value::Null;
        for s in schema.get_any_of().map(Vec::as_slice).unwrap_or(&[]) {
            match SchemaValidator::validate_in(&mut parents.clone(), Some(s), repository, element.clone(), convert, mode).await {
                Ok(v) => {
                    flag = true;
                    result = v;
                    break;
                }
                Err(e) => {
                    flag = false;
                    list.push(e);
                }
            }
        }

        if flag {
            return Ok(result);
        }

        Err(KIRunError::schema_validation_with(
            parents.path(),
            "The value don't satisfy any of the schemas.",
            std::mem::take(list),
        ))
    }

    #[allow(clippy::too_many_arguments)]
    async fn all_of(
        parents: &mut SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
        list: &mut Vec<KIRunError>,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        let mut flag = 0;
        let all_of = schema.get_all_of().map(Vec::as_slice).unwrap_or(&[]);
        for s in all_of {
            match SchemaValidator::validate_in(&mut parents.clone(), Some(s), repository, element.clone(), convert, mode).await {
                Ok(_) => flag += 1,
                Err(e) => list.push(e),
            }
        }

        if flag == all_of.len() {
            return Ok(element);
        }

        Err(KIRunError::schema_validation_with(
            parents.path(),
            // kirun-java's wording (kirun-js writes "doesn't").
            "The value don't satisfy some of the schemas.",
            std::mem::take(list),
        ))
    }

    #[allow(clippy::too_many_arguments)]
    async fn one_of(
        parents: &mut SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
        list: &mut Vec<KIRunError>,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        let mut flag = 0;
        for s in schema.get_one_of().map(Vec::as_slice).unwrap_or(&[]) {
            match SchemaValidator::validate_in(&mut parents.clone(), Some(s), repository, element.clone(), convert, mode).await {
                Ok(_) => flag += 1,
                Err(e) => list.push(e),
            }
        }

        if flag == 1 {
            return Ok(element);
        }

        Err(KIRunError::schema_validation_with(
            parents.path(),
            if flag == 0 { "The value does not satisfy any schema" } else { "The value satisfy more than one schema" },
            std::mem::take(list),
        ))
    }
}
