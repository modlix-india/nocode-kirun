//! Port of kirun-js `__tests__/engine/function/system/array/AddFirstTest.ts`.

use kirun::function::system::array::add_first::AddFirst;
use serde_json::{json, Value};

use crate::common::*;

#[tokio::test]
async fn add_test_1() {
    let add_first = AddFirst::new();
    let source = json!(["c", "p", 3, 4, 5]);
    let temp = json!("a");
    let temp2 = json!(["a", "c", "p", 3, 4, 5]);

    let fep = fep_with_args(args! { "source" => source, "element" => temp });

    assert_json_eq!(first_value(&add_first, fep, "result").await.unwrap(), temp2);
}

#[tokio::test]
async fn add_test_2() {
    let add_first = AddFirst::new();
    let source = json!(["a", "b", "c", "d", "a", "b", "c", "e", "d"]);
    let temp = json!("surendhar");
    let temp2 = json!(["surendhar", "a", "b", "c", "d", "a", "b", "c", "e", "d"]);

    let fep = fep_with_args(args! { "source" => source, "element" => temp });

    assert_json_eq!(first_value(&add_first, fep, "result").await.unwrap(), temp2);
}

#[tokio::test]
async fn add_test_3() {
    let add_first = AddFirst::new();
    let source = json!(["doing", "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to"]);
    let temp = json!("surendhar");
    let temp2 = json!([
        "surendhar", "doing", "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to"
    ]);

    let fep = fep_with_args(args! { "source" => source, "element" => temp });

    assert_json_eq!(first_value(&add_first, fep, "result").await.unwrap(), temp2);
}

#[tokio::test]
async fn add_test_5() {
    let add_first = AddFirst::new();

    let source1 = json!(["doing", "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to"]);
    let source2 = json!([
        "doing", "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "with", "typescript"
    ]);
    let source3 = json!([
        "doing", "test", "Driven", "developement", "I", "am", "using", "eclipse", "I", "to", "with", "typescript",
        "newone", "framework"
    ]);
    let source4 = json!(["am", "using", "eclipse", "I", "to", "with", "typescript", "newone"]);

    let source = json!([
        source1, source4, source1, source2, source3, source4, source1, source2, source3
    ]);

    let obj = json!({
        "fname": "surendhar",
        "lname": "s",
        "age": 23,
        "company": " Fincity Corporation ",
    });

    let temp2 = json!([
        obj, source1, source4, source1, source2, source3, source4, source1, source2, source3
    ]);

    let fep = fep_with_args(args! { "source" => source, "element" => obj });

    assert_json_eq!(first_value(&add_first, fep, "result").await.unwrap(), temp2);
}

#[tokio::test]
async fn add_test_4() {
    let add_first = AddFirst::new();
    let temp = json!("a");

    let fep = fep_with_args(args! { "source" => Value::Null, "elementObject" => temp });

    // JS: rejects.toThrow() with no message.
    assert!(first_value(&add_first, fep, "result").await.is_err());
}

/// Second JS test named 'Add Test 5'.
#[tokio::test]
async fn add_test_5_2() {
    let add_first = AddFirst::new();
    let temp = Value::Null;
    // JS `undefined` inside the array is `null` here.
    let temp2 = json!(["a", "c", "p", null, 3, 4, 5]);

    let fep = fep_with_args(args! { "source" => temp2, "element" => temp });

    // JS expects [undefined, 'a', 'c', 'p', undefined, 3, 4, 5]; undefined == null in Rust.
    assert_json_eq!(
        first_value(&add_first, fep, "result").await.unwrap(),
        json!([null, "a", "c", "p", null, 3, 4, 5])
    );
}
