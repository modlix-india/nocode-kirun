//! Port of kirun-js `__tests__/engine/function/system/array/DeleteFromTest.ts`.

use kirun::function::system::array::delete_from::DeleteFrom;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn delete_from_1() {
    let del = DeleteFrom::new();
    let arr = json!(["a", "b", "c", "d", "e", "f", "g", "h", "i", "a", "a", "a"]);

    let fep = fep_with_args(args! { "source" => arr, "srcFrom" => 6, "length" => 6 });

    let res = json!(["a", "b", "c", "d", "e", "f"]);
    assert_json_eq!(first_value(&del, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn del_from_2() {
    let del = DeleteFrom::new();
    let arr = json!(["a", "b", "c", "d", "e", "f", "g", "h", "i", "a", "a", "a"]);

    let fep = fep_with_args(args! { "source" => arr, "srcFrom" => 6, "length" => 3 });

    let res = json!(["a", "b", "c", "d", "e", "f", "a", "a", "a"]);
    assert_json_eq!(first_value(&del, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn del_from_3() {
    let del = DeleteFrom::new();

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

    let obj = json!({ "fname": "surendhar", "lname": "s", "age": 23, "company": "fincity" });

    let arr = json!([obj, array2, array4, array1, array1, array3, array2, array4, array1, array1, array4]);
    let res = json!([obj, array2, array4, array1, array1, array1, array1, array4]);

    let fep = fep_with_args(args! { "source" => arr, "srcFrom" => 5, "length" => 3 });
    assert_json_eq!(first_value(&del, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn del_from_4() {
    let del = DeleteFrom::new();
    let arr = json!(["a", "b", "c", "d", "e", "f", "g", "h", "i", "a", "a", "a"]);
    let res = json!(["a", "b", "c"]);

    let fep = fep_with_args(args! { "source" => arr, "srcFrom" => 3 });
    assert_json_eq!(first_value(&del, fep, "result").await.unwrap(), res);
}
