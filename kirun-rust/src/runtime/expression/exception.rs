//! Port of kirun-js `runtime/expression/exception/ExpressionEvaluationException.ts`.
//!
//! JS message is `expression + ' : ' + message`, reproduced by `KIRunError::ExpressionEvaluation`.

use crate::KIRunError;

pub fn expression_evaluation_exception(expression: &str, message: &str) -> KIRunError {
    KIRunError::expression_evaluation(expression, message)
}
