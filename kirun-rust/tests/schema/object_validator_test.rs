//! Port of validator/ObjectValidatorTest.ts.

use std::sync::Arc;

use kirun::hybrid_repository::HybridRepository;
use kirun::json::schema::r#type::SchemaType;
use kirun::json::schema::validator::SchemaValidator;
use kirun::json::schema::Schema;
use kirun::repository::{KIRunSchemaRepository, Repository};
use kirun::Value;
use serde_json::json;

use crate::common::assert_json_eq;
use crate::{schema_from, MapSchemaRepository};

async fn validate(schema: &Schema, repo: &KIRunSchemaRepository, v: Value) -> kirun::Result<Value> {
    SchemaValidator::validate(Some(vec![]), Some(schema), Some(repo), v).await
}

#[tokio::test]
async fn schema_object_validator_test_boolean_value() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "OBJECT",
        "properties": { "name": { "type": "STRING" } },
        "additionalProperties": false,
    }));
    assert!(schema.get_type().unwrap().contains(SchemaType::Object));

    assert_json_eq!(validate(&schema, &repo, json!({ "name": "Kiran" })).await.unwrap(), json!({ "name": "Kiran" }));

    assert!(validate(&schema, &repo, json!({ "name": "Kiran", "lastName": "Grandhi" })).await.is_err());
}

#[tokio::test]
async fn schema_object_validator_test_schema_based() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "OBJECT",
        "properties": { "name": { "type": "STRING" } },
        "additionalProperties": { "type": "INTEGER" },
    }));
    assert!(schema.get_type().unwrap().contains(SchemaType::Object));

    assert_json_eq!(
        validate(&schema, &repo, json!({ "name": "Kiran", "num": 23 })).await.unwrap(),
        json!({ "name": "Kiran", "num": 23 })
    );

    assert!(validate(&schema, &repo, json!({ "name": "Kiran", "num": 23, "lastName": "grandhi" }))
        .await
        .is_err());
}

#[tokio::test]
async fn schema_object_validator_test_boolean_value_old_style() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "OBJECT",
        "properties": { "name": { "type": "STRING" } },
        "additionalProperties": { "booleanValue": false },
    }));

    assert_json_eq!(validate(&schema, &repo, json!({ "name": "Kiran" })).await.unwrap(), json!({ "name": "Kiran" }));

    assert!(validate(&schema, &repo, json!({ "name": "Kiran", "lastName": "Grandhi" })).await.is_err());
}

#[tokio::test]
async fn schema_object_validator_test_schema_based_old_style() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "OBJECT",
        "properties": { "name": { "type": "STRING" } },
        "additionalProperties": { "schemaValue": { "type": "INTEGER" } },
    }));
    assert!(schema.get_type().unwrap().contains(SchemaType::Object));

    assert_json_eq!(
        validate(&schema, &repo, json!({ "name": "Kiran", "num": 23 })).await.unwrap(),
        json!({ "name": "Kiran", "num": 23 })
    );

    assert!(validate(&schema, &repo, json!({ "name": "Kiran", "num": 23, "lastName": "grandhi" }))
        .await
        .is_err());
}

#[tokio::test]
async fn schema_object_validator_test_schema_based_old_array_style() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "OBJECT",
        "properties": { "name": { "type": "STRING" } },
        "additionalProperties": { "schemaValue": { "type": "ARRAY" } },
    }));
    assert!(schema.get_type().unwrap().contains(SchemaType::Object));

    let obj = json!({ "name": "Kiran", "num": [1, 2, 3] });
    assert_json_eq!(validate(&schema, &repo, obj.clone()).await.unwrap(), obj);

    assert!(validate(&schema, &repo, json!({ "name": "Kiran", "num": 23, "lastName": "grandhi" }))
        .await
        .is_err());
}

