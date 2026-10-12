//! JS value semantics used by the expression engine (no JS counterpart file): truthiness,
//! `Number(x)`, `String(x)`, `parseInt`, 32-bit integer conversion.
//!
//! `Value::Null` stands for both JS `null` and `undefined`; where they differ it behaves as
//! `undefined` (e.g. it is not `typeof 'object'`).

use crate::util::primitive::number_value;
use crate::Value;

/// JS `typeof v === 'object'` for a non-null value (arrays and objects).
pub fn is_object(v: &Value) -> bool {
    matches!(v, Value::Array(_) | Value::Object(_))
}

/// JS truthiness (`!!v`).
pub fn truthy(v: &Value) -> bool {
    match v {
        Value::Null => false,
        Value::Bool(b) => *b,
        Value::Number(n) => n.as_f64().is_some_and(|f| f != 0.0 && !f.is_nan()),
        Value::String(s) => !s.is_empty(),
        Value::Array(_) | Value::Object(_) => true,
    }
}

/// Builds a JSON number from a JS number (integral values become integers).
pub fn num(f: f64) -> Value {
    number_value(f)
}

/// JS `Number(v)`.
pub fn to_number(v: &Value) -> f64 {
    match v {
        Value::Null => f64::NAN,
        Value::Bool(b) => {
            if *b {
                1.0
            } else {
                0.0
            }
        }
        Value::Number(n) => n.as_f64().unwrap_or(f64::NAN),
        Value::String(s) => parse_number(s).unwrap_or(f64::NAN),
        Value::Array(a) => match a.len() {
            0 => 0.0,
            1 => to_number(&Value::String(to_js_string(&a[0]))),
            _ => f64::NAN,
        },
        Value::Object(_) => f64::NAN,
    }
}

/// JS `Number(str)`; `None` when the result is `NaN`.
pub fn parse_number(s: &str) -> Option<f64> {
    let t = s.trim();
    if t.is_empty() {
        return Some(0.0);
    }
    let (neg, body) = match t.as_bytes()[0] {
        b'-' => (true, &t[1..]),
        b'+' => (false, &t[1..]),
        _ => (false, t),
    };
    let lower = body.to_ascii_lowercase();
    let radix = if lower.starts_with("0x") {
        Some(16)
    } else if lower.starts_with("0o") {
        Some(8)
    } else if lower.starts_with("0b") {
        Some(2)
    } else {
        None
    };
    if let Some(r) = radix {
        // JS does not accept a sign on hex/octal/binary literals.
        if t.as_bytes()[0] == b'-' || t.as_bytes()[0] == b'+' {
            return None;
        }
        let digits = &body[2..];
        if digits.is_empty() {
            return None;
        }
        let mut acc = 0f64;
        for c in digits.chars() {
            acc = acc * r as f64 + c.to_digit(r)? as f64;
        }
        return Some(acc);
    }
    let v = if body == "Infinity" {
        f64::INFINITY
    } else {
        // Only decimal literal characters; Rust's parser would accept "inf"/"nan".
        if body.is_empty()
            || !body.chars().all(|c| c.is_ascii_digit() || matches!(c, '.' | 'e' | 'E' | '+' | '-'))
            || !body.chars().any(|c| c.is_ascii_digit())
        {
            return None;
        }
        body.parse::<f64>().ok()?
    };
    Some(if neg { -v } else { v })
}

/// JS `parseInt(str)` (radix 10); `None` for `NaN`.
pub fn parse_int(s: &str) -> Option<f64> {
    let t = s.trim_start();
    let (neg, body) = match t.as_bytes().first() {
        Some(b'-') => (true, &t[1..]),
        Some(b'+') => (false, &t[1..]),
        _ => (false, t),
    };
    let digits: String = body.chars().take_while(|c| c.is_ascii_digit()).collect();
    if digits.is_empty() {
        return None;
    }
    let v: f64 = digits.parse().ok()?;
    Some(if neg { -v } else { v })
}

/// JS `Number.prototype.toString()`.
pub fn number_to_string(f: f64) -> String {
    if f.is_nan() {
        return "NaN".into();
    }
    if f.is_infinite() {
        return if f > 0.0 { "Infinity".into() } else { "-Infinity".into() };
    }
    if f == 0.0 {
        return "0".into();
    }
    let a = f.abs();
    if a >= 1e21 || a < 1e-6 {
        let s = format!("{f:e}");
        // Rust: 1e21 / 1.5e-7; JS: 1e+21 / 1.5e-7
        return match s.find('e') {
            Some(i) if !s[i + 1..].starts_with('-') => format!("{}e+{}", &s[..i], &s[i + 1..]),
            _ => s,
        };
    }
    if f.fract() == 0.0 {
        return format!("{f:.0}");
    }
    format!("{f}")
}

