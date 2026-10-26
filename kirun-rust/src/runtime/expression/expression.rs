//! Port of kirun-js `runtime/expression/Expression.ts`.
//!
//! JS keeps `tokens` and `ops` in a `LinkedList` whose `push` adds at the head; Rust uses
//! `VecDeque` (`push_front` == JS `push`, `pop_front` == JS `pop`, `front` == JS `peek`).
//!
//! `new Expression(str)` parses with `ExpressionParser`; when that fails it falls back to the
//! legacy character scanner (JS `evaluate()`), which is what accepts `{{ }}` templates and
//! `a[..4]` style ranges.

use std::collections::VecDeque;
use std::fmt;

use crate::runtime::expression::exception::expression_evaluation_exception;
use crate::runtime::expression::expression_parser::ExpressionParser;
use crate::runtime::expression::expression_token::ExpressionToken;
use crate::runtime::expression::expression_token_value::ExpressionTokenValue;
use crate::runtime::expression::operation::Operation;
use crate::util::string::string_formatter::StringFormatter;
use crate::util::string::string_util::StringUtil;
use crate::{KIRunError, Result, Value};

/// JS `Expression` (parsed expression tree).
#[derive(Debug, Clone, PartialEq, Default)]
pub struct Expression {
    expression: String,
    tokens: VecDeque<ExpressionToken>,
    ops: VecDeque<Operation>,
}

fn blank(s: &str) -> bool {
    StringUtil::is_null_or_blank(Some(s))
}

impl Expression {
    /// JS `new Expression(expression)`: trims and parses. JS throws on a parse error.
    pub fn new(expression: &str) -> Result<Self> {
        let mut e = Expression { expression: expression.to_string(), ..Default::default() };

        if !expression.trim().is_empty() {
            let trimmed = expression.trim();
            match ExpressionParser::new(trimmed).parse() {
                Ok(parsed) => {
                    e.expression = trimmed.to_string();
                    e.tokens = parsed.tokens;
                    e.ops = parsed.ops;
                }
                Err(_) => {
                    e.evaluate()?;
                    if e.ops.back().is_some_and(|o| o.get_operator() == "..") && e.tokens.len() == 1 {
                        e.tokens.push_front(ExpressionToken::new(""));
                    }
                }
            }
        }
        Ok(e)
    }

    /// JS `new Expression('', l, r, op)`: builds a node directly, without parsing.
    pub fn from_parts(l: Option<ExpressionToken>, r: Option<ExpressionToken>, op: Option<Operation>) -> Self {
        let mut e = Expression::default();
        let (mut l, mut r) = (l, r);
        if op.is_some_and(|o| o.get_operator() == "..") {
            if l.is_none() {
                l = Some(ExpressionToken::Value(ExpressionTokenValue::new("", Value::String(String::new()))));
            } else if r.is_none() {
                r = Some(ExpressionToken::Value(ExpressionTokenValue::new("", Value::String(String::new()))));
            }
        }
        if let Some(l) = l {
            e.tokens.push_front(l);
        }
        if let Some(r) = r {
            e.tokens.push_front(r);
        }
        if let Some(op) = op {
            e.ops.push_front(op);
        }
        e
    }

    /// JS `Expression.createTernary`. Tokens end up as `[falseExpr, trueExpr, condition]`.
    pub fn create_ternary(condition: ExpressionToken, true_expr: ExpressionToken, false_expr: ExpressionToken) -> Self {
        let mut e = Expression::default();
        e.tokens.push_front(condition);
        e.tokens.push_front(true_expr);
        e.tokens.push_front(false_expr);
        e.ops.push_front(Operation::CONDITIONAL_TERNARY_OPERATOR);
        e
    }

    /// JS `Expression.createLeaf`.
    pub fn create_leaf(value: &str) -> Self {
        let mut e = Expression { expression: value.to_string(), ..Default::default() };
        e.tokens.push_front(ExpressionToken::new(value));
        e
    }

    /// JS `getExpression()`: the (trimmed) source text.
    pub fn get_expression(&self) -> &str {
        &self.expression
    }

    pub fn get_tokens(&self) -> &VecDeque<ExpressionToken> {
        &self.tokens
    }

    pub fn get_operations(&self) -> &VecDeque<Operation> {
        &self.ops
    }

    /// JS `equals(o)`.
    pub fn equals(&self, o: &Expression) -> bool {
        self.expression == o.expression
    }

    fn err(&self, message: &str) -> KIRunError {
        expression_evaluation_exception(&self.expression, message)
    }

    fn pop_token(&mut self) -> Result<ExpressionToken> {
        self.tokens
            .pop_front()
            .ok_or_else(|| KIRunError::runtime("List is empty and cannot pop further."))
    }

