//! Port of kirun-js `runtime/expression/operators/unary/ArithmeticUnaryMinusOperator.ts`, answering as kirun-java's
//! (see `java_semantics`).

use super::unary_operator::UnaryOperator;
use crate::runtime::expression::operators::java_semantics as java;
use crate::{Result, Value};

#[derive(Debug, Default, Clone, Copy)]
pub struct ArithmeticUnaryMinusOperator;

impl ArithmeticUnaryMinusOperator {
    pub fn new() -> Self {
        Self
    }
}

impl UnaryOperator for ArithmeticUnaryMinusOperator {
    fn apply(&self, t: &Value) -> Result<Value> {
        java::negate(t)
    }
}
