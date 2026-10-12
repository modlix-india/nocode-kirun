//! Port of kirun-js `runtime/expression/operators/binary/LogicalAndOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;

#[derive(Debug, Default, Clone, Copy)]
pub struct LogicalAndOperator;

impl LogicalAndOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for LogicalAndOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                let ok = |v: &Value| js::truthy(v) && v.as_str() != Some("");
        Ok(Value::Bool(ok(t) && ok(u)))
    }
}
