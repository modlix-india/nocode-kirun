//! Port of kirun-js `__tests__/engine/function/system/array/LastIndexOfArrayTest.ts`.

use kirun::function::system::array::last_index_of_array::LastIndexOfArray;
use serde_json::{json, Value};

use crate::common::*;

#[tokio::test]
async fn last_index_of_array_test_1() {
    let larr = LastIndexOfArray::new();
    let array = json!(["a", "b", "c", "d", "a", "b", "c", "e", "d", "b", "c", "d"]);
    let res = json!(["b", "c", "d"]);

    let fep = crate::common::fep().set_arguments(args! { "source" => array, "secondSource" => res, "findFrom" => 1 });
    assert_json_eq!(first_value(&larr, fep, "result").await.unwrap(), json!(9));
}

#[tokio::test]
async fn last_index_of_array_test_2() {
    let larr = LastIndexOfArray::new();
    let arr = json!(["b", "c", "d", "a", "b", "c", "e", "d", "b", "c", "d"]);
    let res = json!(["b", "d"]);

    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "secondSource" => res, "findFrom" => 1 });
    assert_json_eq!(first_value(&larr, fep, "result").await.unwrap(), json!(-1));
}

#[tokio::test]
async fn last_index_of_array_test_3() {
    let larr = LastIndexOfArray::new();
    let arr = json!(["a", "b", "c", "d", "a", "b", "c", "e", "d", "b", "c", "d"]);

    // JS secondSource is `undefined`; null here.
    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "secondSource" => Value::Null, "findFrom" => 1 });
    assert!(first_value(&larr, fep, "result").await.is_err());

    let fep1 = crate::common::fep().set_arguments(args! { "source" => Value::Null, "secondSource" => Value::Null, "findFrom" => 1 });
    assert!(first_value(&larr, fep1, "result").await.is_err());
}

#[tokio::test]
async fn last_index_of_array_test_4() {
    let larr = LastIndexOfArray::new();

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

    let arr = json!([array2, array1, array1, array4, array3, array2, array4, array1, array1, array4]);
    let res = json!([array1, array1, array4]);

    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "secondSource" => res, "findFrom" => 2 });
    assert_json_eq!(first_value(&larr, fep, "result").await.unwrap(), json!(7));
}