#[tokio::test]
async fn schema_object_validator_test_schema_based_old_object_style() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "OBJECT",
        "properties": {
            "name": { "type": "STRING" },
            "lastname": { "type": "STRING" },
            "age": { "type": "INTEGER" },
        },
        "additionalProperties": { "schemaValue": { "type": "OBJECT" } },
    }));

    let obj = json!({ "name": "Kiran", "age": 23 });
    assert_json_eq!(validate(&schema, &repo, obj.clone()).await.unwrap(), obj);

    let objwith_additional = json!({
        "name": "Kiran",
        "lastname": "grandhi",
        "addresses": {
            "area": "j.p.nagar",
            "city": "banga",
        },
    });

    assert_json_eq!(validate(&schema, &repo, objwith_additional.clone()).await.unwrap(), objwith_additional);

    let objwith_more_additional = json!({
        "name": "Kiran",
        "lastname": "grandhi",
        "addresses": {
            "area": "j.p.nagar",
            "city": "banga",
        },
        "city": "kakinada",
    });

    assert!(validate(&schema, &repo, objwith_more_additional).await.is_err());
}

#[tokio::test]
async fn schema_test_with_object_value_as_null_for_any() {
    let filter_operator = schema_from(json!({
        "namespace": "test",
        "name": "filterOperator",
        "version": 1,
        "type": "STRING",
        "defaultValue": "EQUALS",
        "enums": ["EQUALS", "LESS_THAN", "GREATER_THAN", "LESS_THAN_EQUAL", "BETWEEN", "IN"],
    }));

    let any_types = json!(["FLOAT", "BOOLEAN", "STRING", "DOUBLE", "INTEGER", "LONG", "NULL", "ARRAY", "OBJECT"]);

    let filter_condition = schema_from(json!({
        "namespace": "test",
        "name": "FilterCondition",
        "version": 1,
        "type": "OBJECT",
        "properties": {
            "field": {
                "namespace": "_",
                "name": "field",
                "version": 1,
                "type": "STRING",
            },
            "multiValue": {
                "namespace": "_",
                "name": "multiValue",
                "version": 1,
                "type": "ARRAY",
                "items": {
                    "namespace": "_",
                    "name": "singleType",
                    "version": 1,
                    "type": any_types,
                },
            },
            "isValue": {
                "namespace": "_",
                "name": "isValue",
                "version": 1,
                "type": "BOOLEAN",
                "defaultValue": false,
            },
            "toValue": {
                "namespace": "_",
                "name": "toValue",
                "version": 1,
                "type": any_types,
            },
            "operator": {
                "namespace": "_",
                "version": 1,
                "ref": "test.filterOperator",
            },
            "negate": {
                "namespace": "_",
                "name": "negate",
                "version": 1,
                "type": "BOOLEAN",
                "defaultValue": false,
            },
            "value": {
                "namespace": "_",
                "name": "value",
                "version": 1,
                "type": any_types,
            },
            "isToValue": {
                "namespace": "_",
                "name": "isToValue",
                "version": 1,
                "type": "BOOLEAN",
                "defaultValue": false,
            },
        },
        "additionalProperties": false,
        "required": ["operator", "field"],
    }));

    // JS TestRepository: any non-empty namespace looks the name up; filter returns [].
    let test_repo = MapSchemaRepository::new(
        None,
        vec![("filterOperator", filter_operator), ("FilterCondition", filter_condition.clone())],
        false,
    );
    let repo = HybridRepository::<Schema>::new(vec![
        Arc::new(test_repo) as Arc<dyn Repository<Schema>>,
        Arc::new(KIRunSchemaRepository::new()),
    ]);

    let temp_ob2 = json!({ "field": "nullcheck", "operator": "LESS_THAN", "value": null, "isValue": true });

    let res3 = SchemaValidator::validate(None, Some(&filter_condition), Some(&repo), temp_ob2.clone()).await.unwrap();
    // JS validates `tempOb2` in place, so `toStrictEqual(tempOb2)` compares against the object
    // after the defaulted properties (negate, isToValue) were written into it. Rust values are
    // owned, so the expected value spells those defaults out.
    let mut expected = temp_ob2;
    expected["negate"] = json!(false);
    expected["isToValue"] = json!(false);
    assert_json_eq!(res3, expected);
}
