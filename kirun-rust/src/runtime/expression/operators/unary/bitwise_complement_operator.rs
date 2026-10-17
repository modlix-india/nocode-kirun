//! Port of kirun-js `runtime/expression/operators/unary/BitwiseComplementOperator.ts`, answering as kirun-java's
//! (see `java_semantics`).

use super::unary_operator::UnaryOperator;
use crate::runtime::expression::operators::java_semantics as java;
use crate::{Result, Value};

#[derive(Debug, Default, Clone, Copy)]
pub struct BitwiseComplementOperator;

impl BitwiseComplementOperator {
    pub fn new() -> Self {
        Self
    }
}

impl UnaryOperator for BitwiseComplementOperator {
    fn apply(&self, t: &Value) -> Result<Value> {
        java::complement(t)
    }
}
