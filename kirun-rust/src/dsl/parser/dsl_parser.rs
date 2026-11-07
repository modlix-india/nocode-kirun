//! Port of kirun-js `dsl/parser/DSLParser.ts`: recursive-descent parser, tokens → AST.

use std::collections::HashMap;

use crate::dsl::lexer::{DSLToken, DSLTokenType as T};
use crate::dsl::parser::ast::*;
use crate::dsl::parser::dsl_parser_error::parser_error;
use crate::util::string::js_string;
use crate::{Map, Result, Value};

pub struct DSLParser {
    tokens: Vec<DSLToken>,
    current: usize,
    /// The source as chars, so expressions keep their exact text (positions are char indexes).
    original_input: Vec<char>,
    /// `@name` uses by the start position of their `@`, filled in once VALUES is parsed.
    value_refs: HashMap<usize, String>,
}

impl DSLParser {
    pub fn new(tokens: Vec<DSLToken>, original_input: &str) -> Self {
        Self { tokens, current: 0, original_input: original_input.chars().collect(), value_refs: HashMap::new() }
    }

    pub fn parse(&mut self) -> Result<FunctionDefNode> {
        self.parse_function_definition()
    }

    fn parse_function_definition(&mut self) -> Result<FunctionDefNode> {
        let start = self.expect(T::Keyword, Some("FUNCTION"))?;
        let name = self.expect_identifier()?;
        let namespace = self.parse_namespace_decl()?;
        let parameters = self.parse_parameters_decl()?;
        let events = self.parse_events_decl()?;
        self.expect(T::Keyword, Some("LOGIC"))?;
        let mut logic = self.parse_logic_block()?;
        let values = self.parse_values_section()?;
        self.resolve_value_refs(&mut logic, &values)?;
        Ok(FunctionDefNode { name, namespace, parameters, events, logic, location: start.location })
    }

    // Named values, `@name`, used in argument position and defined after LOGIC:
    //
    //     LOGIC
    //         cfg: System.Make(resultShape = @cfg.resultShape)
    //     VALUES
    //         @cfg.resultShape = { ... }
    //
    // They keep large JSON out of the step list. Each use is replaced by a copy of the value
    // at compile time, so the compiled JSON is the same as writing the value inline.

    /// `VALUES` followed by `@` or the end: the start of the values section.
    fn at_values_section(&self) -> bool {
        let t = self.peek();
        if t.value != "VALUES" || !matches!(t.r#type, T::Identifier | T::Keyword) {
            return false;
        }
        match self.peek_ahead(1) {
            None => true,
            Some(n) => n.r#type == T::Eof || (n.r#type == T::Operator && n.value == "@"),
        }
    }

