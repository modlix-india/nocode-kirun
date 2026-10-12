//! Port of kirun-js `__tests__/engine/function/system/array/FrequencyTest.ts`.

use kirun::function::system::array::frequency::Frequency;
use serde_json::{json, Value};

use crate::common::*;

fn words() -> Value {
    json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", "Driven", "developement"
    ])
}

#[tokio::test]
async fn freq_test_1() {
    let freq = Frequency::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => words(), "element" => "I", "srcFrom" => 2, "length" => 10 });
    assert_json_eq!(first_value(&freq, fep, "result").await.unwrap(), json!(2));
}

#[tokio::test]
async fn freq_test_2() {
    let freq = Frequency::new();

    let fep = crate::common::fep().set_arguments(args! {
        "source" => words(),
        "element" => "developement",
        "srcFrom" => -2,
        "length" => 20,
    });
    assert!(first_value(&freq, fep, "result").await.is_err());

    let fep = crate::common::fep().set_arguments(args! {
        "source" => words(),
        "element" => "developement",
        "srcFrom" => 2,
        "length" => 20,
    });
    assert!(first_value(&freq, fep, "result").await.is_err());
}

#[tokio::test]
async fn freq_test_3() {
    let freq = Frequency::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => json!([]), "element" => "I", "srcFrom" => 2, "length" => 10 });
    assert_json_eq!(first_value(&freq, fep, "result").await.unwrap(), json!(0));
}
