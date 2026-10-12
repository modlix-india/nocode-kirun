//! Port of kirun-js `model/ParameterReference.ts` and `model/ParameterReferenceType.ts`.

use crate::{KIRunError, Result, Value};

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum ParameterReferenceType {
    Value,
    Expression,
}

impl ParameterReferenceType {
    pub fn name(&self) -> &'static str {
        match self {
            ParameterReferenceType::Value => "VALUE",
            ParameterReferenceType::Expression => "EXPRESSION",
        }
    }

    pub fn parse(s: &str) -> Result<Self> {
        match s.to_ascii_uppercase().as_str() {
            "VALUE" => Ok(ParameterReferenceType::Value),
            "EXPRESSION" => Ok(ParameterReferenceType::Expression),
            _ => Err(KIRunError::runtime(format!("Unknown parameter reference type : {s}"))),
        }
    }
}

#[derive(Debug, Clone, PartialEq)]
pub struct ParameterReference {
    pub key: String,
    pub r#type: ParameterReferenceType,
    pub value: Value,
    pub expression: Option<String>,
    pub order: Option<i64>,
}

impl ParameterReference {
    /// A new reference with a random UUID key.
    pub fn new(r#type: ParameterReferenceType) -> Self {
        Self {
            key: crate::util::uuid::uuid(),
            r#type,
            value: Value::Null,
            expression: None,
            order: None,
        }
    }

    pub fn get_type(&self) -> ParameterReferenceType {
        self.r#type
    }
    pub fn set_type(mut self, t: ParameterReferenceType) -> Self {
        self.r#type = t;
        self
    }
    pub fn get_key(&self) -> &str {
        &self.key
    }
    pub fn set_key(mut self, key: impl Into<String>) -> Self {
        self.key = key.into();
        self
    }
    pub fn get_value(&self) -> &Value {
        &self.value
    }
    pub fn set_value(mut self, value: Value) -> Self {
        self.value = value;
        self
    }
    pub fn get_expression(&self) -> Option<&str> {
        self.expression.as_deref()
    }
    pub fn set_expression(mut self, expression: impl Into<String>) -> Self {
        self.expression = Some(expression.into());
        self
    }
    pub fn get_order(&self) -> Option<i64> {
        self.order
    }
    pub fn set_order(mut self, order: i64) -> Self {
        self.order = Some(order);
        self
    }

    pub fn of_expression(expression: &str) -> (String, ParameterReference) {
        let p = ParameterReference::new(ParameterReferenceType::Expression).set_expression(expression);
        (p.key.clone(), p)
    }

    pub fn of_value(value: Value) -> (String, ParameterReference) {
        let p = ParameterReference::new(ParameterReferenceType::Value).set_value(value);
        (p.key.clone(), p)
    }

    pub fn from_value(e: &Value) -> Result<ParameterReference> {
        // Stored definitions sometimes carry the type as a one-element array (`["EXPRESSION"]`);
        // anything unrecognised is a VALUE reference.
        let type_str = match &e["type"] {
            Value::String(s) => Some(s.as_str()),
            Value::Array(a) => a.iter().find_map(Value::as_str),
            _ => None,
        };
        let r#type = type_str
            .and_then(|s| ParameterReferenceType::parse(s).ok())
            .unwrap_or(ParameterReferenceType::Value);
        let mut p = ParameterReference::new(r#type);
        p.value = e["value"].clone();
        p.expression = e["expression"].as_str().map(str::to_string);
        if let Some(k) = e["key"].as_str() {
            p.key = k.to_string();
        }
        p.order = e["order"].as_i64();
        Ok(p)
    }

    pub fn to_json(&self) -> Value {
        let mut obj = serde_json::Map::new();
        obj.insert("key".into(), Value::String(self.key.clone()));
        obj.insert("type".into(), Value::String(self.r#type.name().into()));
        if !self.value.is_null() {
            obj.insert("value".into(), self.value.clone());
        }
        if let Some(e) = &self.expression {
            obj.insert("expression".into(), Value::String(e.clone()));
        }
        if let Some(o) = self.order {
            obj.insert("order".into(), Value::from(o));
        }
        Value::Object(obj)
    }
}
