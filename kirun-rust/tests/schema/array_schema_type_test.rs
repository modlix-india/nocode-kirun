//! Port of validator/ArraySchemaTypeTest.ts.

use kirun::json::schema::array::ArraySchemaType;
use kirun::json::schema::validator::{ArrayValidator, SchemaValidator};
use kirun::json::schema::{AdditionalType, Schema};
use kirun::repository::KIRunSchemaRepository;
use serde_json::json;

use crate::common::assert_json_eq;

fn tuple3_object() -> Vec<Schema> {
    vec![Schema::of_integer("item1"), Schema::of_string("item2"), Schema::of_object("item3")]
}

#[tokio::test]
async fn schema_array_validator_test_for_single() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_single_schema(Schema::of_integer("ast"));

    let schema = Schema::of_array("schema", vec![]).set_items(ast);

    let arr = json!([12, 23, 54, 45]);

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), arr.clone()).await.unwrap(), arr);
}

#[tokio::test]
async fn schema_array_validator_test_for_tuple() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_tuple_schema(tuple3_object());

    let schema = Schema::of_array("schema", vec![]).set_items(ast);

    let arr = json!([12, "surendhar", { "a": "val1", "b": "val2" }]);

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), arr.clone()).await.unwrap(), arr);
}

// Second JS test named 'schema array validator test for single' (adds additionalItems false).
#[tokio::test]
async fn schema_array_validator_test_for_single_2() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_single_schema(Schema::of_integer("ast"));

    let schema = Schema::of_array("schema", vec![])
        .set_items(ast)
        .set_additional_items(AdditionalType::new().set_boolean_value(false));

    let arr = json!([12, 23, 54, 45]);

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), arr.clone()).await.unwrap(), arr);
}

// Second JS test named 'schema array validator test for tuple' (adds additionalItems true).
#[tokio::test]
async fn schema_array_validator_test_for_tuple_2() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_tuple_schema(tuple3_object());

    let schema = Schema::of_array("schema", vec![])
        .set_items(ast)
        .set_additional_items(AdditionalType::new().set_boolean_value(true));

    let arr = json!([12, "surendhar", { "a": "val1", "b": "val2" }, 1, 2, 4]);

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), arr.clone()).await.unwrap(), arr);
}

#[tokio::test]
async fn schema_array_validator_test_for_tuple_with_add_schema() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_tuple_schema(tuple3_object());

    let schema = Schema::of_array("schema", vec![])
        .set_items(ast)
        .set_additional_items(AdditionalType::new().set_schema_value(Schema::of_integer("itemInt")));

    let arr = json!([12, "surendhar", { "a": "val1", "b": "val2" }, 1, 2]);

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), arr.clone()).await.unwrap(), arr);
}

#[tokio::test]
async fn schema_array_validator_test_for_tuple_with_add_schema_fail() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_tuple_schema(tuple3_object());

    let schema = Schema::of_array("schema", vec![])
        .set_items(ast)
        .set_additional_items(AdditionalType::new().set_schema_value(Schema::of_integer("itemInt")));

    let arr = json!([12, "surendhar", { "a": "val1", "b": "val2" }, 1, 2, 4, "surendhar"]);

    assert!(ArrayValidator::validate(&[], &schema, Some(&repo), arr).await.is_err());
}

// Second JS test named 'schema array validator test for tuple with add schema fail'.
#[tokio::test]
async fn schema_array_validator_test_for_tuple_with_add_schema_fail_2() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_tuple_schema(vec![Schema::of_integer("item1"), Schema::of_string("item2")]);

    let schema = Schema::of_array("schema", vec![])
        .set_items(ast)
        .set_additional_items(AdditionalType::new().set_schema_value(Schema::of_integer("itemInt")));

    let arr = json!([12, "surendhar", { "a": "val1", "b": "val2" }, 1, 2, 4, ["ve", 23, "ctor"]]);

    assert!(ArrayValidator::validate(&[], &schema, Some(&repo), arr).await.is_err());
}

#[tokio::test]
async fn schema_array_validator_test_for_single_with_additional() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_single_schema(Schema::of_integer("ast"));

    let schema = Schema::of_array("schema", vec![])
        .set_items(ast)
        .set_additional_items(AdditionalType::new().set_boolean_value(true));

    let arr = json!([12, 23, 54, 45, "abcd", "df"]);

    assert!(ArrayValidator::validate(&[], &schema, Some(&repo), arr).await.is_err());
}

#[tokio::test]
async fn schema_array_validator_test_for_tuple_without_additional() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_tuple_schema(tuple3_object());

    let schema = Schema::of_array("schema", vec![]).set_items(ast);

    let arr = json!([12, "surendhar", { "a": "val1", "b": "val2" }, "add"]);

    assert!(ArrayValidator::validate(&[], &schema, Some(&repo), arr).await.is_err());
}

#[tokio::test]
async fn schema_array_validator_tuple_schema_with_json_object() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_tuple_schema(tuple3_object());

    let schema = Schema::of_array("schema", vec![]).set_items(ast);

    let obj = json!([1, "asd", { "val": "stringtype" }, "stringOnemore"]);

    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj).await.is_err());
}

#[tokio::test]
async fn schema_array_validator_tuple_schema_similar_to_json_object() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_tuple_schema(vec![
        Schema::of_integer("item1"),
        Schema::of_string("item2"),
        Schema::of_boolean("item3"),
    ]);

    let obj_sc = Schema::of_object("obj");

    let schema = Schema::of_array("schema", vec![])
        .set_items(ast)
        .set_additional_items(AdditionalType::new().set_schema_value(obj_sc));

    let obj = json!([1, "asd", true, { "val": "stringtype" }, false]);

    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj).await.is_err());
}
