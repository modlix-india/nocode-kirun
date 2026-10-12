//! Port of kirun-js `__tests__/engine/function/system/string/TrimToTest.ts`.

use kirun::function::system::string::trim_to::TrimTo;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn trim_to_test1() {
    let trim = TrimTo::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "length" => 14,
    });

    assert_json_eq!(first_value(&trim, fep, "result").await.unwrap(), json!(" THIScompatY I"));
}

#[tokio::test]
async fn trim_to_test2() {
    let trim = TrimTo::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "length" => 0,
    });

    assert_json_eq!(first_value(&trim, fep, "result").await.unwrap(), json!(""));
}
