//! Port of kirun-js `__tests__/engine/function/system/array/MisMatchTest.ts`.

use kirun::function::system::array::mis_match::MisMatch;
use serde_json::{json, Value};

use crate::common::*;

fn letters() -> Value {
    json!(["a", "b", "c", "d", "l", "d", "a", "b", "c", "e", "d"])
}

#[tokio::test]
async fn mismatch_test_1() {
    let mismatch = MisMatch::new();
    let fep = crate::common::fep().set_arguments(args! {
        "source" => letters(),
        "findFrom" => 7,
        "secondSource" => json!(["b", "c", "d"]),
        "secondSrcFrom" => 0,
        "length" => 3,
    });
    assert_json_eq!(first_value(&mismatch, fep, "result").await.unwrap(), json!(2));
}

#[tokio::test]
async fn mismatch_test_2() {
    let mismatch = MisMatch::new();
    let fep = crate::common::fep().set_arguments(args! {
        "source" => letters(),
        "findFrom" => 0,
        "secondSource" => json!(["b", "c", "d"]),
        "secondSrcFrom" => 2,
        "length" => 5,
    });
    assert!(first_value(&mismatch, fep, "result").await.is_err());
}

#[tokio::test]
async fn mismatch_test_3() {
    let mismatch = MisMatch::new();

    let array1 = json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", "Driven", "developement"
    ]);
    let array2 = json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with"
    ]);
    let array3 = json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", "Driven", "developement"
    ]);
    let array4 = json!(["test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to"]);

    let arr = json!([array2, array4, array1, array1, array1, array3, array2, array4, array1, array1, array4]);
    let res = json!([array1, array1, array4]);

    let fep = crate::common::fep().set_arguments(args! {
        "source" => arr,
        "findFrom" => 2,
        "secondSource" => res,
        "secondSrcFrom" => 3,
        "length" => 3,
    });
    assert_json_eq!(first_value(&mismatch, fep, "result").await.unwrap(), json!(2));
}

#[tokio::test]
async fn mismatch_test_4() {
    let mismatch = MisMatch::new();
    let fep = crate::common::fep().set_arguments(args! {
        "source" => letters(),
        "findFrom" => 1,
        "secondSource" => json!(["b", "c", "d"]),
        "secondSrcFrom" => 0,
        "length" => 3,
    });
    assert_json_eq!(first_value(&mismatch, fep, "result").await.unwrap(), json!(-1));
}
