//! Port of kirun-js `runtime/expression/operators/binary/LogicalGreaterThanEqualOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;

#[derive(Debug, Default, Clone, Copy)]
pub struct LogicalGreaterThanEqualOperator;

impl LogicalGreaterThanEqualOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for LogicalGreaterThanEqualOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                js::compare(t, u, ">=", |a, b| a >= b, |a, b| a >= b)
    }
}
