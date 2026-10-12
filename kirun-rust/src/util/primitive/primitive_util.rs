//! Port of kirun-js `util/primitive/PrimitiveUtil.ts`.

use crate::json::schema::r#type::SchemaType;
use crate::util::primitive::{js_string_to_number, js_to_string};
use crate::{KIRunError, Result, Value};

pub struct PrimitiveUtil;

impl PrimitiveUtil {
    /// JS `findPrimitiveNullAsBoolean`: null is `(BOOLEAN, false)`.
    pub fn find_primitive_null_as_boolean(element: &Value) -> Result<(SchemaType, Value)> {
        match element {
            Value::Null => Ok((SchemaType::Boolean, Value::Bool(false))),
            Value::Array(_) | Value::Object(_) => Err(not_primitive(element)),
            Value::Bool(_) => Ok((SchemaType::Boolean, element.clone())),
            Value::String(_) => Ok((SchemaType::String, element.clone())),
            Value::Number(_) => Self::find_primitive_number_type(element),
        }
    }

    /// JS `findPrimitive`: null is `(NULL, undefined)`.
    pub fn find_primitive(element: &Value) -> Result<(SchemaType, Value)> {
        match element {
            Value::Null => Ok((SchemaType::Null, Value::Null)),
            Value::Array(_) | Value::Object(_) => Err(not_primitive(element)),
            Value::Bool(_) => Ok((SchemaType::Boolean, element.clone())),
            Value::String(_) => Ok((SchemaType::String, element.clone())),
            Value::Number(_) => Self::find_primitive_number_type(element),
        }
    }

    /// JS `findPrimitiveNumberType`: integral numbers are LONG, the rest DOUBLE. As in JS, a
    /// string or boolean is passed through as DOUBLE (JS `Number.isInteger` is false for them).
    pub fn find_primitive_number_type(element: &Value) -> Result<(SchemaType, Value)> {
        match element {
            Value::Null | Value::Array(_) | Value::Object(_) => Err(KIRunError::execution(format!(
                "Unable to convert {} to a number.",
                js_to_string(element)
            ))),
            Value::Number(n) => {
                let integral = n.is_i64() || n.is_u64() || n.as_f64().is_some_and(|f| f.fract() == 0.0);
                Ok((if integral { SchemaType::Long } else { SchemaType::Double }, element.clone()))
            }
            _ => Ok((SchemaType::Double, element.clone())),
        }
    }

    /// JS `compare`: arrays by length then item by item, objects before everything else,
    /// otherwise `compare_primitive`. The result's sign is what matters (it can be fractional).
    pub fn compare(a: &Value, b: &Value) -> f64 {
        if std::ptr::eq(a, b) || loose_equal(a, b) {
            return 0.0;
        }
        if a.is_null() || b.is_null() {
            return if a.is_null() { -1.0 } else { 1.0 };
        }
        match (a, b) {
            (Value::Array(x), Value::Array(y)) => {
                if x.len() != y.len() {
                    return x.len() as f64 - y.len() as f64;
                }
                for (p, q) in x.iter().zip(y) {
                    let cmp = Self::compare(p, q);
                    if cmp != 0.0 {
                        return cmp;
                    }
                }
                return 0.0;
            }
            (Value::Array(_), _) => return -1.0,
            (_, Value::Array(_)) => return 1.0,
            _ => {}
        }
        // JS walks the keys of two objects but its forEach callback discards the result, so
        // any object compares as -1 (or 1 when only the right side is an object).
        if a.is_object() || b.is_object() {
            return if a.is_object() { -1.0 } else { 1.0 };
        }
        Self::compare_primitive(a, b)
    }

    /// JS `comparePrimitive`.
    pub fn compare_primitive(oa: &Value, ob: &Value) -> f64 {
        if oa.is_null() || ob.is_null() {
            if oa.is_null() && ob.is_null() {
                return 0.0;
            }
            return if oa.is_null() { -1.0 } else { 1.0 };
        }
        if loose_equal(oa, ob) {
            return 0.0;
        }
        if oa.is_boolean() || ob.is_boolean() {
            return if crate::util::primitive::js_truthy(oa) { -1.0 } else { 1.0 };
        }
        if oa.is_string() || ob.is_string() {
            return if js_to_string(oa) < js_to_string(ob) { -1.0 } else { 1.0 };
        }
        if let (Some(x), Some(y)) = (oa.as_f64(), ob.as_f64()) {
            return x - y;
        }
        0.0
    }

    /// JS `toPrimitiveType`: an identity cast.
    pub fn to_primitive_type(e: &Value) -> Value {
        e.clone()
    }
}

fn not_primitive(element: &Value) -> KIRunError {
    KIRunError::execution(format!("{} is not a primitive type", js_to_string(element)))
}

fn to_number(v: &Value) -> f64 {
    match v {
        Value::Null => 0.0,
        Value::Bool(b) => f64::from(u8::from(*b)),
        Value::Number(n) => n.as_f64().unwrap_or(f64::NAN),
        Value::String(s) => js_string_to_number(s),
        _ => f64::NAN,
    }
}

/// JS `==` for primitives (objects and arrays only equal by reference, handled by the caller).
fn loose_equal(a: &Value, b: &Value) -> bool {
    match (a, b) {
        (Value::Null, Value::Null) => true,
        (Value::Null, _) | (_, Value::Null) => false,
        (Value::String(x), Value::String(y)) => x == y,
        (Value::Array(_) | Value::Object(_), _) | (_, Value::Array(_) | Value::Object(_)) => false,
        _ => to_number(a) == to_number(b),
    }
}