    fn is_value_name_token(t: &DSLToken) -> bool {
        matches!(t.r#type, T::Identifier | T::Keyword | T::Boolean | T::Null | T::Number | T::Dot)
    }

    /// `@` immediately followed by a name: a reference to a named value.
    fn at_value_ref(&self) -> bool {
        let t = self.peek();
        t.r#type == T::Operator
            && t.value == "@"
            && self
                .peek_ahead(1)
                .is_some_and(|n| n.location.start_pos == t.location.end_pos && Self::is_value_name_token(n))
    }

    /// Reads `@name`; the name is every adjacent identifier, number and dot.
    fn parse_value_name(&mut self) -> Result<String> {
        let mut last = self.expect(T::Operator, Some("@"))?;
        let mut name = String::new();
        while self.peek().location.start_pos == last.location.end_pos && Self::is_value_name_token(self.peek()) {
            last = self.advance();
            name.push_str(&last.value);
        }
        if name.is_empty() {
            let t = self.peek().clone();
            return Err(parser_error("Expected a value name after @", Some(t.location), &["name".to_string()], Some(&t)));
        }
        Ok(name)
    }

    fn parse_values_section(&mut self) -> Result<Map<Value>> {
        let mut values: Map<Value> = Map::new();
        if !self.at_values_section() {
            return Ok(values);
        }
        self.advance(); // VALUES
        while !self.matches(T::Eof, None) {
            let name_token = self.peek().clone();
            let name = self.parse_value_name()?;
            if values.contains_key(&name) {
                return Err(parser_error(&format!("Value @{name} is defined twice"), Some(name_token.location), &[], Some(&name_token)));
            }
            self.expect(T::Equals, None)?;
            let value = self.parse_json_value()?;
            values.insert(name, value);
        }
        Ok(values)
    }

    /// Replaces every `@name` placeholder with its own copy of the value.
    fn resolve_value_refs(&self, statements: &mut [StatementNode], values: &Map<Value>) -> Result<()> {
        for stmt in statements {
            for arg in stmt.function_call.arguments_map.values_mut() {
                for value in &mut arg.values {
                    let ArgumentValue::Complex(node) = value else { continue };
                    let Some(name) = self.value_refs.get(&node.location.start_pos) else { continue };
                    match values.get(name) {
                        Some(v) => node.value = Some(v.clone()),
                        None => {
                            return Err(parser_error(&format!("Value @{name} is not defined in VALUES"), Some(node.location), &[], None))
                        }
                    }
                }
            }
            for block in stmt.nested_blocks.values_mut() {
                self.resolve_value_refs(block, values)?;
            }
        }
        Ok(())
    }

    fn parse_namespace_decl(&mut self) -> Result<Option<String>> {
        if self.matches(T::Keyword, Some("NAMESPACE")) {
            self.advance();
            return Ok(Some(self.expect_dotted_identifier()?));
        }
        Ok(None)
    }

    fn parse_parameters_decl(&mut self) -> Result<Vec<ParameterDeclNode>> {
        if !self.matches(T::Keyword, Some("PARAMETERS")) {
            return Ok(Vec::new());
        }
        self.advance();
        let mut parameters = Vec::new();
        while !self.matches(T::Keyword, Some("EVENTS")) && !self.matches(T::Keyword, Some("LOGIC")) && !self.matches(T::Eof, None) {
            parameters.push(self.parse_parameter_decl()?);
        }
        Ok(parameters)
    }

    fn parse_parameter_decl(&mut self) -> Result<ParameterDeclNode> {
        let location = self.peek().location;
        let name = self.expect_identifier()?;
        self.expect(T::Keyword, Some("AS"))?;
        let schema = self.parse_schema_spec()?;
        Ok(ParameterDeclNode { name, schema, location })
    }

    fn parse_events_decl(&mut self) -> Result<Vec<EventDeclNode>> {
        if !self.matches(T::Keyword, Some("EVENTS")) {
            return Ok(Vec::new());
        }
        self.advance();
        let mut events = Vec::new();
        while !self.matches(T::Keyword, Some("LOGIC")) && !self.matches(T::Eof, None) {
            events.push(self.parse_event_decl()?);
        }
        Ok(events)
    }

    fn parse_event_decl(&mut self) -> Result<EventDeclNode> {
        let location = self.peek().location;
        let name = self.expect_identifier()?;
        let mut parameters = Vec::new();
        while !self.matches(T::Keyword, Some("LOGIC")) && !self.matches(T::Eof, None) && self.peek().r#type == T::Identifier {
            if self.peek_ahead(1).is_some_and(|t| t.is(T::Keyword, Some("AS"))) {
                parameters.push(self.parse_parameter_decl()?);
            } else {
                break;
            }
        }
        Ok(EventDeclNode { name, parameters, location })
    }

    fn parse_logic_block(&mut self) -> Result<Vec<StatementNode>> {
        let mut statements = Vec::new();
        while !self.matches(T::Eof, None) && !self.at_values_section() {
            statements.push(self.parse_statement(0)?);
        }
        Ok(statements)
    }

