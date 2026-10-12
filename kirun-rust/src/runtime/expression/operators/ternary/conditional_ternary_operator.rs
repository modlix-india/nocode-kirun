//! Port of kirun-js `runtime/expression/operators/ternary/ConditionalTernaryOperator.ts`.

use super::ternary_operator::TernaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;

#[derive(Debug, Default, Clone, Copy)]
pub struct ConditionalTernaryOperator;

impl ConditionalTernaryOperator {
    pub fn new() -> Self {
        Self
    }
}

impl TernaryOperator for ConditionalTernaryOperator {
    fn apply(&self, t: &Value, u: &Value, v: &Value) -> Result<Value> {
                Ok(if js::truthy(t) { u.clone() } else { v.clone() })
    }
}
