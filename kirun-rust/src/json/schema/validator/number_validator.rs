//! kirun-java `json/schema/validator/NumberValidator.java` (the port began from kirun-js
//! `NumberValidator.ts`; stored server functions run on kirun-java, so its rules win):
//!
//! - an `INTEGER` must be whole and fit a Java `int`, a `LONG` whole and fit a `long`
//!   (kirun-js only checks that the number is whole, so `9007199254740993` passed as an
//!   `INTEGER`); `FLOAT` and `DOUBLE` take any number;
//! - a value that is not a number reads `<value> is not a <Type>`, the value as JSON;
//! - `multipleOf` on a `FLOAT` / `DOUBLE` divides and wants a whole quotient that fits an `int`
//!   (kirun-java `Double.valueOf("" + d.intValue()).equals(d)`), on an `INTEGER` / `LONG` it is
//!   the integer remainder.

use crate::json::schema::r#type::SchemaType;
use crate::json::schema::validator::schema_validator::{java_json, SchemaPath};
use crate::json::schema::Schema;
use crate::util::primitive::js_number_to_string;
use crate::{KIRunError, Result, Value};

pub struct NumberValidator;

/// The number a validator compares, as kirun-java extracts it for the schema type.
#[derive(Clone, Copy)]
enum Extracted {
    /// `INTEGER` / `LONG`: Java compares these as `long`.
    Whole(i64),
    /// `FLOAT` (already rounded to a float) / `DOUBLE`: Java compares these as `double`.
    Real(f64),
}

impl Extracted {
    fn as_f64(self) -> f64 {
        match self {
            Extracted::Whole(n) => n as f64,
            Extracted::Real(n) => n,
        }
    }
}

/// 2^63 as a double: the first value a Java `long` cannot hold.
const LONG_LIMIT: f64 = 9_223_372_036_854_775_808.0;

