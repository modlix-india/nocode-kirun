//! Port of kirun-js `runtime/expression/operators/unary/ArithmeticUnaryPlusOperator.ts`.

use super::unary_operator::UnaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;
use crate::runtime::expression::operation::Operation;

#[derive(Debug, Default, Clone, Copy)]
pub struct ArithmeticUnaryPlusOperator;

impl ArithmeticUnaryPlusOperator {
    pub fn new() -> Self {
        Self
    }
}

impl UnaryOperator for ArithmeticUnaryPlusOperator {
    fn apply(&self, t: &Value) -> Result<Value> {
                self.null_check(t, &Operation::UNARY_PLUS)?;
        js::find_primitive_number_type(t)?;
        Ok(t.clone())
    }
}
