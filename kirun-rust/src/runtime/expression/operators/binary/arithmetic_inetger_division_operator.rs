//! Port of kirun-js `runtime/expression/operators/binary/ArithmeticInetgerDivisionOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;
use crate::runtime::expression::operation::Operation;

#[derive(Debug, Default, Clone, Copy)]
pub struct ArithmeticInetgerDivisionOperator;

impl ArithmeticInetgerDivisionOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for ArithmeticInetgerDivisionOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                self.null_check(t, u, &Operation::DIVISION)?;
        Ok(js::num((js::to_number(t) / js::to_number(u)).floor()))
    }
}
