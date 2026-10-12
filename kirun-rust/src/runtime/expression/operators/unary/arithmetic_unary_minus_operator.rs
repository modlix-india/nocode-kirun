//! Port of kirun-js `runtime/expression/operators/unary/ArithmeticUnaryMinusOperator.ts`.

use super::unary_operator::UnaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;
use crate::runtime::expression::operation::Operation;

#[derive(Debug, Default, Clone, Copy)]
pub struct ArithmeticUnaryMinusOperator;

impl ArithmeticUnaryMinusOperator {
    pub fn new() -> Self {
        Self
    }
}

impl UnaryOperator for ArithmeticUnaryMinusOperator {
    fn apply(&self, t: &Value) -> Result<Value> {
                self.null_check(t, &Operation::UNARY_MINUS)?;
        js::find_primitive_number_type(t)?;
        Ok(js::num(-js::to_number(t)))
    }
}
