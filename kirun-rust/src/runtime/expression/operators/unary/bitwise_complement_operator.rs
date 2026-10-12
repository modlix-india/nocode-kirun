//! Port of kirun-js `runtime/expression/operators/unary/BitwiseComplementOperator.ts`.

use super::unary_operator::UnaryOperator;
use crate::{Result, Value};
use crate::runtime::expression::js;
use crate::runtime::expression::operation::Operation;
use crate::util::string::string_formatter::StringFormatter;
use crate::KIRunError;

#[derive(Debug, Default, Clone, Copy)]
pub struct BitwiseComplementOperator;

impl BitwiseComplementOperator {
    pub fn new() -> Self {
        Self
    }
}

impl UnaryOperator for BitwiseComplementOperator {
    fn apply(&self, t: &Value) -> Result<Value> {
                self.null_check(t, &Operation::UNARY_BITWISE_COMPLEMENT)?;
        let is_integer = js::find_primitive_number_type(t)?;
        if !is_integer {
            return Err(KIRunError::execution(StringFormatter::format(
                "Unable to apply bitwise operator on $",
                &[&js::to_js_string(t)],
            )));
        }
        Ok(js::num(!js::to_int32(js::to_number(t)) as f64))
    }
}
