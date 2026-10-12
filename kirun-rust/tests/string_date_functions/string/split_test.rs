//! Port of kirun-js `__tests__/engine/function/system/string/SplitTest.ts`.

use kirun::function::system::string::split::Split;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn split_test1() {
    let spli = Split::new();
    let fep = fep().set_arguments(args! {
        "string" => "I am using eclipse to test the changes with test Driven developement",
        "searchString" => " ",
    });

    let array = json!([
        "I", "am", "using", "eclipse", "to", "test", "the", "changes", "with", "test", "Driven", "developement"
    ]);

    assert_json_eq!(first_value(&spli, fep, "result").await.unwrap(), array);
}

#[tokio::test]
async fn split_test2() {
    let spli = Split::new();
    let fep = fep().set_arguments(args! {
        "string" => "I am using eclipse to test the changes with test Driven developement",
        "searchString" => "e",
    });

    let array = json!([
        "I am using ",
        "clips",
        " to t",
        "st th",
        " chang",
        "s with t",
        "st Driv",
        "n d",
        "v",
        "lop",
        "m",
        "nt"
    ]);

    assert_json_eq!(first_value(&spli, fep, "result").await.unwrap(), array);
}
