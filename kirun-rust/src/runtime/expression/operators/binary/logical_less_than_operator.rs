//! Port of kirun-js `runtime/expression/operators/binary/LogicalLessThanOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;

#[derive(Debug, Default, Clone, Copy)]
pub struct LogicalLessThanOperator;

impl LogicalLessThanOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for LogicalLessThanOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                js::compare(t, u, "<", |a, b| a < b, |a, b| a < b)
    }
}
