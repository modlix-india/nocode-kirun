//! Port of validator/ArrayValidatorTest.ts.

use kirun::json::schema::validator::SchemaValidator;
use kirun::repository::KIRunSchemaRepository;
use kirun::Value;
use serde_json::json;

use crate::common::assert_json_eq;
use crate::schema_from;

#[tokio::test]
async fn schema_array_validator_tuple_schema_test_for_additional_items_with_boolean_different_datatype() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": [
            { "type": "INTEGER" },
            { "type": "STRING" },
            { "type": "BOOLEAN" },
            { "type": "OBJECT" },
        ],
        "additionalItems": {
            "schemaValue": { "type": "OBJECT" },
        },
    }));

    let obj = json!([1, "asd", { "val": "stringtype" }, "stringOnemore"]);

    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj).await.is_err());
}

#[tokio::test]
async fn schema_array_validator_tuple_schema_test_for_additional_items_with_boolean_true_datatype() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": [
            { "type": "STRING" },
            { "type": "BOOLEAN" },
        ],
        "additionalItems": {
            "schemaValue": { "type": "OBJECT" },
        },
    }));

    let obj = json!(["asd", true, { "a": "b" }]);

    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj.clone()).await.unwrap(),
        obj
    );
}

#[tokio::test]
async fn schema_array_validator_test_for_additional_items_with_boolean_false_different_datatype() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": { "type": "INTEGER" },
        "additionalItems": { "booleanValue": false },
    }));
    let obj = json!([1, 2, 3, 4, "stringtype", true]);

    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj).await.is_err());
}

#[tokio::test]
async fn schema_array_validator_test_for_additional_items_with_boolean_true_different_datatype() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": { "type": "INTEGER" },
        "additionalItems": {
            "schemaValue": { "type": "STRING" },
        },
    }));

    let obj = json!([1, 2, 3, "stringtype", true]);

    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj).await.is_err());
}

// Same JS name as the first test; this one has a 3-item tuple.
#[tokio::test]
async fn schema_array_validator_tuple_schema_test_for_additional_items_with_boolean_different_datatype_2() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": [
            { "type": "INTEGER" },
            { "type": "STRING" },
            { "type": "BOOLEAN" },
        ],
        "additionalItems": {
            "schemaValue": { "type": "OBJECT" },
        },
    }));

    let obj = json!([1, "asd", { "val": "stringtype" }, "stringOnemore"]);

    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj).await.is_err());
}

#[tokio::test]
async fn multi_level_validation_inner_object_pollution_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": {
            "type": "OBJECT",
            "properties": {
                "x": { "type": "INTEGER" },
            },
        },
        "defaultValue": [{ "x": 20 }, { "x": 30 }],
    }));

    let xschema = schema_from(json!({
        "type": "ARRAY",
        "items": {
            "type": "OBJECT",
            "properties": {
                "x": { "type": "INTEGER" },
                "y": { "type": "STRING", "defaultValue": "Kiran" },
            },
            "required": ["x"],
        },
    }));

    let value = SchemaValidator::validate(
        None,
        Some(&xschema),
        Some(&repo),
        SchemaValidator::validate(None, Some(&schema), Some(&repo), Value::Null).await.unwrap(),
    )
    .await
    .unwrap();

    // JS `toBeUndefined()`: the schema's default value must not have been polluted with `y`.
    assert_eq!(schema.get_default_value().unwrap()[0]["y"], Value::Null);
    assert_json_eq!(value[0]["y"].clone(), json!("Kiran"));
}
