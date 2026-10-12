//! Port of validator/NumberValidatorTest.ts.

use kirun::json::schema::validator::SchemaValidator;
use serde_json::json;

use crate::common::assert_err_contains;
use crate::schema_from;

#[tokio::test]
async fn check_for_valid_custom_messages_in_numerical_values() {
    let schema = schema_from(json!({
        "type": "INTEGER",
        "minimum": 10,
        "details": {
            "validationMessages": {
                "minimum": "Minimum value is 10",
            }
        }
    }));

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&schema), None, json!(-23)).await,
        "Minimum value is 10",
    );
}
