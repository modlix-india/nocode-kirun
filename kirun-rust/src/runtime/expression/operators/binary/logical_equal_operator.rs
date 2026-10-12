//! Port of kirun-js `runtime/expression/operators/binary/LogicalEqualOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::util::deep_equal::deep_equal;

#[derive(Debug, Default, Clone, Copy)]
pub struct LogicalEqualOperator;

impl LogicalEqualOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for LogicalEqualOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                Ok(Value::Bool(deep_equal(t, u)))
    }
}
