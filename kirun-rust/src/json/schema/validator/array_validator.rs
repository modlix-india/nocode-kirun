//! Port of kirun-js `json/schema/validator/ArrayValidator.ts`.

use crate::json::schema::array::ArraySchemaType;
use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::validator::schema_validator::{java_json, js_deep_equal, SchemaParents};
use crate::json::schema::validator::SchemaValidator;
use crate::json::schema::Schema;
use crate::repository::Repository;
use crate::{KIRunError, Result, Value};

pub struct ArrayValidator;

impl ArrayValidator {
    /// JS `ArrayValidator.validate(parents, schema, repository, element)`.
    pub async fn validate(
        parents: &[Schema],
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        element: Value,
    ) -> Result<Value> {
        Self::validate_with(parents, schema, repository, element, false, None).await
    }

    /// JS `ArrayValidator.validate(..., convert, mode)`.
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
        let mut array = match element {
            Value::Null => {
                return Err(KIRunError::schema_validation(parents.path(), "Expected an array but found null"))
            }
            Value::Array(a) => a,
            other => {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    // kirun-java's wording: `<value> is not an Array`, the value as JSON.
                    format!("{} is not an Array", java_json(&other)),
                ))
            }
        };

        Self::check_min_max_items(parents, schema, &array)?;

        Self::check_items(parents, schema, repository, &mut array, convert, mode).await?;

        Self::check_unique_items(parents, schema, &array)?;

        Self::check_contains(schema, parents, repository, &array).await?;

        Ok(Value::Array(array))
    }

    async fn check_contains(
        schema: &Schema,
        parents: &SchemaParents,
        repository: Option<&dyn Repository<Schema>>,
        array: &[Value],
    ) -> Result<()> {
        if schema.get_contains().is_none() {
            return Ok(());
        }

        let count = Self::count_contains(
            parents,
            schema,
            repository,
            array,
            schema.get_min_contains().is_none() && schema.get_max_contains().is_none(),
        )
        .await;

        if count == 0 {
            return Err(KIRunError::schema_validation(
                parents.path(),
                "None of the items are of type contains schema",
            ));
        }

        if let Some(min) = schema.get_min_contains() {
            if *min > count {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    format!(
                        "The minimum number of the items of type contains schema should be {min} but found {count}"
                    ),
                ));
            }
        }

        if let Some(max) = schema.get_max_contains() {
            if *max < count {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    format!(
                        "The maximum number of the items of type contains schema should be {max} but found {count}"
                    ),
                ));
            }
        }

        Ok(())
    }

    async fn count_contains(
        parents: &SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        array: &[Value],
        stop_on_first: bool,
    ) -> usize {
        let mut count = 0;
        for item in array {
            let mut new_parents = parents.clone();
            if SchemaValidator::validate_in(&mut new_parents, schema.get_contains(), repository, item.clone(), false, None)
                .await
                .is_ok()
            {
                count += 1;
                if stop_on_first {
                    break;
                }
            }
        }
        count
    }

    /// kirun-java puts the items in a `HashSet<JsonElement>`: objects and arrays compare by
    /// content (kirun-js's `new Set` keeps every object and array as unique).
    fn check_unique_items(parents: &SchemaParents, schema: &Schema, array: &[Value]) -> Result<()> {
        if schema.get_unique_items() == Some(&true) {
            for (i, v) in array.iter().enumerate() {
                if array[..i].iter().any(|s| js_deep_equal(s, v)) {
                    return Err(KIRunError::schema_validation(parents.path(), "Items on the array are not unique"));
                }
            }
        }
        Ok(())
    }

    fn check_min_max_items(parents: &SchemaParents, schema: &Schema, array: &[Value]) -> Result<()> {
        if let Some(min) = schema.get_min_items().copied() {
            if min > array.len() {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    format!("Array should have minimum of {min} elements"),
                ));
            }
        }

        // kirun-java checks a maximum of 0 too (kirun-js skips a falsy one).
        if let Some(max) = schema.get_max_items().copied() {
            if max < array.len() {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    format!("Array can have  maximum of {max} elements"),
                ));
            }
        }
        Ok(())
    }

    async fn check_items(
        parents: &SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        array: &mut Vec<Value>,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<()> {
        let Some(r#type) = schema.get_items() else {
            return Ok(());
        };

        if let Some(single) = r#type.get_single_schema() {
            for item in array.iter_mut() {
                let mut new_parents = parents.clone();
                *item = SchemaValidator::validate_in(
                    &mut new_parents,
                    Some(single),
                    repository,
                    std::mem::take(item),
                    convert,
                    mode,
                )
                .await?;
            }
        }

        if let Some(tuple) = r#type.get_tuple_schema() {
            if tuple.len() != array.len() && schema.get_additional_items().is_none() {
                return Err(KIRunError::schema_validation(
                    parents.path(),
                    format!("Expected an array with only {} but found {}", tuple.len(), array.len()),
                ));
            }

            // JS does not pass convert/mode on to the tuple items.
            Self::check_items_in_tuple_schema(parents, repository, array, r#type, false, None).await?;

            Self::check_additional_items(parents, schema, repository, array, r#type).await?;
        }

        Ok(())
    }

    async fn check_items_in_tuple_schema(
        parents: &SchemaParents,
        repository: Option<&dyn Repository<Schema>>,
        array: &mut Vec<Value>,
        r#type: &ArraySchemaType,
        convert: bool,
        mode: Option<ConversionMode>,
    ) -> Result<()> {
        let tuple = r#type.get_tuple_schema().map(Vec::as_slice).unwrap_or(&[]);
        for (i, s) in tuple.iter().enumerate() {
            let mut new_parents = parents.clone();
            let item = array.get_mut(i).map(std::mem::take).unwrap_or(Value::Null);
            let v = SchemaValidator::validate_in(&mut new_parents, Some(s), repository, item, convert, mode).await?;
            // JS `array[i] = ...` grows a shorter array.
            if i >= array.len() {
                array.resize(i + 1, Value::Null);
            }
            array[i] = v;
        }
        Ok(())
    }

    async fn check_additional_items(
        parents: &SchemaParents,
        schema: &Schema,
        repository: Option<&dyn Repository<Schema>>,
        array: &mut [Value],
        r#type: &ArraySchemaType,
    ) -> Result<()> {
        let Some(additional) = schema.get_additional_items() else {
            return Ok(());
        };
        if additional.get_boolean_value() == Some(true) {
            // As additional items is true it should validate against any valid schema.
            let any_schema_type = Schema::of_any("item");
            Self::check_each_item_in_additional_items(parents, repository, array, r#type, &any_schema_type).await?;
        } else if let Some(sv) = additional.get_schema_value() {
            Self::check_each_item_in_additional_items(parents, repository, array, r#type, sv).await?;
        }
        Ok(())
    }

    async fn check_each_item_in_additional_items(
        parents: &SchemaParents,
        repository: Option<&dyn Repository<Schema>>,
        array: &mut [Value],
        r#type: &ArraySchemaType,
        schema_type: &Schema,
    ) -> Result<()> {
        let start = r#type.get_tuple_schema().map_or(0, Vec::len);
        for item in array.iter_mut().skip(start) {
            let mut new_parents = parents.clone();
            *item = SchemaValidator::validate_in(
                &mut new_parents,
                Some(schema_type),
                repository,
                std::mem::take(item),
                false,
                None,
            )
            .await?;
        }
        Ok(())
    }
}
