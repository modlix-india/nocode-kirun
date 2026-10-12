//! Port of kirun-js `runtime/expression/ExpressionLexer.ts`.

use std::fmt;

use crate::runtime::expression::exception::expression_evaluation_exception;
use crate::Result;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum TokenType {
    Identifier,
    Number,
    String,
    Operator,
    LeftParen,
    RightParen,
    LeftBracket,
    RightBracket,
    Dot,
    Question,
    Colon,
    Whitespace,
    Eof,
}

impl fmt::Display for TokenType {
    /// JS enum string value (`IDENTIFIER`, `LEFT_PAREN`, ...).
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.write_str(match self {
            TokenType::Identifier => "IDENTIFIER",
            TokenType::Number => "NUMBER",
            TokenType::String => "STRING",
            TokenType::Operator => "OPERATOR",
            TokenType::LeftParen => "LEFT_PAREN",
            TokenType::RightParen => "RIGHT_PAREN",
            TokenType::LeftBracket => "LEFT_BRACKET",
            TokenType::RightBracket => "RIGHT_BRACKET",
            TokenType::Dot => "DOT",
            TokenType::Question => "QUESTION",
            TokenType::Colon => "COLON",
            TokenType::Whitespace => "WHITESPACE",
            TokenType::Eof => "EOF",
        })
    }
}

/// JS lexer `Token`.
#[derive(Debug, Clone, PartialEq)]
pub struct Token {
    pub r#type: TokenType,
    pub value: String,
    pub start_pos: usize,
    pub end_pos: usize,
}

impl Token {
    pub fn new(r#type: TokenType, value: impl Into<String>, start_pos: usize, end_pos: usize) -> Self {
        Self { r#type, value: value.into(), start_pos, end_pos }
    }
}

impl fmt::Display for Token {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "Token({}, \"{}\", {}-{})", self.r#type, self.value, self.start_pos, self.end_pos)
    }
}

pub struct ExpressionLexer {
    pos: usize,
    input: Vec<char>,
}

impl ExpressionLexer {
    pub fn new(expression: &str) -> Self {
        Self { pos: 0, input: expression.chars().collect() }
    }

    pub fn get_position(&self) -> usize {
        self.pos
    }

    pub fn peek(&mut self) -> Result<Option<Token>> {
        let saved_pos = self.pos;
        let token = self.next_token();
        self.pos = saved_pos;
        token
    }

    fn len(&self) -> usize {
        self.input.len()
    }

    fn at(&self, i: usize) -> char {
        self.input[i]
    }

    fn input_string(&self) -> String {
        self.input.iter().collect()
    }

    pub fn next_token(&mut self) -> Result<Option<Token>> {
        if self.pos >= self.len() {
            return Ok(Some(Token::new(TokenType::Eof, "", self.pos, self.pos)));
        }

        while self.pos < self.len() && is_whitespace(self.at(self.pos)) {
            self.pos += 1;
        }

        if self.pos >= self.len() {
            return Ok(Some(Token::new(TokenType::Eof, "", self.pos, self.pos)));
        }

        let start_pos = self.pos;
        let ch = self.at(self.pos);

        if ch == '"' || ch == '\'' {
            return self.read_string_literal(ch, start_pos).map(Some);
        }

        if is_digit(ch) {
            return Ok(Some(self.read_number(start_pos)));
        }

        match ch {
            '(' => {
                self.pos += 1;
                return Ok(Some(Token::new(TokenType::LeftParen, "(", start_pos, self.pos)));
            }
            ')' => {
                self.pos += 1;
                return Ok(Some(Token::new(TokenType::RightParen, ")", start_pos, self.pos)));
            }
            '[' => {
                self.pos += 1;
                return Ok(Some(Token::new(TokenType::LeftBracket, "[", start_pos, self.pos)));
            }
            ']' => {
                self.pos += 1;
                return Ok(Some(Token::new(TokenType::RightBracket, "]", start_pos, self.pos)));
            }
            '.' => {
                if self.pos + 1 < self.len() && self.at(self.pos + 1) == '.' {
                    self.pos += 2;
                    return Ok(Some(Token::new(TokenType::Operator, "..", start_pos, self.pos)));
                }
                self.pos += 1;
                return Ok(Some(Token::new(TokenType::Dot, ".", start_pos, self.pos)));
            }
            '?' => {
                if self.pos + 1 < self.len() && self.at(self.pos + 1) == '?' {
                    self.pos += 2;
                    return Ok(Some(Token::new(TokenType::Operator, "??", start_pos, self.pos)));
                }
                self.pos += 1;
                return Ok(Some(Token::new(TokenType::Question, "?", start_pos, self.pos)));
            }
            ':' => {
                self.pos += 1;
                return Ok(Some(Token::new(TokenType::Colon, ":", start_pos, self.pos)));
            }
            _ => {}
        }

        if let Some(op) = self.read_operator(start_pos) {
            return Ok(Some(op));
        }

        self.read_identifier(start_pos).map(Some)
    }

