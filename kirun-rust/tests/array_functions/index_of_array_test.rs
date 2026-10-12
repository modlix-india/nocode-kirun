//! Port of kirun-js `__tests__/engine/function/system/array/IndexOfArrayTest.ts`.

use kirun::function::system::array::index_of_array::IndexOfArray;
use serde_json::{json, Value};

use crate::common::*;

fn abcd() -> Value {
    json!(["a", "b", "c", "d", "a", "b", "c", "e", "d"])
}

/// The nested-array fixture shared by both 'index of array 3' tests: (arr, res).
fn nested() -> (Value, Value) {
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

    let arr = json!([array2, array4, array1, array1, array3, array2, array4, array1, array1, array4]);
    let res = json!([array1, array1, array4]);
    (arr, res)
}

#[tokio::test]
async fn index_of_array_1() {
    let ioa = IndexOfArray::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => abcd(), "secondSource" => json!(["b", "c", "d"]), "findFrom" => 1 });
    assert_json_eq!(first_value(&ioa, fep, "result").await.unwrap(), json!(1));
}

#[tokio::test]
async fn index_of_array_2() {
    let ioa = IndexOfArray::new();
    let fep = crate::common::fep().set_arguments(args! {
        "source" => abcd(),
        "secondSource" => json!(["b", "c", "e", "d"]),
        "findFrom" => 4,
    });
    assert_json_eq!(first_value(&ioa, fep, "result").await.unwrap(), json!(5));
}

#[tokio::test]
async fn index_of_array_3() {
    let ioa = IndexOfArray::new();
    let (arr, res) = nested();
    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "secondSource" => res, "findFrom" => 2 });
    assert_json_eq!(first_value(&ioa, fep, "result").await.unwrap(), json!(7));
}

#[tokio::test]
async fn index_of_array_test_4() {
    let ioa = IndexOfArray::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => abcd(), "secondSource" => json!(["b", "e", "d"]), "findFrom" => 4 });
    assert_json_eq!(first_value(&ioa, fep, "result").await.unwrap(), json!(-1));
}

#[tokio::test]
async fn index_of_array_5() {
    let ioa = IndexOfArray::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => abcd(), "secondSource" => json!(["c", "e"]) });
    assert_json_eq!(first_value(&ioa, fep, "result").await.unwrap(), json!(6));
}

#[tokio::test]
async fn index_of_array_6() {
    let ioa = IndexOfArray::new();
    // JS secondSource is `undefined`; null here.
    let fep = crate::common::fep().set_arguments(args! { "source" => json!(["a", "b", "c"]), "secondSource" => Value::Null });
    assert!(first_value(&ioa, fep, "result").await.is_err());
}

/// Second JS test named 'index of array 3'.
#[tokio::test]
async fn index_of_array_3_2() {
    let ioa = IndexOfArray::new();
    let (arr, res) = nested();
    let len = arr.as_array().unwrap().len();
    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "secondSource" => res, "findFrom" => len });
    assert_json_eq!(first_value(&ioa, fep, "result").await.unwrap(), json!(-1));
}
