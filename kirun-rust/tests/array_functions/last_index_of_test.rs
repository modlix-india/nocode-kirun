//! Port of kirun-js `__tests__/engine/function/system/array/LastIndexOfTest.ts`.

use kirun::function::system::array::last_index_of::LastIndexOf;
use serde_json::{json, Value};

use crate::common::*;

fn words() -> Value {
    json!([
        "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "test", "the", "changes",
        "with", "test", "Driven", "developement"
    ])
}

#[tokio::test]
async fn last_index_of_test_1() {
    let lind = LastIndexOf::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => words(), "elementObject" => "test", "findFrom" => 2 });
    assert_json_eq!(first_value(&lind, fep, "result").await.unwrap(), json!(13));
}

#[tokio::test]
async fn last_index_of_test_2() {
    let lind = LastIndexOf::new();
    // JS `let find;` is undefined; null here.
    let fep = crate::common::fep().set_arguments(args! { "source" => words(), "elementObject" => Value::Null, "findFrom" => -2 });
    assert!(first_value(&lind, fep, "result").await.is_err());
}

#[tokio::test]
async fn last_index_of_test_3() {
    let lind = LastIndexOf::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => Value::Null, "elementObject" => Value::Null, "findFrom" => 2 });
    assert!(first_value(&lind, fep, "result").await.is_err());
}

#[tokio::test]
async fn last_index_of_test_4() {
    let lind = LastIndexOf::new();

    let fep = crate::common::fep().set_arguments(args! { "source" => words(), "elementObject" => "developement", "findFrom" => 12 });
    assert_json_eq!(first_value(&lind, fep, "result").await.unwrap(), json!(15));

    let fep = crate::common::fep().set_arguments(args! { "source" => words(), "elementObject" => "newas", "findFrom" => 12 });
    assert_json_eq!(first_value(&lind, fep, "result").await.unwrap(), json!(-1));
}

#[tokio::test]
async fn last_index_of_test_5() {
    let lind = LastIndexOf::new();
    let fep = crate::common::fep().set_arguments(args! { "source" => words(), "elementObject" => "changes", "findFrom" => -2 });
    assert!(first_value(&lind, fep, "result").await.is_err());
}

#[tokio::test]
async fn last_index_of_test_6() {
    let lind = LastIndexOf::new();

    let array1 = json!(["test", "Driven", "developement", "I", "am"]);
    let js1 = json!({ "boolean": false, "array": array1, "char": "o" });
    let js2 = json!({ "boolean": false, "array": array1, "char": "asd" });
    let js3 = json!({ "array": array1 });
    let js4 = json!({ "boolean": false, "array": array1, "char": "asdsd" });

    let arr = json!([js1, js2, js4, js3, js4, js1, js1]);

    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "elementObject" => js4, "findFrom" => 1 });
    assert_json_eq!(first_value(&lind, fep, "result").await.unwrap(), json!(4));
}
