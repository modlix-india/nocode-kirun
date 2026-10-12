//! Port of validator/NullValidatorTest.ts.

use kirun::json::schema::validator::SchemaValidator;
use kirun::Value;
use serde_json::json;

use crate::common::assert_json_eq;
use crate::schema_from;

#[tokio::test]
async fn check_for_valid_null_value() {
    let schema = schema_from(json!({
        "type": "NULL",
    }));

    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), None, json!(23)).await.is_err());
    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), None, json!(0)).await.is_err());
    assert!(SchemaValidator::validate(Some(vec![]), Some(&schema), None, json!("")).await.is_err());
    assert_json_eq!(SchemaValidator::validate(Some(vec![]), Some(&schema), None, Value::Null).await.unwrap(), Value::Null);
    // JS passes `undefined` and expects `undefined` back; both are Value::Null in Rust.
    assert_json_eq!(SchemaValidator::validate(Some(vec![]), Some(&schema), None, Value::Null).await.unwrap(), Value::Null);
}
