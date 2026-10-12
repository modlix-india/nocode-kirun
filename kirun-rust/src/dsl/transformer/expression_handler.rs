//! Port of kirun-js `dsl/transformer/ExpressionHandler.ts`.

use crate::runtime::expression::Expression;
use crate::{Result, Value};

pub struct ExpressionHandler;

impl ExpressionHandler {
    pub fn parse(expression_text: &str) -> Result<Expression> {
        Expression::new(expression_text)
    }

    pub fn validate(expression_text: &str) -> bool {
        Expression::new(expression_text).is_ok()
    }

    /// `{ isExpression: true, value: "<string>" }`
    pub fn is_expression(value: &Value) -> bool {
        value.get("isExpression") == Some(&Value::Bool(true)) && value.get("value").is_some_and(Value::is_string)
    }

    pub fn extract_expression_text(value: &Value) -> Option<&str> {
        if Self::is_expression(value) {
            value["value"].as_str()
        } else {
            None
        }
    }
}