    fn read_string_literal(&mut self, quote_char: char, start_pos: usize) -> Result<Token> {
        self.pos += 1;
        let mut value = String::new();
        let mut escaped = false;

        while self.pos < self.len() {
            let ch = self.at(self.pos);

            if escaped {
                value.push(ch);
                escaped = false;
                self.pos += 1;
                continue;
            }

            if ch == '\\' {
                escaped = true;
                value.push(ch);
                self.pos += 1;
                continue;
            }

            if ch == quote_char {
                self.pos += 1;
                let end_pos = self.pos;
                return Ok(Token::new(
                    TokenType::String,
                    format!("{quote_char}{value}{quote_char}"),
                    start_pos,
                    end_pos,
                ));
            }

            value.push(ch);
            self.pos += 1;
        }

        Err(expression_evaluation_exception(
            &self.input_string(),
            &format!("Missing string ending marker {quote_char}"),
        ))
    }

    fn read_number(&mut self, start_pos: usize) -> Token {
        let mut value = String::new();
        let mut has_decimal = false;

        while self.pos < self.len() {
            let ch = self.at(self.pos);
            if is_digit(ch) {
                value.push(ch);
                self.pos += 1;
            } else if ch == '.' && !has_decimal && self.pos + 1 < self.len() && is_digit(self.at(self.pos + 1)) {
                value.push(ch);
                has_decimal = true;
                self.pos += 1;
            } else {
                break;
            }
        }

        Token::new(TokenType::Number, value, start_pos, self.pos)
    }

    fn read_operator(&mut self, start_pos: usize) -> Option<Token> {
        const OPERATORS: [&str; 20] = [
            ">>>", "<<", ">>", "<=", ">=", "!=", "==", "//", "+", "-", "*", "/", "%", "=", "<", ">", "&", "|", "^",
            "~",
        ];

        for op in OPERATORS {
            let n = op.chars().count();
            if self.pos + n <= self.len() {
                let candidate: String = self.input[self.pos..self.pos + n].iter().collect();
                if candidate == op {
                    self.pos += n;
                    return Some(Token::new(TokenType::Operator, op, start_pos, self.pos));
                }
            }
        }
        None
    }

    fn read_identifier(&mut self, start_pos: usize) -> Result<Token> {
        let mut value = String::new();

        while self.pos < self.len() {
            let ch = self.at(self.pos);

            if ch == '[' {
                if self.is_static_bracket_content() {
                    let content = self.read_static_bracket_content();
                    value.push_str(&content);
                    continue;
                } else {
                    break;
                }
            }

            if !is_identifier_char(ch) {
                break;
            }

            value.push(ch);
            self.pos += 1;
        }

        if value.is_empty() {
            let c = if self.pos < self.len() { self.at(self.pos).to_string() } else { "undefined".to_string() };
            return Err(expression_evaluation_exception(
                &self.input_string(),
                &format!("Unexpected character: {c}"),
            ));
        }

        let lower = value.to_lowercase();
        if lower == "and" || lower == "or" || lower == "not" {
            return Ok(Token::new(TokenType::Operator, lower, start_pos, self.pos));
        }

        Ok(Token::new(TokenType::Identifier, value, start_pos, self.pos))
    }

    fn is_static_bracket_content(&self) -> bool {
        let mut peek_pos = self.pos + 1;

        while peek_pos < self.len() && is_whitespace(self.at(peek_pos)) {
            peek_pos += 1;
        }

        if peek_pos >= self.len() {
            return false;
        }

        let first_char = self.at(peek_pos);

        if first_char == '"' || first_char == '\'' {
            return true;
        }

        if is_digit(first_char)
            || (first_char == '-' && peek_pos + 1 < self.len() && is_digit(self.at(peek_pos + 1)))
        {
            let mut num_end = peek_pos;
            if self.at(num_end) == '-' {
                num_end += 1;
            }
            while num_end < self.len() && (is_digit(self.at(num_end)) || self.at(num_end) == '.') {
                if self.at(num_end) == '.' && num_end + 1 < self.len() && self.at(num_end + 1) == '.' {
                    return true;
                }
                num_end += 1;
            }
            while num_end < self.len() && is_whitespace(self.at(num_end)) {
                num_end += 1;
            }
            if num_end < self.len()
                && (self.at(num_end) == ']'
                    || (self.at(num_end) == '.' && num_end + 1 < self.len() && self.at(num_end + 1) == '.'))
            {
                return true;
            }
            return false;
        }

        false
    }

    fn read_static_bracket_content(&mut self) -> String {
        let mut content = String::from("[");
        self.pos += 1;

        let mut bracket_count = 1;
        while self.pos < self.len() && bracket_count > 0 {
            let c = self.at(self.pos);
            content.push(c);
            self.pos += 1;

            if c == '[' {
                bracket_count += 1;
            } else if c == ']' {
                bracket_count -= 1;
            } else if c == '"' || c == '\'' {
                let quote_char = c;
                while self.pos < self.len() {
                    let next_char = self.at(self.pos);
                    content.push(next_char);
                    self.pos += 1;
                    if next_char == quote_char && self.pos > 1 && self.at(self.pos - 2) != '\\' {
                        break;
                    }
                }
            }
        }

        content
    }
}

fn is_digit(c: char) -> bool {
    c.is_ascii_digit()
}

fn is_whitespace(c: char) -> bool {
    c == ' ' || c == '\t' || c == '\n' || c == '\r'
}

fn is_identifier_char(c: char) -> bool {
    c.is_ascii_alphanumeric() || c == '_'
}
