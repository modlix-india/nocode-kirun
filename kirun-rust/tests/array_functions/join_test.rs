//! Port of kirun-js `__tests__/engine/function/system/array/JoinTest.ts` (describe 'Join Tests').

use kirun::function::system::array::join::Join;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn join_test_1() {
    let join = Join::new();
    let array = json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", "Driven", "developement"
    ]);

    let fep = fep_with_args(args! { "source" => array, "delimiter" => " " });

    let res = "test Driven developement I am using eclipse I to test the changes with test Driven developement";
    assert_json_eq!(first_value(&join, fep, "result").await.unwrap(), json!(res));
}

#[tokio::test]
async fn join_test_without_delimiter() {
    let join = Join::new();
    let array = json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", "Driven", "developement"
    ]);

    let fep = fep_with_args(args! { "source" => array });

    let res = "testDrivendevelopementIamusingeclipseItotestthechangeswithtestDrivendevelopement";
    assert_json_eq!(first_value(&join, fep, "result").await.unwrap(), json!(res));
}

#[tokio::test]
async fn join_test_with_mixxed_data_types() {
    let join = Join::new();
    let array = json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", 4, 5
    ]);

    let fep = fep_with_args(args! { "source" => array });

    let res = "testDrivendevelopementIamusingeclipseItotestthechangeswithtest45";
    assert_json_eq!(first_value(&join, fep, "result").await.unwrap(), json!(res));
}

#[tokio::test]
async fn join_test_with_undefined() {
    let join = Join::new();
    // JS `undefined` element is null here; JS Array.join renders both as ''.
    let array = json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", null,
        "with", "test", 4, 5
    ]);

    let fep = fep_with_args(args! { "source" => array });

    let res = "testDrivendevelopementIamusingeclipseItotestthewithtest45";
    assert_json_eq!(first_value(&join, fep, "result").await.unwrap(), json!(res));
}
