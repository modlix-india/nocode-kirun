//! Port of kirun-js `__tests__/engine/function/system/json/JSONStringifyTest.ts`.

use kirun::function::system::json::JSONStringify;
use kirun::Value;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn should_return_the_stringified_json() {
    let json_stringify = JSONStringify::new();

    let fep = fep().set_arguments(args! { "source" => json!({ "name": "John", "age": 30 }) });

    assert_json_eq!(first_value(&json_stringify, fep, "value").await.unwrap(), json!(r#"{"name":"John","age":30}"#));
}

#[tokio::test]
async fn should_return_the_stringified_if_source_is_undefined() {
    let json_stringify = JSONStringify::new();

    // JS passes `undefined`; in Rust undefined and null are both Value::Null.
    let fep = fep().set_arguments(args! { "source" => Value::Null });

    assert_json_eq!(first_value(&json_stringify, fep, "value").await.unwrap(), json!("null"));
}
