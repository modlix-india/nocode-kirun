//! Port of kirun-js `__tests__/engine/function/system/array/DeleteTest.ts`.

use kirun::function::system::array::delete::Delete;
use serde_json::{json, Value};

use crate::common::*;

#[tokio::test]
async fn delete_test_1() {
    let delet = Delete::new();
    let fep = fep_with_args(args! { "source" => json!([12, 14, 15, 9]), "element" => json!([14, 15]) });
    assert_json_eq!(first_value(&delet, fep, "result").await.unwrap(), json!([12, 9]));
}

#[tokio::test]
async fn delete_test_2() {
    let delet = Delete::new();
    let fep = fep_with_args(args! { "source" => json!(["nocode", "platform", 14]), "element" => json!(["platform"]) });
    assert_json_eq!(first_value(&delet, fep, "result").await.unwrap(), json!(["nocode", 14]));
}

#[tokio::test]
async fn delete_test_3() {
    let delet = Delete::new();
    // JS source is `undefined`; null here.
    let fep = fep_with_args(args! { "source" => Value::Null, "element" => json!(["platform"]) });
    assert!(first_value(&delet, fep, "result").await.is_err());
}

/// Second JS test named 'Delete Test 3'.
#[tokio::test]
async fn delete_test_3_2() {
    let delet = Delete::new();
    // JS element is `undefined`; null here.
    let fep = fep_with_args(args! { "source" => json!(["platform"]), "element" => Value::Null });
    assert!(first_value(&delet, fep, "result").await.is_err());
}

#[tokio::test]
async fn delete_test_4() {
    let delet = Delete::new();
    let fep = fep_with_args(args! { "source" => json!(["platform"]), "element" => json!([]) });
    assert!(first_value(&delet, fep, "result").await.is_err());
}

#[tokio::test]
async fn delete_test_5() {
    let delet = Delete::new();

    let arr1 = json!(["nocode", "platform", 14]);
    let arr2 = json!(["nocode", "platiform", 14]);
    let obj = json!({ "arr": arr1, "sri": "krishna", "name": "surendhar" });

    let arr = json!([arr1, arr2, obj, arr2, obj]);
    let del_arr = json!([obj, "2", []]);
    let res = json!([arr1, arr2, arr2]);

    let fep = fep_with_args(args! { "source" => arr, "element" => del_arr });
    assert_json_eq!(first_value(&delet, fep, "result").await.unwrap(), res);
}
