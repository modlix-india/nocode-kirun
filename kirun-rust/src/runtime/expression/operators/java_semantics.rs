//! kirun-java's expression operators (`runtime/expression/operators/*` and `PrimitiveUtil`),
//! which the operator types delegate to. Stored server functions run on kirun-java, whose
//! operators differ from kirun-js's in their null, string and boolean handling:
//!
//! - arithmetic refuses booleans and non-numeric strings ("Unable to convert \"abc\" to a
//!   number.", "Cannot add the values 1 and true"), and reads numeric strings as numbers
//!   (`'5' - 1` is 4); `+` concatenates when either side is a string, a null right side printing
//!   `null` (`'a' + null` is `anull`);
//! - `=` / `!=` read null as `false` (`null = false` is true, `null = 0` false) and compare a
//!   string with anything by text (`5 = '5'` is true);
//! - `and` / `or` / `not` treat every string, even `''`, as true;
//! - `<`, `<=`, `>`, `>=` answer null when either side is null, and compare by text when either
//!   side is a string;
//! - whole numbers never wrap (kirun-js numbers, not Java's `int` / `long`): `+`, `-`, `*` and
//!   unary `-` widen an `int` result to a `long`, and a `long` that overflows to a `double`
//!   (`40000 * 12 * 10000` is 4800000000); a whole division with a remainder is a `double` one
//!   (`7 / 2` is 3.5, `1 / 3` 0.3333333333333333), an exact one stays whole (`6 / 3` is 2);
//!   `/` and `%` by a whole zero fail ("/ by zero"), and `%` keeps the dividend's sign.
//!
//! Numbers are read as kirun-java's `PrimitiveUtil.findPrimitiveNumberType` would read the same
//! JSON: a whole number is an `int` when it fits, else a `long`; a decimal is a `double`. (Java
//! reads a decimal it parsed from request text as a `float`, and one from a storage row or an
//! earlier step as a `double`; a JSON value does not say which, so kirun-rust keeps the double.)

use crate::{KIRunError, Result, Value};

/// A Java number: `Integer`, `Long`, `Float` or `Double`.
#[derive(Clone, Copy, Debug, PartialEq)]
pub enum JNum {
    Int(i32),
    Long(i64),
    Float(f32),
    Double(f64),
}

impl JNum {
    fn rank(self) -> u8 {
        match self {
            JNum::Int(_) => 0,
            JNum::Long(_) => 1,
            JNum::Float(_) => 2,
            JNum::Double(_) => 3,
        }
    }

    fn as_f64(self) -> f64 {
        match self {
            JNum::Int(i) => i as f64,
            JNum::Long(l) => l as f64,
            JNum::Float(f) => f as f64,
            JNum::Double(d) => d,
        }
    }

    fn as_f32(self) -> f32 {
        match self {
            JNum::Int(i) => i as f32,
            JNum::Long(l) => l as f32,
            JNum::Float(f) => f,
            JNum::Double(d) => d as f32,
        }
    }

    /// Java `Number.longValue()` (a double saturates, NaN is 0).
    fn as_i64(self) -> i64 {
        match self {
            JNum::Int(i) => i as i64,
            JNum::Long(l) => l,
            JNum::Float(f) => f as i64,
            JNum::Double(d) => d as i64,
        }
    }

    /// Java `Number.intValue()`.
    fn as_i32(self) -> i32 {
        match self {
            JNum::Int(i) => i,
            JNum::Long(l) => l as i32,
            JNum::Float(f) => f as i32,
            JNum::Double(d) => d as i32,
        }
    }

    /// Java `Number.toString()`.
    pub fn java_string(self) -> String {
        match self {
            JNum::Int(i) => i.to_string(),
            JNum::Long(l) => l.to_string(),
            JNum::Float(f) => java_float_string(f),
            JNum::Double(d) => java_double_string(d),
        }
    }