impl NumberValidator {
    /// kirun-java `NumberValidator.validate(type, parents, schema, element)`.
    pub fn validate(r#type: SchemaType, parents: &[Schema], schema: &Schema, element: Value) -> Result<Value> {
        Self::validate_in(r#type, parents, schema, element)
    }

    pub(crate) fn validate_in<P: SchemaPath + ?Sized>(
        r#type: SchemaType,
        parents: &P,
        schema: &Schema,
        element: Value,
    ) -> Result<Value> {
        let n = match &element {
            Value::Null => {
                return Err(KIRunError::schema_validation(parents.schema_path(), "Expected a number but found null"))
            }
            Value::Number(n) => n,
            _ => {
                return Err(KIRunError::schema_validation(
                    parents.schema_path(),
                    format!("{} is not a {}", java_json(&element), r#type.as_str()),
                ))
            }
        };

        let n = Self::extract_number(r#type, parents, &element, n)?;

        Self::check_range(parents, schema, &element, n)?;

        // Only call when set: inlined, LLVM hoisted the arithmetic above the `Some` check.
        if schema.get_multiple_of().is_some() {
            Self::check_multiple_of(parents, schema, &element, n)?;
        }

        Ok(element)
    }

    /// kirun-java `extractNumber`: `getAsInt` / `getAsLong` / `getAsFloat` / `getAsDouble`, and a
    /// whole type whose extracted value is not the number itself is refused.
    fn extract_number<P: SchemaPath + ?Sized>(
        r#type: SchemaType,
        parents: &P,
        element: &Value,
        n: &serde_json::Number,
    ) -> Result<Extracted> {
        let refuse = || {
            KIRunError::schema_validation(
                parents.schema_path(),
                format!("{} is not a number of type {}", java_json(element), r#type.as_str()),
            )
        };

        let whole = || -> Option<i64> {
            if let Some(i) = n.as_i64() {
                return Some(i);
            }
            if n.is_u64() {
                return None;
            }
            let f = n.as_f64()?;
            (f.is_finite() && f.fract() == 0.0 && (-LONG_LIMIT..LONG_LIMIT).contains(&f)).then_some(f as i64)
        };

        match r#type {
            SchemaType::Long => whole().map(Extracted::Whole).ok_or_else(refuse),
            SchemaType::Integer => whole()
                .filter(|i| i32::try_from(*i).is_ok())
                .map(Extracted::Whole)
                .ok_or_else(refuse),
            SchemaType::Float => Ok(Extracted::Real(java_float(n.as_f64().unwrap_or(f64::NAN)))),
            _ => Ok(Extracted::Real(n.as_f64().unwrap_or(f64::NAN))),
        }
    }

    #[inline(never)]
    fn check_multiple_of<P: SchemaPath + ?Sized>(
        parents: &P,
        schema: &Schema,
        element: &Value,
        n: Extracted,
    ) -> Result<()> {
        let Some(m) = schema.get_multiple_of().copied().filter(|m| *m != 0.0 && !m.is_nan()) else {
            return Ok(());
        };
        let multiple = match n {
            // kirun-java's `multipleOf` is a `Long`, so the remainder is a `long` one.
            Extracted::Whole(i) => match as_long(m) {
                Some(mi) => i.checked_rem(mi).is_none_or(|r| r == 0),
                None => (i as f64) % m == 0.0,
            },
            Extracted::Real(d) => {
                let q = d / m;
                q.fract() == 0.0 && q >= i32::MIN as f64 && q <= i32::MAX as f64
            }
        };
        if !multiple {
            return Err(KIRunError::schema_validation(
                parents.schema_path(),
                message(schema, "multipleOf")
                    .unwrap_or_else(|| format!("{} is not multiple of {}", java_json(element), js_number_to_string(m))),
            ));
        }
        Ok(())
    }

    fn check_range<P: SchemaPath + ?Sized>(parents: &P, schema: &Schema, element: &Value, n: Extracted) -> Result<()> {
        let fail = |key: &str, text: &str, limit: f64| {
            Err(KIRunError::schema_validation(
                parents.schema_path(),
                message(schema, key)
                    .unwrap_or_else(|| format!("{} {text} {}", java_json(element), js_number_to_string(limit))),
            ))
        };
        let cmp = |limit: f64| compare(n, limit);

        if let Some(&min) = schema.get_minimum() {
            if cmp(min).is_lt() {
                return fail("minimum", "should be greater than or equal to", min);
            }
        }

        if let Some(&max) = schema.get_maximum() {
            if cmp(max).is_gt() {
                return fail("maximum", "should be less than or equal to", max);
            }
        }

        if let Some(&emin) = schema.get_exclusive_minimum() {
            if cmp(emin).is_le() {
                return fail("exclusiveMinimum", "should be greater than", emin);
            }
        }

        // kirun-java (and kirun-js) compare `> 0` here, not `>= 0`: the exclusive maximum
        // itself passes.
        if let Some(&emax) = schema.get_exclusive_maximum() {
            if cmp(emax).is_gt() {
                return fail("exclusiveMaximum", "should be less than", emax);
            }
        }

        Ok(())
    }
}

/// kirun-java `numberCompare`: whole types compare as `long`, the others as `double`.
fn compare(n: Extracted, limit: f64) -> std::cmp::Ordering {
    match (n, as_long(limit)) {
        (Extracted::Whole(i), Some(l)) => i.cmp(&l),
        _ => n.as_f64().partial_cmp(&limit).unwrap_or(std::cmp::Ordering::Equal),
    }
}

/// `limit` as a `long` when it is a whole number a `long` holds.
fn as_long(limit: f64) -> Option<i64> {
    (limit.fract() == 0.0 && (-LONG_LIMIT..LONG_LIMIT).contains(&limit)).then_some(limit as i64)
}

/// Java `jp.getAsFloat()` compared through `Double.valueOf(f.toString())`: the float's shortest
/// decimal form read back as a double.
fn java_float(d: f64) -> f64 {
    let f = d as f32;
    if !f.is_finite() {
        return f as f64;
    }
    format!("{f}").parse::<f64>().unwrap_or(f as f64)
}

fn message(schema: &Schema, key: &str) -> Option<String> {
    schema.get_details().and_then(|d| d.get_validation_message(key)).cloned()
}
