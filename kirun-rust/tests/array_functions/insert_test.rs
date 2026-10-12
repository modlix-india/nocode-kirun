//! Port of kirun-js `__tests__/engine/function/system/array/InsertTest.ts`.

use kirun::function::system::array::insert::Insert;
use serde_json::{json, Value};

use crate::common::*;

fn words() -> Value {
    json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", "Driven", "developement"
    ])
}

#[tokio::test]
async fn insert_of_test_1() {
    let ins = Insert::new();
    let fep = fep_with_args(args! { "source" => words(), "offset" => 4, "element" => json!(["this is an array"]) });

    let res = json!([
        "test", "Driven", "developement", "I", ["this is an array"], "am", "using", "eclipse", "I", "to", "test",
        "the", "changes", "with", "test", "Driven", "developement"
    ]);
    assert_json_eq!(first_value(&ins, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn insert_of_test_2() {
    let ins = Insert::new();
    let arr = json!([1, 2, 3, 4, 5, 6, 7, 8]);
    let fep = fep_with_args(args! { "source" => arr, "offset" => 2, "element" => json!(["this is an array"]) });

    let res = json!([1, 2, ["this is an array"], 3, 4, 5, 6, 7, 8]);
    assert_json_eq!(first_value(&ins, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn insert_of_test_3() {
    let ins = Insert::new();
    let arr = json!([1, 2, 3, 4, 5, 6, 7, 8]);
    let fep = fep_with_args(args! { "source" => arr, "offset" => 0, "element" => json!(["this is an array"]) });

    let res = json!([["this is an array"], 1, 2, 3, 4, 5, 6, 7, 8]);
    assert_json_eq!(first_value(&ins, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn insert_of_test_4() {
    let ins = Insert::new();
    let arr = json!([1, 2, 3, 4, 5, 6, 7, 8]);
    // offset = arr.length
    let fep = fep_with_args(args! { "source" => arr, "offset" => 8, "element" => json!(["this is an array"]) });

    let res = json!([1, 2, 3, 4, 5, 6, 7, 8, ["this is an array"]]);
    assert_json_eq!(first_value(&ins, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn insert_of_test_5() {
    let ins = Insert::new();
    let fep = fep_with_args(args! { "source" => json!([]), "element" => json!(["this is an array"]) });

    let res = json!([["this is an array"]]);
    assert_json_eq!(first_value(&ins, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn insert_of_test_6() {
    let ins = Insert::new();
    let fep = fep_with_args(args! { "source" => Value::Null, "offset" => 0, "element" => json!(["this is an array"]) });
    assert!(first_value(&ins, fep, "result").await.is_err());
}
