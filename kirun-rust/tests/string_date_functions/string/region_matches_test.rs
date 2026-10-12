//! Port of kirun-js `__tests__/engine/function/system/string/RegionMatchesTest.ts`.
//! (The JS tests are named `toString test1..3`; the names are kept.)

use kirun::function::system::string::region_matches::RegionMatches;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn to_string_test1() {
    let region = RegionMatches::new();
    let fep = fep().set_arguments(args! {
        "boolean" => true,
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "firstOffset" => 5,
        "otherString" => " fincitY compatY ",
        "secondOffset" => 9,
        "length" => 7,
    });

    assert_json_eq!(first_value(&region, fep, "result").await.unwrap(), json!(true));
}

#[tokio::test]
async fn to_string_test2() {
    let region = RegionMatches::new();
    let fep = fep().set_arguments(args! {
        "boolean" => false,
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "firstOffset" => 5,
        "otherString" => " fincitY compatY ",
        "secondOffset" => 1,
        "length" => 7,
    });

    assert_json_eq!(first_value(&region, fep, "result").await.unwrap(), json!(false));
}

#[tokio::test]
async fn to_string_test3() {
    let region = RegionMatches::new();
    let fep = fep().set_arguments(args! {
        "boolean" => true,
        "string" => " THIScompatY IS A NOcoDE plATFNORM",
        "firstOffset" => 10,
        "otherString" => " fincitY compatY ",
        "secondOffset" => 6,
        "length" => 3,
    });

    assert_json_eq!(first_value(&region, fep, "result").await.unwrap(), json!(true));
}
