//! Port of kirun-js `dsl/parser/ast/*`. JS AST node classes become plain structs; the
//! `ArgumentValue` union is an enum. JS `undefined` values are `Value::Null`.

use crate::dsl::lexer::SourceLocation;
use crate::{Map, Value};

#[derive(Debug, Clone, PartialEq)]
pub struct ExpressionNode {
    pub expression_text: String,
    pub location: SourceLocation,
}

/// `value` is `None` for the DSL literal `undefined` (the JSON omits the key), `Some(Null)`
/// for `null`.
#[derive(Debug, Clone, PartialEq)]
pub struct ComplexValueNode {
    pub value: Option<Value>,
    pub location: SourceLocation,
}

/// `schema_spec` is DSL schema text (`Value::String`, e.g. `ARRAY OF INTEGER`) or a JSON schema.
#[derive(Debug, Clone, PartialEq)]
pub struct SchemaNode {
    pub schema_spec: Value,
    pub location: SourceLocation,
}

#[derive(Debug, Clone, PartialEq)]
pub struct SchemaLiteralNode {
    pub schema: SchemaNode,
    pub default_value: Option<ExpressionNode>,
    pub location: SourceLocation,
}

#[derive(Debug, Clone, PartialEq)]
pub enum ArgumentValue {
    Expression(ExpressionNode),
    Complex(ComplexValueNode),
    SchemaLiteral(SchemaLiteralNode),
}

#[derive(Debug, Clone, PartialEq)]
pub struct ArgumentNode {
    pub key: String,
    pub values: Vec<ArgumentValue>,
    pub location: SourceLocation,
}

impl ArgumentNode {
    pub fn value(&self) -> &ArgumentValue {
        &self.values[0]
    }

    pub fn is_multi_value(&self) -> bool {
        self.values.len() > 1
    }
}

#[derive(Debug, Clone, PartialEq)]
pub struct FunctionCallNode {
    pub namespace: String,
    pub name: String,
    pub arguments_map: Map<ArgumentNode>,
    pub location: SourceLocation,
}

#[derive(Debug, Clone, PartialEq)]
pub struct StatementNode {
    pub statement_name: String,
    pub function_call: FunctionCallNode,
    pub after_steps: Vec<String>,
    pub execute_if_steps: Vec<String>,
    pub nested_blocks: Map<Vec<StatementNode>>,
    pub location: SourceLocation,
    pub comment: String,
    /// `CATCH ERRORS` (kirun-rust only): the step's `catchErrors`.
    pub catch_errors: bool,
}

#[derive(Debug, Clone, PartialEq)]
pub struct ParameterDeclNode {
    pub name: String,
    pub schema: SchemaNode,
    pub location: SourceLocation,
}

#[derive(Debug, Clone, PartialEq)]
pub struct EventDeclNode {
    pub name: String,
    pub parameters: Vec<ParameterDeclNode>,
    pub location: SourceLocation,
}

#[derive(Debug, Clone, PartialEq)]
pub struct FunctionDefNode {
    pub name: String,
    pub namespace: Option<String>,
    pub parameters: Vec<ParameterDeclNode>,
    pub events: Vec<EventDeclNode>,
    pub logic: Vec<StatementNode>,
    pub location: SourceLocation,
}
