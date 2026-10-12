//! Port of kirun-js `__tests__/engine/function/system/array/DeleteLastTest.ts`.

use kirun::function::system::array::delete_last::DeleteLast;
use serde_json::{json, Value};

use crate::common::*;

#[tokio::test]
async fn delete_last_test_1() {
    let delet = DeleteLast::new();
    let fep = fep_with_args(args! { "source" => json!([12, 14, 15, 9]) });
    assert_json_eq!(first_value(&delet, fep, "result").await.unwrap(), json!([12, 14, 15]));
}

#[tokio::test]
async fn delete_last_test_2() {
    let delet = DeleteLast::new();
    let fep = fep_with_args(args! { "source" => json!(["c", "p", "i", "e"]) });
    assert_json_eq!(first_value(&delet, fep, "result").await.unwrap(), json!(["c", "p", "i"]));
}

#[tokio::test]
async fn delete_last_test_3() {
    let delet = DeleteLast::new();
    let fep = fep_with_args(args! { "source" => json!([]) });
    assert!(first_value(&delet, fep, "result").await.is_err());
}

/// JS name: 'DeleteFirst Test 4' (copy-paste name in DeleteLastTest.ts).
#[tokio::test]
async fn delete_first_test_4() {
    let delet = DeleteLast::new();

    let array1 = json!(["test", "Driven", "developement", "I", "am"]);
    let js1 = json!({ "boolean": false, "array": array1, "char": "o" });
    let js2 = json!({ "boolean": false, "array": array1, "char": "asd" });
    let js3 = json!({ "array": array1 });
    let js4 = json!({ "boolean": false, "array": array1, "char": "ocbfr" });

    let arr = json!([js1, js2, js3, js4, js1]);
    let res = json!([js1, js2, js3, js4]);

    let fep = fep_with_args(args! { "source" => arr });
    assert_json_eq!(first_value(&delet, fep, "result").await.unwrap(), res);
}

/// JS name: 'DeleteFirst Test 5'.
#[tokio::test]
async fn delete_first_test_5() {
    let delet = DeleteLast::new();
    let fep = fep_with_args(args! { "source" => Value::Null });
    assert!(first_value(&delet, fep, "result").await.is_err());
}
