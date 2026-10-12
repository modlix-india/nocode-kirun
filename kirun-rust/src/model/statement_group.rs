//! Port of kirun-js `model/StatementGroup.ts`.

use crate::model::Position;
use crate::{Map, Result, Value};

#[derive(Debug, Clone, PartialEq, Default)]
pub struct StatementGroup {
    pub statement_group_name: String,
    pub statements: Map<bool>,
    pub comment: Option<String>,
    pub description: Option<String>,
    pub position: Option<Position>,
    pub r#override: bool,
}

impl StatementGroup {
    pub fn new(statement_group_name: impl Into<String>, statements: Map<bool>) -> Self {
        Self { statement_group_name: statement_group_name.into(), statements, ..Default::default() }
    }
    pub fn get_statement_group_name(&self) -> &str {
        &self.statement_group_name
    }
    pub fn get_statements(&self) -> &Map<bool> {
        &self.statements
    }
    pub fn set_statements(mut self, statements: Map<bool>) -> Self {
        self.statements = statements;
        self
    }

    pub fn from_value(json: &Value) -> Result<StatementGroup> {
        let statements = json["statements"]
            .as_object()
            .map(|o| {
                o.iter()
                    .map(|(k, v)| {
                        let s = match v {
                            Value::String(s) => s.to_lowercase(),
                            other => other.to_string(),
                        };
                        (k.clone(), s == "true")
                    })
                    .collect()
            })
            .unwrap_or_default();
        Ok(StatementGroup {
            statement_group_name: json["statementGroupName"].as_str().unwrap_or_default().to_string(),
            statements,
            comment: json["comment"].as_str().map(str::to_string),
            description: json["description"].as_str().map(str::to_string),
            position: Position::from_value(&json["position"]),
            r#override: json["override"].as_bool().unwrap_or(false),
        })
    }

    pub fn to_json(&self) -> Value {
        let mut obj = serde_json::Map::new();
        obj.insert("statementGroupName".into(), Value::String(self.statement_group_name.clone()));
        obj.insert(
            "statements".into(),
            Value::Object(self.statements.iter().map(|(k, b)| (k.clone(), Value::Bool(*b))).collect()),
        );
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
        Value::Object(obj)
    }
}
