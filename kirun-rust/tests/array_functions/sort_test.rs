//! Port of kirun-js `__tests__/engine/function/system/array/SortTest.ts`.

use kirun::function::system::array::sort::Sort;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn sort_test_1() {
    let sort = Sort::new();
    let arr = json!([12, 15, 98, 1]);
    // length = arr.length
    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "findFrom" => 0, "length" => 4 });
    assert_json_eq!(first_value(&sort, fep, "result").await.unwrap(), json!([1, 12, 15, 98]));
}

#[tokio::test]
async fn sort_test_2() {
    let sort = Sort::new();
    let arr = json!([12, 15, 98, 1]);
    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "findFrom" => 1, "ascending" => false });
    assert_json_eq!(first_value(&sort, fep, "result").await.unwrap(), json!([12, 98, 15, 1]));
}

#[tokio::test]
async fn sort_test_3() {
    let sort = Sort::new();
    let arr = json!([12, 15, 98, 1, "sure", "c"]);
    let res = json!([12, 15, 1, 98, "c", "sure"]);
    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "findFrom" => 2, "ascending" => true });
    assert_json_eq!(first_value(&sort, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn sort_test_4() {
    let sort = Sort::new();
    let arr = json!([12, null, null, 15, 98, 1, "sure", "c"]);
    let res = json!([12, null, "sure", "c", 98, 15, 1, null]);
    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "findFrom" => 2, "ascending" => false });
    assert_json_eq!(first_value(&sort, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn sort_test_5() {
    let sort = Sort::new();
    let arr = json!(["Banana", "Orange", "Apple", "Mango"]);
    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "findFrom" => 1, "length" => 3 });
    let res = json!(["Banana", "Apple", "Mango", "Orange"]);
    assert_json_eq!(first_value(&sort, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn sort_test_6() {
    let sort = Sort::new();
    let arr = json!([
        { "order": 13 },
        { "order": 3 },
        { "order": 130 },
        { "order": 10 },
        { "order": 21 },
        { "order": 1 },
    ]);
    let fep = crate::common::fep().set_arguments(args! { "source" => arr, "findFrom" => 0, "length" => 6, "keyPath" => "order" });
    let res = json!([
        { "order": 1 },
        { "order": 3 },
        { "order": 10 },
        { "order": 13 },
        { "order": 21 },
        { "order": 130 },
    ]);
    assert_json_eq!(first_value(&sort, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn sort_test_7() {
    let sort = Sort::new();
    let arr = json!([
        { "order": { "order": 13 } },
        { "order": { "order": 3 } },
        { "order": { "order": 130 } },
        { "order": { "order": 10 } },
        { "order": { "order": 21 } },
        { "order": { "order": 1 } },
    ]);
    let fep = crate::common::fep().set_arguments(args! {
        "source" => arr,
        "findFrom" => 0,
        "length" => 6,
        "keyPath" => "order.order",
    });
    let res = json!([
        { "order": { "order": 1 } },
        { "order": { "order": 3 } },
        { "order": { "order": 10 } },
        { "order": { "order": 13 } },
        { "order": { "order": 21 } },
        { "order": { "order": 130 } },
    ]);
    assert_json_eq!(first_value(&sort, fep, "result").await.unwrap(), res);
}

#[tokio::test]
async fn sort_test_8() {
    let sort = Sort::new();
    let arr = json!([
        { "order": { "order": 13 } },
        { "order": { "order": 3 } },
        { "order": { "order": 130 } },
        { "order": { "order": 10 } },
        { "order": { "order": 21 } },
        { "order": { "order": 1 } },
    ]);
    let fep = crate::common::fep().set_arguments(args! {
        "source" => arr,
        "findFrom" => 0,
        "length" => 6,
        "keyPath" => "order.order",
        "ascending" => false,
    });
    let res = json!([
        { "order": { "order": 130 } },
        { "order": { "order": 21 } },
        { "order": { "order": 13 } },
        { "order": { "order": 10 } },
        { "order": { "order": 3 } },
        { "order": { "order": 1 } },
    ]);
    assert_json_eq!(first_value(&sort, fep, "result").await.unwrap(), res);
}