/// JS `String(v)`. `Value::Null` prints as `undefined`.
pub fn to_js_string(v: &Value) -> String {
    match v {
        Value::Null => "undefined".into(),
        Value::Bool(b) => b.to_string(),
        Value::Number(n) => {
            if let Some(i) = n.as_i64() {
                i.to_string()
            } else if let Some(u) = n.as_u64() {
                u.to_string()
            } else {
                number_to_string(n.as_f64().unwrap_or(f64::NAN))
            }
        }
        Value::String(s) => s.clone(),
        Value::Array(a) => a
            .iter()
            .map(|e| if e.is_null() { String::new() } else { to_js_string(e) })
            .collect::<Vec<_>>()
            .join(","),
        Value::Object(_) => "[object Object]".into(),
    }
}

/// JS `ToInt32`.
pub fn to_int32(f: f64) -> i32 {
    if !f.is_finite() {
        return 0;
    }
    let t = f.trunc();
    let m = t.rem_euclid(4294967296.0);
    m as u32 as i32
}

/// JS `ToUint32`.
pub fn to_uint32(f: f64) -> u32 {
    to_int32(f) as u32
}

/// JS `s.length` (UTF-16 code units).
pub fn str_len(s: &str) -> usize {
    s.encode_utf16().count()
}

/// JS `s.substring(start, end)` on UTF-16 code units, `start <= end` assumed clamped.
pub fn substring(s: &str, start: usize, end: usize) -> String {
    let u: Vec<u16> = s.encode_utf16().collect();
    let end = end.min(u.len());
    let start = start.min(end);
    String::from_utf16_lossy(&u[start..end])
}

/// JS `s[i]`.
pub fn char_at(s: &str, i: usize) -> Option<String> {
    let u: Vec<u16> = s.encode_utf16().collect();
    u.get(i).map(|c| String::from_utf16_lossy(&[*c]))
}

/// JS `PrimitiveUtil.findPrimitiveNumberType`: errors for null/array/object, otherwise reports
/// whether the value is an integer (`SchemaType.INTEGER`/`LONG`).
pub fn find_primitive_number_type(v: &Value) -> crate::Result<bool> {
    match v {
        Value::Null | Value::Array(_) | Value::Object(_) => Err(crate::KIRunError::execution(
            crate::util::string::string_formatter::StringFormatter::format(
                "Unable to convert $ to a number.",
                &[&to_js_string(v)],
            ),
        )),
        Value::Number(n) => Ok(n.as_f64().is_some_and(|f| f.is_finite() && f.fract() == 0.0)),
        _ => Ok(false),
    }
}

/// The body shared by the JS `<`, `<=`, `>`, `>=` operators
/// (`PrimitiveUtil.findPrimitiveNullAsBoolean` on both sides, then a JS relational compare).
pub fn compare(
    t: &Value,
    u: &Value,
    sym: &str,
    nf: impl Fn(f64, f64) -> bool,
    sf: impl Fn(&str, &str) -> bool,
) -> crate::Result<Value> {
    use crate::util::string::string_formatter::StringFormatter;
    if t.is_null() || u.is_null() {
        return Ok(Value::Null);
    }
    for v in [t, u] {
        if is_object(v) {
            return Err(crate::KIRunError::execution(StringFormatter::format(
                "$ is not a primitive type",
                &[&to_js_string(v)],
            )));
        }
    }
    if t.is_boolean() || u.is_boolean() {
        return Err(crate::KIRunError::execution(StringFormatter::format(
            &format!("Cannot compare {sym} with the values $ and $"),
            &[&to_js_string(t), &to_js_string(u)],
        )));
    }
    if let (Value::String(a), Value::String(b)) = (t, u) {
        let a: Vec<u16> = a.encode_utf16().collect();
        let b: Vec<u16> = b.encode_utf16().collect();
        let a = String::from_utf16_lossy(&a);
        let b = String::from_utf16_lossy(&b);
        return Ok(Value::Bool(sf(&a, &b)));
    }
    Ok(Value::Bool(nf(to_number(t), to_number(u))))
}