    // ------------------------------------------------------------------ legacy scanner

    /// JS private `evaluate()`: the legacy character scanner.
    fn evaluate(&mut self) -> Result<()> {
        let chars: Vec<char> = self.expression.chars().collect();
        let length = chars.len();

        let mut sb = String::new();
        let mut i = 0usize;
        let mut is_prev_op = false;

        while i < length {
            let chr = chars[i];
            let buff = sb.clone();

            match chr {
                ' ' => {
                    is_prev_op = self.process_token_sepearator(&mut sb, &buff, is_prev_op)?;
                }
                '(' => {
                    i = self.process_sub_expression(&chars, &mut sb, &buff, i, is_prev_op)?;
                    is_prev_op = false;
                }
                ')' => return Err(self.err("Extra closing parenthesis found")),
                ']' => return Err(self.err("Extra closing square bracket found")),
                '\'' | '"' => {
                    let (ni, p) = if is_prev_op && self.ops.front() == Some(&Operation::ARRAY_OPERATOR) {
                        self.process(&chars, &mut sb, i)?
                    } else {
                        self.process_string_literal(&chars, chr, i)?
                    };
                    i = ni;
                    is_prev_op = p;
                }
                '?' if i + 1 < length && chars[i + 1] != '?' && i != 0 && chars[i - 1] != '?' => {
                    i = self.process_ternary_operator(&chars, &mut sb, &buff, i, is_prev_op)?;
                }
                _ => {
                    let (ni, p) = self.process_others(&chars, chr, &mut sb, &buff, i, is_prev_op)?;
                    i = ni;
                    is_prev_op = p;
                    if is_prev_op && self.ops.front() == Some(&Operation::ARRAY_OPERATOR) {
                        let (ni, p) = self.process(&chars, &mut sb, i)?;
                        i = ni;
                        is_prev_op = p;
                    }
                }
            }

            i += 1;
        }

        let buff = sb;
        if !blank(&buff) {
            if Operation::is_operator_str(&buff) {
                return Err(self.err("Expression is ending with an operator"));
            }
            self.tokens.push_front(ExpressionToken::new(buff));
        }
        Ok(())
    }

    fn process_string_literal(&mut self, chars: &[char], chr: char, i: usize) -> Result<(usize, bool)> {
        let length = chars.len();
        let mut str_constant = String::new();

        let mut j = i + 1;
        while j < length {
            let next_char = chars[j];
            if next_char == chr && chars[j - 1] != '\\' {
                break;
            }
            str_constant.push(next_char);
            j += 1;
        }

        if j == length && chars[j - 1] != chr {
            return Err(self.err(&format!("Missing string ending marker {chr}")));
        }

        self.tokens.push_front(ExpressionToken::Value(ExpressionTokenValue::new(
            str_constant.clone(),
            Value::String(str_constant),
        )));
        Ok((j, false))
    }

    fn process(&mut self, chars: &[char], sb: &mut String, i: usize) -> Result<(usize, bool)> {
        let length = chars.len();
        let mut cnt = 1;
        let mut i = i + 1;
        while i < length && cnt != 0 {
            let c = chars[i];
            if c == ']' {
                cnt -= 1;
            } else if c == '[' {
                cnt += 1;
            }
            if cnt != 0 {
                sb.push(c);
                i += 1;
            }
        }
        self.tokens.push_front(ExpressionToken::Expression(Box::new(Expression::new(sb)?)));
        sb.clear();

        Ok((i, false))
    }

    fn process_others(
        &mut self,
        chars: &[char],
        chr: char,
        sb: &mut String,
        buff: &str,
        i: usize,
        is_prev_op: bool,
    ) -> Result<(usize, bool)> {
        let length = chars.len();
        let start = (length - i).min(Operation::biggest_operator_size());
        let mut is_prev_op = is_prev_op;

        for size in (1..=start).rev() {
            let op: String = chars[i..i + size].iter().collect();
            if Operation::is_operator_without_space_wrap(&op) {
                if !blank(buff) {
                    self.tokens.push_front(ExpressionToken::new(buff));
                    is_prev_op = false;
                } else if op == ".." && self.tokens.is_empty() {
                    self.tokens.push_front(ExpressionToken::new("0"));
                    is_prev_op = false;
                }
                self.check_unary_operator(Operation::from_operator(&op), is_prev_op)?;
                sb.clear();
                return Ok((i + size - 1, true));
            }
        }

        sb.push(chr);
        Ok((i, false))
    }

