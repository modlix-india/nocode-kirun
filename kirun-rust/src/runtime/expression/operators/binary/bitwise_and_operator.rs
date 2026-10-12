//! Port of kirun-js `runtime/expression/operators/binary/BitwiseAndOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;
use crate::runtime::expression::operation::Operation;

#[derive(Debug, Default, Clone, Copy)]
pub struct BitwiseAndOperator;

impl BitwiseAndOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for BitwiseAndOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                self.null_check(t, u, &Operation::BITWISE_AND)?;
        Ok(js::num((js::to_int32(js::to_number(t)) & js::to_int32(js::to_number(u))) as f64))
    }
}
