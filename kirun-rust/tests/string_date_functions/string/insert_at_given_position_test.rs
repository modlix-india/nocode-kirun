//! Port of kirun-js `__tests__/engine/function/system/string/InsertAtGivenPositionTest.ts`.

use kirun::function::system::string::insert_at_given_position::InsertAtGivenPosition;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn insert_at_given_positions_test1() {
    let reve = InsertAtGivenPosition::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "position" => 6,
        "insertString" => "surendhar",
    });

    let padded = " THIScsurendharompatY IS A NOcoDE plATFNORM";

    assert_json_eq!(first_value(&reve, fep, "result").await.unwrap(), json!(padded));
}

#[tokio::test]
async fn insert_at_given_positions_test2() {
    let reve = InsertAtGivenPosition::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "position" => 6,
        "insertString" => "surendhar",
    });

    let padded = " THIScsurendharompatY IS A NOcoDE plATFNORM";

    assert_json_eq!(first_value(&reve, fep, "result").await.unwrap(), json!(padded));
}

#[tokio::test]
async fn insert_at_given_positions_test3() {
    let reve = InsertAtGivenPosition::new();
    let fep = fep().set_arguments(args! {
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "position" => 29,
        "insertString" => "surendhar",
    });

    let padded = " THIScompatY IS A NOcoDE plATsurendharFNORM";

    assert_json_eq!(first_value(&reve, fep, "result").await.unwrap(), json!(padded));
}
