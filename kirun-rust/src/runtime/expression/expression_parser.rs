//! Port of kirun-js `runtime/expression/ExpressionParser.ts` (recursive descent parser).

use crate::runtime::expression::exception::expression_evaluation_exception;
use crate::runtime::expression::expression_lexer::{ExpressionLexer, Token, TokenType};
use crate::runtime::expression::expression_token::ExpressionToken;
use crate::runtime::expression::expression_token_value::ExpressionTokenValue;
use crate::runtime::expression::operation::Operation;
use crate::runtime::expression::Expression;
use crate::{KIRunError, Result};

pub struct ExpressionParser {
    lexer: ExpressionLexer,
    current_token: Option<Token>,
    previous_token_value: Option<Token>,
    original_expression: String,
    parse_depth: usize,
    /// JS throws from the constructor when the first token cannot be read; Rust reports it
    /// from `parse`.
    init_error: Option<KIRunError>,
}

fn leaf_expr(e: Expression) -> ExpressionToken {
    ExpressionToken::Expression(Box::new(e))
}

impl ExpressionParser {
    pub fn new(expression: &str) -> Self {
        let mut lexer = ExpressionLexer::new(expression);
        let (current_token, init_error) = match lexer.next_token() {
            Ok(t) => (t, None),
            Err(e) => (None, Some(e)),
        };
        Self {
            lexer,
            current_token,
            previous_token_value: None,
            original_expression: expression.to_string(),
            parse_depth: 0,
            init_error,
        }
    }

    fn create_parser_error(&self, message: &str) -> KIRunError {
        let position = self.lexer.get_position();
        expression_evaluation_exception(&self.original_expression, &format!("{message} at position {position}"))
    }

    pub fn parse(&mut self) -> Result<Expression> {
        if let Some(e) = self.init_error.take() {
            return Err(e);
        }
        if self.current_token.is_none() {
            return Err(self.create_parser_error("Empty expression"));
        }

        let result = (|| -> Result<Expression> {
            let expr = self.parse_expression()?;
            if let Some(t) = &self.current_token {
                if t.r#type != TokenType::Eof {
                    let msg = format!("Unexpected token \"{}\" - expected end of expression", t.value);
                    return Err(self.create_parser_error(&msg));
                }
            }
            Ok(expr)
        })();

