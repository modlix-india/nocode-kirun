//! Port of kirun-js `dsl/lexer/DSLLexer.ts`: DSL text → tokens. Block comments are kept as
//! COMMENT tokens (they attach to statements); `//` is not a comment (integer division).

use crate::dsl::lexer::dsl_token::{DSLToken, DSLTokenType, SourceLocation};
use crate::dsl::lexer::keywords::is_keyword;
use crate::dsl::lexer::lexer_error::lexer_error;
use crate::Result;

pub struct DSLLexer {
    input: String,
    chars: Vec<char>,
    pos: usize,
    line: usize,
    column: usize,
    tokens: Vec<DSLToken>,
}

const TWO_CHAR_OPS: [&str; 14] = ["!=", "==", "<=", ">=", "&&", "||", "??", "?.", "++", "--", "+=", "-=", "*=", "/="];

impl DSLLexer {
    pub fn new(input: &str) -> Self {
        Self { input: input.to_string(), chars: input.chars().collect(), pos: 0, line: 1, column: 1, tokens: Vec::new() }
    }

    pub fn tokenize(mut self) -> Result<Vec<DSLToken>> {
        while self.pos < self.chars.len() {
            self.skip_whitespace();
            if self.pos >= self.chars.len() {
                break;
            }
            let token = self.read_token()?;
            if token.r#type != DSLTokenType::Whitespace {
                self.tokens.push(token);
            }
        }
        self.tokens.push(DSLToken::new(
            DSLTokenType::Eof,
            "",
            SourceLocation::new(self.line, self.column, self.pos, self.pos),
        ));
        Ok(self.tokens)
    }

    fn skip_whitespace(&mut self) {
        while self.pos < self.chars.len() {
            match self.chars[self.pos] {
                ' ' | '\t' | '\r' => {
                    self.advance();
                }
                '\n' => {
                    self.advance();
                    self.line += 1;
                    self.column = 1;
                }
                _ => break,
            }
        }
    }

    fn loc(&self, line: usize, column: usize, start: usize) -> SourceLocation {
        SourceLocation::new(line, column, start, self.pos)
    }

    fn read_token(&mut self) -> Result<DSLToken> {
        let (start, line, column) = (self.pos, self.line, self.column);
        let ch = self.peek();

        if ch == Some('/') && self.peek_ahead(1) == Some('*') {
            return self.read_block_comment();
        }
        if ch == Some('"') || ch == Some('\'') {
            return self.read_string_literal(ch.unwrap());
        }
        if ch == Some('`') {
            return Ok(self.read_backtick_string());
        }
        if ch.is_some_and(|c| c.is_ascii_digit()) || (ch == Some('-') && self.peek_ahead(1).is_some_and(|c| c.is_ascii_digit())) {
            return Ok(self.read_number());
        }
        if ch.is_some_and(is_identifier_start) {
            return Ok(self.read_identifier());
        }
        if let Some(t) = self.read_single_char_token() {
            return Ok(t);
        }
        Err(lexer_error(
            &format!("Unexpected character '{}'", ch.unwrap_or(' ')),
            self.loc(line, column, start),
            &self.input,
        ))
    }

    fn read_single_char_token(&mut self) -> Option<DSLToken> {
        let (start, line, column) = (self.pos, self.line, self.column);
        let ch = self.peek()?;

        if let Some(next) = self.peek_ahead(1) {
            let two: String = [ch, next].iter().collect();
            if TWO_CHAR_OPS.contains(&two.as_str()) {
                self.advance();
                self.advance();
                return Some(DSLToken::new(DSLTokenType::Operator, two, self.loc(line, column, start)));
            }
        }

        let r#type = match ch {
            ':' => DSLTokenType::Colon,
            ',' => DSLTokenType::Comma,
            '.' => DSLTokenType::Dot,
            '=' => DSLTokenType::Equals,
            '(' => DSLTokenType::LeftParen,
            ')' => DSLTokenType::RightParen,
            '{' => DSLTokenType::LeftBrace,
            '}' => DSLTokenType::RightBrace,
            '[' => DSLTokenType::LeftBracket,
            ']' => DSLTokenType::RightBracket,
            '+' | '-' | '*' | '/' | '%' | '<' | '>' | '!' | '?' | '&' | '|' | '@' | '^' | '~' | '#' | '\\' => {
                DSLTokenType::Operator
            }
            _ => return None,
        };
        self.advance();
        Some(DSLToken::new(r#type, ch.to_string(), self.loc(line, column, start)))
    }

    fn read_identifier(&mut self) -> DSLToken {
        let (start, line, column) = (self.pos, self.line, self.column);
        let mut value = String::new();
        while let Some(c) = self.peek().filter(|c| is_identifier_start(*c) || c.is_ascii_digit()) {
            value.push(c);
            self.advance();
        }
        let location = self.loc(line, column, start);
        // true/false/null are keywords, so the BOOLEAN and NULL branches below never fire
        // (kept for parity with kirun-js).
        let r#type = if is_keyword(&value) {
            DSLTokenType::Keyword
        } else if value == "true" || value == "false" {
            DSLTokenType::Boolean
        } else if value == "null" {
            DSLTokenType::Null
        } else {
            DSLTokenType::Identifier
        };
        DSLToken::new(r#type, value, location)
    }

    fn read_number(&mut self) -> DSLToken {
        let (start, line, column) = (self.pos, self.line, self.column);
        let mut value = String::new();
        if self.peek() == Some('-') {
            value.push('-');
            self.advance();
        }
        while let Some(c) = self.peek().filter(char::is_ascii_digit) {
            value.push(c);
            self.advance();
        }
        if self.peek() == Some('.') && self.peek_ahead(1).is_some_and(|c| c.is_ascii_digit()) {
            value.push('.');
            self.advance();
            while let Some(c) = self.peek().filter(char::is_ascii_digit) {
                value.push(c);
                self.advance();
            }
        }
        DSLToken::new(DSLTokenType::Number, value, self.loc(line, column, start))
    }

    fn read_string_literal(&mut self, quote: char) -> Result<DSLToken> {
        let (start, line, column) = (self.pos, self.line, self.column);
        let mut value = String::from(quote);
        self.advance();
        let mut closed = false;
        while let Some(c) = self.peek() {
            if c == quote {
                value.push(c);
                self.advance();
                closed = true;
                break;
            }
            if c == '\\' {
                value.push(c);
                self.advance();
                if let Some(e) = self.peek() {
                    value.push(e);
                    self.advance();
                }
                continue;
            }
            value.push(c);
            self.advance();
        }
        // JS checks `value.endsWith(quote)`: an unclosed string ending in an escaped quote passes.
        if !closed && !(value.len() > 1 && value.ends_with(quote)) {
            return Err(lexer_error("Unterminated string literal", self.loc(line, column, start), &self.input));
        }
        Ok(DSLToken::new(DSLTokenType::String, value, self.loc(line, column, start)))
    }

    /// Only \` and \\ are escapes; other backslashes are kept.
    fn read_backtick_string(&mut self) -> DSLToken {
        let (start, line, column) = (self.pos, self.line, self.column);
        let mut value = String::new();
        self.advance();
        while let Some(c) = self.peek() {
            if c == '`' {
                self.advance();
                break;
            }
            if c == '\\' {
                let next = self.peek_ahead(1);
                if next == Some('`') || next == Some('\\') {
                    self.advance();
                    value.push(next.unwrap());
                    self.advance();
                    continue;
                }
                value.push(c);
                self.advance();
                continue;
            }
            value.push(c);
            self.advance();
        }
        DSLToken::new(DSLTokenType::BacktickString, value, self.loc(line, column, start))
    }

    fn read_block_comment(&mut self) -> Result<DSLToken> {
        let (start, line, column) = (self.pos, self.line, self.column);
        let mut value = String::from("/*");
        self.advance();
        self.advance();
        let mut closed = false;
        while self.pos < self.chars.len() {
            if self.peek() == Some('*') && self.peek_ahead(1) == Some('/') {
                value.push_str("*/");
                self.advance();
                self.advance();
                closed = true;
                break;
            }
            let c = self.chars[self.pos];
            self.advance();
            value.push(c);
            if c == '\n' {
                self.line += 1;
                self.column = 1;
            }
        }
        if !closed && !(value.len() >= 4 && value.ends_with("*/")) {
            return Err(lexer_error("Unterminated block comment", self.loc(line, column, start), &self.input));
        }
        Ok(DSLToken::new(DSLTokenType::Comment, value, self.loc(line, column, start)))
    }

    fn peek(&self) -> Option<char> {
        self.chars.get(self.pos).copied()
    }

    fn peek_ahead(&self, n: usize) -> Option<char> {
        self.chars.get(self.pos + n).copied()
    }

    fn advance(&mut self) {
        self.pos += 1;
        self.column += 1;
    }
}

fn is_identifier_start(c: char) -> bool {
    c.is_ascii_alphabetic() || c == '_'
}
