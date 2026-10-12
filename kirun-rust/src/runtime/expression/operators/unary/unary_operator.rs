//! Port of kirun-js `runtime/expression/operators/unary/UnaryOperator.ts`.

use crate::runtime::expression::operation::Operation;
use crate::{Result, Value};
use crate::util::string::string_formatter::StringFormatter;
use crate::KIRunError;

/// JS abstract class `UnaryOperator`.
pub trait UnaryOperator: Send + Sync {
    fn apply(&self, t: &Value) -> Result<Value>;

    /// JS `nullCheck`: errors with `<op> cannot be applied to a null value`.
    fn null_check(&self, t: &Value, op: &Operation) -> Result<()> {
                if t.is_null() {
            return Err(KIRunError::execution(StringFormatter::format(
                "$ cannot be applied to a null value",
                &[op.get_operator_name()],
            )));
        }
        Ok(())
    }
}
