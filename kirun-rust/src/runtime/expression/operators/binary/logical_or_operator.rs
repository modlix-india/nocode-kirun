//! Port of kirun-js `runtime/expression/operators/binary/LogicalOrOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;

#[derive(Debug, Default, Clone, Copy)]
pub struct LogicalOrOperator;

impl LogicalOrOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for LogicalOrOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                let ok = |v: &Value| js::truthy(v) && v.as_str() != Some("");
        Ok(Value::Bool(ok(t) || ok(u)))
    }
}
