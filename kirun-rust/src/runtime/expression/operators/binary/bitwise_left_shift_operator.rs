//! Port of kirun-js `runtime/expression/operators/binary/BitwiseLeftShiftOperator.ts`, answering as kirun-java's
//! (see `java_semantics`).

use super::binary_operator::BinaryOperator;
use crate::runtime::expression::operators::java_semantics as java;
use crate::{Result, Value};

#[derive(Debug, Default, Clone, Copy)]
pub struct BitwiseLeftShiftOperator;

impl BitwiseLeftShiftOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for BitwiseLeftShiftOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
        java::bitwise(t, u, "<<")
    }
}
