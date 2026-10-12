//! Port of kirun-js `runtime/expression/ExpressionTokenValue.ts`.

use std::borrow::Cow;
use std::fmt;

use crate::util::string::string_formatter::StringFormatter;
use crate::Value;

#[derive(Debug, Clone, PartialEq)]
pub struct ExpressionTokenValue {
    /// Usually an operator's static text (`+`), so it borrows instead of allocating.
    pub expression: Cow<'static, str>,
    pub element: Value,
}

impl ExpressionTokenValue {
    pub fn new(expression: impl Into<Cow<'static, str>>, element: Value) -> Self {
        Self { expression: expression.into(), element }
    }

    pub fn get_expression(&self) -> &str {
        &self.expression
    }

    pub fn get_token_value(&self) -> &Value {
        &self.element
    }

    pub fn get_element(&self) -> &Value {
        &self.element
    }
}

impl fmt::Display for ExpressionTokenValue {
    /// JS `StringFormatter.format('$: $', expression, element)`.
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.write_str(&StringFormatter::format(
            "$: $",
            &[&self.expression, &crate::runtime::expression::js::to_js_string(&self.element)],
        ))
    }
}