    fn collapse_top(&mut self) -> Result<()> {
        let prev = self.ops.pop_front().ok_or_else(|| KIRunError::runtime("List is empty and cannot pop further."))?;
        if prev.is_unary() {
            let l = self.pop_token()?;
            self.tokens.push_front(ExpressionToken::Expression(Box::new(Expression::from_parts(Some(l), None, Some(prev)))));
        } else {
            let r = self.pop_token()?;
            let l = self.pop_token()?;
            self.tokens
                .push_front(ExpressionToken::Expression(Box::new(Expression::from_parts(Some(l), Some(r), Some(prev)))));
        }
        Ok(())
    }

    fn process_ternary_operator(
        &mut self,
        chars: &[char],
        sb: &mut String,
        buff: &str,
        i: usize,
        is_prev_op: bool,
    ) -> Result<usize> {
        let length = chars.len();
        if is_prev_op {
            return Err(self.err("Ternary operator is followed by an operator"));
        }

        if !buff.trim().is_empty() {
            self.tokens.push_front(ExpressionToken::Expression(Box::new(Expression::new(buff)?)));
            sb.clear();
        }

        let mut i = i + 1;
        let mut cnt = 1;
        let mut in_chr = '\0';
        let start = i;
        while i < length && cnt > 0 {
            in_chr = chars[i];
            if in_chr == '?' {
                cnt += 1;
            } else if in_chr == ':' {
                cnt -= 1;
            }
            i += 1;
        }

        if in_chr != ':' {
            return Err(self.err("':' operater is missing"));
        }

        if i >= length {
            return Err(self.err("Third part of the ternary expression is missing"));
        }

        while let Some(top) = self.ops.front().copied() {
            if !self.has_precedence(Operation::CONDITIONAL_TERNARY_OPERATOR, top)? {
                break;
            }
            self.collapse_top()?;
        }

        self.ops.push_front(Operation::CONDITIONAL_TERNARY_OPERATOR);
        let middle: String = chars[start..i - 1].iter().collect();
        self.tokens.push_front(ExpressionToken::Expression(Box::new(Expression::new(&middle)?)));

        let second_exp: String = chars[i..].iter().collect();
        if second_exp.trim().is_empty() {
            return Err(self.err("Third part of the ternary expression is missing"));
        }

        self.tokens.push_front(ExpressionToken::Expression(Box::new(Expression::new(&second_exp)?)));

        Ok(length - 1)
    }

    fn process_sub_expression(
        &mut self,
        chars: &[char],
        sb: &mut String,
        buff: &str,
        i: usize,
        is_prev_op: bool,
    ) -> Result<usize> {
        let length = chars.len();
        if Operation::is_operator_str(buff) {
            self.check_unary_operator(Operation::from_operator(buff), is_prev_op)?;
            sb.clear();
        } else if !blank(buff) {
            return Err(self.err(&StringFormatter::format("Unkown token : $ found.", &[buff])));
        }

        let mut cnt = 1;
        let mut sub_exp: Vec<char> = Vec::new();
        let mut in_chr = chars[i];
        let mut i = i + 1;
        while i < length && cnt > 0 {
            in_chr = chars[i];
            if in_chr == '(' {
                cnt += 1;
            } else if in_chr == ')' {
                cnt -= 1;
            }
            if cnt != 0 {
                sub_exp.push(in_chr);
                i += 1;
            }
        }

        if in_chr != ')' {
            return Err(self.err("Missing a closed parenthesis"));
        }

        while sub_exp.len() > 2 && has_matching_outer_parentheses(&sub_exp) {
            sub_exp.remove(0);
            sub_exp.pop();
        }

        let s: String = sub_exp.iter().collect();
        self.tokens.push_front(ExpressionToken::Expression(Box::new(Expression::new(s.trim())?)));
        Ok(i)
    }

    fn process_token_sepearator(&mut self, sb: &mut String, buff: &str, is_prev_op: bool) -> Result<bool> {
        let mut is_prev_op = is_prev_op;
        if !blank(buff) {
            if Operation::is_operator_str(buff) {
                self.check_unary_operator(Operation::from_operator(buff), is_prev_op)?;
                is_prev_op = true;
            } else {
                self.tokens.push_front(ExpressionToken::new(buff));
                is_prev_op = false;
            }
        }
        sb.clear();
        Ok(is_prev_op)
    }

    fn check_unary_operator(&mut self, op: Option<Operation>, is_prev_op: bool) -> Result<()> {
        let Some(op) = op else { return Ok(()) };
        if is_prev_op || self.tokens.is_empty() {
            if let Some(x) = op.to_unary() {
                self.ops.push_front(x);
            } else {
                return Err(self.err(&StringFormatter::format("Extra operator $ found.", &[op.get_operator()])));
            }
        } else {
            while let Some(top) = self.ops.front().copied() {
                if !self.has_precedence(op, top)? {
                    break;
                }
                self.collapse_top()?;
            }
            self.ops.push_front(op);
        }
        Ok(())
    }