    /// The JSON value Gson writes for the number (a `float` by its shortest decimal form).
    pub fn to_value(self) -> Value {
        match self {
            JNum::Int(i) => Value::from(i),
            JNum::Long(l) => Value::from(l),
            JNum::Float(f) => {
                if !f.is_finite() {
                    return Value::Null;
                }
                format!("{f}").parse::<f64>().map(Value::from).unwrap_or(Value::Null)
            }
            JNum::Double(d) => serde_json::Number::from_f64(d).map(Value::Number).unwrap_or(Value::Null),
        }
    }
}

/// The wider of two operands' kinds (Java's `DOUBLE`, then `FLOAT`, then `LONG`, then `INTEGER`).
fn wider(a: JNum, b: JNum) -> u8 {
    a.rank().max(b.rank())
}

/// Java `Double.toString`: shortest digits; plain between 10^-3 and 10^7, else `d.dddE±n`.
pub fn java_double_string(d: f64) -> String {
    if d.is_nan() {
        return "NaN".into();
    }
    if d.is_infinite() {
        return if d > 0.0 { "Infinity".into() } else { "-Infinity".into() };
    }
    java_decimal_string(&format!("{d:e}"), d.abs())
}

/// Java `Float.toString`.
pub fn java_float_string(f: f32) -> String {
    if f.is_nan() {
        return "NaN".into();
    }
    if f.is_infinite() {
        return if f > 0.0 { "Infinity".into() } else { "-Infinity".into() };
    }
    java_decimal_string(&format!("{f:e}"), f.abs() as f64)
}

/// Lays out Rust's shortest `d.ddde±n` the way Java's `toString` does.
fn java_decimal_string(sci: &str, abs: f64) -> String {
    let (mantissa, exp) = sci.split_once('e').unwrap_or((sci, "0"));
    let exp: i32 = exp.parse().unwrap_or(0);
    let negative = mantissa.starts_with('-');
    let digits: String = mantissa.chars().filter(|c| c.is_ascii_digit()).collect();
    let sign = if negative { "-" } else { "" };
    if abs == 0.0 {
        return format!("{sign}0.0");
    }
    if (1e-3..1e7).contains(&abs) {
        let point = exp + 1;
        let (int_part, frac_part) = if point <= 0 {
            ("0".to_string(), format!("{}{}", "0".repeat((-point) as usize), digits))
        } else if (point as usize) >= digits.len() {
            (format!("{}{}", digits, "0".repeat(point as usize - digits.len())), String::new())
        } else {
            (digits[..point as usize].to_string(), digits[point as usize..].to_string())
        };
        let frac = if frac_part.is_empty() { "0".to_string() } else { frac_part };
        format!("{sign}{int_part}.{frac}")
    } else {
        let frac = if digits.len() > 1 { &digits[1..] } else { "0" };
        format!("{sign}{}.{}E{exp}", &digits[..1], frac)
    }
}

/// A value as `PrimitiveUtil.findPrimitive` classifies it.
#[derive(Clone, Debug)]
pub enum Prim {
    Null,
    Bool(bool),
    Str(String),
    Num(JNum),
}

impl Prim {
    /// The `Object.toString()` kirun-java prints and concatenates.
    pub fn java_string(&self) -> String {
        match self {
            Prim::Null => "null".into(),
            Prim::Bool(b) => b.to_string(),
            Prim::Str(s) => s.clone(),
            Prim::Num(n) => n.java_string(),
        }
    }
}

fn execution(message: String) -> KIRunError {
    KIRunError::execution(message)
}

/// Gson's `JsonElement.toString()`.
fn json(v: &Value) -> String {
    serde_json::to_string(v).unwrap_or_default()
}

/// A JSON number as Java holds it.
pub fn number_kind(n: &serde_json::Number) -> JNum {
    if let Some(i) = n.as_i64() {
        return match i32::try_from(i) {
            Ok(i) => JNum::Int(i),
            Err(_) => JNum::Long(i),
        };
    }
    if let Some(u) = n.as_u64() {
        return JNum::Long(u as i64);
    }
    JNum::Double(n.as_f64().unwrap_or(f64::NAN))
}

