//! Port of kirun-js `util/deepEqual.ts`: iterative structural equality (no recursion, so deep
//! values cannot overflow the stack). An array never equals an object.
//!
//! `Value::Null` stands for both JS `null` and `undefined`; it follows the JS `undefined`
//! rule (equal to any falsy value), so `deep_equal(null, false)` is `true` here.

use crate::Value;

fn falsy(v: &Value) -> bool {
    match v {
        Value::Null => true,
        Value::Bool(b) => !*b,
        Value::Number(n) => n.as_f64().map_or(false, |f| f == 0.0 || f.is_nan()),
        Value::String(s) => s.is_empty(),
        _ => false,
    }
}

pub fn deep_equal(x: &Value, y: &Value) -> bool {
    let mut xa: Vec<&Value> = vec![x];
    let mut yb: Vec<&Value> = vec![y];
    // Sentinel for a key missing on the right-hand object (JS `b[k]` is undefined).
    static NULL: Value = Value::Null;

    while let (Some(a), Some(b)) = (xa.pop(), yb.pop()) {
        if a.is_null() || b.is_null() {
            if falsy(a) && falsy(b) {
                continue;
            }
            return false;
        }
        match (a, b) {
            (Value::Bool(p), Value::Bool(q)) => {
                if p != q {
                    return false;
                }
            }
            (Value::Number(p), Value::Number(q)) => {
                if p.as_f64() != q.as_f64() {
                    return false;
                }
            }
            (Value::String(p), Value::String(q)) => {
                if p != q {
                    return false;
                }
            }
            (Value::Array(p), Value::Array(q)) => {
                if p.len() != q.len() {
                    return false;
                }
                for (i, j) in p.iter().zip(q.iter()) {
                    xa.push(i);
                    yb.push(j);
                }
            }
            (Value::Object(p), Value::Object(q)) => {
                if p.len() != q.len() {
                    return false;
                }
                for (k, v) in p.iter() {
                    xa.push(v);
                    yb.push(q.get(k).unwrap_or(&NULL));
                }
            }
            _ => return false,
        }
    }
    true
}
