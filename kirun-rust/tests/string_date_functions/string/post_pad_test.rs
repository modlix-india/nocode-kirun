//! Port of kirun-js `__tests__/engine/function/system/string/PostPadTest.ts`.

use kirun::function::system::string::post_pad::PostPad;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn postpad_test1() {
    let reve = PostPad::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "postpadString" => "hiran",
        "length" => 12,
    });

    let padded = " THIScompatY IS A NOcoDE plATFNORMhiranhiranhi";

    assert_json_eq!(first_value(&reve, fep, "result").await.unwrap(), json!(padded));
}

#[tokio::test]
async fn postpad_test2() {
    let reve = PostPad::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "postpadString" => " h ",
        "length" => 15,
    });

    let reveresed = " THIScompatY IS A NOcoDE plATFNORM h  h  h  h  h ";

    assert_json_eq!(first_value(&reve, fep, "result").await.unwrap(), json!(reveresed));
}

#[tokio::test]
async fn postpad_test3() {
    let reve = PostPad::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "postpadString" => " surendhar ",
        "length" => 100,
    });

    let reveresed = " THIScompatY IS A NOcoDE plATFNORM surendhar  surendhar  surendhar  surendhar  surendhar  surendhar  surendhar  surendhar  surendhar  ";

    assert_json_eq!(first_value(&reve, fep, "result").await.unwrap(), json!(reveresed));
}
