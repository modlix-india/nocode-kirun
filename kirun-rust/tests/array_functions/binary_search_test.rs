//! Port of kirun-js `__tests__/engine/function/system/array/BinarySearchTest.ts`.

use kirun::function::system::array::binary_search::BinarySearch;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn binary_search_test_1() {
    let bsearch = BinarySearch::new();
    let src = json!([1, 4, 6, 7, 10, 14, 16, 20]);
    let search = json!(16);

    let fep = crate::common::fep().set_arguments(args! {
        "source" => src,
        "srcFrom" => 1,
        "findPrimitive" => search,
        "length" => 6,
    });

    assert_json_eq!(first_value(&bsearch, fep, "index").await.unwrap(), json!(6));
}

#[tokio::test]
async fn binary_search_test_2() {
    let bsearch = BinarySearch::new();
    let src = json!([1, 4, 6, 7, 10, 14, 16, 20]);
    let search = json!(78);

    let fep = crate::common::fep().set_arguments(args! {
        "source" => src,
        "srcFrom" => 1,
        "findPrimitive" => search,
        "length" => 8 - 2, // src.length - 2
    });

    assert_json_eq!(first_value(&bsearch, fep, "index").await.unwrap(), json!(-1));
}

#[tokio::test]
async fn binary_search_test_3() {
    let bsearch = BinarySearch::new();
    let src = json!([1, 4, 6, 7, 10, 14, 16, 20]);
    let search = json!(78);

    let fep = crate::common::fep().set_arguments(args! {
        "source" => src,
        "srcFrom" => 1,
        "findPrimitive" => search,
        "length" => 100,
    });

    // JS: rejects.toThrow() with no message.
    assert!(first_value(&bsearch, fep, "index").await.is_err());
}

#[tokio::test]
async fn binary_search_test_6() {
    let bsearch = BinarySearch::new();
    let src = json!([1, 4, 6, 7, 10, 14, 17, 20]);
    let search = json!(17);

    let fep = crate::common::fep().set_arguments(args! {
        "source" => src,
        "srcFrom" => 0,
        "findPrimitive" => search,
        "length" => 5,
    });

    assert_json_eq!(first_value(&bsearch, fep, "index").await.unwrap(), json!(-1));
}

// JS 'Binary Search test 7' is commented out in the source; not ported.

#[tokio::test]
async fn binary_search_test_4() {
    let bsearch = BinarySearch::new();
    let src = json!(["a", "b", "d", "f", "h", "k", "z"]);
    let search = json!("z");

    let fep = crate::common::fep().set_arguments(args! { "source" => src, "findPrimitive" => search });

    // src.length - 1
    assert_json_eq!(first_value(&bsearch, fep, "index").await.unwrap(), json!(6));
}

#[tokio::test]
async fn binary_search_test_5() {
    let bsearch = BinarySearch::new();
    let arr = json!(["a", "b", "c", "d", "e", "g", "i", "j", "k", "r", "s", "z"]);
    let search = json!("s");

    let fep = crate::common::fep().set_arguments(args! {
        "source" => arr,
        "findPrimitive" => search,
        "length" => 12 - 1, // arr.length - 1
    });

    assert_json_eq!(first_value(&bsearch, fep, "index").await.unwrap(), json!(10));
}
