//! Port of kirun-js `runtime/expression/operators/binary/LogicalGreaterThanOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;

#[derive(Debug, Default, Clone, Copy)]
pub struct LogicalGreaterThanOperator;

impl LogicalGreaterThanOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for LogicalGreaterThanOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                js::compare(t, u, ">", |a, b| a > b, |a, b| a > b)
    }
}
