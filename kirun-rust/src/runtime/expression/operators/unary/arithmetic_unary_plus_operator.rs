//! Port of kirun-js `runtime/expression/operators/unary/ArithmeticUnaryPlusOperator.ts`, answering as kirun-java's
//! (see `java_semantics`).

use super::unary_operator::UnaryOperator;
use crate::runtime::expression::operators::java_semantics as java;
use crate::{Result, Value};

#[derive(Debug, Default, Clone, Copy)]
pub struct ArithmeticUnaryPlusOperator;

impl ArithmeticUnaryPlusOperator {
    pub fn new() -> Self {
        Self
    }
}

impl UnaryOperator for ArithmeticUnaryPlusOperator {
    fn apply(&self, t: &Value) -> Result<Value> {
        java::plus(t)
    }
}
