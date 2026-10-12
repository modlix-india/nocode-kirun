//! Port of kirun-js `json/schema/convertor/NumberConvertor.ts`.

use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::validator::schema_validator::SchemaPath;
use crate::json::schema::Schema;
use crate::util::json::convertor_util::ConvertorUtil;
use crate::util::primitive::{js_string_to_number, js_to_string, number_value};
use crate::{Result, Value};

const MAX_SAFE_INTEGER: f64 = 9_007_199_254_740_991.0;

pub struct NumberConvertor;

impl NumberConvertor {
    /// JS `NumberConvertor.convert(parents, type, schema, mode, element)`. Unconvertible values in
    /// STRICT mode fail with `KIRunError::SchemaConversion`.
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
        if element.is_null() {
            return ConvertorUtil::handle_un_convertible_value_with_default(
                parents,
                Some(mode),
                element,
                Self::get_default(schema),
                "Expected a number but found null",
            );
        }

        let not_a = |e: &str| format!("{e} is not a {}", r#type.as_str());

        let num: f64 = match element {
            Value::Object(_) | Value::Array(_) | Value::Bool(_) => {
                return ConvertorUtil::handle_un_convertible_value_with_default(
                    parents,
                    Some(mode),
                    element,
                    Self::get_default(schema),
                    &not_a(&js_to_string(element)),
                );
            }
            Value::String(s) => {
                let n = js_string_to_number(s);
                if n.is_nan() {
                    // JS has already replaced the element with NaN at this point.
                    return ConvertorUtil::handle_un_convertible_value_with_default(
                        parents,
                        Some(mode),
                        &Value::Null,
                        Self::get_default(schema),
                        &not_a("NaN"),
                    );
                }
                n
            }
            Value::Number(n) => n.as_f64().unwrap_or(f64::NAN),
            Value::Null => unreachable!(),
        };

        match Self::extract_number(r#type, num, mode) {
            Some(n) => Ok(number_value(n)),
            None => ConvertorUtil::handle_un_convertible_value_with_default(
                parents,
                Some(mode),
                &number_value(num),
                Self::get_default(schema),
                &not_a(&crate::util::primitive::js_number_to_string(num)),
            ),
        }
    }

    fn extract_number(schema_type: SchemaType, element: f64, mode: ConversionMode) -> Option<f64> {
        match schema_type {
            SchemaType::Integer => Self::is_integer(element, mode).then(|| element.floor()),
            SchemaType::Long => Self::is_long(element, mode).then(|| element.floor()),
            SchemaType::Double => Some(element),
            SchemaType::Float => Self::is_float(element, mode).then_some(element),
            _ => None,
        }
    }

    fn is_integer(value: f64, mode: ConversionMode) -> bool {
        if mode != ConversionMode::Strict {
            return true;
        }
        value.is_finite() && value.fract() == 0.0
    }

    fn is_long(value: f64, mode: ConversionMode) -> bool {
        if mode != ConversionMode::Strict {
            return true;
        }
        value.is_finite() && value.fract() == 0.0 && (-MAX_SAFE_INTEGER..=MAX_SAFE_INTEGER).contains(&value)
    }

    fn is_float(value: f64, mode: ConversionMode) -> bool {
        if mode != ConversionMode::Strict {
            return true;
        }
        (-f64::MAX..=f64::MAX).contains(&value)
    }

    /// JS returns `Number(schema.getDefaultValue)` (the getter function, not its result) for a
    /// numeric default, which is `NaN`; `NaN` has no JSON form, so either way this is `null`.
    fn get_default(_schema: &Schema) -> Value {
        Value::Null
    }
}
