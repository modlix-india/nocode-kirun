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

// kirun-java's JSONStringify is `new Gson().toJson(source)`; measured on kirun-java 4.9.2.

async fn java_stringify(source: Value) -> Value {
    first_value(&JSONStringify::new(), fep().set_arguments(args! { "source" => source }), "value").await.unwrap()
}

#[tokio::test]
async fn java_quotes_and_escapes_strings_like_gson() {
    assert_eq!(java_stringify(json!("DESC")).await, json!("\"DESC\""));
    assert_eq!(java_stringify(json!("a<b>&='c'")).await, json!(r#""a\u003cb\u003e\u0026\u003d\u0027c\u0027""#));
    assert_eq!(java_stringify(json!("\u{2028}x\u{2029}")).await, json!(r#""\u2028x\u2029""#));
}

#[tokio::test]
async fn java_leaves_out_null_members_and_prints_java_numbers() {
    let source = json!({ "a": 1, "b": null, "c": "x", "d": { "e": null } });
    assert_eq!(java_stringify(source).await, json!(r#"{"a":1,"c":"x","d":{}}"#));
    assert_eq!(java_stringify(json!([1, null, "x", [], {}])).await, json!(r#"[1,null,"x",[],{}]"#));
    assert_eq!(java_stringify(json!({ "f": 5.0, "g": 7 })).await, json!(r#"{"f":5.0,"g":7}"#));
    assert_eq!(java_stringify(json!([5.0, 0.5, 1e7])).await, json!("[5.0,0.5,1.0E7]"));
}
