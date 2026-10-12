//! Port of kirun-js `json/schema/validator/ObjectValidator.ts`.

use indexmap::IndexSet;

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::schema_util::SchemaUtil;
use crate::json::schema::validator::schema_validator::{format_value, SchemaParents};
use crate::json::schema::validator::string_validator::js_regex_test;
use crate::json::schema::validator::SchemaValidator;
use crate::json::schema::Schema;
use crate::repository::Repository;
use crate::{KIRunError, Map, Result, Value};

type JsonObject = serde_json::Map<String, Value>;

pub struct ObjectValidator;

impl ObjectValidator {
    /// JS `ObjectValidator.validate(parents, schema, repository, element)`.
    pub async fn validate(
        parents: &[Schema],
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
    ) -> Result<Value> {
        Self::validate_with(parents, schema, repository, element, false, None).await
    }

    /// JS `ObjectValidator.validate(..., convert, mode)`.
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

    pub(crate) async fn validate_in(
        parents: &mut SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<Value> {
        let mut json_object = match element {
            Value::Null => {
                return Err(KIRunError::schema_validation(parents.path(), "Expected an object but found null"))
            }
            Value::Object(o) => o,
            other => {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    format!("Expected an object but found {}", format_value(&other)),
                ))
            }
        };

        let mut keys: IndexSet<String> = json_object.keys().cloned().collect();

        Self::check_min_max_properties(parents, schema, &keys)?;

        if schema.get_property_names().is_some() {
            Self::check_property_name_schema(parents, schema, repository, &keys).await?;
        }

        if schema.get_required().is_some() {
            Self::check_required(parents, schema, &json_object)?;
        }

        if let Some(properties) = schema.get_properties() {
            Self::check_properties(parents, properties, repository, &mut json_object, &mut keys, convert, mode)
                .await?;
        }

        if let Some(pattern_properties) = schema.get_pattern_properties() {
            Self::check_pattern_properties(parents, pattern_properties, repository, &mut json_object, &mut keys)
                .await?;
        }

        if schema.get_additional_properties().is_some() {
            Self::check_additional_properties(parents, schema, repository, &mut json_object, &keys).await?;
        }

        Ok(Value::Object(json_object))
    }

    async fn check_property_name_schema(
        parents: &mut SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        keys: &IndexSet<String>,
    ) -> Result<()> {
        for key in keys {
            // JS hands the same parents array down here, so it grows with every key.
            let res = SchemaValidator::validate_in(
                parents,
                schema.get_property_names(),
                repository,
                Value::String(key.clone()),
                false,
                None,
            )
            .await;
            if res.is_err() {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    format!("Property name '{key}' does not fit the property schema"),
                ));
            }
        }
        Ok(())
    }

    fn check_required(parents: &SchemaParents, schema: &Schema, json_object: &JsonObject) -> Result<()> {
        for key in schema.get_required().map(Vec::as_slice).unwrap_or(&[]) {
            if json_object.get(key).is_none_or(Value::is_null) {
                let msg = schema
                    .get_properties()
                    .and_then(|p| p.get(key))
                    .and_then(|s| s.get_details())
                    .and_then(|d| d.get_validation_message("mandatory"))
                    .cloned()
                    .unwrap_or_else(|| format!("{key} is mandatory"));
                return Err(KIRunError::schema_validation(parents.path(), msg));
            }
        }
        Ok(())
    }

    async fn check_additional_properties(
        parents: &SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        json_object: &mut JsonObject,
        keys: &IndexSet<String>,
    ) -> Result<()> {
        let apt = schema.get_additional_properties().expect("checked by caller");
        if let Some(sv) = apt.get_schema_value() {
            for key in keys {
                let mut new_parents = parents.clone();
                let value = json_object.get(key).cloned().unwrap_or(Value::Null);
                let v = SchemaValidator::validate_in(&mut new_parents, Some(sv), repository, value, false, None).await?;
                json_object.insert(key.clone(), v);
            }
        } else if apt.get_boolean_value() == Some(false) && !keys.is_empty() {
            return Err(KIRunError::schema_validation(
                parents.path(),
                format!(
                    "{} is/are additional properties which are not allowed.",
                    keys.iter().cloned().collect::<Vec<_>>().join(",")
                ),
            ));
        }
        Ok(())
    }

    async fn check_pattern_properties(
        parents: &SchemaParents,
        pattern_properties: &Map<Schema>,
        repository: Option<&dyn Repository<Schema>>,
        json_object: &mut JsonObject,
        keys: &mut IndexSet<String>,
    ) -> Result<()> {
        for key in keys.clone() {
            let mut new_parents = parents.clone();
            for (pattern, pschema) in pattern_properties {
                let matched = js_regex_test(pattern, &key).map_err(|e| {
                    KIRunError::schema_validation(
                        parents.path(),
                        format!("Invalid regular expression: /{pattern}/: {e}"),
                    )
                })?;
                if matched {
                    let value = json_object.get(&key).cloned().unwrap_or(Value::Null);
                    let v = SchemaValidator::validate_in(&mut new_parents, Some(pschema), repository, value, false, None)
                        .await?;
                    json_object.insert(key.clone(), v);
                    keys.shift_remove(&key);
                    break;
                }
            }
        }
        Ok(())
    }

    #[allow(clippy::too_many_arguments)]
    async fn check_properties(
        parents: &SchemaParents,
        properties: &Map<Schema>,
        repository: Option<&dyn Repository<Schema>>,
        json_object: &mut JsonObject,
        keys: &mut IndexSet<String>,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<()> {
        for (name, pschema) in properties {
            let value = json_object.get(name).cloned();

            if value.as_ref().is_none_or(Value::is_null) && !json_object.contains_key(name) {
                let def_value = SchemaUtil::get_default_value(Some(pschema), repository).await?;
                if def_value.is_null() {
                    continue;
                }
            }

            let mut new_parents = parents.clone();
            let v = SchemaValidator::validate_in(
                &mut new_parents,
                Some(pschema),
                repository,
                value.unwrap_or(Value::Null),
                convert,
                mode,
            )
            .await?;
            json_object.insert(name.clone(), v);
            keys.shift_remove(name);
        }
        Ok(())
    }

    fn check_min_max_properties(parents: &SchemaParents, schema: &Schema, keys: &IndexSet<String>) -> Result<()> {
        if let Some(min) = schema.get_min_properties().copied().filter(|m| *m != 0) {
            if keys.len() < min {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    format!("Object should have minimum of {min} properties"),
                ));
            }
        }

        if let Some(max) = schema.get_max_properties().copied().filter(|m| *m != 0) {
            if keys.len() > max {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    format!("Object can have maximum of {max} properties"),
                ));
            }
        }
        Ok(())
    }
}
