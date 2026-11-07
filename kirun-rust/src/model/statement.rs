//! Port of kirun-js `model/Statement.ts` (AbstractStatement fields are inlined).

use crate::model::{ParameterReference, Position};
use crate::{Map, Result, Value};

#[derive(Debug, Clone, PartialEq, Default)]
pub struct Statement {
    pub statement_name: String,
    pub namespace: String,
    pub name: String,
    pub parameter_map: Map<Map<ParameterReference>>,
    pub dependent_statements: Map<bool>,
    pub execute_iftrue: Map<bool>,
    // AbstractStatement
    pub comment: Option<String>,
    pub description: Option<String>,
    pub position: Option<Position>,
    pub r#override: bool,
    /// `catchErrors` (kirun-rust only): when the step's function fails and steps wait on this
    /// step's `error` event, they get the failure as that event instead of the run failing.
    /// Written only when set, so a definition without it reads and writes as before.
    pub catch_errors: bool,
}

impl Statement {
    pub const SCHEMA_NAME: &'static str = "Statement";

    pub fn new(statement_name: impl Into<String>, namespace: impl Into<String>, name: impl Into<String>) -> Self {
        Self {
            statement_name: statement_name.into(),
            namespace: namespace.into(),
            name: name.into(),
            ..Default::default()
        }
    }

    pub fn get_statement_name(&self) -> &str {
        &self.statement_name
    }
    pub fn set_statement_name(mut self, v: impl Into<String>) -> Self {
        self.statement_name = v.into();
        self
    }
    pub fn get_namespace(&self) -> &str {
        &self.namespace
    }
    pub fn set_namespace(mut self, v: impl Into<String>) -> Self {
        self.namespace = v.into();
        self
    }
    pub fn get_name(&self) -> &str {
        &self.name
    }
    pub fn set_name(mut self, v: impl Into<String>) -> Self {
        self.name = v.into();
        self
    }
    pub fn get_parameter_map(&self) -> &Map<Map<ParameterReference>> {
        &self.parameter_map
    }
    pub fn set_parameter_map(mut self, v: Map<Map<ParameterReference>>) -> Self {
        self.parameter_map = v;
        self
    }
    pub fn get_dependent_statements(&self) -> &Map<bool> {
        &self.dependent_statements
    }
    pub fn set_dependent_statements(mut self, v: Map<bool>) -> Self {
        self.dependent_statements = v;
        self
    }
    pub fn get_execute_iftrue(&self) -> &Map<bool> {
        &self.execute_iftrue
    }
    pub fn set_execute_iftrue(mut self, v: Map<bool>) -> Self {
        self.execute_iftrue = v;
        self
    }
    pub fn get_comment(&self) -> Option<&str> {
        self.comment.as_deref()
    }
    pub fn set_comment(mut self, v: impl Into<String>) -> Self {
        self.comment = Some(v.into());
        self
    }
    pub fn get_description(&self) -> Option<&str> {
        self.description.as_deref()
    }
    pub fn set_description(mut self, v: impl Into<String>) -> Self {
        self.description = Some(v.into());
        self
    }
    pub fn get_position(&self) -> Option<&Position> {
        self.position.as_ref()
    }
    pub fn set_position(mut self, v: Option<Position>) -> Self {
        self.position = v;
        self
    }
    pub fn is_override(&self) -> bool {
        self.r#override
    }
    pub fn set_override(mut self, v: bool) -> Self {
        self.r#override = v;
        self
    }

    pub fn is_catch_errors(&self) -> bool {
        self.catch_errors
    }
    pub fn set_catch_errors(mut self, v: bool) -> Self {
        self.catch_errors = v;
        self
    }

    pub fn of_entry(statement: Statement) -> (String, Statement) {
        (statement.statement_name.clone(), statement)
    }

    pub fn from_value(json: &Value) -> Result<Statement> {
        let mut parameter_map = Map::new();
        if let Some(obj) = json["parameterMap"].as_object() {
            for (k, v) in obj {
                let mut refs = Map::new();
                if let Some(inner) = v.as_object() {
                    for iv in inner.values() {
                        let pr = ParameterReference::from_value(iv)?;
                        refs.insert(pr.key.clone(), pr);
                    }
                }
                parameter_map.insert(k.clone(), refs);
            }
        }
        let bool_map = |v: &Value| -> Map<bool> {
            v.as_object()
                .map(|o| o.iter().map(|(k, b)| (k.clone(), b.as_bool().unwrap_or(false))).collect())
                .unwrap_or_default()
        };
        Ok(Statement {
            statement_name: json["statementName"].as_str().unwrap_or_default().to_string(),
            namespace: json["namespace"].as_str().unwrap_or_default().to_string(),
            name: json["name"].as_str().unwrap_or_default().to_string(),
            parameter_map,
            dependent_statements: bool_map(&json["dependentStatements"]),
            execute_iftrue: bool_map(&json["executeIftrue"]),
            comment: json["comment"].as_str().map(str::to_string),
            description: json["description"].as_str().map(str::to_string),
            position: Position::from_value(&json["position"]),
            r#override: json["override"].as_bool().unwrap_or(false),
            catch_errors: json["catchErrors"].as_bool().unwrap_or(false),
        })
    }

    pub fn to_json(&self) -> Value {
        let mut pm = serde_json::Map::new();
        for (k, v) in &self.parameter_map {
            pm.insert(
                k.clone(),
                Value::Object(v.iter().map(|(ik, iv)| (ik.clone(), iv.to_json())).collect()),
            );
        }
        let bool_obj = |m: &Map<bool>| -> Value {
            Value::Object(m.iter().map(|(k, b)| (k.clone(), Value::Bool(*b))).collect())
        };
        let mut obj = serde_json::Map::new();
        obj.insert("statementName".into(), Value::String(self.statement_name.clone()));
        obj.insert("namespace".into(), Value::String(self.namespace.clone()));
        obj.insert("name".into(), Value::String(self.name.clone()));
        obj.insert("parameterMap".into(), Value::Object(pm));
        obj.insert("dependentStatements".into(), bool_obj(&self.dependent_statements));
        obj.insert("executeIftrue".into(), bool_obj(&self.execute_iftrue));
        if let Some(p) = &self.position {
            obj.insert("position".into(), p.to_json());
        }
        if let Some(c) = &self.comment {
            obj.insert("comment".into(), Value::String(c.clone()));
        }
        if let Some(d) = &self.description {
            obj.insert("description".into(), Value::String(d.clone()));
        }
        obj.insert("override".into(), Value::Bool(self.r#override));
        if self.catch_errors {
            obj.insert("catchErrors".into(), Value::Bool(true));
        }
        Value::Object(obj)
    }
}
