//! Port of kirun-js `dsl/transformer/ASTToJSON.ts`: AST → FunctionDefinition JSON. Only
//! non-empty fields are written; JS `undefined` properties are simply absent.

use serde_json::json;

use crate::dsl::parser::ast::*;
use crate::dsl::parser::dsl_parser::parse_number_literal;
use crate::dsl::transformer::schema_transformer::SchemaTransformer;
use crate::{Result, Value};

type JsonMap = serde_json::Map<String, Value>;

#[derive(Debug, Default)]
pub struct ASTToJSONTransformer;

impl ASTToJSONTransformer {
    pub fn new() -> Self {
        Self
    }

    pub fn transform(&self, ast: &FunctionDefNode) -> Result<Value> {
        let mut json = JsonMap::new();
        json.insert("name".into(), Value::String(ast.name.clone()));
        if let Some(ns) = ast.namespace.as_ref().filter(|n| !n.is_empty()) {
            json.insert("namespace".into(), Value::String(ns.clone()));
        }

        let mut parameters = JsonMap::new();
        for p in &ast.parameters {
            parameters.insert(
                p.name.clone(),
                json!({
                    "parameterName": p.name,
                    "schema": SchemaTransformer::transform(&p.schema.schema_spec)?,
                    "variableArgument": false,
                    "type": "EXPRESSION",
                }),
            );
        }
        if !parameters.is_empty() {
            json.insert("parameters".into(), Value::Object(parameters));
        }

        let mut events = JsonMap::new();
        for e in &ast.events {
            let mut params = JsonMap::new();
            for p in &e.parameters {
                params.insert(p.name.clone(), SchemaTransformer::transform(&p.schema.schema_spec)?);
            }
            events.insert(e.name.clone(), json!({ "name": e.name, "parameters": params }));
        }
        if !events.is_empty() {
            json.insert("events".into(), Value::Object(events));
        }

        let mut steps = JsonMap::new();
        for s in &ast.logic {
            steps.insert(s.statement_name.clone(), self.transform_statement(s)?);
        }
        json.insert("steps".into(), Value::Object(steps));
        Ok(Value::Object(json))
    }

    fn transform_statement(&self, stmt: &StatementNode) -> Result<Value> {
        let mut json = JsonMap::new();
        json.insert("statementName".into(), Value::String(stmt.statement_name.clone()));
        json.insert("namespace".into(), Value::String(stmt.function_call.namespace.clone()));
        json.insert("name".into(), Value::String(stmt.function_call.name.clone()));

        let parameter_map = self.transform_parameter_map(&stmt.function_call.arguments_map)?;
        if !parameter_map.is_empty() {
            json.insert("parameterMap".into(), Value::Object(parameter_map));
        }

        // Only explicit AFTER clauses become dependentStatements; nesting is implicit.
        let deps: JsonMap = stmt.after_steps.iter().map(|s| (s.clone(), Value::Bool(true))).collect();
        if !deps.is_empty() {
            json.insert("dependentStatements".into(), Value::Object(deps));
        }

        let exec_if: JsonMap = stmt.execute_if_steps.iter().map(|s| (s.clone(), Value::Bool(true))).collect();
        if !exec_if.is_empty() {
            json.insert("executeIftrue".into(), Value::Object(exec_if));
        }

        if !stmt.comment.trim().is_empty() {
            json.insert("comment".into(), Value::String(stmt.comment.clone()));
        }
        Ok(Value::Object(json))
    }

    fn transform_parameter_map(&self, args: &crate::Map<ArgumentNode>) -> Result<JsonMap> {
        let mut result = JsonMap::new();
        for (param_name, arg) in args {
            if Self::is_empty_argument(arg) {
                continue;
            }
            let mut refs = JsonMap::new();
            for (i, value) in arg.values.iter().enumerate() {
                let r = self.transform_argument_value(value, i + 1)?;
                refs.insert(r["key"].as_str().unwrap_or_default().to_string(), r);
            }
            result.insert(param_name.clone(), Value::Object(refs));
        }
        Ok(result)
    }

