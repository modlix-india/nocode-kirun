//! Port of kirun-js `runtime/expression/operators/unary/LogicalNotOperator.ts`.

use super::unary_operator::UnaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;

#[derive(Debug, Default, Clone, Copy)]
pub struct LogicalNotOperator;

impl LogicalNotOperator {
    pub fn new() -> Self {
        Self
    }
}

impl UnaryOperator for LogicalNotOperator {
    fn apply(&self, t: &Value) -> Result<Value> {
                Ok(Value::Bool(!js::truthy(t) && t.as_str() != Some("")))
    }
}
