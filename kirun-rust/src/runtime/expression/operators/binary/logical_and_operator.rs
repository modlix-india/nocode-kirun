//! Port of kirun-js `runtime/expression/operators/binary/LogicalAndOperator.ts`, answering as kirun-java's
//! (see `java_semantics`).

use super::binary_operator::BinaryOperator;
use crate::runtime::expression::operators::java_semantics as java;
use crate::{Result, Value};

#[derive(Debug, Default, Clone, Copy)]
pub struct LogicalAndOperator;

impl LogicalAndOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for LogicalAndOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
        Ok(java::and(t, u))
    }
}