/// `PrimitiveUtil.findPrimitive`.
pub fn find_primitive(v: &Value) -> Result<Prim> {
    Ok(match v {
        Value::Null => Prim::Null,
        Value::Bool(b) => Prim::Bool(*b),
        Value::String(s) => Prim::Str(s.clone()),
        Value::Number(n) => Prim::Num(number_kind(n)),
        _ => return Err(execution(format!("{} is not a primitive type", json(v)))),
    })
}

/// `PrimitiveUtil.findPrimitiveNullAsBoolean`: null reads as `false`.
pub fn find_primitive_null_as_boolean(v: &Value) -> Result<Prim> {
    match v {
        Value::Null => Ok(Prim::Bool(false)),
        other => find_primitive(other),
    }
}

/// `PrimitiveUtil.findPrimitiveNumberType`: a number, or a string Gson reads as one
/// (`LazilyParsedNumber`: `Long.parseLong`, then `BigDecimal`, without a dot; `parseDouble`, kept
/// as a `float` when it fits, with one).
pub fn find_number(v: &Value) -> Result<JNum> {
    let fail = || execution(format!("Unable to convert {} to a number.", json(v)));
    match v {
        Value::Number(n) => Ok(number_kind(n)),
        Value::String(s) => {
            if !s.contains('.') {
                let n = s.parse::<i64>().ok().or_else(|| big_decimal_long(s)).ok_or_else(fail)?;
                return Ok(match i32::try_from(n) {
                    Ok(i) => JNum::Int(i),
                    Err(_) => JNum::Long(n),
                });
            }
            let d = java_parse_double(s).ok_or_else(fail)?;
            let f = d as f32;
            Ok(if d == 0.0 || (f != 0.0 && f.is_finite()) { JNum::Float(f) } else { JNum::Double(d) })
        }
        _ => Err(fail()),
    }
}

/// `new BigDecimal(s).longValue()` for the forms `Long.parseLong` refuses (`1e5`, `+5`).
fn big_decimal_long(s: &str) -> Option<i64> {
    let body = s.strip_prefix(['+', '-']).unwrap_or(s);
    if body.is_empty() || !body.chars().next().is_some_and(|c| c.is_ascii_digit()) {
        return None;
    }
    if !body.chars().all(|c| c.is_ascii_digit() || c == 'e' || c == 'E' || c == '+' || c == '-') {
        return None;
    }
    s.parse::<f64>().ok().filter(|d| d.is_finite()).map(|d| d as i64)
}

/// Java `Double.parseDouble`: surrounding whitespace allowed, an `f`/`d` suffix allowed.
fn java_parse_double(s: &str) -> Option<f64> {
    let t = s.trim_matches(|c: char| c <= ' ');
    let t = t.strip_suffix(['d', 'D', 'f', 'F']).unwrap_or(t);
    if t.is_empty() || !t.chars().all(|c| c.is_ascii_digit() || ".eE+-".contains(c)) {
        return None;
    }
    t.parse::<f64>().ok()
}

/// `BinaryOperator.nullCheck`.
pub fn null_check(t: &Value, u: &Value, op: &str) -> Result<()> {
    if t.is_null() || u.is_null() {
        return Err(execution(format!("{op} cannot be applied to a null value")));
    }
    Ok(())
}

fn int_div_zero() -> KIRunError {
    execution("/ by zero".into())
}

/// `ArithmeticAdditionOperator`.
pub fn add(t: &Value, u: &Value) -> Result<Value> {
    let tp = find_primitive(t)?;
    let up = find_primitive(u)?;
    if matches!(tp, Prim::Null) {
        return Ok(u.clone());
    }
    if matches!(tp, Prim::Str(_)) || matches!(up, Prim::Str(_)) {
        return Ok(Value::String(tp.java_string() + &up.java_string()));
    }
    if matches!(tp, Prim::Bool(_)) || matches!(up, Prim::Bool(_)) {
        return Err(execution(format!("Cannot add the values {} and {}", tp.java_string(), up.java_string())));
    }
    let (Prim::Num(a), Prim::Num(b)) = (&tp, &up) else {
        // A number plus null: kirun-java casts the JsonNull to a Number.
        return Err(execution(
            "class com.google.gson.JsonNull cannot be cast to class java.lang.Number (com.google.gson.JsonNull is in unnamed module of loader 'app'; java.lang.Number is in module java.base of loader 'bootstrap')".into(),
        ));
    };
    Ok(arith(*a, *b, i64::checked_add, |x, y| x + y, |x, y| x + y))
}

