//! Port of kirun-js `runtime/expression/operators/binary/ArrayRangeOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;

#[derive(Debug, Default, Clone, Copy)]
pub struct ArrayRangeOperator;

impl ArrayRangeOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for ArrayRangeOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                let s = |v: &Value| if v.is_null() { String::new() } else { js::to_js_string(v) };
        Ok(Value::String(format!("{}..{}", s(t), s(u))))
    }
}