    /// `min_block_column`: blocks at or left of this column belong to an ancestor.
    fn parse_statement(&mut self, min_block_column: usize) -> Result<StatementNode> {
        let location = self.peek().location;

        let statement_name = if self.matches(T::Colon, None) {
            // Anonymous statement: JS names it after the token index (the colon is not consumed
            // here, so parse_function_call sees it; kept for parity).
            format!("_anonymous_{}", self.current)
        } else {
            let n = self.expect_identifier()?;
            self.expect(T::Colon, None)?;
            n
        };

        let function_call = self.parse_function_call()?;

        let mut after_steps = Vec::new();
        if self.matches(T::Keyword, Some("AFTER")) {
            self.advance();
            loop {
                after_steps.push(self.expect_dotted_identifier()?);
                if self.matches(T::Comma, None) {
                    self.advance();
                } else {
                    break;
                }
            }
        }

        let mut execute_if_steps = Vec::new();
        if self.matches(T::Keyword, Some("IF")) {
            self.advance();
            loop {
                execute_if_steps.push(self.expect_dotted_identifier()?);
                if self.matches(T::Comma, None) {
                    self.advance();
                } else {
                    break;
                }
            }
        }

        // `CATCH ERRORS` (kirun-rust only): two plain words, so they reserve nothing; a step or a
        // block named CATCH is followed by a colon instead.
        let catch_errors = self.matches(T::Identifier, Some("CATCH"))
            && self.peek_ahead(1).is_some_and(|t| t.is(T::Identifier, Some("ERRORS")));
        if catch_errors {
            self.advance();
            self.advance();
        }

        let mut comment = String::new();
        if self.matches(T::Comment, None) {
            let t = self.advance();
            let text = t.value.trim_start_matches("/*").trim_start();
            let text = text.strip_suffix("*/").unwrap_or(text).trim_end();
            comment = text.trim().to_string();
        }

        let nested_blocks = self.parse_nested_blocks(min_block_column)?;

        Ok(StatementNode {
            statement_name,
            function_call,
            after_steps,
            execute_if_steps,
            nested_blocks,
            location,
            comment,
            catch_errors,
        })
    }

