//! Port of kirun-js `function/system/loop/*`.

pub mod count_loop;
pub use count_loop::CountLoop;
pub mod for_each_loop;
pub use for_each_loop::ForEachLoop;
pub mod r#break;
pub use r#break::Break;
pub mod range_loop;
pub use range_loop::RangeLoop;

use crate::util::primitive::js_truthy;
use crate::{Map, Shared, Value};

/// JS `statementName && context.getExecutionContext()?.get(statementName)`: has `Break` marked
/// this loop step?
pub(crate) fn break_requested(execution_context: &Shared<Map<Value>>, statement_name: Option<&str>) -> bool {
    statement_name.is_some_and(|n| execution_context.read().get(n).is_some_and(js_truthy))
}

/// JS `if (statementName) context.getExecutionContext()?.delete(statementName)`.
pub(crate) fn clear_break(execution_context: &Shared<Map<Value>>, statement_name: Option<&str>) {
    if let Some(n) = statement_name {
        execution_context.write().shift_remove(n);
    }
}
