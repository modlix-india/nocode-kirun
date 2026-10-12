//! Port of kirun-js `model/FunctionDefinition.ts`. JS `extends FunctionSignature` becomes
//! a `signature` field plus `Deref`, so `fd.get_name()` etc. work unchanged.

use std::ops::{Deref, DerefMut};

use crate::model::{Event, FunctionSignature, Parameter, Statement, StatementGroup};
use crate::{Map, Result, Value};

#[derive(Debug, Clone, PartialEq)]
pub struct FunctionDefinition {
    pub signature: FunctionSignature,
    pub version: i64,
    pub steps: Map<Statement>,
    pub step_groups: Option<Map<StatementGroup>>,
    pub parts: Option<Vec<FunctionDefinition>>,
}

impl Deref for FunctionDefinition {
    type Target = FunctionSignature;
    fn deref(&self) -> &FunctionSignature {
        &self.signature
    }
}

impl DerefMut for FunctionDefinition {
    fn deref_mut(&mut self) -> &mut FunctionSignature {
        &mut self.signature
    }
}

impl FunctionDefinition {
    pub fn new(name: impl Into<String>) -> Self {
        Self {
            signature: FunctionSignature::new(name),
            version: 1,
            steps: Map::new(),
            step_groups: None,
            parts: None,
        }
    }

    pub fn get_version(&self) -> i64 {
        self.version
    }
    pub fn set_version(mut self, version: i64) -> Self {
        self.version = version;
        self
    }
    pub fn get_steps(&self) -> &Map<Statement> {
        &self.steps
    }
    pub fn set_steps(mut self, steps: Map<Statement>) -> Self {
        self.steps = steps;
        self
    }
    pub fn get_step_groups(&self) -> Option<&Map<StatementGroup>> {
        self.step_groups.as_ref()
    }
    pub fn set_step_groups(mut self, groups: Map<StatementGroup>) -> Self {
        self.step_groups = Some(groups);
        self
    }
    pub fn get_parts(&self) -> Option<&Vec<FunctionDefinition>> {
        self.parts.as_ref()
    }
    pub fn set_parts(mut self, parts: Vec<FunctionDefinition>) -> Self {
        self.parts = Some(parts);
        self
    }

    // Builder versions of the FunctionSignature setters, so chains stay `FunctionDefinition`.
    pub fn set_namespace(mut self, namespace: impl Into<String>) -> Self {
        self.signature.namespace = namespace.into();
        self
    }
    pub fn set_name(mut self, name: impl Into<String>) -> Self {
        self.signature.name = name.into();
        self
    }
    pub fn set_parameters(mut self, parameters: Map<Parameter>) -> Self {
        self.signature.parameters = parameters;
        self
    }
    pub fn set_events(mut self, events: Map<Event>) -> Self {
        self.signature.events = events;
        self
    }

    pub fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    pub fn from_value(json: &Value) -> Result<FunctionDefinition> {
        if json.is_null() {
            return Ok(FunctionDefinition::new("unknown"));
        }
        let values = |v: &Value| -> Vec<Value> {
            match v {
                Value::Object(o) => o.values().filter(|e| !e.is_null()).cloned().collect(),
                Value::Array(a) => a.iter().filter(|e| !e.is_null()).cloned().collect(),
                _ => Vec::new(),
            }
        };

        let mut steps = Map::new();
        for s in values(&json["steps"]) {
            let st = Statement::from_value(&s)?;
            steps.insert(st.statement_name.clone(), st);
        }
        let mut groups = Map::new();
        for g in values(&json["stepGroups"]) {
            let sg = StatementGroup::from_value(&g)?;
            groups.insert(sg.statement_group_name.clone(), sg);
        }
        let mut parts = Vec::new();
        for p in values(&json["parts"]) {
            parts.push(FunctionDefinition::from_value(&p)?);
        }
        let mut events = Map::new();
        for e in values(&json["events"]) {
            let ev = Event::from_value(&e)?;
            events.insert(ev.name.clone(), ev);
        }
        let mut parameters = Map::new();
        for p in values(&json["parameters"]) {
            let pa = Parameter::from_value(&p)?;
            parameters.insert(pa.parameter_name.clone(), pa);
        }

        Ok(FunctionDefinition::new(json["name"].as_str().unwrap_or_default())
            .set_steps(steps)
            .set_step_groups(groups)
            .set_parts(parts)
            .set_version(json["version"].as_i64().unwrap_or(1))
            .set_events(events)
            .set_parameters(parameters)
            .set_namespace(json["namespace"].as_str().unwrap_or_default()))
    }

    /// JS `FunctionDefinition.fromText`: compile DSL text.
    pub fn from_text(text: &str) -> Result<FunctionDefinition> {
        let json = crate::dsl::DSLCompiler::compile(text)?;
        FunctionDefinition::from_value(&json)
    }

    /// JS `toText`: decompile to DSL text.
    pub fn to_text(&self) -> Result<String> {
        crate::dsl::DSLCompiler::decompile(&self.to_json())
    }

    pub fn to_json(&self) -> Value {
        let mut obj = match self.signature.to_json() {
            Value::Object(o) => o,
            _ => unreachable!(),
        };
        obj.insert("version".into(), Value::from(self.version));
        obj.insert(
            "steps".into(),
            Value::Object(self.steps.iter().map(|(k, s)| (k.clone(), s.to_json())).collect()),
        );
        obj.insert(
            "stepGroups".into(),
            Value::Object(
                self.step_groups
                    .iter()
                    .flatten()
                    .map(|(k, g)| (k.clone(), g.to_json()))
                    .collect(),
            ),
        );
        obj.insert(
            "parts".into(),
            Value::Array(self.parts.iter().flatten().map(|p| p.to_json()).collect()),
        );
        Value::Object(obj)
    }
}
