//! Port of kirun-js `__tests__/engine/function/system/ValidateSchemaTest.ts`.

use kirun::function::system::validate_schema::ValidateSchema;
use kirun::Value;
use serde_json::json;

use crate::common::*;

async fn expect_validation(schema: Value, source: Value, expected_result: bool) {
    // JS `beforeEach` creates a fresh validator per test.
    let validator = ValidateSchema::new();
    let context = fep().set_arguments(args! { "source" => source, "schema" => schema });

    let is_valid = first_value(&validator, context, "isValid").await.unwrap();

    assert_eq!(is_valid, json!(expected_result));
}

#[tokio::test]
async fn complex_object_validation_should_pass() {
    let schema = json!({
        "name": "lead",
        "type": ["OBJECT"],
        "version": 1,
        "properties": {
            "name": { "type": ["STRING"], "minLength": 3 },
            "mobileNumber": { "type": ["STRING"], "minLength": 3 },
            "formType": { "type": ["STRING"], "enums": ["LEAD_FORM", "CONTACT_FORM"] },
            "email": { "type": ["STRING"] },
            "address": {
                "type": ["OBJECT"],
                "properties": {
                    "street": { "type": ["STRING"], "minLength": 5 },
                    "city": { "type": ["STRING"] },
                    "state": { "type": ["STRING"] },
                    "postalCode": { "type": ["STRING"], "pattern": "^[0-9]{6}$" }
                },
                "required": ["street", "city", "state"]
            },
            "employment": {
                "type": ["OBJECT"],
                "properties": {
                    "company": { "type": ["STRING"] },
                    "position": { "type": ["STRING"] },
                    "experience": { "type": ["INTEGER"], "minimum": 0 },
                    "skills": {
                        "type": ["ARRAY"],
                        "items": { "type": ["STRING"] },
                        "minItems": 1
                    }
                },
                "required": ["company", "position"]
            }
        },
        "required": ["name", "email", "formType"]
    });

    let source = json!({
        "name": "John Doe",
        "mobileNumber": "9876543210",
        "formType": "LEAD_FORM",
        "email": "john.doe@example.com",
        "address": {
            "street": "123 Main Street",
            "city": "New York",
            "state": "NY",
            "postalCode": "100001"
        },
        "employment": {
            "company": "Tech Corp",
            "position": "Senior Developer",
            "experience": 5,
            "skills": ["Java", "Spring", "React"]
        }
    });

    expect_validation(schema, source, true).await;
}

#[tokio::test]
async fn simple_string_validation_should_pass() {
    let schema = json!({ "type": ["STRING"], "minLength": 3, "maxLength": 10 });

    expect_validation(schema, json!("Hello"), true).await;
}

#[tokio::test]
async fn number_validation_should_pass() {
    let schema = json!({ "type": ["INTEGER"], "minimum": 0, "maximum": 100 });

    expect_validation(schema, json!(50), true).await;
}

#[tokio::test]
async fn simple_array_validation_should_pass() {
    let schema = json!({
        "type": ["ARRAY"],
        "items": { "type": ["STRING"] },
        "minItems": 1,
        "maxItems": 3
    });

    expect_validation(schema, json!(["item1", "item2"]), true).await;
}

#[tokio::test]
async fn validation_should_fail_for_invalid_input() {
    let schema = json!({ "type": ["STRING"], "minLength": 5 });

    expect_validation(schema, json!("Hi"), false).await;
}
