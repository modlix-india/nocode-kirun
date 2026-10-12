//! Port of kirun-js `__tests__/engine/function/system/object/ObjectValuesTest.ts`.

use kirun::function::system::object::ObjectValues;
use kirun::Value;
use serde_json::json;

use crate::common::*;

async fn run(source: Value) -> Value {
    let obj_vals = ObjectValues::new();
    // JS `fep.setArguments(MapUtil.of('source', obj))`.
    let fep = fep().set_arguments(args! { "source" => source });
    first_value(&obj_vals, fep, "value").await.unwrap()
}

#[tokio::test]
async fn entry_test_1() {
    let obj = json!({ "a": 1, "b": 2, "d": ["a", "b", "c"] });

    let res = json!([1, 2, ["a", "b", "c"]]);

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

    assert_json_eq!(run(obj).await, json!(["s", "u", "r", "e", "n", "d", "h", "a", "r"]));
}

#[tokio::test]
async fn entry_nested_object_test() {
    let obj = json!({ "a": { "b": { "c": { "d": { "e": [1, 2, 4, 5] } } } }, "c": ["q", "w", "e", "r"] });

    let res = json!([{ "b": { "c": { "d": { "e": [1, 2, 4, 5] } } } }, ["q", "w", "e", "r"]]);

    assert_json_eq!(run(obj).await, res);
}

#[tokio::test]
async fn entry_array_test() {
    let obj = json!([1, 2, 3, 4, 1, 12, 3, 4, 5]);

    let res = json!([1, 2, 3, 4, 1, 12, 3, 4, 5]);

    assert_json_eq!(run(obj).await, res);
}

#[tokio::test]
async fn entry_duplicate_entry_test() {
    let parent = json!({ "a": { "b": "c" }, "k": { "b": "c" } });

    // JS `{ ...parent, ... }` overriding both keys (key order stays the parent's).
    let obj = json!({ "a": "overridden", "k": " so only child objects are returned from child " });

    let child_res = json!(["overridden", " so only child objects are returned from child "]);

    assert_json_eq!(run(obj).await, child_res);

    let parent_res = json!([{ "b": "c" }, { "b": "c" }]);

    assert_json_eq!(run(parent).await, parent_res);
}
