//! Port of kirun-js `__tests__/engine/util/deepEqualTest.ts`.
//! JS `undefined` is `Value::Null` in Rust (DESIGN.md), so `null` vs `undefined` cases compare
//! two nulls; a key holding `undefined` is still a present key, as in JS.

use kirun::util::deep_equal::deep_equal;
use kirun::Value;
use serde_json::json;

fn data() -> Value {
    json!([
        { "name": "Single Array", "data": [0, 10, 20, 30, 40, 50] },
        {
            "name": "Single Array With Labels",
            "data": [0, 10, 20, 30, 40, 50],
            "labels": ["First", "Second", "Third", "Fourth", "Fifth", "Sixth"]
        },
        {
            "name": "Multiple Arrays",
            "data": [
                [-10, 10, 20, 30, 40, 50],
                [-20, -10, 0, null, 20, 30],
                [-60, -30, 10, 100, 20, 30]
            ]
        },
        {
            "name": "Simple Objects With Labels",
            "data": [
                { "x": "First", "y": 0 },
                { "x": "Second", "y": 10 },
                { "x": "Third", "y": 20 },
                { "x": "Fourth", "y": 30 },
                { "x": "Fifth", "y": 40 },
                { "x": "Sixth", "y": 50 }
            ]
        },
        {
            "name": "Multiple Objects With Labels",
            "data": [
                { "x": "First", "y": 0, "z": -100 },
                { "x": "Second", "y": 10, "z": -20 },
                { "x": "Third", "y": 20, "z": 30 },
                { "x": "Fourth", "y": 30, "z": 40 },
                { "x": "Fifth", "y": 40, "z": 10 },
                { "x": "Sixth", "y": 50, "z": 80 }
            ]
        }
    ])
}

#[test]
fn deep_equal_test() {
    let data = data();
    assert!(deep_equal(&Value::Null, &Value::Null));
    assert!(!deep_equal(&Value::Null, &data));
    assert!(deep_equal(&data[0], &data[0]));
    assert!(!deep_equal(&data[0], &data[1]));
    assert!(!deep_equal(&data[0], &data[2]));
    assert!(!deep_equal(&data[0], &data[3]));
    assert!(!deep_equal(&data[0], &data[4]));
    assert!(!deep_equal(&json!(true), &json!(false)));
    assert!(deep_equal(&json!(2), &json!(2)));
    // JS deepEqual(null, undefined): both are Value::Null here.
    assert!(deep_equal(&Value::Null, &Value::Null));
    assert!(deep_equal(&json!([1, 2, 3, { "a": [3, 4] }]), &json!([1, 2, 3, { "a": [3, 4] }])));
    assert!(!deep_equal(&json!([1, 2, 3, { "a": [3, 4] }]), &json!([1, 2, 3, { "a": [3] }])));
    assert!(!deep_equal(&json!([1, 2, 3, { "a": [3, 4] }]), &json!([1, 2, 3, { "a": [3, "4"] }])));
    // JS { b: null } vs { b: undefined }
    assert!(deep_equal(
        &json!([1, 2, 3, { "a": [3, 4], "b": null }]),
        &json!([1, 2, 3, { "a": [3, 4], "b": null }])
    ));
}

#[test]
fn deep_equal_key_sets_of_the_same_size_but_different_names() {
    assert!(!deep_equal(&json!({ "x": 1 }), &json!({ "y": 1 })));
    assert!(!deep_equal(&json!({ "a": 1, "b": 2 }), &json!({ "a": 1, "c": 2 })));
    // a present-but-undefined key still counts towards the key set
    assert!(!deep_equal(&json!({ "a": 1 }), &json!({ "a": 1, "b": null })));
}

#[test]
fn deep_equal_does_not_conflate_falsy_values_of_different_types() {
    assert!(!deep_equal(&json!(0), &json!("")));
    assert!(!deep_equal(&json!(0), &json!(false)));
    assert!(!deep_equal(&json!({ "a": 0 }), &json!({ "a": false })));
    assert!(!deep_equal(&json!([1]), &Value::Null));
    assert!(!deep_equal(&Value::Null, &json!([1])));
}

// An array is never equal to a plain object, in either direction. This matches
// kirun-java (LogicalEqualOperator guards both directions explicitly) and
// kirun-py (deep_equal rejects on type(a) is not type(b)).
#[test]
fn deep_equal_never_equates_an_object_with_an_array() {
    assert!(!deep_equal(&json!([]), &json!({})));
    assert!(!deep_equal(&json!({}), &json!([])));
    assert!(!deep_equal(&json!([1]), &json!({ "0": 1 })));
    assert!(!deep_equal(&json!({ "0": 1 }), &json!([1])));
    assert!(!deep_equal(&json!([1]), &json!({ "a": 1 })));
    assert!(!deep_equal(&json!({ "a": 1 }), &json!([1])));
    // nested, not just at the top level
    assert!(!deep_equal(&json!({ "v": {} }), &json!({ "v": [] })));
    assert!(!deep_equal(&json!({ "v": [] }), &json!({ "v": {} })));
    assert!(!deep_equal(&json!([[]]), &json!([{}])));
    // and the cases that must keep working
    assert!(deep_equal(&json!([]), &json!([])));
    assert!(deep_equal(&json!({}), &json!({})));
    assert!(deep_equal(&json!({ "v": [1, 2] }), &json!({ "v": [1, 2] })));
}

#[test]
fn deep_equal_short_circuits_on_shared_references() {
    // Rust values are owned, so "shared" here is two equal copies of the same tree.
    let shared: Value = json!({ "big": (0..1000).map(|i| json!({ "i": i })).collect::<Vec<_>>() });
    assert!(deep_equal(&json!({ "v": shared.clone() }), &json!({ "v": shared.clone() })));
    assert!(deep_equal(&json!({ "v": shared.clone() }), &json!({ "v": shared.clone() })));
}

/// Builds nested values without recursion, and drops them iteratively, since serde_json's
/// recursive Drop would itself overflow at this depth.
fn chain(n: usize) -> Value {
    let mut cur = json!({ "value": 1 });
    for _ in 0..n {
        // Not json!: it would re-serialize `cur` recursively.
        let mut m = serde_json::Map::new();
        m.insert("next".to_string(), cur);
        cur = Value::Object(m);
    }
    cur
}

fn nested_array(n: usize) -> Value {
    let mut a = json!([1]);
    for _ in 0..n {
        a = Value::Array(vec![a]);
    }
    a
}

fn drop_deep(mut v: Value) {
    let mut stack = Vec::new();
    loop {
        match &mut v {
            Value::Array(a) => stack.extend(a.drain(..)),
            Value::Object(o) => {
                let keys: Vec<String> = o.keys().cloned().collect();
                for k in keys {
                    if let Some(x) = o.shift_remove(&k) {
                        stack.push(x);
                    }
                }
            }
            _ => {}
        }
        match stack.pop() {
            Some(next) => v = next,
            None => break,
        }
    }
}

/// Drops its value iteratively, also when a failing assertion unwinds.
struct Deep(Value);

impl Drop for Deep {
    fn drop(&mut self) {
        drop_deep(std::mem::take(&mut self.0));
    }
}

#[test]
fn deep_equal_handles_deeply_nested_values_without_overflowing_the_stack() {
    let (a, b) = (Deep(chain(50000)), Deep(chain(50000)));
    assert!(deep_equal(&a.0, &b.0));

    let (a, b) = (Deep(nested_array(50000)), Deep(nested_array(50000)));
    assert!(deep_equal(&a.0, &b.0));
}