    fn is_block_token(t: &DSLToken) -> bool {
        matches!(t.r#type, T::Identifier | T::Boolean | T::Keyword)
    }

    fn parse_nested_blocks(&mut self, min_block_column: usize) -> Result<Map<Vec<StatementNode>>> {
        let mut blocks: Map<Vec<StatementNode>> = Map::new();

        while !self.matches(T::Eof, None) && !self.at_values_section() {
            let token = self.peek().clone();
            let followed_by_colon = self.peek_ahead(1).is_some_and(|t| t.r#type == T::Colon);
            let block_column = token.location.column;
            if block_column <= min_block_column {
                break;
            }

            if !(Self::is_block_token(&token) && !followed_by_colon) {
                break;
            }

            let block_name = self.advance().value;
            let mut statements = Vec::new();
            let this_block_column = block_column;

            while !self.matches(T::Eof, None) && !self.at_values_section() {
                let next = self.peek().clone();
                if Self::is_block_token(&next) && next.location.column <= this_block_column {
                    // A sibling block, or a statement at the same or lesser indentation.
                    break;
                }
                if next.r#type == T::Colon || Self::is_block_token(&next) {
                    statements.push(self.parse_statement(this_block_column)?);
                } else {
                    break;
                }
            }

            blocks.insert(block_name, statements);
        }
        Ok(blocks)
    }

    fn parse_function_call(&mut self) -> Result<FunctionCallNode> {
        let location = self.peek().location;
        let full_name = self.expect_dotted_identifier()?;
        let (namespace, name) = match full_name.rsplit_once('.') {
            Some((ns, n)) => (ns.to_string(), n.to_string()),
            None => (String::new(), full_name),
        };
        self.expect(T::LeftParen, None)?;
        let arguments_map = self.parse_argument_list()?;
        self.expect(T::RightParen, None)?;
        Ok(FunctionCallNode { namespace, name, arguments_map, location })
    }

    /// Repeating a parameter name adds values to a multi-value argument, in order.
    fn parse_argument_list(&mut self) -> Result<Map<ArgumentNode>> {
        let mut arguments: Map<ArgumentNode> = Map::new();
        if self.matches(T::RightParen, None) {
            return Ok(arguments);
        }
        loop {
            let location = self.peek().location;
            let param_name = self.expect_identifier()?;
            self.expect(T::Equals, None)?;
            let value = self.parse_argument_value()?;
            match arguments.get_mut(&param_name) {
                Some(existing) => existing.values.push(value),
                None => {
                    arguments.insert(param_name.clone(), ArgumentNode { key: param_name, values: vec![value], location });
                }
            }
            if self.matches(T::Comma, None) {
                self.advance();
            } else {
                break;
            }
            if self.matches(T::RightParen, None) || self.matches(T::Eof, None) {
                break;
            }
        }
        Ok(arguments)
    }

    fn parse_argument_value(&mut self) -> Result<ArgumentValue> {
        let token = self.peek().clone();

        // Named value from the VALUES section: @name
        if self.at_value_ref() {
            let name = self.parse_value_name()?;
            self.value_refs.insert(token.location.start_pos, name);
            return Ok(ArgumentValue::Complex(ComplexValueNode { value: None, location: token.location }));
        }

        if self.matches(T::LeftParen, None) {
            let saved = self.current;
            match self.parse_schema_literal() {
                Ok(s) => return Ok(ArgumentValue::SchemaLiteral(s)),
                Err(_) => self.current = saved,
            }
        }

        if self.matches(T::BacktickString, None) {
            let t = self.advance();
            return Ok(ArgumentValue::Expression(ExpressionNode { expression_text: t.value, location: t.location }));
        }

        if self.matches(T::String, None) {
            let t = self.peek().clone();
            if t.value.starts_with('\'') {
                // 'a' + 'b' is one expression; a lone single-quoted string is an expression too.
                if self.peek_ahead(1).is_some_and(|n| n.r#type == T::Operator) {
                    return Ok(ArgumentValue::Expression(self.parse_expression()));
                }
                self.advance();
                return Ok(ArgumentValue::Expression(ExpressionNode { expression_text: t.value, location: t.location }));
            }
            // Double-quoted → VALUE.
            self.advance();
            let inner = strip_quotes(&t.value);
            return Ok(ArgumentValue::Complex(ComplexValueNode {
                value: Some(Value::String(unescape_json_string(&inner))),
                location: t.location,
            }));
        }

        if self.matches(T::Number, None) {
            if self.peek_ahead(1).is_some_and(|n| n.r#type == T::Operator || n.r#type == T::Equals) {
                return Ok(ArgumentValue::Expression(self.parse_expression()));
            }
            let t = self.advance();
            return Ok(ArgumentValue::Complex(ComplexValueNode { value: Some(parse_number_literal(&t.value)), location: t.location }));
        }

        if self.matches(T::Boolean, None) || (self.matches(T::Keyword, None) && (token.value == "true" || token.value == "false")) {
            let t = self.advance();
            return Ok(ArgumentValue::Complex(ComplexValueNode { value: Some(Value::Bool(t.value == "true")), location: t.location }));
        }

        if self.matches(T::Null, None) || (self.matches(T::Keyword, None) && token.value == "null") {
            let t = self.advance();
            return Ok(ArgumentValue::Complex(ComplexValueNode { value: Some(Value::Null), location: t.location }));
        }

        if self.matches(T::Identifier, None) && token.value == "undefined" {
            let t = self.advance();
            return Ok(ArgumentValue::Complex(ComplexValueNode { value: None, location: t.location }));
        }

        if self.matches(T::LeftBrace, None) {
            // `{{` is a KIRun expression reference, a single `{` a JSON object.
            if self.peek_ahead(1).is_some_and(|n| n.r#type != T::LeftBrace) {
                return Ok(ArgumentValue::Complex(self.parse_complex_value()?));
            }
        }

        if self.matches(T::LeftBracket, None) {
            return Ok(ArgumentValue::Complex(self.parse_complex_value()?));
        }

        Ok(ArgumentValue::Expression(self.parse_expression()))
    }

    /// Everything up to a top-level comma or closing bracket, as the exact source text.
    fn parse_expression(&mut self) -> ExpressionNode {
        let start = self.peek().clone();
        let start_pos = start.location.start_pos;

        if matches!(start.r#type, T::Comma | T::RightParen | T::RightBracket | T::RightBrace) {
            return ExpressionNode { expression_text: String::new(), location: start.location };
        }

        let mut depth = 0usize;
        let mut last = start.clone();
        while !self.matches(T::Eof, None) {
            let t = self.peek().clone();
            match t.r#type {
                T::LeftParen | T::LeftBracket | T::LeftBrace => depth += 1,
                T::RightParen | T::RightBracket | T::RightBrace => {
                    if depth == 0 {
                        break;
                    }
                    depth -= 1;
                }
                T::Comma if depth == 0 => break,
                _ => {}
            }
            last = t;
            self.advance();
        }

        if !self.original_input.is_empty() {
            let end_pos = self.peek().location.start_pos;
            if end_pos > start_pos {
                let text: String = self.original_input[start_pos..end_pos.min(self.original_input.len())].iter().collect();
                return ExpressionNode { expression_text: trim_expression_text(&text), location: start.location };
            }
            if last.location.end_pos > start_pos {
                let text: String = self.original_input[start_pos..last.location.end_pos].iter().collect();
                return ExpressionNode { expression_text: trim_expression_text(&text), location: start.location };
            }
        }
        self.reconstruct_expression_from_tokens(&start, &last)
    }

