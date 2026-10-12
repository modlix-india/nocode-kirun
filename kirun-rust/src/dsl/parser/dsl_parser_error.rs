//! Port of kirun-js `dsl/parser/DSLParserError.ts`.

use crate::dsl::lexer::{DSLToken, SourceLocation};
use crate::KIRunError;

pub fn parser_error(
    message: &str,
    location: Option<SourceLocation>,
    expected: &[String],
    actual: Option<&DSLToken>,
) -> KIRunError {
    let mut msg = match location {
        Some(l) => format!("{message} at {l}"),
        None => message.to_string(),
    };
    if !expected.is_empty() {
        msg.push_str(&format!("\nExpected: {}", expected.join(", ")));
    }
    if let Some(t) = actual {
        msg.push_str(&format!("\nActual: {} ({})", t.r#type.name(), t.value));
    }
    KIRunError::runtime(msg)
}
