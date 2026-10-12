//! Port of kirun-js `model/FunctionSignature.ts`.

use crate::json::schema::Schema;
use crate::model::{Event, Parameter};
use crate::{Map, Value};

#[derive(Debug, Clone, PartialEq)]
pub struct FunctionSignature {
    pub namespace: String,
    pub name: String,
    pub parameters: Map<Parameter>,
    pub events: Map<Event>,
    pub description: Option<String>,
    pub documentation: Option<String>,
    pub metadata: Option<Map<Value>>,
}

impl Default for FunctionSignature {
    /// JS defaults the namespace to `_`.
    fn default() -> Self {
        Self {
            namespace: crate::json::schema::TEMPORARY.to_string(),
            name: String::new(),
            parameters: Map::new(),
            events: Map::new(),
            description: None,
            documentation: None,
            metadata: None,
        }
    }
}

impl FunctionSignature {
    pub const SCHEMA_NAME: &'static str = "FunctionSignature";

    pub fn new(name: impl Into<String>) -> Self {
        Self { name: name.into(), ..Default::default() }
    }

    /// JS `FunctionSignature.SCHEMA`.
    pub fn schema() -> &'static Schema {
        static SCHEMA: std::sync::LazyLock<Schema> = std::sync::LazyLock::new(|| {
            use crate::json::schema::AdditionalType;
            Schema::new()
                .set_namespace(crate::namespaces::Namespaces::SYSTEM)
                .set_name(FunctionSignature::SCHEMA_NAME)
                .set_properties(Map::from([
                    ("name".to_string(), Schema::of_string("name")),
                    ("namespace".to_string(), Schema::of_string("namespace")),
                    (
                        "parameters".to_string(),
                        Schema::of_object("parameters")
                            .set_additional_properties(AdditionalType::new().set_schema_value(Parameter::schema().clone())),
                    ),
                    (
                        "events".to_string(),
                        Schema::of_object("events")
                            .set_additional_properties(AdditionalType::new().set_schema_value(Event::schema().clone())),
                    ),
                ]))
        });
        &SCHEMA
    }

    pub fn get_namespace(&self) -> &str {
        &self.namespace
    }
    pub fn set_namespace(mut self, namespace: impl Into<String>) -> Self {
        self.namespace = namespace.into();
        self
    }
    pub fn get_name(&self) -> &str {
        &self.name
    }
    pub fn set_name(mut self, name: impl Into<String>) -> Self {
        self.name = name.into();
        self
    }
    pub fn get_parameters(&self) -> &Map<Parameter> {
        &self.parameters
    }
    pub fn set_parameters(mut self, parameters: Map<Parameter>) -> Self {
        self.parameters = parameters;
        self
    }
    pub fn get_events(&self) -> &Map<Event> {
        &self.events
    }
    pub fn set_events(mut self, events: Map<Event>) -> Self {
        self.events = events;
        self
    }
    pub fn get_full_name(&self) -> String {
        format!("{}.{}", self.namespace, self.name)
    }
    pub fn get_description(&self) -> Option<&str> {
        self.description.as_deref()
    }
    pub fn set_description(mut self, description: impl Into<String>) -> Self {
        self.description = Some(description.into());
        self
    }
    pub fn get_documentation(&self) -> Option<&str> {
        self.documentation.as_deref()
    }
    pub fn set_documentation(mut self, documentation: impl Into<String>) -> Self {
        self.documentation = Some(documentation.into());
        self
    }
    pub fn get_metadata(&self) -> Option<&Map<Value>> {
        self.metadata.as_ref()
    }
    pub fn set_metadata(mut self, metadata: Map<Value>) -> Self {
        self.metadata = Some(metadata);
        self
    }

    pub fn to_json(&self) -> Value {
        let mut obj = serde_json::Map::new();
        obj.insert("namespace".into(), Value::String(self.namespace.clone()));
        obj.insert("name".into(), Value::String(self.name.clone()));
        obj.insert(
            "parameters".into(),
            Value::Object(self.parameters.iter().map(|(k, p)| (k.clone(), p.to_json())).collect()),
        );
        obj.insert(
            "events".into(),
            Value::Object(self.events.iter().map(|(k, e)| (k.clone(), e.to_json())).collect()),
        );
        if let Some(d) = &self.description {
            obj.insert("description".into(), Value::String(d.clone()));
        }
        if let Some(d) = &self.documentation {
            obj.insert("documentation".into(), Value::String(d.clone()));
        }
        if let Some(m) = &self.metadata {
            obj.insert(
                "metadata".into(),
                Value::Object(m.iter().map(|(k, v)| (k.clone(), v.clone())).collect()),
            );
        }
        Value::Object(obj)
    }
}