    /// Only stray delimiters count as empty; `''` and null values are kept.
    fn is_empty_argument(arg: &ArgumentNode) -> bool {
        match arg.value() {
            ArgumentValue::Expression(e) => {
                let t = e.expression_text.trim();
                t == "," || t == ")"
            }
            _ => false,
        }
    }

    fn transform_argument_value(&self, value: &ArgumentValue, order: usize) -> Result<Value> {
        let key = crate::util::uuid::uuid();
        Ok(match value {
            ArgumentValue::Expression(e) => {
                json!({ "key": key, "type": "EXPRESSION", "expression": e.expression_text, "order": order })
            }
            // JS `value: undefined` is dropped by JSON.stringify.
            ArgumentValue::Complex(c) => match &c.value {
                Some(v) => json!({ "key": key, "type": "VALUE", "value": v, "order": order }),
                None => json!({ "key": key, "type": "VALUE", "order": order }),
            },
            ArgumentValue::SchemaLiteral(s) => {
                let schema = SchemaTransformer::transform(&s.schema.schema_spec)?;
                let value = match &s.default_value {
                    Some(d) => Self::evaluate_default_value(d),
                    None => schema,
                };
                json!({ "key": key, "type": "VALUE", "value": value, "order": order })
            }
        })
    }

    fn evaluate_default_value(expr: &ExpressionNode) -> Value {
        let text = expr.expression_text.trim();
        match text {
            "[]" => return json!([]),
            "{}" => return json!({}),
            "null" => return Value::Null,
            "true" => return Value::Bool(true),
            "false" => return Value::Bool(false),
            _ => {}
        }
        // JS parseFloat: the longest numeric prefix.
        if let Some(prefix) = js_parse_float_prefix(text) {
            return parse_number_literal(prefix);
        }
        let quoted = (text.starts_with('"') && text.ends_with('"')) || (text.starts_with('\'') && text.ends_with('\''));
        if quoted && text.chars().count() >= 2 {
            let chars: Vec<char> = text.chars().collect();
            return Value::String(chars[1..chars.len() - 1].iter().collect());
        }
        Value::String(text.to_string())
    }

    /// Flattens nested blocks into the top-level statement list (parents first, then their
    /// nested statements depth-first), clearing each statement's blocks.
    pub fn flatten_nested_blocks(&self, ast: &mut FunctionDefNode) {
        fn collect(stmt: &mut StatementNode, all: &mut Vec<StatementNode>) {
            let blocks = std::mem::take(&mut stmt.nested_blocks);
            for (_, nested) in blocks {
                for mut n in nested {
                    let mut children = Vec::new();
                    collect(&mut n, &mut children);
                    all.push(n);
                    all.extend(children);
                }
            }
        }
        let mut logic = std::mem::take(&mut ast.logic);
        let mut nested_all = Vec::new();
        for stmt in logic.iter_mut() {
            collect(stmt, &mut nested_all);
        }
        logic.extend(nested_all);
        ast.logic = logic;
    }
}

/// The numeric prefix JS `parseFloat` would read (`"12px"` → `"12"`), or `None` for NaN.
fn js_parse_float_prefix(s: &str) -> Option<&str> {
    let b = s.as_bytes();
    let mut i = 0;
    if i < b.len() && (b[i] == b'+' || b[i] == b'-') {
        i += 1;
    }
    let int_start = i;
    while i < b.len() && b[i].is_ascii_digit() {
        i += 1;
    }
    let mut digits = i - int_start;
    if i < b.len() && b[i] == b'.' {
        let frac_start = i + 1;
        let mut j = frac_start;
        while j < b.len() && b[j].is_ascii_digit() {
            j += 1;
        }
        digits += j - frac_start;
        if j > frac_start || digits > 0 {
            i = j;
        }
    }
    if digits == 0 {
        return None;
    }
    if i < b.len() && (b[i] == b'e' || b[i] == b'E') {
        let mut j = i + 1;
        if j < b.len() && (b[j] == b'+' || b[j] == b'-') {
            j += 1;
        }
        let exp_start = j;
        while j < b.len() && b[j].is_ascii_digit() {
            j += 1;
        }
        if j > exp_start {
            i = j;
        }
    }
    Some(&s[..i])
}
