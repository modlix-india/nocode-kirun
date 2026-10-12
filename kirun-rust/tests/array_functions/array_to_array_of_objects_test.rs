//! Port of kirun-js `__tests__/engine/function/system/array/ArrayToArrayOfObjectsTest.ts`.

use kirun::function::system::array::array_to_array_of_objects::ArrayToArrayOfObjects;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn array_to_array_of_objects() {
    let fun = ArrayToArrayOfObjects::new();

    let result = first_value(&fun, fep().set_arguments(args! { "source" => json!([1, 2, 3]) }), "result")
        .await
        .unwrap();
    assert_json_eq!(result, json!([{ "value": 1 }, { "value": 2 }, { "value": 3 }]));

    let result = first_value(
        &fun,
        fep().set_arguments(args! { "source" => json!([1, 2, 3]), "keyName" => json!(["number"]) }),
        "result",
    )
    .await
    .unwrap();
    assert_json_eq!(result, json!([{ "number": 1 }, { "number": 2 }, { "number": 3 }]));

    let result = first_value(
        &fun,
        fep().set_arguments(args! { "source" => json!([{ "number": 1 }, { "number": 2 }, { "number": 3 }]) }),
        "result",
    )
    .await
    .unwrap();
    assert_json_eq!(
        result,
        json!([
            { "value": { "number": 1 } },
            { "value": { "number": 2 } },
            { "value": { "number": 3 } },
        ])
    );

    let result = first_value(
        &fun,
        fep().set_arguments(args! { "source" => json!([["a", 1], ["b", 2], ["c", "3"]]) }),
        "result",
    )
    .await
    .unwrap();
    assert_json_eq!(
        result,
        json!([
            { "value1": "a", "value2": 1 },
            { "value1": "b", "value2": 2 },
            { "value1": "c", "value2": "3" },
        ])
    );

    let result = first_value(
        &fun,
        fep().set_arguments(args! {
            "source" => json!([["a", 1], ["b", 2], ["c", "3"]]),
            "keyName" => json!(["key", "value", "other"]),
        }),
        "result",
    )
    .await
    .unwrap();
    // JS `other: undefined` (key present, value undefined) is `"other": null` here.
    assert_json_eq!(
        result,
        json!([
            { "key": "a", "value": 1, "other": null },
            { "key": "b", "value": 2, "other": null },
            { "key": "c", "value": "3", "other": null },
        ])
    );

    let result = first_value(
        &fun,
        fep().set_arguments(args! {
            "source" => json!([["a", 1], ["b", 2], ["c", "3"]]),
            "keyName" => json!(["maKey"]),
        }),
        "result",
    )
    .await
    .unwrap();
    assert_json_eq!(result, json!([{ "maKey": "a" }, { "maKey": "b" }, { "maKey": "c" }]));
}

#[tokio::test]
async fn array_with_nested_object_and_mixed() {
    let fun = ArrayToArrayOfObjects::new();

    let result = first_value(
        &fun,
        fep().set_arguments(args! {
            "source" => json!([true, 1, 2, ["a", "b", "c"]]),
            "keyName" => json!(["akey", "bkey", "ckey"]),
        }),
        "result",
    )
    .await
    .unwrap();

    // JS toMatchObject; the JS implementation produces exactly these objects.
    assert_json_eq!(
        result,
        json!([{ "akey": true }, { "akey": 1 }, { "akey": 2 }, { "akey": "a", "bkey": "b", "ckey": "c" }])
    );
}

#[tokio::test]
async fn array_with_nested_array_object_and_mixed_without_key() {
    let fun = ArrayToArrayOfObjects::new();

    let result = first_value(
        &fun,
        fep().set_arguments(args! { "source" => json!([true, 1, 2, ["a", "b", "c"]]) }),
        "result",
    )
    .await
    .unwrap();

    assert_json_eq!(
        result,
        json!([
            { "value": true },
            { "value": 1 },
            { "value": 2 },
            { "value1": "a", "value2": "b", "value3": "c" },
        ])
    );
}

/// Second JS test named 'Array with nested array object and mixed without key'.
#[tokio::test]
async fn array_with_nested_array_object_and_mixed_without_key_2() {
    let fun = ArrayToArrayOfObjects::new();

    let result = first_value(
        &fun,
        fep().set_arguments(args! {
            "source" => json!([true, 1, 2, ["a", "b", "c", ["d", "e"], { "obj1": "val1" }]]),
        }),
        "result",
    )
    .await
    .unwrap();

    assert_json_eq!(
        result,
        json!([
            { "value": true },
            { "value": 1 },
            { "value": 2 },
            {
                "value1": "a",
                "value2": "b",
                "value3": "c",
                "value4": ["d", "e"],
                "value5": { "obj1": "val1" },
            },
        ])
    );
}

#[tokio::test]
async fn array_with_nested_array_object_and_mixed_with_key_arrays() {
    let fun = ArrayToArrayOfObjects::new();

    let result = first_value(
        &fun,
        fep().set_arguments(args! {
            "source" => json!([[true, false], [1, "surendhar"], ["satya"]]),
            "keyName" => json!(["valueA", "valueB", "valueC", "valueD"]),
        }),
        "result",
    )
    .await
    .unwrap();

    // JS undefined values are null here.
    assert_json_eq!(
        result,
        json!([
            { "valueA": true, "valueB": false, "valueC": null, "valueD": null },
            { "valueA": 1, "valueB": "surendhar", "valueC": null, "valueD": null },
            { "valueA": "satya", "valueB": null, "valueC": null, "valueD": null },
        ])
    );
}
