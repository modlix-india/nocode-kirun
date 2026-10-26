//! Port of kirun-js `__tests__/engine/function/system/string/PrePadTest.ts`.

use kirun::function::system::string::pre_pad::PrePad;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn prepad_test1() {
    let prepad = PrePad::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "prepadString" => "hiran",
        "length" => 12,
    });

    let padded = "hiranhiranhi THIScompatY IS A NOcoDE plATFNORM";

    assert_json_eq!(first_value(&prepad, fep, "result").await.unwrap(), json!(padded));
}

#[tokio::test]
async fn prepad_test2() {
    let prepad = PrePad::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "prepadString" => " h ",
        "length" => 11,
    });

    let padded = " h  h  h  h THIScompatY IS A NOcoDE plATFNORM";

    assert_json_eq!(first_value(&prepad, fep, "result").await.unwrap(), json!(padded));
}

#[tokio::test]
async fn prepad_test3() {
    let prepad = PrePad::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "prepadString" => "hiran",
        "length" => 4,
    });

    let reveresed = "hira THIScompatY IS A NOcoDE plATFNORM";

    assert_json_eq!(first_value(&prepad, fep, "result").await.unwrap(), json!(reveresed));
}

/// kirun-java `PrePadTest.emptyPadStringReturnsTheInput` (4.9.0): an empty pad string leaves
/// the input as it is (Java used to loop forever).
#[tokio::test]
async fn prepad_empty_pad_string_returns_the_input() {
    let fep = fep().set_arguments(args! {
        "string" => "5",
        "prepadString" => "",
        "length" => 3,
    });
    assert_json_eq!(first_value(&PrePad::new(), fep, "result").await.unwrap(), json!("5"));
}
