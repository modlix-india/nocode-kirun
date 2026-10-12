//! Port of kirun-js `runtime/expression/operators/binary/ArrayOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;
use crate::util::string::string_formatter::StringFormatter;
use crate::KIRunError;

#[derive(Debug, Default, Clone, Copy)]
pub struct ArrayOperator;

impl ArrayOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for ArrayOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                if !js::truthy(t) {
            return Err(KIRunError::execution("Cannot apply array operator on a null value"));
        }
        if !js::truthy(u) {
            return Err(KIRunError::execution("Cannot retrive null index value"));
        }
        let len = match t {
            Value::Array(a) => a.len(),
            Value::String(s) => js::str_len(s),
            _ => {
                return Err(KIRunError::execution(StringFormatter::format(
                    "Cannot retrieve value from a primitive value $",
                    &[&js::to_js_string(t)],
                )))
            }
        };
        let idx = js::to_number(u);
        if idx >= len as f64 {
            return Err(KIRunError::execution(StringFormatter::format(
                "Cannot retrieve index $ from the array of length $",
                &[&js::to_js_string(u), &len.to_string()],
            )));
        }
        if idx < 0.0 || idx.fract() != 0.0 {
            return Ok(Value::Null);
        }
        Ok(match t {
            Value::Array(a) => a[idx as usize].clone(),
            Value::String(s) => js::char_at(s, idx as usize).map(Value::String).unwrap_or(Value::Null),
            _ => Value::Null,
        })
    }
}
