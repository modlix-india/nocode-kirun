//! Port of kirun-js `__tests__/engine/function/system/array/RotateTest.ts`.

use kirun::function::system::array::rotate::Rotate;
use serde_json::{json, Value};

use crate::common::*;

fn words() -> Value {
    json!([
        "I", "am", "using", "eclipse", "to", "test", "the", "changes", "with", "test", "Driven", "developement"
    ])
}

#[tokio::test]
async fn rotate_test1() {
    let rotate = Rotate::new();
    let fep = fep_with_args(args! { "source" => words(), "rotateLength" => 16 });

    let res = json!([
        "to", "test", "the", "changes", "with", "test", "Driven", "developement", "I", "am", "using", "eclipse"
    ]);
    assert_json_eq!(first_value(&rotate, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn rotate_test_2() {
    let rotate = Rotate::new();
    // rotateLength = src.length - 1
    let fep = fep_with_args(args! { "source" => words(), "rotateLength" => 11 });

    let res = json!([
        "developement", "I", "am", "using", "eclipse", "to", "test", "the", "changes", "with", "test", "Driven"
    ]);
    assert_json_eq!(first_value(&rotate, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn rotate_test_3() {
    let rotate = Rotate::new();
    let fep = fep_with_args(args! { "source" => words() });

    let res = json!([
        "am", "using", "eclipse", "to", "test", "the", "changes", "with", "test", "Driven", "developement", "I"
    ]);
    assert_json_eq!(first_value(&rotate, fep, "result").await.unwrap(), res);
}