    /// Fallback when the source text is unavailable: re-join the tokens.
    fn reconstruct_expression_from_tokens(&self, start: &DSLToken, end: &DSLToken) -> ExpressionNode {
        let start_idx = self.tokens.iter().position(|t| t.location.start_pos == start.location.start_pos).unwrap_or(0);
        let mut text = String::new();
        let mut depth: i64 = 0;
        for i in start_idx..self.tokens.len() {
            let t = &self.tokens[i];
            if t.location.start_pos > end.location.start_pos {
                break;
            }
            match t.r#type {
                T::LeftParen | T::LeftBracket | T::LeftBrace => depth += 1,
                T::RightParen | T::RightBracket | T::RightBrace => depth -= 1,
                _ => {}
            }
            text.push_str(&t.value);
            if let Some(next) = self.tokens.get(i + 1) {
                if next.location.start_pos <= end.location.start_pos && needs_space_between(t, next, depth) {
                    text.push(' ');
                }
            }
        }
        ExpressionNode { expression_text: text.trim_start().to_string(), location: start.location }
    }

    fn parse_schema_spec(&mut self) -> Result<SchemaNode> {
        let location = self.peek().location;
        if self.matches(T::LeftBrace, None) {
            let v = self.parse_complex_value()?;
            return Ok(SchemaNode { schema_spec: v.value.unwrap_or(Value::Null), location });
        }
        Ok(SchemaNode { schema_spec: Value::String(self.parse_simple_schema()?), location })
    }

    fn parse_simple_schema(&mut self) -> Result<String> {
        if self.matches(T::Keyword, Some("ARRAY")) {
            self.advance();
            self.expect(T::Keyword, Some("OF"))?;
            return Ok(format!("ARRAY OF {}", self.parse_simple_schema()?));
        }
        if self.matches(T::Keyword, Some("OBJECT")) {
            return Ok(self.advance().value);
        }
        let token = self.peek().clone();
        if token.r#type == T::Keyword {
            return Ok(self.advance().value);
        }
        Err(parser_error(
            "Expected schema type",
            Some(token.location),
            &["INTEGER", "LONG", "FLOAT", "DOUBLE", "STRING", "BOOLEAN", "NULL", "ANY", "ARRAY", "OBJECT"]
                .map(String::from),
            Some(&token),
        ))
    }

    /// `(SchemaSpec) WITH DEFAULT VALUE expr`
    fn parse_schema_literal(&mut self) -> Result<SchemaLiteralNode> {
        let location = self.peek().location;
        self.expect(T::LeftParen, None)?;
        let schema = self.parse_schema_spec()?;
        self.expect(T::RightParen, None)?;
        let mut default_value = None;
        if self.matches(T::Keyword, Some("WITH")) {
            self.advance();
            self.expect(T::Keyword, Some("DEFAULT"))?;
            self.expect(T::Keyword, Some("VALUE"))?;
            default_value = Some(self.parse_expression());
        }
        Ok(SchemaLiteralNode { schema, default_value, location })
    }

    fn parse_complex_value(&mut self) -> Result<ComplexValueNode> {
        let start = self.peek().clone();
        if self.matches(T::LeftBrace, None) {
            return Ok(ComplexValueNode { value: Some(self.parse_object()?), location: start.location });
        }
        if self.matches(T::LeftBracket, None) {
            return Ok(ComplexValueNode { value: Some(self.parse_array()?), location: start.location });
        }
        Err(parser_error("Expected { or [", Some(start.location), &["{".into(), "[".into()], Some(&start)))
    }

    fn parse_object(&mut self) -> Result<Value> {
        self.expect(T::LeftBrace, None)?;
        let mut obj = serde_json::Map::new();
        if self.matches(T::RightBrace, None) {
            self.advance();
            return Ok(Value::Object(obj));
        }
        loop {
            let key = if self.matches(T::String, None) {
                strip_quotes(&self.advance().value)
            } else {
                self.expect_identifier()?
            };
            self.expect(T::Colon, None)?;
            let value = self.parse_json_value()?;
            obj.insert(key, value);
            if self.matches(T::Comma, None) {
                self.advance();
            } else {
                break;
            }
            if self.matches(T::RightBrace, None) || self.matches(T::Eof, None) {
                break;
            }
        }
        self.expect(T::RightBrace, None)?;
        Ok(Value::Object(obj))
    }

    fn parse_array(&mut self) -> Result<Value> {
        self.expect(T::LeftBracket, None)?;
        let mut arr = Vec::new();
        if self.matches(T::RightBracket, None) {
            self.advance();
            return Ok(Value::Array(arr));
        }
        loop {
            arr.push(self.parse_json_value()?);
            if self.matches(T::Comma, None) {
                self.advance();
            } else {
                break;
            }
            if self.matches(T::RightBracket, None) || self.matches(T::Eof, None) {
                break;
            }
        }
        self.expect(T::RightBracket, None)?;
        Ok(Value::Array(arr))
    }

    fn parse_json_value(&mut self) -> Result<Value> {
        let t = self.peek().clone();
        match t.r#type {
            T::String => {
                self.advance();
                Ok(Value::String(unescape_json_string(&strip_quotes(&t.value))))
            }
            T::Number => {
                self.advance();
                Ok(parse_number_literal(&t.value))
            }
            T::Boolean => {
                self.advance();
                Ok(Value::Bool(t.value == "true"))
            }
            T::Keyword if t.value == "true" || t.value == "false" => {
                self.advance();
                Ok(Value::Bool(t.value == "true"))
            }
            T::Null => {
                self.advance();
                Ok(Value::Null)
            }
            T::Keyword if t.value == "null" => {
                self.advance();
                Ok(Value::Null)
            }
            T::Identifier if t.value == "undefined" => {
                self.advance();
                Ok(Value::Null)
            }
            T::LeftBrace => self.parse_object(),
            T::LeftBracket => self.parse_array(),
            // A backtick string is an expression, as it is in argument position
            T::BacktickString => {
                self.advance();
                Ok(serde_json::json!({ "isExpression": true, "value": t.value }))
            }
            _ => {
                let expr = self.parse_expression();
                Ok(serde_json::json!({ "isExpression": true, "value": expr.expression_text }))
            }
        }
    }

    // ----- helpers

    fn peek(&self) -> &DSLToken {
        self.tokens.get(self.current).unwrap_or_else(|| self.tokens.last().expect("token stream ends with EOF"))
    }

    fn peek_ahead(&self, n: usize) -> Option<&DSLToken> {
        self.tokens.get(self.current + n)
    }

    fn advance(&mut self) -> DSLToken {
        let t = self.peek().clone();
        self.current += 1;
        t
    }

    fn matches(&self, r#type: T, value: Option<&str>) -> bool {
        self.peek().is(r#type, value)
    }

    fn expect(&mut self, r#type: T, value: Option<&str>) -> Result<DSLToken> {
        if !self.matches(r#type, value) {
            let token = self.peek().clone();
            let expected = match value {
                Some(v) => format!("{} ({v})", r#type.name()),
                None => r#type.name().to_string(),
            };
            return Err(parser_error(&format!("Expected {expected}"), Some(token.location), &[expected], Some(&token)));
        }
        Ok(self.advance())
    }

    /// Identifiers, and also keywords/booleans (block names like `true`, `iteration`).
    fn expect_identifier(&mut self) -> Result<String> {
        if matches!(self.peek().r#type, T::Identifier | T::Boolean | T::Keyword) {
            return Ok(self.advance().value);
        }
        Ok(self.expect(T::Identifier, None)?.value)
    }

    fn expect_dotted_identifier(&mut self) -> Result<String> {
        let mut name = self.expect_identifier()?;
        while self.matches(T::Dot, None) {
            self.advance();
            name.push('.');
            name.push_str(&self.expect_identifier()?);
        }
        Ok(name)
    }
}

/// Leading whitespace is dropped. Trailing whitespace is kept, since stored expressions can
/// end in spaces, unless it runs onto another line: that is the layout of an argument list
/// split over lines, before its closing `)` or `}`.
fn trim_expression_text(text: &str) -> String {
    let start = js_string::trim_start(text);
    let body = js_string::trim_end(&start);
    if start[body.len()..].contains('\n') {
        body
    } else {
        start
    }
}

/// JS `value.slice(1, -1)`.
fn strip_quotes(s: &str) -> String {
    let chars: Vec<char> = s.chars().collect();
    if chars.len() < 2 {
        return String::new();
    }
    chars[1..chars.len() - 1].iter().collect()
}

/// A DSL number literal as the JSON number type it was written as (kirun-py `parse_number`):
/// integers stay integers, a literal with a fraction stays a float.
pub fn parse_number_literal(text: &str) -> Value {
    if !text.contains('.') {
        if let Ok(i) = text.parse::<i64>() {
            return Value::from(i);
        }
    }
    text.parse::<f64>()
        .ok()
        .and_then(serde_json::Number::from_f64)
        .map(Value::Number)
        .unwrap_or(Value::Null)
}

/// JS `unescapeJsonString`, including its off-by-one `\uXXXX` length check.
fn unescape_json_string(s: &str) -> String {
    let chars: Vec<char> = s.chars().collect();
    let mut out = String::new();
    let mut i = 0;
    while i < chars.len() {
        if chars[i] == '\\' && i + 1 < chars.len() {
            let simple = match chars[i + 1] {
                'n' => Some('\n'),
                'r' => Some('\r'),
                't' => Some('\t'),
                'b' => Some('\u{8}'),
                'f' => Some('\u{c}'),
                '\\' => Some('\\'),
                '"' => Some('"'),
                '/' => Some('/'),
                _ => None,
            };
            if let Some(c) = simple {
                out.push(c);
                i += 2;
                continue;
            }
            if chars[i + 1] == 'u' && i + 5 < chars.len() {
                let hex: String = chars[i + 2..i + 6].iter().collect();
                if hex.chars().all(|c| c.is_ascii_hexdigit()) {
                    if let Some(c) = u32::from_str_radix(&hex, 16).ok().and_then(char::from_u32) {
                        out.push(c);
                        i += 6;
                        continue;
                    }
                }
            }
            out.push(chars[i]);
            i += 1;
        } else {
            out.push(chars[i]);
            i += 1;
        }
    }
    out
}

/// JS `needsSpaceBetween` (token-rejoin fallback).
fn needs_space_between(current: &DSLToken, next: &DSLToken, depth: i64) -> bool {
    let (c, n) = (current.r#type, next.r#type);
    if (c == T::LeftBrace && n == T::LeftBrace) || (c == T::RightBrace && n == T::RightBrace) {
        return false;
    }
    if c == T::Dot || n == T::Dot || c == T::LeftBracket || n == T::RightBracket {
        return false;
    }
    if c == T::LeftParen || n == T::RightParen || c == T::LeftBrace || n == T::RightBrace {
        return false;
    }
    let is_op = |t: &DSLToken, v: &str| t.r#type == T::Operator && t.value == v;
    if is_op(current, "??") || is_op(next, "??") || is_op(current, "-") || is_op(next, "-") {
        return false;
    }
    if depth > 0 {
        let arith = |t: &DSLToken| t.r#type == T::Operator && ["+", "*", "/", "%"].contains(&t.value.as_str());
        if arith(current) || arith(next) {
            return false;
        }
    }
    let word = |t: T| matches!(t, T::Identifier | T::Keyword | T::Boolean | T::Number);
    if word(c) && word(n) {
        return true;
    }
    if c == T::Equals || n == T::Equals {
        return true;
    }
    c == T::Operator || n == T::Operator
}
