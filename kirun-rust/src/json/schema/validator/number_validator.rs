//! Port of kirun-js `json/schema/validator/NumberValidator.ts`.

use crate::json::schema::r#type::SchemaType;
use crate::json::schema::validator::schema_validator::{format_value, SchemaPath};
use crate::json::schema::Schema;
use crate::util::primitive::{js_number_to_string, js_to_string};
use crate::{KIRunError, Result, Value};

pub struct NumberValidator;

impl NumberValidator {
    /// JS `NumberValidator.validate(type, parents, schema, element)`.
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
            Value::Number(n) => n.as_f64().unwrap_or(f64::NAN),
            _ => {
                return Err(KIRunError::schema_validation(
                    parents.schema_path(),
                    format!("Expected a {} but found {}", r#type.as_str(), format_value(&element)),
                ))
            }
        };

        let n = Self::extract_number(r#type, parents, &element, n)?;

        Self::check_range(parents, schema, &element, n)?;

        // Only call when set: inlined, LLVM hoisted `n % m` above the `Some` check, so every
        // number paid for an fmod (slow for large values) even without multipleOf.
        if schema.get_multiple_of().is_some() {
            Self::check_multiple_of(parents, schema, &element, n)?;
        }

        Ok(element)
    }

    fn extract_number<P: SchemaPath + ?Sized>(r#type: SchemaType, parents: &P, element: &Value, value: f64) -> Result<f64> {
        let integral = matches!(r#type, SchemaType::Long | SchemaType::Integer);
        // JS Math.round: halves round towards +Infinity.
        let n = if integral { (value + 0.5).floor() } else { value };

        if n.is_nan() || (integral && n != value) {
            return Err(KIRunError::schema_validation(
                parents.schema_path(),
                format!("{} is not a number of type {}", js_to_string(element), r#type.as_str()),
            ));
        }

        Ok(n)
    }

    #[inline(never)]
    fn check_multiple_of<P: SchemaPath + ?Sized>(parents: &P, schema: &Schema, element: &Value, n: f64) -> Result<()> {
        if let Some(m) = schema.get_multiple_of().copied().filter(|m| *m != 0.0 && !m.is_nan()) {
            if n % m != 0.0 {
                return Err(KIRunError::schema_validation(
                    parents.schema_path(),
                    message(schema, "multipleOf").unwrap_or_else(|| {
                        format!("{} is not multiple of {}", js_to_string(element), js_number_to_string(m))
                    }),
                ));
            }
        }
        Ok(())
    }

    fn check_range<P: SchemaPath + ?Sized>(parents: &P, schema: &Schema, element: &Value, n: f64) -> Result<()> {
        let fail = |key: &str, text: &str, limit: f64| {
            Err(KIRunError::schema_validation(
                parents.schema_path(),
                message(schema, key)
                    .unwrap_or_else(|| format!("{} {text} {}", js_to_string(element), js_number_to_string(limit))),
            ))
        };

        if let Some(&min) = schema.get_minimum() {
            if n - min < 0.0 {
                return fail("minimum", "should be greater than or equal to", min);
            }
        }

        if let Some(&max) = schema.get_maximum() {
            if n - max > 0.0 {
                return fail("maximum", "should be less than or equal to", max);
            }
        }

        if let Some(&emin) = schema.get_exclusive_minimum() {
            if n - emin <= 0.0 {
                return fail("exclusiveMinimum", "should be greater than", emin);
            }
        }

        // JS compares `> 0` here (not `>= 0`), so the exclusive maximum itself passes.
        if let Some(&emax) = schema.get_exclusive_maximum() {
            if n - emax > 0.0 {
                return fail("exclusiveMaximum", "should be less than", emax);
            }
        }

        Ok(())
    }
}

fn message(schema: &Schema, key: &str) -> Option<String> {
    schema.get_details().and_then(|d| d.get_validation_message(key)).cloned()
}
