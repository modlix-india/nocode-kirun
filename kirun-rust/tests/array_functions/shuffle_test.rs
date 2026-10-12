//! Port of kirun-js `__tests__/engine/function/system/array/ShuffleTest.ts`.
//!
//! Both JS tests only check that the caller's source array still holds the same set of
//! elements after shuffling (Shuffle copies before shuffling). Rust passes arguments by
//! value, so the same check is kept on the input; the run itself must still succeed.

use kirun::function::system::array::shuffle::Shuffle;
use serde_json::{json, Value};

use crate::common::*;

/// JS `new Set(values)`: distinct values by deep equality.
fn to_set(values: &[Value]) -> Vec<Value> {
    let mut set: Vec<Value> = Vec::new();
    for v in values {
        if !set.iter().any(|s| json_eq(s, v)) {
            set.push(v.clone());
        }
    }
    set
}

fn assert_same_set(a: &[Value], b: &[Value]) {
    let (a, b) = (to_set(a), to_set(b));
    assert!(
        a.len() == b.len() && a.iter().all(|x| b.iter().any(|y| json_eq(x, y))),
        "sets differ\n  left: {a:?}\n right: {b:?}"
    );
}

#[tokio::test]
async fn shuffle_test_1() {
    let shuffle = Shuffle::new();

    let array = json!([
        "I", "am", "using", "eclipse", "to", "test", "the", "changes", "with", "test", "Driven", "developement"
    ]);
    let res = array.clone();
    let set1 = to_set(res.as_array().unwrap());

    let fep = fep_with_args(args! { "source" => array.clone() });

    // JS reads `.get(Shuffle.EVENT_RESULT_EMPTY.getName())` ('output') and ignores it.
    let _ = first_result(&shuffle, fep).await.unwrap().get("output").cloned();

    assert_same_set(&to_set(array.as_array().unwrap()), &set1);
}

#[tokio::test]
async fn shuffle_test_2() {
    let shuffle = Shuffle::new();

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

    let js1 = json!({ "boolean": false, "array": array4, "char": "asda" });

    let arr = json!([array1, array3, array2, array4, array1, js1]);
    // JS Sets hold object references, so set1 has 6 entries there (array1 twice is one entry,
    // array3 is a distinct object with equal contents). Deep-equality sets are compared here.
    let set1 = to_set(arr.as_array().unwrap());

    let fep = fep_with_args(args! { "source" => arr.clone() });

    first_result(&shuffle, fep).await.unwrap();

    assert_same_set(&to_set(arr.as_array().unwrap()), &set1);
}