/// A whole number kept as small as it fits: an `int` when it does, else a `long`.
pub fn whole(n: i64) -> JNum {
    match i32::try_from(n) {
        Ok(i) => JNum::Int(i),
        Err(_) => JNum::Long(n),
    }
}

/// `+`, `-` and `*` on two numbers. Whole operands are worked as `long`s and a result past the
/// `long` range becomes a `double`, so nothing wraps (kirun-js numbers never do).
pub fn arith_num(
    a: JNum,
    b: JNum,
    long: fn(i64, i64) -> Option<i64>,
    float: fn(f32, f32) -> f32,
    double: fn(f64, f64) -> f64,
) -> JNum {
    match wider(a, b) {
        3 => JNum::Double(double(a.as_f64(), b.as_f64())),
        2 => JNum::Float(float(a.as_f32(), b.as_f32())),
        _ => match long(a.as_i64(), b.as_i64()) {
            Some(r) => whole(r),
            None => JNum::Double(double(a.as_f64(), b.as_f64())),
        },
    }
}

fn arith(
    a: JNum,
    b: JNum,
    long: fn(i64, i64) -> Option<i64>,
    float: fn(f32, f32) -> f32,
    double: fn(f64, f64) -> f64,
) -> Value {
    arith_num(a, b, long, float, double).to_value()
}

/// `ArithmeticSubtractionOperator`.
pub fn subtract(t: &Value, u: &Value) -> Result<Value> {
    null_check(t, u, "-")?;
    let (a, b) = (find_number(t)?, find_number(u)?);
    Ok(arith(a, b, i64::checked_sub, |x, y| x - y, |x, y| x - y))
}

/// `ArithmeticMultiplicationOperator`: a string times a number repeats it.
pub fn multiply(t: &Value, u: &Value) -> Result<Value> {
    null_check(t, u, "*")?;
    let tp = find_primitive(t)?;
    let up = find_primitive(u)?;
    if matches!(tp, Prim::Bool(_)) || matches!(up, Prim::Bool(_)) {
        return Err(execution(format!("Cannot multiply the values {} and {}", tp.java_string(), up.java_string())));
    }
    match (&tp, &up) {
        (Prim::Str(a), Prim::Str(b)) => Err(execution(format!("Cannot multiply strings {a} and {b}"))),
        (Prim::Str(s), Prim::Num(n)) | (Prim::Num(n), Prim::Str(s)) => Ok(Value::String(repeat(s, *n))),
        (Prim::Num(a), Prim::Num(b)) => {
            Ok(arith(*a, *b, i64::checked_mul, |x, y| x * y, |x, y| x * y))
        }
        _ => Err(execution(format!("Cannot multiply the values {} and {}", tp.java_string(), up.java_string()))),
    }
}

/// kirun-java's string repetition: whole copies, then the fraction's share of the characters,
/// reversed for a negative count.
fn repeat(s: &str, n: JNum) -> String {
    let d = n.as_f64();
    let mut times = n.as_i64();
    let reverse = d < 0.0;
    if reverse {
        times = -times;
    }
    let units: Vec<u16> = s.encode_utf16().collect();
    let mut out: Vec<u16> = Vec::new();
    for _ in 0..times.max(0) {
        out.extend_from_slice(&units);
    }
    let chars = (units.len() as f64 * (d - n.as_i64() as f64)).floor().abs() as usize;
    if chars != 0 {
        out.extend_from_slice(&units[..chars.min(units.len())]);
    }
    if reverse {
        let text = String::from_utf16_lossy(&out);
        return text.chars().rev().collect();
    }
    String::from_utf16_lossy(&out)
}

