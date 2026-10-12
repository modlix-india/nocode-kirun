//! Port of kirun-js `__tests__/engine/function/system/string/ConcatenateTest.ts`.

use kirun::function::system::string::concatenate::Concatenate;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn conatenate_test1() {
    let cat = Concatenate::new();
    let array = vec![
        "I ", "am ", "using ", "eclipse ", "to ", "test ", "the ", "changes ", "with ", "test ", "Driven ",
        "developement",
    ];
    let fep = fep().set_arguments(args! { "value" => array });

    assert_json_eq!(
        first_value(&cat, fep, "value").await.unwrap(),
        json!("I am using eclipse to test the changes with test Driven developement")
    );
}

#[tokio::test]
async fn conatenate_test2() {
    let cat = Concatenate::new();
    let list = vec![
        "no code ",
        " Kirun ",
        " true ",
        "\"'this is between the strings qith special characters'\"",
        " PLATform ",
        "2",
    ];
    let fep = fep().set_arguments(args! { "value" => list });

    assert_json_eq!(
        first_value(&cat, fep, "value").await.unwrap(),
        json!("no code  Kirun  true \"'this is between the strings qith special characters'\" PLATform 2")
    );
}
