//! Port of kirun-js `util/primitive/PrimitiveUtil.ts`, plus JS-number helpers.

pub mod primitive_util;

use crate::Value;

/// Builds a JSON number the way JS would print it: integral values that fit `i64`
/// become integers (`3`, not `3.0`). Non-finite values become `null` (as `JSON.stringify`).
pub fn number_value(n: f64) -> Value {
    if !n.is_finite() {
        return Value::Null;
    }
    if n.fract() == 0.0 && n.abs() < 9.007_199_254_740_992e15 {
        return Value::from(n as i64);
    }
    serde_json::Number::from_f64(n).map(Value::Number).unwrap_or(Value::Null)
}

/// JS `String(n)` for a number: integers without a fraction, exponent form at or beyond
/// `1e21` and below `1e-6`, as JS prints them.
pub fn js_number_to_string(n: f64) -> String {
    if n.is_nan() {
        return "NaN".into();
    }
    if n.is_infinite() {
        return if n > 0.0 { "Infinity".into() } else { "-Infinity".into() };
    }
    if n == 0.0 {
        return "0".into();
    }
    let abs = n.abs();
    if !(1e-6..1e21).contains(&abs) {
        // Rust: "1e21" / "1.5e-7"; JS: "1e+21" / "1.5e-7".
        let s = format!("{n:e}");
        return match s.split_once('e') {
            Some((m, e)) if !e.starts_with('-') => format!("{m}e+{e}"),
            _ => s,
        };
    }
    format!("{n}")
}

/// JS `String(v)` for a JSON number value.
pub fn js_number_value_to_string(n: &serde_json::Number) -> String {
    if let Some(i) = n.as_i64() {
        return i.to_string();
    }
    if let Some(u) = n.as_u64() {
        return u.to_string();
    }
    js_number_to_string(n.as_f64().unwrap_or(f64::NAN))
}

/// JS `String(value)` / `'' + value`. `Value::Null` prints as `null`; arrays join with `,`
/// (null items empty); objects print as `[object Object]`.
pub fn js_to_string(v: &Value) -> String {
    match v {
        Value::Null => "null".into(),
        Value::Bool(b) => b.to_string(),
        Value::Number(n) => js_number_value_to_string(n),
        Value::String(s) => s.clone(),
        Value::Array(a) => a
            .iter()
            .map(|e| if e.is_null() { String::new() } else { js_to_string(e) })
            .collect::<Vec<_>>()
            .join(","),
        Value::Object(_) => "[object Object]".into(),
    }
}

/// JS `JSON.stringify(value)` (numbers printed the JS way: `1`, not `1.0`).
pub fn js_stringify(v: &Value) -> String {
    let mut out = String::new();
    write_js_json(v, &mut out);
    out
}

fn write_js_json(v: &Value, out: &mut String) {
    match v {
        Value::Null => out.push_str("null"),
        Value::Bool(b) => out.push_str(if *b { "true" } else { "false" }),
        Value::Number(n) => {
            let f = n.as_f64().unwrap_or(0.0);
            if n.is_f64() && !f.is_finite() {
                out.push_str("null")
            } else {
                out.push_str(&js_number_value_to_string(n))
            }
        }
        Value::String(s) => out.push_str(&serde_json::to_string(s).unwrap_or_default()),
        Value::Array(a) => {
            out.push('[');
            for (i, e) in a.iter().enumerate() {
                if i > 0 {
                    out.push(',');
                }
                write_js_json(e, out);
            }
            out.push(']');
        }
        Value::Object(m) => {
            out.push('{');
            for (i, (k, e)) in m.iter().enumerate() {
                if i > 0 {
                    out.push(',');
                }
                out.push_str(&serde_json::to_string(k).unwrap_or_default());
                out.push(':');
                write_js_json(e, out);
            }
            out.push('}');
        }
    }
}

/// JS truthiness: `null`, `false`, `0`, `NaN` and `""` are falsy; arrays and objects are truthy.
pub fn js_truthy(v: &Value) -> bool {
    match v {
        Value::Null => false,
        Value::Bool(b) => *b,
        Value::Number(n) => n.as_f64().is_some_and(|f| f != 0.0 && !f.is_nan()),
        Value::String(s) => !s.is_empty(),
        Value::Array(_) | Value::Object(_) => true,
    }
}

/// JS `Number(str)`: trims whitespace, the empty string is `0`, accepts `Infinity`, hex/octal/binary
/// prefixes and decimal/exponent forms; anything else is `NaN`.
pub fn js_string_to_number(s: &str) -> f64 {
    let t = s.trim();
    if t.is_empty() {
        return 0.0;
    }
    let (sign, body) = match t.as_bytes()[0] {
        b'+' => (1.0, &t[1..]),
        b'-' => (-1.0, &t[1..]),
        _ => (1.0, t),
    };
    if body == "Infinity" {
        return sign * f64::INFINITY;
    }
    let radix = |p: &str, r: u32| -> Option<f64> {
        if p.is_empty() {
            return None;
        }
        u64::from_str_radix(p, r).ok().map(|v| v as f64)
    };
    let lower = t.to_ascii_lowercase();
    // Prefixed forms do not take a sign in JS.
    for (prefix, r) in [("0x", 16), ("0o", 8), ("0b", 2)] {
        if let Some(rest) = lower.strip_prefix(prefix) {
            return radix(rest, r).unwrap_or(f64::NAN);
        }
    }
    let valid = {
        let b = body.as_bytes();
        let mut i = 0;
        let mut digits = 0;
        while i < b.len() && b[i].is_ascii_digit() {
            i += 1;
            digits += 1;
        }
        if i < b.len() && b[i] == b'.' {
            i += 1;
            while i < b.len() && b[i].is_ascii_digit() {
                i += 1;
                digits += 1;
            }
        }
        let mut ok = digits > 0;
        if ok && i < b.len() && (b[i] == b'e' || b[i] == b'E') {
            i += 1;
            if i < b.len() && (b[i] == b'+' || b[i] == b'-') {
                i += 1;
            }
            let start = i;
            while i < b.len() && b[i].is_ascii_digit() {
                i += 1;
            }
            ok = i > start;
        }
        ok && i == b.len()
    };
    if !valid {
        return f64::NAN;
    }
    body.parse::<f64>().map(|v| sign * v).unwrap_or(f64::NAN)
}
