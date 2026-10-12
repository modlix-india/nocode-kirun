//! Port of kirun-js `__tests__/engine/function/system/array/CompareTest.ts`.

use kirun::function::system::array::compare::Compare;
use serde_json::{json, Value};

use crate::common::*;

fn arr(v: Value) -> Vec<Value> {
    v.as_array().unwrap().clone()
}

#[tokio::test]
async fn compare_test_1() {
    let compare = Compare::new();

    let source = arr(json!([2, 2, 3, 4, 5]));
    let find = arr(json!([2, 2, 2, 3, 4, 5]));

    assert_eq!(compare.compare(&source, 0, 2, &find, 1, 3).unwrap(), 0.0);

    let find = arr(json!([2, 2, 3, 4, 5]));
    assert_eq!(
        compare
            .compare(&source, 0, source.len() as i64, &find, 0, find.len() as i64)
            .unwrap(),
        0.0
    );

    let source = arr(json!([true, true]));
    let find = arr(json!([true, null]));
    assert_eq!(
        compare
            .compare(&source, 0, source.len() as i64, &find, 0, find.len() as i64)
            .unwrap(),
        1.0
    );
}

#[tokio::test]
async fn compare_test_2() {
    let compare = Compare::new();

    let source = json!([4, 5]);
    let find = json!([4, 6]);

    let fep = crate::common::fep().set_arguments(args! { "source" => source, "find" => find });

    assert_json_eq!(first_value(&compare, fep, "result").await.unwrap(), json!(5 - 6));
}
