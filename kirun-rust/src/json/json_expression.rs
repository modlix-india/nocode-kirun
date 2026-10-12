//! Port of kirun-js `json/JsonExpression.ts`.

#[derive(Debug, Clone, PartialEq)]
pub struct JsonExpression {
    expression: String,
}

impl JsonExpression {
    pub fn new(expression: impl Into<String>) -> Self {
        Self { expression: expression.into() }
    }

    pub fn get_expression(&self) -> &str {
        &self.expression
    }
}
