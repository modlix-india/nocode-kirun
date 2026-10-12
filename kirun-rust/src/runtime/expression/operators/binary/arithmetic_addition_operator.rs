//! Port of kirun-js `runtime/expression/operators/binary/ArithmeticAdditionOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;

#[derive(Debug, Default, Clone, Copy)]
pub struct ArithmeticAdditionOperator;

impl ArithmeticAdditionOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for ArithmeticAdditionOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                if t.is_null() {
            return Ok(u.clone());
        } else if u.is_null() {
            return Ok(t.clone());
        }
        // JS `t + u`: string concatenation when either side is not a number/boolean.
        let stringy = |v: &Value| matches!(v, Value::String(_) | Value::Array(_) | Value::Object(_));
        if stringy(t) || stringy(u) {
            return Ok(Value::String(js::to_js_string(t) + &js::to_js_string(u)));
        }
        Ok(js::num(js::to_number(t) + js::to_number(u)))
    }
}
