//! Port of kirun-java `function/system/object/ObjectConvertTest.java`, plus the kirun-java
//! behaviour for a `$ref` or typeless target schema (kirun-js refuses both with "Type is missing
//! in schema for declared <mode>"; stored server functions rely on kirun-java's answer).

use kirun::function::system::object::ObjectConvert;
use kirun::Value;
use serde_json::json;

use crate::common::*;

async fn convert(source: Value, schema: Value, mode: &str) -> kirun::Result<Value> {
    let fep = fep_with_args(args! { "source" => source, "schema" => schema, "conversionMode" => mode });
    first_value(&ObjectConvert::new(), fep, "value").await
}

#[tokio::test]
async fn test_boolean_array_conversion() {
    let source = json!([true, false, "yes", "no", "y", "n", 1, 0]);
    let schema = json!({
        "name": "ArrayType",
        "type": "ARRAY",
        "defaultValue": [],
        "items": { "name": "EachElement", "type": "BOOLEAN" }
    });

    let value = convert(source, schema, "STRICT").await.unwrap();

    assert_json_eq!(value, json!([true, false, true, false, true, false, true, false]));
}

#[tokio::test]
async fn ref_schema_is_resolved_and_converted_to() {
    let value = convert(json!({ "year": "2024", "month": 3 }), json!({ "ref": "System.Date.TimeObject" }), "STRICT")
        .await
        .unwrap();

    assert_json_eq!(value, json!({ "year": 2024, "month": 3 }));
}

#[tokio::test]
async fn ref_schema_inside_an_object_is_converted_to() {
    let schema = json!({ "type": "OBJECT", "properties": { "t": { "ref": "System.Date.TimeObject" } } });

    let value = convert(json!({ "t": { "year": "7" } }), schema, "SKIP").await.unwrap();

    assert_json_eq!(value, json!({ "t": { "year": 7 } }));
}

#[tokio::test]
async fn typeless_schema_leaves_the_value_as_it_is() {
    let value = convert(json!({ "year": "abc" }), json!({}), "STRICT").await.unwrap();
    assert_json_eq!(value, json!({ "year": "abc" }));

    // Properties without a type on the object schema are not looked at either.
    let schema = json!({ "properties": { "year": { "type": "INTEGER" } } });
    let value = convert(json!({ "year": "12" }), schema, "STRICT").await.unwrap();
    assert_json_eq!(value, json!({ "year": "12" }));
}

#[tokio::test]
async fn ref_schema_still_refuses_a_value_it_cannot_convert() {
    let err = convert(json!({ "year": "abc" }), json!({ "ref": "System.Date.TimeObject" }), "STRICT")
        .await
        .unwrap_err();

    // kirun-java converts to the first number type (double) and lets the refusal out at once.
    assert_eq!(
        err.to_string(),
        "System.Date.TimeObject - System.Date.TimeObject.year - \"abc\" is not a Double"
    );
}

#[tokio::test]
async fn number_conversions_follow_kirun_java() {
    let int = json!({ "type": "INTEGER" });
    // A fraction is dropped towards zero outside STRICT; `parseDouble` reads `1e5` but not `0x10`.
    assert_json_eq!(convert(json!(-2.5), int.clone(), "LENIENT").await.unwrap(), json!(-2));
    assert_json_eq!(convert(json!("1e5"), int.clone(), "STRICT").await.unwrap(), json!(100000));
    assert_eq!(convert(json!("0x10"), int.clone(), "STRICT").await.unwrap_err().to_string(), "\"0x10\" is not a Integer");
    assert_eq!(convert(json!(""), int.clone(), "STRICT").await.unwrap_err().to_string(), "\"\" is not a Integer");
    // A double stays a double, a float is rounded to a float.
    assert_eq!(convert(json!("5"), json!({ "type": "DOUBLE" }), "STRICT").await.unwrap(), json!(5.0));
    assert_eq!(convert(json!("0.3333333333"), json!({ "type": "FLOAT" }), "STRICT").await.unwrap(), json!(0.33333334));
    // A numeric default is used; a string becomes the value as JSON.
    assert_json_eq!(convert(json!("abc"), json!({ "type": "INTEGER", "defaultValue": 7 }), "USE_DEFAULT").await.unwrap(), json!(7));
    assert_eq!(convert(json!({ "a": [1, 2.0] }), json!({ "type": "STRING" }), "STRICT").await.unwrap(), json!("{\"a\":[1,2.0]}"));
    // kirun-java tries the number types first: an object under [OBJECT, INTEGER] fails STRICT.
    let either = json!({ "type": ["OBJECT", "INTEGER"] });
    assert_eq!(convert(json!({ "a": 1 }), either, "STRICT").await.unwrap_err().to_string(), "{\"a\":1} is not a Integer");
}
