//! Port of kirun-js `__tests__/engine/function/system/array/ConcatenateTest.ts`.

use kirun::function::system::array::concatenate::Concatenate;
use serde_json::{json, Value};

use crate::common::*;

#[tokio::test]
async fn concatenate_test_1() {
    let add = Concatenate::new();
    let source = json!([2, 2, 3, 4, 5]);
    let second_source = json!([2, 2, 2, 3, 4, 5]);
    // temp1.splice(temp1.length, 0, ...temp2)
    let temp1 = json!([2, 2, 3, 4, 5, 2, 2, 2, 3, 4, 5]);

    let fep = fep_with_args(args! { "source" => source, "secondSource" => second_source });

    assert_json_eq!(first_value(&add, fep, "result").await.unwrap(), temp1);
}

#[tokio::test]
async fn concatenate_test_2() {
    let add = Concatenate::new();
    let source = json!(["nocode", "platform"]);
    let second_source = json!([]);
    let temp1 = json!(["nocode", "platform"]);

    let fep = fep_with_args(args! { "source" => source.clone(), "secondSource" => second_source });

    first_value(&add, fep, "result").await.unwrap();

    // JS checks the caller's source array is not mutated. Rust passes arguments by value,
    // so this only confirms the input is unchanged.
    assert_json_eq!(source, temp1);
}

#[tokio::test]
async fn concatenate_test_3() {
    let add = Concatenate::new();
    let source = json!([]);
    let second_source = json!([]);
    let temp1 = json!([]);

    let fep = fep_with_args(args! { "source" => source.clone(), "secondSource" => second_source });

    first_value(&add, fep, "result").await.unwrap();

    // See concatenate_test_2: Rust cannot mutate the caller's value.
    assert_json_eq!(source, temp1);
}

#[tokio::test]
async fn concatenate_test_4() {
    let add = Concatenate::new();
    let second_source = json!([]);

    let fep = fep_with_args(args! { "source" => Value::Null, "secondSource" => second_source });

    assert_err_contains(first_value(&add, fep, "result").await, "");
}

#[tokio::test]
async fn concatenate_test_5() {
    let add = Concatenate::new();
    let second_source = json!([]);

    // JS passes `undefined` for secondSource; it is null here.
    let fep = fep_with_args(args! { "source" => second_source, "secondSource" => Value::Null });

    // kirun-java's wording: each schema error carries its path (`eachSecondSource - ...`).
    assert_err_contains(
        first_value(&add, fep, "result").await,
        "Error while executing the function System.Array.Concatenate's parameter secondSource [Expected: Array<Integer | Long | Float | Double | String | Boolean | Array | Null | Object>]: eachSecondSource - eachSecondSource - Expected an array but found null",
    );
}
