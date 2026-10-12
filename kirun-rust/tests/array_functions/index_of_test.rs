//! Port of kirun-js `__tests__/engine/function/system/array/IndexOfTest.ts`.

use kirun::function::system::array::index_of::IndexOf;
use serde_json::{json, Value};

use crate::common::*;

fn array() -> Value {
    json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", "Driven", "developement"
    ])
}

#[tokio::test]
async fn index_of_test_1() {
    let ind = IndexOf::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => array(), "elementObject" => "with", "findFrom" => 2 });
    assert_json_eq!(first_value(&ind, fep, "result").await.unwrap(), json!(12));
}

#[tokio::test]
async fn index_of_test_2() {
    let ind = IndexOf::new();
    let find = "with";

    let fep = crate::common::fep().set_arguments(args! { "source" => array(), "elementObject" => find, "findFrom" => -2 });
    assert!(first_value(&ind, fep, "result").await.is_err());

    let fep = crate::common::fep().set_arguments(args! { "source" => json!([]), "elementObject" => find, "findFrom" => -2 });
    assert_json_eq!(first_value(&ind, fep, "result").await.unwrap(), json!(-1));
}

#[tokio::test]
async fn index_of_test_3() {
    let ind = IndexOf::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => array(), "elementObject" => "witah", "findFrom" => 0 });
    assert_json_eq!(first_value(&ind, fep, "result").await.unwrap(), json!(-1));
}

#[tokio::test]
async fn index_of_test_4() {
    let ind = IndexOf::new();

    let array = array();
    let array2 = json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with"
    ]);
    let array3 = json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", "Driven", "developement"
    ]);
    let array4 = json!(["test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to"]);

    let arr = json!([array3, array, array3, array2, array4, array]);
    let find = array.clone();

    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "elementObject" => find, "findFrom" => 2 });
    // array3 has the same contents as array, so deep equality finds index 2.
    assert_json_eq!(first_value(&ind, fep, "result").await.unwrap(), json!(2));
}

#[tokio::test]
async fn indexof_test_5() {
    let ind = IndexOf::new();

    let array1 = json!(["test", "Driven", "developement", "I", "am"]);
    let js1 = json!({ "boolean": false, "array": array1, "char": "o" });
    let js2 = json!({ "boolean": false, "array": array1, "char": "asd" });
    let js3 = json!({ "array": array1 });
    let js4 = json!({ "boolean": false, "array": array1, "char": "oa" });

    let arr = json!([js1, js2, js1, js3, js3, js4, js1]);
    let find = js4.clone();

    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "elementObject" => find });
    assert_json_eq!(first_value(&ind, fep, "result").await.unwrap(), json!(5));

    let fep = crate::common::fep().set_arguments(args! {});
    assert!(first_value(&ind, fep, "result").await.is_err());
}
