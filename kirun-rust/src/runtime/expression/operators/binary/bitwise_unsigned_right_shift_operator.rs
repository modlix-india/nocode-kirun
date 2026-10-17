//! Port of kirun-js `runtime/expression/operators/binary/BitwiseUnsignedRightShiftOperator.ts`, answering as kirun-java's
//! (see `java_semantics`).

use super::binary_operator::BinaryOperator;
use crate::runtime::expression::operators::java_semantics as java;
use crate::{Result, Value};

#[derive(Debug, Default, Clone, Copy)]
pub struct BitwiseUnsignedRightShiftOperator;

impl BitwiseUnsignedRightShiftOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for BitwiseUnsignedRightShiftOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
        java::bitwise(t, u, ">>>")
    }
}
