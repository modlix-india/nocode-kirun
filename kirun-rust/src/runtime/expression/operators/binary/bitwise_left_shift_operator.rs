//! Port of kirun-js `runtime/expression/operators/binary/BitwiseLeftShiftOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;
use crate::runtime::expression::operation::Operation;

#[derive(Debug, Default, Clone, Copy)]
pub struct BitwiseLeftShiftOperator;

impl BitwiseLeftShiftOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for BitwiseLeftShiftOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                self.null_check(t, u, &Operation::BITWISE_LEFT_SHIFT)?;
        Ok(js::num(js::to_int32(js::to_number(t)).wrapping_shl(js::to_uint32(js::to_number(u)) & 31) as f64))
    }
}
