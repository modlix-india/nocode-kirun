//! Port of kirun-js `runtime/expression/operators/binary/ArithmeticMultiplicationOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;
use crate::runtime::expression::operation::Operation;

#[derive(Debug, Default, Clone, Copy)]
pub struct ArithmeticMultiplicationOperator;

impl ArithmeticMultiplicationOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for ArithmeticMultiplicationOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                self.null_check(t, u, &Operation::MULTIPLICATION)?;

        let is_t_string = t.is_string();
        if is_t_string || u.is_string() {
            let str = if is_t_string { js::to_js_string(t) } else { js::to_js_string(u) };
            let mut num = js::to_number(if is_t_string { u } else { t });
            if num.is_nan() {
                return Ok(Value::String(String::new()));
            }

            let mut sb = String::new();
            let reverse = num < 0.0;
            num = num.abs();

            let mut times = num.floor();
            while times > 0.0 {
                sb.push_str(&str);
                times -= 1.0;
            }

            let len = js::str_len(&str);
            let chrs = (len as f64 * (num - num.floor())).floor().abs() as usize;
            if chrs != 0 {
                sb.push_str(&js::substring(&str, 0, chrs));
            }

            if reverse {
                let mut units: Vec<u16> = sb.encode_utf16().collect();
                units.reverse();
                return Ok(Value::String(String::from_utf16_lossy(&units)));
            }
            return Ok(Value::String(sb));
        }

        Ok(js::num(js::to_number(t) * js::to_number(u)))
    }
}
