//! Port of kirun-js `dsl/lexer/DSLToken.ts`.

use std::fmt;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum DSLTokenType {
    Keyword,
    Identifier,
    Number,
    String,
    /// `...` for expressions
    BacktickString,
    Boolean,
    Null,
    Colon,
    Comma,
    Dot,
    Equals,
    /// + - * / % < > ! ? & | @ ^ ~ # \ and two-character operators
    Operator,
    LeftParen,
    RightParen,
    LeftBrace,
    RightBrace,
    LeftBracket,
    RightBracket,
    Newline,
    Comment,
    Whitespace,
    Eof,
}

impl DSLTokenType {
    /// JS enum value (`LEFT_PAREN`), used in parser error messages.
    pub fn name(&self) -> &'static str {
        match self {
            DSLTokenType::Keyword => "KEYWORD",
            DSLTokenType::Identifier => "IDENTIFIER",
            DSLTokenType::Number => "NUMBER",
            DSLTokenType::String => "STRING",
            DSLTokenType::BacktickString => "BACKTICK_STRING",
            DSLTokenType::Boolean => "BOOLEAN",
            DSLTokenType::Null => "NULL",
            DSLTokenType::Colon => "COLON",
            DSLTokenType::Comma => "COMMA",
            DSLTokenType::Dot => "DOT",
            DSLTokenType::Equals => "EQUALS",
            DSLTokenType::Operator => "OPERATOR",
            DSLTokenType::LeftParen => "LEFT_PAREN",
            DSLTokenType::RightParen => "RIGHT_PAREN",
            DSLTokenType::LeftBrace => "LEFT_BRACE",
            DSLTokenType::RightBrace => "RIGHT_BRACE",
            DSLTokenType::LeftBracket => "LEFT_BRACKET",
            DSLTokenType::RightBracket => "RIGHT_BRACKET",
            DSLTokenType::Newline => "NEWLINE",
            DSLTokenType::Comment => "COMMENT",
            DSLTokenType::Whitespace => "WHITESPACE",
            DSLTokenType::Eof => "EOF",
        }
    }
}

/// Position of a token. `start_pos`/`end_pos` index the input's chars.
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct SourceLocation {
    pub line: usize,
    pub column: usize,
    pub start_pos: usize,
    pub end_pos: usize,
}

impl SourceLocation {
    pub fn new(line: usize, column: usize, start_pos: usize, end_pos: usize) -> Self {
        Self { line, column, start_pos, end_pos }
    }
}

impl fmt::Display for SourceLocation {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "Line {}, Column {} (pos {}-{})", self.line, self.column, self.start_pos, self.end_pos)
    }
}

#[derive(Debug, Clone, PartialEq)]
pub struct DSLToken {
    pub r#type: DSLTokenType,
    pub value: String,
    pub location: SourceLocation,
}

impl DSLToken {
    pub fn new(r#type: DSLTokenType, value: impl Into<String>, location: SourceLocation) -> Self {
        Self { r#type, value: value.into(), location }
    }

    pub fn is(&self, r#type: DSLTokenType, value: Option<&str>) -> bool {
        self.r#type == r#type && value.is_none_or(|v| self.value == v)
    }
}

impl fmt::Display for DSLToken {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}({}) at {}", self.r#type.name(), serde_json::Value::String(self.value.clone()), self.location)
    }
}