/// `ArithmeticDivisionOperator`, answering as kirun-js: a whole division with a remainder is a
/// `double` one (`7 / 2` is 3.5, `480000 / 6000000` 0.08), an exact one stays whole (`6 / 3`
/// is 2). A whole zero divisor fails as kirun-java's does ("/ by zero").
pub fn divide(t: &Value, u: &Value) -> Result<Value> {
    null_check(t, u, "/")?;
    let (a, b) = (find_number(t)?, find_number(u)?);
    Ok(match wider(a, b) {
        3 => JNum::Double(a.as_f64() / b.as_f64()),
        2 => JNum::Float(a.as_f32() / b.as_f32()),
        _ => {
            let (x, y) = (a.as_i64(), b.as_i64());
            if y == 0 {
                return Err(int_div_zero());
            }
            match (x.checked_rem(y), x.checked_div(y)) {
                (Some(0), Some(q)) => whole(q),
                // A remainder, or `long` MIN / -1 (past the `long` range).
                _ => JNum::Double(x as f64 / y as f64),
            }
        }
    }
    .to_value())
}

/// `ArithmeticModulusOperator`: the remainder takes the dividend's sign (`-7 % 3` is -1), as
/// in kirun-js and Java.
pub fn modulus(t: &Value, u: &Value) -> Result<Value> {
    null_check(t, u, "%")?;
    let (a, b) = (find_number(t)?, find_number(u)?);
    Ok(match wider(a, b) {
        3 => JNum::Double(a.as_f64() % b.as_f64()),
        2 => JNum::Float(a.as_f32() % b.as_f32()),
        _ => {
            let y = b.as_i64();
            if y == 0 {
                return Err(int_div_zero());
            }
            // `long` MIN % -1 is 0 (checked_rem refuses it only for the overflowing quotient).
            whole(a.as_i64().checked_rem(y).unwrap_or(0))
        }
    }
    .to_value())
}

/// The bitwise operators: both sides must be `int` or `long`.
pub fn bitwise(t: &Value, u: &Value, op: &str) -> Result<Value> {
    null_check(t, u, op)?;
    let (a, b) = (find_number(t)?, find_number(u)?);
    let whole = |n: JNum| matches!(n, JNum::Int(_) | JNum::Long(_));
    if !whole(a) || !whole(b) {
        return Err(execution(format!(
            "{} and {} has to be either integer or long type",
            a.java_string(),
            b.java_string()
        )));
    }
    let long = wider(a, b) == 1;
    Ok(if long {
        let (x, y) = (a.as_i64(), b.as_i64());
        JNum::Long(match op {
            "&" => x & y,
            "|" => x | y,
            "^" => x ^ y,
            "<<" => x.wrapping_shl((y & 63) as u32),
            ">>" => x.wrapping_shr((y & 63) as u32),
            _ => ((x as u64) >> ((y & 63) as u32)) as i64,
        })
    } else {
        let (x, y) = (a.as_i32(), b.as_i32());
        JNum::Int(match op {
            "&" => x & y,
            "|" => x | y,
            "^" => x ^ y,
            "<<" => x.wrapping_shl((y & 31) as u32),
            ">>" => x.wrapping_shr((y & 31) as u32),
            _ => ((x as u32) >> ((y & 31) as u32)) as i32,
        })
    }
    .to_value())
}

/// kirun-java's truthiness for `and` / `or`: null and `false` and `0` are false; every string
/// (even `''`), array and object is true.
fn and_or_truthy(v: &Value) -> bool {
    match v {
        Value::Null => false,
        Value::Bool(b) => *b,
        Value::Number(n) => n.as_f64().is_some_and(|d| d != 0.0),
        _ => true,
    }
}

/// `LogicalAndOperator`.
pub fn and(t: &Value, u: &Value) -> Value {
    Value::Bool(and_or_truthy(t) && and_or_truthy(u))
}

/// `LogicalOrOperator`.
pub fn or(t: &Value, u: &Value) -> Value {
    Value::Bool(and_or_truthy(t) || and_or_truthy(u))
}

