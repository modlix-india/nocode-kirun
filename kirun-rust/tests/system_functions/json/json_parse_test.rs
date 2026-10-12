//! Port of kirun-js `__tests__/engine/function/system/json/JSONParseTest.ts`.

use kirun::function::system::json::JSONParse;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn should_return_the_parsed_json_object() {
    let json_parse = JSONParse::new();

    let fep = fep().set_arguments(args! { "source" => r#"{"name":"John","age":30}"# });

    assert_json_eq!(first_value(&json_parse, fep, "value").await.unwrap(), json!({ "name": "John", "age": 30 }));
}

#[tokio::test]
async fn should_return_the_parsed_json_array() {
    let json_parse = JSONParse::new();

    let fep = fep().set_arguments(args! { "source" => "[1,2,3,4,5]" });

    assert_json_eq!(first_value(&json_parse, fep, "value").await.unwrap(), json!([1, 2, 3, 4, 5]));
}

#[tokio::test]
async fn should_return_null_if_source_is_null() {
    let json_parse = JSONParse::new();

    let fep = fep().set_arguments(args! { "source" => "null" });

    assert_json_eq!(first_value(&json_parse, fep, "value").await.unwrap(), json!(null));
}

#[tokio::test]
async fn should_return_null_if_source_is_not_a_valid_json() {
    let json_parse = JSONParse::new();

    let fep = fep().set_arguments(args! { "source" => "invalid JSON" });

    // The expected text is V8's `JSON.parse` message; the Rust port must reproduce it.
    assert_json_eq!(
        first_value(&json_parse, fep, "errorMessage").await.unwrap(),
        json!("Unexpected token 'i', \"invalid JSON\" is not valid JSON")
    );
}

#[tokio::test]
async fn should_return_null_if_source_is_undefined() {
    let json_parse = JSONParse::new();

    let fep = fep().set_arguments(args! { "source" => "" });

    assert_json_eq!(first_value(&json_parse, fep, "value").await.unwrap(), json!(null));
}
