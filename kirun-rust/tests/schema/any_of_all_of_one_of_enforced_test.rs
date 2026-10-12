//! Port of validator/AnyOfAllOfOneOfEnforcedTest.ts: anyOf / allOf / oneOf are checked the way
//! kirun-java checks them (kirun-js used to skip them).

use kirun::json::schema::validator::SchemaValidator;
use kirun::json::schema::Schema;
use kirun::repository::KIRunSchemaRepository;
use kirun::Value;
use serde_json::json;

use crate::common::{assert_err_contains, assert_json_eq};

async fn validate(schema: &Schema, element: Value) -> kirun::Result<Value> {
    let repo = KIRunSchemaRepository::new();
    SchemaValidator::validate(None, Some(schema), Some(&repo), element).await
}

fn any_of() -> Schema {
    Schema::new().set_any_of(vec![Schema::of_integer("i"), Schema::of_string("s")])
}

#[tokio::test]
async fn any_of_passes_when_one_schema_matches() {
    assert_json_eq!(validate(&any_of(), json!(10)).await.unwrap(), json!(10));
    assert_json_eq!(validate(&any_of(), json!("x")).await.unwrap(), json!("x"));
}

#[tokio::test]
async fn any_of_rejects_a_value_no_schema_matches() {
    assert_err_contains(validate(&any_of(), json!(true)).await, "The value don't satisfy any of the schemas.");
}

#[tokio::test]
async fn any_of_returns_the_value_as_validated_by_the_matching_schema() {
    let with_default = Schema::new().set_any_of(vec![Schema::of_string("s").set_default_value(json!("d"))]);
    assert_json_eq!(validate(&with_default, Value::Null).await.unwrap(), json!("d"));
}

#[tokio::test]
async fn all_of_needs_every_schema_to_match() {
    let all_of = Schema::new()
        .set_all_of(vec![Schema::of_number("min").set_minimum(5.0), Schema::of_number("max").set_maximum(10.0)]);
    assert_json_eq!(validate(&all_of, json!(7)).await.unwrap(), json!(7));
    assert_err_contains(validate(&all_of, json!(12)).await, "The value doesn't satisfy some of the schemas.");
}

#[tokio::test]
async fn one_of_needs_exactly_one_schema_to_match() {
    let distinct = Schema::new().set_one_of(vec![Schema::of_string("s"), Schema::of_boolean("b")]);
    let overlapping = Schema::new().set_one_of(vec![Schema::of_integer("i"), Schema::of_number("n")]);
    assert_json_eq!(validate(&distinct, json!("x")).await.unwrap(), json!("x"));
    assert_err_contains(validate(&distinct, json!(7)).await, "The value does not satisfy any schema");
    assert_err_contains(validate(&overlapping, json!(7)).await, "The value satisfy more than one schema");
}
