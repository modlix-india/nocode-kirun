//! Port of kirun-js `__tests__/engine/function/system/MakeTest.ts`.

use kirun::function::system::make::Make;
use kirun::runtime::function_execution_parameters::StepsMap;
use kirun::{Map, Value};
use serde_json::json;

use crate::common::*;

/// JS `createOutputMap(data)`: `Steps.step1.output.<key>` for every key of `data`.
fn create_output_map(data: Value) -> StepsMap {
    let output: Map<Value> = data.as_object().unwrap().iter().map(|(k, v)| (k.clone(), v.clone())).collect();
    let mut step1 = Map::new();
    step1.insert("output".to_string(), output);
    let mut steps = StepsMap::new();
    steps.insert("step1".to_string(), step1);
    steps
}

async fn run_make(result_shape: Value, steps: StepsMap) -> Value {
    let make = Make::new();
    let fep = fep().set_arguments(args! { "resultShape" => result_shape }).set_context(Map::new()).set_steps(steps);
    first_value(&make, fep, "value").await.unwrap()
}

#[tokio::test]
async fn test_simple_object_with_expression() {
    let source = json!({ "name": "John", "age": 30 });

    let result_shape = json!({
        "fullName": "{{Steps.step1.output.source.name}}",
        "userAge": "{{Steps.step1.output.source.age}}",
    });

    let value = run_make(result_shape, create_output_map(json!({ "source": source }))).await;

    assert_json_eq!(value, json!({ "fullName": "John", "userAge": 30 }));
}

#[tokio::test]
async fn test_nested_object_with_expressions() {
    let source = json!({
        "user": { "firstName": "John", "lastName": "Doe" },
        "address": { "city": "NYC", "zip": "10001" },
    });

    let result_shape = json!({
        "person": {
            "name": "{{Steps.step1.output.source.user.firstName}}",
            "surname": "{{Steps.step1.output.source.user.lastName}}",
        },
        "location": {
            "cityName": "{{Steps.step1.output.source.address.city}}",
            "postalCode": "{{Steps.step1.output.source.address.zip}}",
        },
    });

    let value = run_make(result_shape, create_output_map(json!({ "source": source }))).await;

    assert_json_eq!(
        value,
        json!({
            "person": { "name": "John", "surname": "Doe" },
            "location": { "cityName": "NYC", "postalCode": "10001" },
        })
    );
}

#[tokio::test]
async fn test_array_with_expressions() {
    let source = json!({ "items": ["apple", "banana", "cherry"] });

    let result_shape = json!({
        "fruits": [
            "{{Steps.step1.output.source.items[0]}}",
            "{{Steps.step1.output.source.items[1]}}",
            "{{Steps.step1.output.source.items[2]}}",
        ],
    });

    let value = run_make(result_shape, create_output_map(json!({ "source": source }))).await;

    assert_json_eq!(value, json!({ "fruits": ["apple", "banana", "cherry"] }));
}

#[tokio::test]
async fn test_deeply_nested_structure_with_arrays() {
    let source = json!({
        "data": {
            "users": [
                { "id": 1, "name": "Alice" },
                { "id": 2, "name": "Bob" },
            ],
        },
    });

    let result_shape = json!({
        "level1": {
            "level2": {
                "level3": {
                    "userList": [
                        {
                            "userId": "{{Steps.step1.output.source.data.users[0].id}}",
                            "userName": "{{Steps.step1.output.source.data.users[0].name}}",
                        },
                        {
                            "userId": "{{Steps.step1.output.source.data.users[1].id}}",
                            "userName": "{{Steps.step1.output.source.data.users[1].name}}",
                        },
                    ],
                },
            },
        },
    });

    let value = run_make(result_shape, create_output_map(json!({ "source": source }))).await;

    assert_json_eq!(
        value,
        json!({
            "level1": {
                "level2": {
                    "level3": {
                        "userList": [
                            { "userId": 1, "userName": "Alice" },
                            { "userId": 2, "userName": "Bob" },
                        ],
                    },
                },
            },
        })
    );
}

