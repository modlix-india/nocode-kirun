//! Port of kirun-js `__tests__/engine/function/system/array/SubArrayTest.ts`.

use kirun::function::system::array::sub_array::SubArray;
use serde_json::{json, Value};

use crate::common::*;

fn letters() -> Value {
    json!(["a", "b", "c", "d", "l", "d", "a", "b", "c", "e", "d"])
}

#[tokio::test]
async fn sub_array_of_test_1() {
    let sub = SubArray::new();
    let array = json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", "Driven", "developement"
    ]);
    let res = json!(["am", "using", "eclipse", "I", "to", "test", "the", "changes", "with"]);

    let fep = crate::common::fep().set_arguments(args! { "source" => array, "findFrom" => 4, "length" => 9 });
    assert_json_eq!(first_value(&sub, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn sub_array_of_test_2() {
    let sub = SubArray::new();
    // JS source is `undefined`; null here.
    let fep = fep_with_args(args! { "source" => Value::Null, "findFrom" => 4, "length" => 9 });
    assert!(first_value(&sub, fep, "result").await.is_err());
}

#[tokio::test]
async fn sub_array_of_test_5() {
    let sub = SubArray::new();
    let res = json!(["b", "c", "d", "l", "d", "a", "b"]);
    let fep = crate::common::fep().set_arguments(args! { "source" => letters(), "findFrom" => 1, "length" => 7 });
    assert_json_eq!(first_value(&sub, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn sub_array_of_test_3() {
    let sub = SubArray::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => letters(), "findFrom" => 1123, "length" => 7 });
    assert!(first_value(&sub, fep, "result").await.is_err());
}

#[tokio::test]
async fn sub_array_of_test_4() {
    let sub = SubArray::new();

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

    let array = json!([array2, array4, array1, array1, array1, array3, array2, array4, array1, array1, array4]);
    let res = json!([array2, array4, array1]);

    // JS has findFrom commented out.
    let fep = crate::common::fep().set_arguments(args! { "source" => array, "length" => 3 });
    assert_json_eq!(first_value(&sub, fep, "result").await.unwrap(), res);
}
