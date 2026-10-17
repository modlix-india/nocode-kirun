//! kirun-java `json/schema/convertor/NumberConvertor.java` (the port began from kirun-js's): a
//! string goes through Java's `Double.parseDouble` (so `""` and `0x10` do not convert, `1e5` and
//! ` 5 ` do); a whole type takes a fraction only outside STRICT and drops it towards zero; a
//! `LONG` takes anything a Java `long` holds; a `FLOAT` is rounded to a float and a `DOUBLE`
//! stays a double (`"5"` becomes `5.0`); an unconvertible value is named as it came in, as JSON.

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::validator::schema_validator::{java_json, SchemaPath};
use crate::json::schema::Schema;
use crate::runtime::expression::operators::java_semantics::JNum;
use crate::util::json::convertor_util::ConvertorUtil;
use crate::{Result, Value};

/// 2^63 as a double: Java's `value <= Long.MAX_VALUE` lets it through (the long rounds to it).
const LONG_LIMIT: f64 = 9_223_372_036_854_775_808.0;

pub struct NumberConvertor;

/// The number a converted element holds: `getAsDouble` of a string's `parseDouble`, or the JSON
/// number as it is.
enum Source {
    /// A JSON whole number (Gson's `LazilyParsedNumber` without a fraction): exact.
    Whole(i64),
    Real(f64),
}

impl NumberConvertor {
    /// kirun-java `NumberConvertor.convert(parents, type, schema, mode, element)`. Unconvertible
    /// values in STRICT mode fail with `KIRunError::SchemaConversion`.
    pub fn convert(
        parents: &[Schema],
        r#type: SchemaType,
        schema: &Schema,
        mode: ConversionMode,
        element: &Value,
    ) -> Result<Value> {
        Self::convert_in(parents, r#type, schema, mode, element)
    }

    pub(crate) fn convert_in<P: SchemaPath + ?Sized>(
        parents: &P,
        r#type: SchemaType,
        schema: &Schema,
        mode: ConversionMode,
        element: &Value,
    ) -> Result<Value> {
        let refuse = |message: String| {
            ConvertorUtil::handle_un_convertible_value_with_default(
                parents,
                Some(mode),
                element,
                Self::get_default(schema),
                &message,
            )
        };
        let not_a = || format!("{} is not a {}", java_json(element), r#type.as_str());

        let source = match element {
            Value::Null => return refuse("Expected a Number but found null".into()),
            Value::String(s) => match java_parse_double(s) {
                Some(d) => Source::Real(d),
                None => return refuse(not_a()),
            },
            Value::Number(n) => match n.as_i64() {
                Some(i) => Source::Whole(i),
                None => Source::Real(n.as_f64().unwrap_or(f64::NAN)),
            },
            _ => return refuse(not_a()),
        };

        match Self::extract_number(r#type, &source, mode) {
            Some(n) => Ok(n.to_value()),
            None => refuse(not_a()),
        }
    }

    fn extract_number(schema_type: SchemaType, source: &Source, mode: ConversionMode) -> Option<JNum> {
        let strict = mode == ConversionMode::Strict;
        let d = match source {
            Source::Whole(i) => *i as f64,
            Source::Real(d) => *d,
        };
        match schema_type {
            SchemaType::Integer => {
                if strict && d % 1.0 != 0.0 {
                    return None;
                }
                Some(JNum::Int(match source {
                    Source::Whole(i) => *i as i32,
                    Source::Real(d) => *d as i32,
                }))
            }
            SchemaType::Long => {
                if strict && (d % 1.0 != 0.0 || !(-LONG_LIMIT..=LONG_LIMIT).contains(&d)) {
                    return None;
                }
                Some(JNum::Long(match source {
                    Source::Whole(i) => *i,
                    Source::Real(d) => *d as i64,
                }))
            }
            SchemaType::Double => Some(JNum::Double(d)),
            SchemaType::Float => {
                if strict && !(-(f32::MAX as f64)..=(f32::MAX as f64)).contains(&d) {
                    return None;
                }
                Some(JNum::Float(d as f32))
            }
            _ => None,
        }
    }

    /// kirun-java: the schema's default when it is a number, else null.
    fn get_default(schema: &Schema) -> Value {
        match schema.get_default_value() {
            Some(v @ Value::Number(_)) => v.clone(),
            _ => Value::Null,
        }
    }
}

/// Java `Double.parseDouble`: surrounding whitespace and an `f`/`d` suffix allowed, `NaN` and
/// `Infinity` read, hexadecimal only with a binary exponent (not handled: refused).
fn java_parse_double(s: &str) -> Option<f64> {
    let t = s.trim_matches(|c: char| c <= ' ');
    let t = t.strip_suffix(['d', 'D', 'f', 'F']).unwrap_or(t);
    let body = t.strip_prefix(['+', '-']).unwrap_or(t);
    if body == "NaN" || body == "Infinity" {
        let v = if body == "NaN" { f64::NAN } else { f64::INFINITY };
        return Some(if t.starts_with('-') { -v } else { v });
    }
    if body.is_empty() || !body.chars().all(|c| c.is_ascii_digit() || ".eE+-".contains(c)) {
        return None;
    }
    if !body.chars().next().is_some_and(|c| c.is_ascii_digit() || c == '.') {
        return None;
    }
    t.parse::<f64>().ok()
}
