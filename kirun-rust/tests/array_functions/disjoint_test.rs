//! Port of kirun-js `__tests__/engine/function/system/array/DisjointTest.ts`.

use kirun::function::system::array::disjoint::Disjoint;
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

/// JS `expect(setA).toStrictEqual(setB)`: same size and every element of one is in the other.
fn assert_same_set(a: &[Value], b: &[Value]) {
    let (a, b) = (to_set(a), to_set(b));
    assert!(
        a.len() == b.len() && a.iter().all(|x| b.iter().any(|y| json_eq(x, y))),
        "sets differ\n  left: {a:?}\n right: {b:?}"
    );
}

#[tokio::test]
async fn disjoint_test_1() {
    let dis = Disjoint::new();

    let arr = json!(["a", "b", "c", "d", "e", "f"]);
    let arr2 = json!(["a", "b", "p", "a", "f", "f", "e"]);

    let fep = crate::common::fep().set_arguments(args! {
        "source" => arr,
        "srcFrom" => 2,
        "secondSource" => arr2,
        "secondSrcFrom" => 0,
        "length" => 4,
    });

    let set1 = vec![json!("c"), json!("d"), json!("e"), json!("f"), json!("a"), json!("b"), json!("p")];

    let res = first_value(&dis, fep, "result").await.unwrap();
    assert_same_set(&set1, res.as_array().expect("result must be an array"));
}

#[tokio::test]
async fn disjoint_test_2() {
    let dis = Disjoint::new();

    let arr = json!(["a", "b", "c", "d", "e", "f"]);
    let arr2 = json!(["a", "b", "a", "b", "c", "d", "e", "f"]);

    let fep = crate::common::fep().set_arguments(args! {
        "source" => arr,
        "srcFrom" => -12,
        "secondSource" => arr2,
        "secondSrcFrom" => 2,
        "length" => 3,
    });

    // JS: `expect(dis.execute(fep)).rejects.toThrow()` (not awaited in JS, so never enforced there).
    assert!(first_value(&dis, fep, "result").await.is_err());
}

#[tokio::test]
async fn disjoint_test_3() {
    let dis = Disjoint::new();

    let array1 = json!(["test", "Driven", "developement", "I", "am"]);

    let js1 = json!({ "boolean": false, "array": array1, "char": "o" });
    let js2 = json!({ "boolean": false, "array": array1, "char": "asd" });
    let js3 = json!({ "array": array1 });
    let js4 = json!({ "boolean": false, "array": array1, "char": "s" });
    let js5 = json!({ "boolean": false, "array": array1, "char": "b" });
    let js6 = json!({ "booleasan": false, "arraay": array1, "char": "o" });
    let js7 = json!({ "booleasan": false, "arrrraay": array1, "char": "o" });

    let arr = json!([js1, js2, js3, js4, js1]);
    let arr2 = json!([js5, js6, js7, js1]);

    let d = vec![js2, js3, js4, js5, js6, js7];

    let fep = crate::common::fep().set_arguments(args! {
        "source" => arr,
        "srcFrom" => 1,
        "secondSource" => arr2,
        "secondSrcFrom" => 0,
        "length" => 4,
    });

    let res = first_value(&dis, fep, "result").await.unwrap();

    // JS `expect(set1).toMatchObject(set2)` on Sets: deep set equality.
    assert_same_set(&d, res.as_array().expect("result must be an array"));
}
