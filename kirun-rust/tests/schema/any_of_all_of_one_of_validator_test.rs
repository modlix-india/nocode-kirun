//! Port of validator/AnyOfAllOfOneOfValidatorTest.ts.

use kirun::json::schema::r#type::{SchemaType, TypeUtil};
use kirun::json::schema::validator::AnyOfAllOfOneOfValidator;
use kirun::json::schema::Schema;
use kirun::repository::KIRunSchemaRepository;
use kirun::Value;
use serde_json::json;

use crate::common::assert_json_eq;
use crate::props;

#[tokio::test]
async fn any_of_all_of_one_validator_test_1() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Integer]));

    assert_json_eq!(
        AnyOfAllOfOneOfValidator::validate(&[], &schema, Some(&repo), json!(10)).await.unwrap(),
        json!(10)
    );
}

#[tokio::test]
async fn any_of_all_of_one_validator_test_2() {
    let repo = KIRunSchemaRepository::new();
    let array_schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Array]));

    assert_json_eq!(
        AnyOfAllOfOneOfValidator::validate(&[], &array_schema, Some(&repo), json!([1, 2, 3])).await.unwrap(),
        json!([1, 2, 3])
    );
}

#[tokio::test]
async fn any_of_all_of_one_validator_test_3() {
    let repo = KIRunSchemaRepository::new();
    let obj_schema = Schema::of_object("testObj").set_properties(props(vec![("key", Schema::of_string("key"))]));

    assert_json_eq!(
        AnyOfAllOfOneOfValidator::validate(&[], &obj_schema, Some(&repo), json!({ "key": "value" }))
            .await
            .unwrap(),
        json!({ "key": "value" })
    );
}

// JS reuses the name 'Any Of All Of One Validator Test 3' for the next two tests.
#[tokio::test]
async fn any_of_all_of_one_validator_test_3_null() {
    let repo = KIRunSchemaRepository::new();
    let null_schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Null]));

    assert_json_eq!(
        AnyOfAllOfOneOfValidator::validate(&[], &null_schema, Some(&repo), Value::Null).await.unwrap(),
        Value::Null
    );
}

#[tokio::test]
async fn any_of_all_of_one_validator_test_3_boolean() {
    let repo = KIRunSchemaRepository::new();
    let null_schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Boolean]));

    assert_json_eq!(
        AnyOfAllOfOneOfValidator::validate(&[], &null_schema, Some(&repo), Value::Null).await.unwrap(),
        Value::Null
    );
}
