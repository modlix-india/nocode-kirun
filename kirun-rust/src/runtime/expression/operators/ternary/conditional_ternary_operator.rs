//! Port of kirun-js `runtime/expression/operators/ternary/ConditionalTernaryOperator.ts`, answering
//! as kirun-java's (see `java_semantics`).

use super::ternary_operator::TernaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::operators::java_semantics as java;

#[derive(Debug, Default, Clone, Copy)]
pub struct ConditionalTernaryOperator;

impl ConditionalTernaryOperator {
    pub fn new() -> Self {
        Self
    }
}

impl TernaryOperator for ConditionalTernaryOperator {
    fn apply(&self, t: &Value, u: &Value, v: &Value) -> Result<Value> {
        Ok(if java::ternary_condition(t) { u.clone() } else { v.clone() })
    }
}
