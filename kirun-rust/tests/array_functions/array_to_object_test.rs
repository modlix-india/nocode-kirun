//! Port of kirun-js `__tests__/engine/function/system/array/ArrayToObjectTest.ts`.

use kirun::function::system::array::array_to_object::ArrayToObject;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn array_to_object_test() {
    let atoo = ArrayToObject::new();

    // JS has a trailing `undefined` element; it is `null` here (both are filtered out).
    let source = json!([
        { "name": "A", "num": 1 },
        { "name": "B", "num": 2 },
        null,
        { "name": "C", "num": 3 },
        { "name": "D", "num": 4 },
        { "name": "E", "num": 4 },
        null,
    ]);

    let result = first_value(
        &atoo,
        fep().set_arguments(args! { "source" => source.clone(), "keyPath" => "name", "valuePath" => "num" }),
        "result",
    )
    .await
    .unwrap();
    assert_json_eq!(result, json!({ "A": 1, "B": 2, "C": 3, "D": 4, "E": 4 }));

    let result = first_value(
        &atoo,
        fep().set_arguments(args! { "source" => source.clone(), "keyPath" => "num", "valuePath" => "name" }),
        "result",
    )
    .await
    .unwrap();
    assert_json_eq!(result, json!({ "1": "A", "2": "B", "3": "C", "4": "E" }));

    let result = first_value(
        &atoo,
        fep().set_arguments(args! {
            "source" => source.clone(),
            "keyPath" => "num",
            "valuePath" => "name",
            "ignoreDuplicateKeys" => true,
        }),
        "result",
    )
    .await
    .unwrap();
    assert_json_eq!(result, json!({ "1": "A", "2": "B", "3": "C", "4": "D" }));
}

#[tokio::test]
async fn array_to_object_test_invalid_key_path() {
    let atoo = ArrayToObject::new();

    let source = json!([
        { "name": "A", "num": 1 },
        { "name": "B", "num": 2 },
        { "name": "C", "num": 3 },
        { "name": "D", "num": 4 },
        { "name": "E", "num": 4 },
    ]);

    let result = first_value(
        &atoo,
        fep().set_arguments(args! { "source" => source.clone(), "keyPath" => "name1", "valuePath" => "num" }),
        "result",
    )
    .await
    .unwrap();
    // JS toMatchObject({}) matches any object; the JS implementation yields exactly {}
    // (null keys are skipped because ignoreNullKeys defaults to true).
    assert_json_eq!(result, json!({}));

    let result = first_value(
        &atoo,
        fep().set_arguments(args! { "source" => source.clone(), "keyPath" => "name", "valuePath" => "num1" }),
        "result",
    )
    .await
    .unwrap();
    // JS `undefined` values are null here.
    assert_json_eq!(result, json!({ "A": null, "B": null, "C": null, "D": null, "E": null }));

    let result = first_value(
        &atoo,
        fep().set_arguments(args! {
            "source" => source.clone(),
            "keyPath" => "name",
            "valuePath" => "num1",
            "ignoreNullValues" => true,
        }),
        "result",
    )
    .await
    .unwrap();
    assert_json_eq!(result, json!({}));
}

#[tokio::test]
async fn array_to_object_test_deep_path() {
    let atoo = ArrayToObject::new();

    let source = json!([
        { "name": "A", "num": 1, "info": { "age": 10 } },
        { "name": "B", "num": 2, "info": { "age": 20 } },
        { "name": "C", "num": 3, "info": { "age": 30 } },
        { "name": "D", "num": 4, "info": { "age": 40 } },
        { "name": "E", "num": 4, "info": { "age": 50 } },
    ]);

    let result = first_value(
        &atoo,
        fep().set_arguments(args! { "source" => source, "keyPath" => "info.age", "valuePath" => "name" }),
        "result",
    )
    .await
    .unwrap();
    assert_json_eq!(result, json!({ "10": "A", "20": "B", "30": "C", "40": "D", "50": "E" }));
}
