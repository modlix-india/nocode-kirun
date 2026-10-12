//! Port of kirun-js `dsl/lexer/LexerError.ts`: the message carries the location and a caret
//! under the offending column.

use crate::dsl::lexer::dsl_token::SourceLocation;
use crate::KIRunError;

pub fn lexer_error(message: &str, location: SourceLocation, input: &str) -> KIRunError {
    let mut msg = format!("{message} at {location}");
    let lines: Vec<&str> = input.split('\n').collect();
    if location.line >= 1 && location.line <= lines.len() {
        let pointer = format!("{}^", " ".repeat(location.column.saturating_sub(1)));
        msg.push_str(&format!("\n\n{}\n{pointer}", lines[location.line - 1]));
    }
    KIRunError::runtime(msg)
}
