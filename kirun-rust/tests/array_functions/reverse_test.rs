//! Port of kirun-js `__tests__/engine/function/system/array/ReverseTest.ts`.

use kirun::function::system::array::reverse::Reverse;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn reverse_test_1() {
    let rev = Reverse::new();
    let fep = fep_with_args(args! { "source" => json!([4, 5, 6, 7]), "srcFrom" => 0, "length" => 2 });
    assert_json_eq!(first_value(&rev, fep, "result").await.unwrap(), json!([5, 4, 6, 7]));
}

#[tokio::test]
async fn reverse_test_2() {
    let rev = Reverse::new();
    let src = json!([
        "I", "am", "using", "eclipse", "to", "test", "the", "changes", "with", "test", "Driven", "developement"
    ]);
    let fep = fep_with_args(args! { "source" => src, "length" => -2 });
    assert!(first_value(&rev, fep, "result").await.is_err());
}

#[tokio::test]
async fn reverse_test_3() {
    let rev = Reverse::new();
    let arr = json!(["a", "b", "c", "d", "a", "b", "c", "d"]);
    // length = arr.length
    let fep = fep_with_args(args! { "source" => arr, "srcFrom" => 2, "length" => 8 });
    assert!(first_value(&rev, fep, "result").await.is_err());
}

#[tokio::test]
async fn rev_test_4() {
    let rev = Reverse::new();

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

    let arr = json!([array1, array3, array2, array4, array1]);

    // length = arr.length - 2
    let fep = fep_with_args(args! { "source" => arr, "srcFrom" => 1, "length" => 3 });

    let res = json!([array1, array4, array2, array3, array1]);

    let arr = first_value(&rev, fep, "result").await.unwrap();
    assert_json_eq!(arr.clone(), res);

    // length = arr.length - 1
    let fep = fep_with_args(args! { "source" => arr, "srcFrom" => 1, "length" => 4 });

    let res1 = json!([array1, array1, array3, array2, array4]);
    assert_json_eq!(first_value(&rev, fep, "result").await.unwrap(), res1);
}

#[tokio::test]
async fn reverse_test_full_array_reverse_with_len_minus_1() {
    let rev = Reverse::new();
    let fep = fep_with_args(args! { "source" => json!([1, 2, 3, 4, 5]), "srcFrom" => 0, "length" => -1 });
    assert_json_eq!(first_value(&rev, fep, "result").await.unwrap(), json!([5, 4, 3, 2, 1]));
}

#[tokio::test]
async fn reverse_test_full_array_reverse_with_explicit_length() {
    let rev = Reverse::new();
    let fep = fep_with_args(args! { "source" => json!(["a", "b", "c", "d"]), "srcFrom" => 0, "length" => 4 });
    assert_json_eq!(first_value(&rev, fep, "result").await.unwrap(), json!(["d", "c", "b", "a"]));
}

#[tokio::test]
async fn reverse_test_single_element_array() {
    let rev = Reverse::new();
    let fep = fep_with_args(args! { "source" => json!([42]), "srcFrom" => 0, "length" => -1 });
    assert_json_eq!(first_value(&rev, fep, "result").await.unwrap(), json!([42]));
}

#[tokio::test]
async fn reverse_test_full_reverse_from_non_zero_start() {
    let rev = Reverse::new();
    let fep = fep_with_args(args! { "source" => json!([1, 2, 3, 4, 5]), "srcFrom" => 0, "length" => 5 });
    assert_json_eq!(first_value(&rev, fep, "result").await.unwrap(), json!([5, 4, 3, 2, 1]));
}

#[tokio::test]
async fn rev_test_5() {
    let rev = Reverse::new();
    let arr = json!(["a", "b", "a", "c", "d", "a", "b", "c", "d"]);

    let fep = fep_with_args(args! { "source" => arr.clone(), "srcFrom" => 2 });

    let res = json!(["a", "b", "d", "c", "b", "a", "d", "c", "a"]);
    assert_json_eq!(first_value(&rev, fep, "result").await.unwrap(), res);

    // length = arr.length
    let fep = fep_with_args(args! { "source" => arr, "srcFrom" => 2, "length" => 9 });
    assert!(first_value(&rev, fep, "result").await.is_err());
}