/// `LogicalNotOperator`: null is true, any string false, a number true when zero.
pub fn not(t: &Value) -> Value {
    Value::Bool(match t {
        Value::Null => true,
        Value::Bool(b) => !b,
        Value::Number(n) => n.as_f64().is_some_and(|d| d == 0.0),
        _ => false,
    })
}

/// Gson `JsonElement.equals`: content equality; numbers by value (`long` when both are whole,
/// else `double`).
pub fn json_equals(a: &Value, b: &Value) -> bool {
    match (a, b) {
        (Value::Number(x), Value::Number(y)) => match (x.as_i64(), y.as_i64()) {
            (Some(p), Some(q)) => p == q,
            _ => {
                let (p, q) = (x.as_f64().unwrap_or(f64::NAN), y.as_f64().unwrap_or(f64::NAN));
                p == q || (p.is_nan() && q.is_nan())
            }
        },
        (Value::Array(x), Value::Array(y)) => x.len() == y.len() && x.iter().zip(y).all(|(p, q)| json_equals(p, q)),
        (Value::Object(x), Value::Object(y)) => {
            x.len() == y.len() && x.iter().all(|(k, v)| y.get(k).is_some_and(|w| json_equals(v, w)))
        }
        _ => a == b,
    }
}

/// `LogicalEqualOperator`.
pub fn equal(t: &Value, u: &Value) -> Result<bool> {
    let obj = |v: &Value| v.is_object();
    let arr = |v: &Value| v.is_array();
    if (obj(t) && obj(u)) || (arr(t) && arr(u)) {
        return Ok(json_equals(t, u));
    }
    if obj(t) != obj(u) || arr(t) != arr(u) {
        return Ok(false);
    }
    let tp = find_primitive_null_as_boolean(t)?;
    let up = find_primitive_null_as_boolean(u)?;
    Ok(match (&tp, &up) {
        (Prim::Str(_), _) | (_, Prim::Str(_)) => tp.java_string() == up.java_string(),
        (Prim::Bool(a), Prim::Bool(b)) => a == b,
        (Prim::Bool(_), _) | (_, Prim::Bool(_)) => false,
        (Prim::Num(a), Prim::Num(b)) => match wider(*a, *b) {
            3 => a.as_f64() == b.as_f64(),
            2 => a.as_f32() == b.as_f32(),
            _ => a.as_i64() == b.as_i64(),
        },
        _ => false,
    })
}

/// The comparison operators: null when either side is null, text order when either is a
/// string, an error for booleans.
pub fn compare(t: &Value, u: &Value, op: &str) -> Result<Value> {
    if t.is_null() || u.is_null() {
        return Ok(Value::Null);
    }
    let tp = find_primitive_null_as_boolean(t)?;
    let up = find_primitive_null_as_boolean(u)?;
    let ordering = match (&tp, &up) {
        (Prim::Str(_), _) | (_, Prim::Str(_)) => {
            tp.java_string().encode_utf16().cmp(up.java_string().encode_utf16())
        }
        (Prim::Bool(_), _) | (_, Prim::Bool(_)) => {
            return Err(execution(format!(
                "Cannot compare {op} with the values {} and {}",
                tp.java_string(),
                up.java_string()
            )))
        }
        (Prim::Num(a), Prim::Num(b)) => {
            let ord = match wider(*a, *b) {
                3 => a.as_f64().partial_cmp(&b.as_f64()),
                2 => a.as_f32().partial_cmp(&b.as_f32()),
                _ => Some(a.as_i64().cmp(&b.as_i64())),
            };
            match ord {
                Some(o) => o,
                // NaN compares false every way, as in Java.
                None => return Ok(Value::Bool(false)),
            }
        }
        _ => return Ok(Value::Null),
    };
    Ok(Value::Bool(match op {
        "<" => ordering.is_lt(),
        "<=" => ordering.is_le(),
        ">" => ordering.is_gt(),
        _ => ordering.is_ge(),
    }))
}

