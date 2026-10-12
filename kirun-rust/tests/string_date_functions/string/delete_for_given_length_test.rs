//! Port of kirun-js `__tests__/engine/function/system/string/DeleteForGivenLengthTest.ts`.

use kirun::function::system::string::delete_for_given_length::DeleteForGivenLength;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn delete_for_given_length_test1() {
    let delete_t = DeleteForGivenLength::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "startPosition" => 10,
        "endPosition" => 18,
    });

    let output_string = " THIScompaNOcoDE plATFNORM";

    assert_json_eq!(first_value(&delete_t, fep, "result").await.unwrap(), json!(output_string));
}

#[tokio::test]
async fn delete_for_given_length_test2() {
    let delete_t = DeleteForGivenLength::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "startPosition" => 4,
        "endPosition" => 10,
    });

    let output_string = " THItY IS A NOcoDE plATFNORM";

    assert_json_eq!(first_value(&delete_t, fep, "result").await.unwrap(), json!(output_string));
}
