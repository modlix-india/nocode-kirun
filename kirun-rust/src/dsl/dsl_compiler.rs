//! Port of kirun-js `dsl/DSLCompiler.ts`.

use std::sync::LazyLock;

use regex::Regex;

use crate::dsl::lexer::DSLLexer;
use crate::dsl::parser::DSLParser;
use crate::dsl::transformer::{ASTToJSONTransformer, JSONToTextTransformer};
use crate::{KIRunError, Result, Value};

#[derive(Debug, Clone, PartialEq)]
pub struct ValidationResult {
    pub valid: bool,
    pub errors: Vec<FormattedError>,
}

#[derive(Debug, Clone, PartialEq)]
pub struct FormattedError {
    pub message: String,
    pub line: Option<usize>,
    pub column: Option<usize>,
}

/// Lexer and parser messages carry `at Line L, Column C (pos a-b)`.
static LOCATION: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"at Line (\d+), Column (\d+) \(pos").unwrap());

pub struct DSLCompiler;

impl DSLCompiler {
    /// DSL text → FunctionDefinition JSON.
    pub fn compile(text: &str) -> Result<Value> {
        let tokens = DSLLexer::new(text).tokenize()?;
        let mut ast = DSLParser::new(tokens, text).parse()?;
        let transformer = ASTToJSONTransformer::new();
        transformer.flatten_nested_blocks(&mut ast);
        transformer.transform(&ast)
    }

    /// FunctionDefinition JSON → DSL text.
    pub fn decompile(json: &Value) -> Result<String> {
        JSONToTextTransformer::new().transform(json)
    }

    pub fn validate(text: &str) -> ValidationResult {
        match Self::compile(text) {
            Ok(_) => ValidationResult { valid: true, errors: Vec::new() },
            Err(e) => ValidationResult { valid: false, errors: vec![Self::format_error(&e)] },
        }
    }

    /// Parse and regenerate with consistent formatting.
    pub fn format(text: &str) -> Result<String> {
        Self::decompile(&Self::compile(text)?)
    }

    fn format_error(error: &KIRunError) -> FormattedError {
        let message = error.to_string();
        let loc = LOCATION.captures(&message).map(|c| (c[1].parse().ok(), c[2].parse().ok()));
        FormattedError {
            line: loc.and_then(|l| l.0),
            column: loc.and_then(|l| l.1),
            message: if message.is_empty() { "Unknown error".into() } else { message },
        }
    }
}
