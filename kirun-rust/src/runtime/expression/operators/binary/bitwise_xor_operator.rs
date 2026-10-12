//! Port of kirun-js `runtime/expression/operators/binary/BitwiseXorOperator.ts`.

use super::binary_operator::BinaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;
use crate::runtime::expression::operation::Operation;

#[derive(Debug, Default, Clone, Copy)]
pub struct BitwiseXorOperator;

impl BitwiseXorOperator {
    pub fn new() -> Self {
        Self
    }
}

impl BinaryOperator for BitwiseXorOperator {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value> {
                self.null_check(t, u, &Operation::BITWISE_XOR)?;
        Ok(js::num((js::to_int32(js::to_number(t)) ^ js::to_int32(js::to_number(u))) as f64))
    }
}
