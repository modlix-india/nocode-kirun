//! Port of kirun-js `runtime/expression/operators/binary/BinaryOperator.ts`.

use crate::runtime::expression::operation::Operation;
use crate::{Result, Value};
use crate::util::string::string_formatter::StringFormatter;
use crate::KIRunError;

/// JS abstract class `BinaryOperator`.
pub trait BinaryOperator: Send + Sync {
    fn apply(&self, t: &Value, u: &Value) -> Result<Value>;

    /// JS `nullCheck`: errors with `<op> cannot be applied to a null value`.
    fn null_check(&self, t: &Value, u: &Value, op: &Operation) -> Result<()> {
                if t.is_null() || u.is_null() {
            return Err(KIRunError::execution(StringFormatter::format(
                "$ cannot be applied to a null value",
                &[op.get_operator_name()],
            )));
        }
        Ok(())
    }
}