        match result {
            Err(KIRunError::ExpressionEvaluation { expression, message }) => {
                let full = format!("{expression} : {message}");
                if full.contains("Parser Error:") {
                    Err(KIRunError::ExpressionEvaluation { expression, message })
                } else {
                    Err(self.create_parser_error(&full))
                }
            }
            other => other,
        }
    }

    fn parse_expression(&mut self) -> Result<Expression> {
        self.parse_ternary()
    }

    fn parse_ternary(&mut self) -> Result<Expression> {
        let expr = self.parse_logical_or()?;

        if self.match_token(TokenType::Question)? {
            let true_expr = self.parse_ternary()?;
            self.expect_token(TokenType::Colon)?;
            let false_expr = self.parse_ternary()?;
            return Ok(Expression::create_ternary(leaf_expr(expr), leaf_expr(true_expr), leaf_expr(false_expr)));
        }

        Ok(expr)
    }

    fn binary(l: Expression, r: Expression, op: Operation) -> Expression {
        Expression::from_parts(Some(leaf_expr(l)), Some(leaf_expr(r)), Some(op))
    }

    fn parse_logical_or(&mut self) -> Result<Expression> {
        let mut expr = self.parse_nullish_coalescing()?;
        while self.match_operator("or")? {
            let right = self.parse_nullish_coalescing()?;
            expr = Self::binary(expr, right, Operation::OR);
        }
        Ok(expr)
    }

    fn parse_nullish_coalescing(&mut self) -> Result<Expression> {
        let mut expr = self.parse_logical_and()?;
        while self.match_operator("??")? {
            let right = self.parse_logical_and()?;
            expr = Self::binary(expr, right, Operation::NULLISH_COALESCING_OPERATOR);
        }
        Ok(expr)
    }

    fn parse_logical_and(&mut self) -> Result<Expression> {
        let mut expr = self.parse_logical_not()?;
        while self.match_operator("and")? {
            let right = self.parse_logical_not()?;
            expr = Self::binary(expr, right, Operation::AND);
        }
        Ok(expr)
    }

    fn parse_logical_not(&mut self) -> Result<Expression> {
        if self.match_operator("not")? {
            let expr = self.parse_logical_not()?;
            return Ok(Expression::from_parts(Some(leaf_expr(expr)), None, Some(Operation::UNARY_LOGICAL_NOT)));
        }
        self.parse_comparison()
    }

    fn parse_comparison(&mut self) -> Result<Expression> {
        let mut expr = self.parse_bitwise_or()?;
        loop {
            let op = if self.match_operator("<")? {
                Operation::LESS_THAN
            } else if self.match_operator("<=")? {
                Operation::LESS_THAN_EQUAL
            } else if self.match_operator(">")? {
                Operation::GREATER_THAN
            } else if self.match_operator(">=")? {
                Operation::GREATER_THAN_EQUAL
            } else if self.match_operator("=")? {
                Operation::EQUAL
            } else if self.match_operator("!=")? {
                Operation::NOT_EQUAL
            } else {
                break;
            };
            let right = self.parse_bitwise_or()?;
            expr = Self::binary(expr, right, op);
        }
        Ok(expr)
    }

    fn parse_bitwise_or(&mut self) -> Result<Expression> {
        let mut expr = self.parse_bitwise_xor()?;
        while self.match_operator("|")? {
            let right = self.parse_bitwise_xor()?;
            expr = Self::binary(expr, right, Operation::BITWISE_OR);
        }
        Ok(expr)
    }

    fn parse_bitwise_xor(&mut self) -> Result<Expression> {
        let mut expr = self.parse_bitwise_and()?;
        while self.match_operator("^")? {
            let right = self.parse_bitwise_and()?;
            expr = Self::binary(expr, right, Operation::BITWISE_XOR);
        }
        Ok(expr)
    }

    fn parse_bitwise_and(&mut self) -> Result<Expression> {
        let mut expr = self.parse_shift()?;
        while self.match_operator("&")? {
            let right = self.parse_shift()?;
            expr = Self::binary(expr, right, Operation::BITWISE_AND);
        }
        Ok(expr)
    }

    fn parse_shift(&mut self) -> Result<Expression> {
        let mut expr = self.parse_additive()?;
        loop {
            let op = if self.match_operator("<<")? {
                Operation::BITWISE_LEFT_SHIFT
            } else if self.match_operator(">>")? {
                Operation::BITWISE_RIGHT_SHIFT
            } else if self.match_operator(">>>")? {
                Operation::BITWISE_UNSIGNED_RIGHT_SHIFT
            } else {
                break;
            };
            let right = self.parse_additive()?;
            expr = Self::binary(expr, right, op);
        }
        Ok(expr)
    }

    fn parse_additive(&mut self) -> Result<Expression> {
        let mut expr = self.parse_multiplicative()?;
        loop {
            let op = if self.match_operator("+")? {
                Operation::ADDITION
            } else if self.match_operator("-")? {
                Operation::SUBTRACTION
            } else {
                break;
            };
            let right = self.parse_multiplicative()?;
            expr = Self::binary(expr, right, op);
        }
        Ok(expr)
    }

    /// Right-associative, as in JS (`12*13*14/7 = 12*(13*(14/7))`).
    fn parse_multiplicative(&mut self) -> Result<Expression> {
        let expr = self.parse_unary()?;

        let op = if self.match_operator("*")? {
            Operation::MULTIPLICATION
        } else if self.match_operator("/")? {
            Operation::DIVISION
        } else if self.match_operator("//")? {
            Operation::INTEGER_DIVISION
        } else if self.match_operator("%")? {
            Operation::MOD
        } else {
            return Ok(expr);
        };
        let right = self.parse_multiplicative()?;
        Ok(Self::binary(expr, right, op))
    }

    fn parse_unary(&mut self) -> Result<Expression> {
        let op = if self.match_operator("+")? {
            Operation::UNARY_PLUS
        } else if self.match_operator("-")? {
            Operation::UNARY_MINUS
        } else if self.match_operator("~")? {
            Operation::UNARY_BITWISE_COMPLEMENT
        } else {
            return self.parse_postfix();
        };
        let expr = self.parse_unary()?;
        Ok(Expression::from_parts(Some(leaf_expr(expr)), None, Some(op)))
    }

    fn parse_postfix(&mut self) -> Result<Expression> {
        let mut expr = self.parse_primary()?;

        loop {
            if self.match_token(TokenType::Dot)? {
                let right = self.parse_postfix_right_side()?;
                expr = Self::binary(expr, right, Operation::OBJECT_OPERATOR);
                continue;
            } else if self.match_token(TokenType::LeftBracket)? {
                let index_expr = self.parse_bracket_content()?;
                self.expect_token(TokenType::RightBracket)?;
                expr = Self::binary(expr, index_expr, Operation::ARRAY_OPERATOR);
            } else if self.match_operator("..")? {
                let right = self.parse_primary()?;
                expr = Self::binary(expr, right, Operation::ARRAY_RANGE_INDEX_OPERATOR);
            } else {
                break;
            }
        }

        Ok(expr)
    }

    fn parse_postfix_right_side(&mut self) -> Result<Expression> {
        self.parse_depth += 1;

        let ok = matches!(&self.current_token, Some(t) if t.r#type == TokenType::Identifier || t.r#type == TokenType::Number);
        if !ok {
            self.parse_depth -= 1;
            let found = self.current_token.as_ref().map_or("EOF".to_string(), |t| t.r#type.to_string());
            return Err(self.create_parser_error(&format!("Expected identifier or number after dot, but found {found}")));
        }

        let mut identifier_value = self.current_token.as_ref().map(|t| t.value.clone()).unwrap_or_default();
        self.advance()?;

        if let Some(t) = &self.current_token {
            if t.r#type == TokenType::Identifier
                && t.value.chars().next().is_some_and(|c| c.is_ascii_alphabetic() || c == '_')
            {
                identifier_value.push_str(&t.value.clone());
                self.advance()?;
            }
        }

        let mut expr = if !identifier_value.contains('[') {
            Expression::create_leaf(&identifier_value)
        } else {
            self.parse_static_bracket_identifier(&identifier_value)
        };

        while self.match_token(TokenType::LeftBracket)? {
            let index_expr = self.parse_bracket_content()?;
            self.expect_token(TokenType::RightBracket)?;
            expr = Self::binary(expr, index_expr, Operation::ARRAY_OPERATOR);
        }

        while self.match_token(TokenType::Dot)? {
            let right = self.parse_postfix_right_side()?;
            expr = Self::binary(expr, right, Operation::OBJECT_OPERATOR);
        }

        self.parse_depth -= 1;
        Ok(expr)
    }

    fn parse_static_bracket_identifier(&self, identifier_value: &str) -> Expression {
        let chars: Vec<char> = identifier_value.chars().collect();
        let bracket_index = chars.iter().position(|c| *c == '[').unwrap_or(chars.len());

        let base_identifier: String = chars[..bracket_index].iter().collect();
        let mut expr = Expression::create_leaf(&base_identifier);

        let remaining: Vec<char> = chars[bracket_index..].to_vec();
        let mut bracket_start = 0;

        while bracket_start < remaining.len() && remaining[bracket_start] == '[' {
            let mut bracket_count = 1;
            let mut end_index = bracket_start + 1;
            let mut in_string = false;
            let mut string_char = '\0';

            while end_index < remaining.len() && bracket_count > 0 {
                let c = remaining[end_index];
                if in_string {
                    if c == string_char && (end_index == 0 || remaining[end_index - 1] != '\\') {
                        in_string = false;
                    }
                } else if c == '"' || c == '\'' {
                    in_string = true;
                    string_char = c;
                } else if c == '[' {
                    bracket_count += 1;
                } else if c == ']' {
                    bracket_count -= 1;
                }
                end_index += 1;
            }

            let content_end = end_index.saturating_sub(1).max(bracket_start + 1);
            let bracket_content: String = remaining[bracket_start + 1..content_end].iter().collect();

            let quoted = (bracket_content.starts_with('"') && bracket_content.ends_with('"'))
                || (bracket_content.starts_with('\'') && bracket_content.ends_with('\''));
            let index_expr = if quoted && !bracket_content.is_empty() {
                let bc: Vec<char> = bracket_content.chars().collect();
                let quote_char = bc[0];
                let str_value: String =
                    if bc.len() >= 2 { bc[1..bc.len() - 1].iter().collect() } else { String::new() };
                Expression::from_parts(
                    Some(ExpressionToken::Value(ExpressionTokenValue::new(
                        format!("{quote_char}{str_value}{quote_char}"),
                        crate::Value::String(str_value),
                    ))),
                    None,
                    None,
                )
            } else if let Some(range_index) = bracket_content.find("..") {
                let start_expr = if range_index == 0 {
                    Expression::create_leaf("0")
                } else {
                    Expression::create_leaf(&bracket_content[..range_index])
                };
                let end_expr = if range_index + 2 == bracket_content.len() {
                    Expression::create_leaf("")
                } else {
                    Expression::create_leaf(&bracket_content[range_index + 2..])
                };
                Self::binary(start_expr, end_expr, Operation::ARRAY_RANGE_INDEX_OPERATOR)
            } else {
                Expression::create_leaf(&bracket_content)
            };

            expr = Self::binary(expr, index_expr, Operation::ARRAY_OPERATOR);
            bracket_start = end_index;
        }

        expr
    }

    fn parse_identifier_path(&mut self) -> Result<Expression> {
        let path = match &self.current_token {
            Some(t) if t.r#type == TokenType::Identifier => t.value.clone(),
            other => {
                let found = other.as_ref().map_or("EOF".to_string(), |t| t.r#type.to_string());
                return Err(self.create_parser_error(&format!("Expected identifier but found {found}")));
            }
        };
        self.advance()?;
        Ok(Expression::create_leaf(&path))
    }

    fn string_literal_expression(value: &str) -> Expression {
        let chars: Vec<char> = value.chars().collect();
        let str_value: String = if chars.len() >= 2 { chars[1..chars.len() - 1].iter().collect() } else { String::new() };
        Expression::from_parts(
            Some(ExpressionToken::Value(ExpressionTokenValue::new(value.to_string(), crate::Value::String(str_value)))),
            None,
            None,
        )
    }

    fn parse_bracket_content(&mut self) -> Result<Expression> {
        if let Some(t) = &self.current_token {
            if t.r#type == TokenType::String {
                let value = t.value.clone();
                self.advance()?;
                return Ok(Self::string_literal_expression(&value));
            }
        }
        self.parse_expression()
    }

    fn parse_primary(&mut self) -> Result<Expression> {
        if self.match_token(TokenType::Number)? {
            let value = self.previous_token_value.as_ref().map(|t| t.value.clone()).unwrap_or_default();
            return Ok(Expression::from_parts(Some(ExpressionToken::new(value)), None, None));
        }

        if self.match_token(TokenType::String)? {
            let value = self.previous_token_value.as_ref().map(|t| t.value.clone()).unwrap_or_default();
            return Ok(Self::string_literal_expression(&value));
        }

        if matches!(&self.current_token, Some(t) if t.r#type == TokenType::Identifier) {
            return self.parse_identifier_path();
        }

        if self.match_token(TokenType::LeftParen)? {
            let expr = self.parse_expression()?;
            self.expect_token(TokenType::RightParen)?;
            return Ok(expr);
        }

        let found = self.current_token.as_ref().map_or("EOF".to_string(), |t| t.r#type.to_string());
        Err(self.create_parser_error(&format!(
            "Unexpected token in expression - expected number, string, identifier, or '(' but found {found}"
        )))
    }

    fn match_token(&mut self, r#type: TokenType) -> Result<bool> {
        if matches!(&self.current_token, Some(t) if t.r#type == r#type) {
            self.advance()?;
            return Ok(true);
        }
        Ok(false)
    }

    fn match_operator(&mut self, op: &str) -> Result<bool> {
        if matches!(&self.current_token, Some(t) if t.r#type == TokenType::Operator && t.value == op) {
            self.advance()?;
            return Ok(true);
        }
        Ok(false)
    }

    fn expect_token(&mut self, r#type: TokenType) -> Result<Token> {
        match &self.current_token {
            Some(t) if t.r#type == r#type => {
                let token = t.clone();
                self.advance()?;
                Ok(token)
            }
            other => {
                let msg = match other {
                    Some(t) => format!("Expected {}, but found {} (\"{}\")", r#type, t.r#type, t.value),
                    None => format!("Expected {}, but found EOF", r#type),
                };
                Err(self.create_parser_error(&msg))
            }
        }
    }

    fn advance(&mut self) -> Result<()> {
        self.previous_token_value = self.current_token.take();
        self.current_token = self.lexer.next_token()?;
        Ok(())
    }
}
