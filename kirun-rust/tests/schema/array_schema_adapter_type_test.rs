//! Port of validator/ArraySchemaAdapterTypeTest.ts.

use kirun::json::schema::r#type::SchemaType;
use kirun::json::schema::validator::SchemaValidator;
use kirun::repository::KIRunSchemaRepository;
use serde_json::json;

use crate::common::assert_json_eq;
use crate::schema_from;

#[tokio::test]
async fn schema_array_with_single_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": {
            "singleSchema": {
                "type": "OBJECT",
                "properties": { "name": { "type": "STRING" }, "age": { "type": "INTEGER" } },
            },
        },
        "additionalItems": false,
    }));

    let obj = json!([{ "name": "amigo1" }, { "age": 24 }, false, "exampleString"]);

    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj).await.is_err());
}

#[tokio::test]
async fn schema_array_with_out_single_fail_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": {
            "type": "OBJECT",
            "properties": { "name": { "type": "STRING" }, "age": { "type": "INTEGER" } },
        },
        "additionalItems": false,
    }));

    let obj = json!([{ "name": "amigo1" }, { "age": 24 }, false, "exampleString"]);

    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj).await.is_err());
}

#[tokio::test]
async fn schema_array_with_single_pass_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": {
            "singleSchema": {
                "type": "OBJECT",
                "properties": { "name": { "type": "STRING" }, "age": { "type": "INTEGER" } },
            },
        },
        "additionalItems": false,
    }));

    let obj = json!([{ "name": "amigo1" }, { "age": 24 }]);

    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj.clone()).await.unwrap(),
        obj
    );
}

#[tokio::test]
async fn schema_array_with_tuple_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": {
            "tupleSchema": [
                {
                    "type": "OBJECT",
                    "properties": { "name": { "type": "STRING" }, "age": { "type": "INTEGER" } },
                    "required": ["age"],
                },
                { "type": "STRING", "minLength": 2 },
                { "type": "INTEGER", "minimum": 10 },
            ],
        },
        "additionalItems": true,
    }));

    let obj = json!([{ "name": "amigo1", "age": 24 }, "string type", 11, false, 12.34, "mla"]);

    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj.clone()).await.unwrap(),
        obj
    );
}

#[tokio::test]
async fn schema_array_with_out_tuple_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "ARRAY",
        "items": [
            {
                "type": "OBJECT",
                "properties": { "name": { "type": "STRING" }, "age": { "type": "INTEGER" } },
                "required": ["age"],
            },
            { "type": "STRING", "minLength": 2 },
            { "type": "ARRAY", "items": { "type": "INTEGER" }, "additionalItems": false },
        ],
        "additionalItems": true,
    }));

    let obj = json!([
        { "name": "amigo1", "age": 21 },
        "second string",
        [1, 2, 31231],
        "additional items was added here with true and false",
        true,
        false,
    ]);
    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj.clone()).await.unwrap(),
        obj
    );
}

#[tokio::test]
async fn regular_json() {
    let schema = schema_from(json!({
        "type": "object",
        "properties": {
            "productId": {
                "description": "The unique identifier for a product",
                "type": "integer",
            },
        },
    }));

    assert!(schema.get_type().unwrap().contains(SchemaType::Object));
    assert!(!schema.get_type().unwrap().contains(SchemaType::Integer));
    assert!(schema
        .get_properties()
        .unwrap()
        .get("productId")
        .unwrap()
        .get_type()
        .unwrap()
        .contains(SchemaType::Integer));
}

#[tokio::test]
async fn only_ref_test() {
    let schema = schema_from(json!({ "ref": "System.any" }));

    assert!(schema.get_type().is_none());
    assert_eq!(schema.get_ref(), Some("System.any"));
}
