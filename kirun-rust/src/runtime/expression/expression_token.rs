//! Port of kirun-js `runtime/expression/ExpressionToken.ts`.
//!
//! JS has a class hierarchy `ExpressionToken` <- `ExpressionTokenValue`, `Expression`;
//! Rust models it as one enum.

use std::fmt;

use crate::runtime::expression::expression_token_value::ExpressionTokenValue;
use crate::runtime::expression::Expression;

#[derive(Debug, Clone, PartialEq)]
pub enum ExpressionToken {
    /// A plain JS `ExpressionToken` (identifier, path or literal text).
    Token(String),
    /// JS `ExpressionTokenValue`.
    Value(ExpressionTokenValue),
    /// A nested JS `Expression`.
    Expression(Box<Expression>),
}

impl ExpressionToken {
    pub fn new(expression: impl Into<String>) -> Self {
        ExpressionToken::Token(expression.into())
    }

    pub fn get_expression(&self) -> &str {
        match self {
            ExpressionToken::Token(s) => s,
            ExpressionToken::Value(v) => v.get_expression(),
            ExpressionToken::Expression(e) => e.get_expression(),
        }
    }
}

impl fmt::Display for ExpressionToken {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            ExpressionToken::Token(s) => f.write_str(s),
            ExpressionToken::Value(v) => write!(f, "{v}"),
            ExpressionToken::Expression(e) => write!(f, "{e}"),
        }
    }
}
