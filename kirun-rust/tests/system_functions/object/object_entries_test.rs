//! Port of kirun-js `__tests__/engine/function/system/object/ObjectEntriesTest.ts`.

use kirun::function::system::object::ObjectEntries;
use kirun::Value;
use serde_json::json;

use crate::common::*;

async fn run(source: Value) -> Value {
    let obj_ent = ObjectEntries::new();
    // JS `fep.setArguments(MapUtil.of('source', obj))`.
    let fep = fep().set_arguments(args! { "source" => source });
    first_value(&obj_ent, fep, "value").await.unwrap()
}

#[tokio::test]
async fn entry_test_1() {
    let obj = json!({ "a": 1, "b": 2, "d": ["a", "b", "c"] });

    let res = json!([["a", 1], ["b", 2], ["d", ["a", "b", "c"]]]);

    assert_json_eq!(run(obj).await, res);
}

#[tokio::test]
async fn entry_null_test() {
    let obj = Value::Null;

    assert_json_eq!(run(obj).await, json!([]));
}

// JS passes `undefined`; in Rust undefined and null are both Value::Null.
#[tokio::test]
async fn entry_undefined_test() {
    let obj = Value::Null;

    assert_json_eq!(run(obj).await, json!([]));
}

#[tokio::test]
async fn entry_primitive_number_test() {
    let obj = json!(423);

    assert_json_eq!(run(obj).await, json!([]));
}

#[tokio::test]
async fn entry_primitive_boolean_test() {
    let obj = json!(false);

    assert_json_eq!(run(obj).await, json!([]));
}

#[tokio::test]
async fn entry_primitive_string_test() {
    let obj = json!("surendhar");

    assert_json_eq!(run(obj).await, json!([["0", "s"], ["1", "u"], ["2", "r"], ["3", "e"], ["4", "n"], ["5", "d"], ["6", "h"], ["7", "a"], ["8", "r"]]));
}

#[tokio::test]
async fn entry_nested_object_test() {
    let obj = json!({ "a": { "b": { "c": { "d": { "e": [1, 2, 4, 5] } } } }, "c": ["q", "w", "e", "r"] });

    let res = json!([["a", { "b": { "c": { "d": { "e": [1, 2, 4, 5] } } } }], ["c", ["q", "w", "e", "r"]]]);

    assert_json_eq!(run(obj).await, res);
}

#[tokio::test]
async fn entry_array_test() {
    let obj = json!([1, 2, 3, 4, 1, 12, 3, 4, 5]);

    let res = json!([["0", 1], ["1", 2], ["2", 3], ["3", 4], ["4", 1], ["5", 12], ["6", 3], ["7", 4], ["8", 5]]);

    assert_json_eq!(run(obj).await, res);
}

#[tokio::test]
async fn entry_duplicate_entry_test() {
    let parent = json!({ "k": { "b": "c" }, "a": { "b": "c" } });

    // JS `{ ...parent, ... }` overriding both keys (key order stays the parent's).
    let obj = json!({ "k": " so only child objects are returned from child ", "a": "overridden" });

    let child_res = json!([["a", "overridden"], ["k", " so only child objects are returned from child "]]);

    assert_json_eq!(run(obj).await, child_res);

    let parent_res = json!([["a", { "b": "c" }], ["k", { "b": "c" }]]);

    assert_json_eq!(run(parent).await, parent_res);
}

// JS declares a second test also named 'entry primitive String test '; suffixed here.
#[tokio::test]
async fn entry_primitive_string_test_2() {
    let obj = json!("ab cd");

    assert_json_eq!(run(obj).await, json!([["0", "a"], ["1", "b"], ["2", " "], ["3", "c"], ["4", "d"]]));
}