#[tokio::test]
async fn test_mixed_static_and_dynamic_values() {
    let source = json!({ "dynamicValue": "from source" });

    let result_shape = json!({
        "static": "static string",
        "dynamic": "{{Steps.step1.output.source.dynamicValue}}",
        "nested": {
            "staticNum": 42,
            "dynamicNum": "{{Steps.step1.output.source.dynamicValue}}",
        },
        "array": ["static", "{{Steps.step1.output.source.dynamicValue}}", 123],
    });

    let value = run_make(result_shape, create_output_map(json!({ "source": source }))).await;

    assert_json_eq!(
        value,
        json!({
            "static": "static string",
            "dynamic": "from source",
            "nested": { "staticNum": 42, "dynamicNum": "from source" },
            "array": ["static", "from source", 123],
        })
    );
}

#[tokio::test]
async fn test_null_and_undefined_handling() {
    let result_shape = json!({
        "nullValue": null,
        "nested": { "inner": null },
    });

    let value = run_make(result_shape, StepsMap::new()).await;

    assert_json_eq!(
        value,
        json!({
            "nullValue": null,
            "nested": { "inner": null },
        })
    );
}

#[tokio::test]
async fn test_array_of_arrays() {
    let source = json!({ "matrix": [[1, 2], [3, 4]] });

    let result_shape = json!({
        "grid": [
            ["{{Steps.step1.output.source.matrix[0][0]}}", "{{Steps.step1.output.source.matrix[0][1]}}"],
            ["{{Steps.step1.output.source.matrix[1][0]}}", "{{Steps.step1.output.source.matrix[1][1]}}"],
        ],
    });

    let value = run_make(result_shape, create_output_map(json!({ "source": source }))).await;

    assert_json_eq!(value, json!({ "grid": [[1, 2], [3, 4]] }));
}

#[tokio::test]
async fn test_primitive_result_shape() {
    let source = json!({ "value": "hello" });

    let result_shape = json!("{{Steps.step1.output.source.value}}");

    let value = run_make(result_shape, create_output_map(json!({ "source": source }))).await;

    assert_eq!(value, json!("hello"));
}

#[tokio::test]
async fn test_array_as_root_result_shape() {
    let source = json!({ "a": 1, "b": 2, "c": 3 });

    let result_shape = json!([
        "{{Steps.step1.output.source.a}}",
        "{{Steps.step1.output.source.b}}",
        "{{Steps.step1.output.source.c}}",
    ]);

    let value = run_make(result_shape, create_output_map(json!({ "source": source }))).await;

    assert_json_eq!(value, json!([1, 2, 3]));
}

fn keys(v: &Value) -> Vec<String> {
    v.as_object().unwrap().keys().cloned().collect()
}

#[tokio::test]
async fn keeps_the_result_shape_key_order() {
    // kirun-java's Make builds a JsonObject in the order of the shape it is given (QA-0457).
    let mut context = Map::new();
    context.insert("nid".to_string(), kirun::runtime::context_element::ContextElement::new(None, json!("01JA2B3C4D5E6F7G8H9JKMNPQR")));
    let shape = json!({ "condition": { "field": "id", "operator": "EQUALS", "value": "{{Context.nid}}" }, "page": 0, "size": 1 });
    let make = Make::new();
    let fep = fep().set_arguments(args! { "resultShape" => shape }).set_context(context).set_steps(StepsMap::new());
    let value = first_value(&make, fep, "value").await.unwrap();
    assert_eq!(keys(&value), ["condition", "page", "size"]);
    assert_eq!(keys(&value["condition"]), ["field", "operator", "value"]);
    assert_eq!(value["condition"]["value"], json!("01JA2B3C4D5E6F7G8H9JKMNPQR"));

    // A shape that arrives in another order (the Java core's read path rebuilds stored
    // definitions as HashMaps: field, value, operator) keeps that order too.
    let shape = json!({ "field": "{{Steps.step1.output.source.f}}", "value": "{{Steps.step1.output.source.v}}", "operator": "EQUALS" });
    let value = run_make(shape, create_output_map(json!({ "source": { "f": "id", "v": 3 } }))).await;
    assert_eq!(keys(&value), ["field", "value", "operator"]);
}
