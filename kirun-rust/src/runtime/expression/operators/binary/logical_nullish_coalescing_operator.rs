//! Port of kirun-js `runtime/expression/operators/binary/LogicalNullishCoalescingOperator.ts`, answering as kirun-java's
//! (see `java_semantics`).

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};

#[derive(Debug, Default, Clone, Copy)]
pub struct LogicalNullishCoalescingOperator;

impl LogicalNullishCoalescingOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for LogicalNullishCoalescingOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
        Ok(if t.is_null() { u.clone() } else { t.clone() })
    }
}