    fn has_precedence(&self, op1: Operation, op2: Operation) -> Result<bool> {
        let (Some(pre1), Some(pre2)) = (op1.get_priority(), op2.get_priority()) else {
            return Err(KIRunError::runtime("Unknown operators provided"));
        };
        if pre1 == pre2 {
            let path = |o: Operation| o == Operation::OBJECT_OPERATOR || o == Operation::ARRAY_OPERATOR;
            return Ok(!path(op1) && !path(op2));
        }
        Ok(pre2 < pre1)
    }

    // ------------------------------------------------------------------ toString helpers

    fn format_unary_operation(&self, op: &Operation) -> String {
        let operand = self.tokens.front().map(token_to_string).unwrap_or_default();
        format!("({}{})", &op.get_operator()[4..], operand)
    }

    fn format_ternary_operation(&self) -> String {
        let s = |i: usize| self.tokens.get(i).map(token_to_string).unwrap_or_default();
        format!("({}?{}:{})", s(2), s(1), s(0))
    }

    fn format_binary_operation(&self, op: &Operation) -> String {
        if self.tokens.len() < 2 {
            if !self.expression.is_empty() {
                return self.expression.clone();
            }
            if self.tokens.len() == 1 {
                return token_to_string(&self.tokens[0]);
            }
            return "Error: Invalid binary expression".into();
        }

        let right = &self.tokens[0];
        let left = &self.tokens[1];

        if *op == Operation::ARRAY_OPERATOR {
            return format!("{}[{}]", token_to_string(left), format_array_index(right));
        }

        let left_str = token_to_string(left);
        let right_str = token_to_string(right);

        if *op == Operation::OBJECT_OPERATOR {
            if let ExpressionToken::Expression(r) = right {
                if let Some(right_op) = r.ops.front() {
                    if *right_op == Operation::ARRAY_OPERATOR {
                        return format!("({left_str}.({right_str}))");
                    }
                    return format!("({left_str}.{right_str})");
                }
            }
            return format!("({left_str}.{right_str})");
        }

        if *op == Operation::ARRAY_RANGE_INDEX_OPERATOR {
            return format!("({left_str}..{right_str})");
        }

        format!("({left_str}{}{right_str})", op.get_operator())
    }
}

fn has_matching_outer_parentheses(s: &[char]) -> bool {
    if s.len() < 2 || s[0] != '(' || s[s.len() - 1] != ')' {
        return false;
    }
    let mut level = 0i32;
    for ch in &s[..s.len() - 1] {
        if *ch == '(' {
            level += 1;
        } else if *ch == ')' {
            level -= 1;
        }
        if level == 0 {
            return false;
        }
    }
    level == 1
}

fn is_quoted(s: &str) -> bool {
    s.starts_with('"') || s.starts_with('\'')
}

/// JS `tokenToString`.
fn token_to_string(token: &ExpressionToken) -> String {
    match token {
        ExpressionToken::Expression(e) => e.to_string(),
        ExpressionToken::Value(v) if is_quoted(v.get_expression()) => v.get_expression().to_string(),
        other => other.to_string(),
    }
}

/// JS `formatArrayIndex`.
fn format_array_index(token: &ExpressionToken) -> String {
    match token {
        ExpressionToken::Expression(e) => {
            if e.ops.is_empty() && e.tokens.len() == 1 {
                let inner = e.tokens[0].get_expression();
                if is_quoted(inner) {
                    return inner.to_string();
                }
            }
            e.to_string()
        }
        other => {
            let x = other.get_expression();
            if is_quoted(x) {
                return x.to_string();
            }
            other.to_string()
        }
    }
}

impl fmt::Display for Expression {
    /// JS `toString()`: fully parenthesised form, e.g. `(2+3)`.
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let s = if self.ops.is_empty() {
            // kirun-java `Expression.toString`: the expression text only when nothing was parsed.
            if self.tokens.len() == 1 {
                token_to_string(&self.tokens[0])
            } else if self.tokens.is_empty() && !self.expression.is_empty() {
                self.expression.clone()
            } else {
                "Error: No operations but has tokens".to_string()
            }
        } else {
            let op = self.ops[0];
            if op.get_operator().starts_with("UN: ") {
                self.format_unary_operation(&op)
            } else if op == Operation::CONDITIONAL_TERNARY_OPERATOR {
                self.format_ternary_operation()
            } else {
                self.format_binary_operation(&op)
            }
        };
        f.write_str(&s)
    }
}