/// `ArithmeticUnaryMinusOperator`.
pub fn negate(t: &Value) -> Result<Value> {
    if t.is_null() {
        return Err(execution("- cannot be applied to a null value".into()));
    }
    Ok(match find_number(t)? {
        // `-(int MIN)` is a `long`, `-(long MIN)` a `double`: nothing wraps.
        JNum::Int(i) => whole(-(i as i64)),
        JNum::Long(l) => l.checked_neg().map_or(JNum::Double(-(l as f64)), whole),
        JNum::Float(f) => JNum::Float(-f),
        JNum::Double(d) => JNum::Double(-d),
    }
    .to_value())
}

/// `ArithmeticUnaryPlusOperator`: checks the value reads as a number and returns it unchanged.
pub fn plus(t: &Value) -> Result<Value> {
    if t.is_null() {
        return Err(execution("+ cannot be applied to a null value".into()));
    }
    find_number(t)?;
    Ok(t.clone())
}

/// `BitwiseComplementOperator`.
pub fn complement(t: &Value) -> Result<Value> {
    if t.is_null() {
        return Err(execution("~ cannot be applied to a null value".into()));
    }
    match find_number(t)? {
        JNum::Int(i) => Ok(Value::from(!i)),
        JNum::Long(l) => Ok(Value::from(!l)),
        _ => Err(execution(format!("Unable to apply bitwise operator on {}", json(t)))),
    }
}

/// `ConditionalTernaryOperator`: null is false; an array or object true; an empty string false.
pub fn ternary_condition(a: &Value) -> bool {
    match a {
        Value::Null => false,
        Value::Bool(b) => *b,
        Value::String(s) => !s.is_empty(),
        Value::Number(n) => n.as_f64().is_some_and(|d| d != 0.0),
        _ => true,
    }
}

#[cfg(test)]
mod tests {
    use serde_json::json;

    use super::*;

    #[test]
    fn java_number_strings() {
        assert_eq!(java_double_string(3.0), "3.0");
        assert_eq!(java_double_string(2.5), "2.5");
        assert_eq!(java_double_string(1e10), "1.0E10");
        assert_eq!(java_double_string(1.234e-5), "1.234E-5");
        assert_eq!(java_double_string(0.001), "0.001");
        assert_eq!(java_double_string(-1234567.5), "-1234567.5");
        assert_eq!(java_float_string(3.3333333), "3.3333333");
    }

    #[test]
    fn operators_answer_as_kirun_java() {
        assert_eq!(add(&json!("abc"), &Value::Null).unwrap(), json!("abcnull"));
        assert_eq!(add(&Value::Null, &json!(5)).unwrap(), json!(5));
        assert!(add(&json!(1), &json!(true)).unwrap_err().to_string().contains("Cannot add the values 1 and true"));
        assert_eq!(add(&json!(2147483647), &json!(1)).unwrap(), json!(2147483648_i64));
        assert_eq!(subtract(&json!("5"), &json!(1)).unwrap(), json!(4));
        assert_eq!(divide(&json!(10), &json!(3)).unwrap(), json!(10.0 / 3.0));
        assert_eq!(divide(&json!(9), &json!(3)).unwrap(), json!(3));
        assert_eq!(divide(&json!(0), &json!(0)).unwrap_err().to_string(), "/ by zero");
        assert!(equal(&Value::Null, &json!(false)).unwrap());
        assert!(!equal(&Value::Null, &json!(0)).unwrap());
        assert!(equal(&json!(5), &json!("5")).unwrap());
        assert_eq!(and(&json!(1), &json!("")), json!(true));
        assert_eq!(compare(&Value::Null, &json!(1), "<").unwrap(), Value::Null);
        assert_eq!(compare(&json!(0), &json!("abc"), "<").unwrap(), json!(true));
        assert_eq!(bitwise(&json!(3000000000_i64), &json!(-1), "&").unwrap(), json!(3000000000_i64));
        assert_eq!(not(&json!("")), json!(false));
        assert_eq!(complement(&json!("5")).unwrap(), json!(-6));
    }
}
