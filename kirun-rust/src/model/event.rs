//! Port of kirun-js `model/Event.ts`.

use crate::json::schema::Schema;
use crate::{KIRunError, Map, Result, Value};

#[derive(Debug, Clone, PartialEq)]
pub struct Event {
    pub name: String,
    pub parameters: Map<Schema>,
}

impl Event {
    pub const OUTPUT: &'static str = "output";
    pub const ERROR: &'static str = "error";
    pub const ITERATION: &'static str = "iteration";
    pub const TRUE: &'static str = "true";
    pub const FALSE: &'static str = "false";
    pub const SCHEMA_NAME: &'static str = "Event";

    pub fn new(name: impl Into<String>, parameters: Map<Schema>) -> Self {
        Self { name: name.into(), parameters }
    }

    /// JS `Event.SCHEMA`.
    pub fn schema() -> &'static Schema {
        static SCHEMA: std::sync::LazyLock<Schema> = std::sync::LazyLock::new(|| {
            use crate::json::schema::r#type::{SchemaType, TypeUtil};
            use crate::json::schema::AdditionalType;
            Schema::new()
                .set_namespace(crate::namespaces::Namespaces::SYSTEM)
                .set_name(Event::SCHEMA_NAME)
                .set_type(TypeUtil::of(&[SchemaType::Object]))
                .set_properties(Map::from([
                    ("name".to_string(), Schema::of_string("name")),
                    (
                        "parameters".to_string(),
                        Schema::of_object("parameter")
                            .set_additional_properties(AdditionalType::new().set_schema_value(Schema::schema().clone())),
                    ),
                ]))
        });
        &SCHEMA
    }

    pub fn get_name(&self) -> &str {
        &self.name
    }
    pub fn set_name(mut self, name: impl Into<String>) -> Self {
        self.name = name.into();
        self
    }
    pub fn get_parameters(&self) -> &Map<Schema> {
        &self.parameters
    }
    pub fn set_parameters(mut self, parameters: Map<Schema>) -> Self {
        self.parameters = parameters;
        self
    }

    pub fn output_event_map_entry(parameters: Map<Schema>) -> (String, Event) {
        Event::event_map_entry(Event::OUTPUT, parameters)
    }

    pub fn event_map_entry(event_name: &str, parameters: Map<Schema>) -> (String, Event) {
        (event_name.to_string(), Event::new(event_name, parameters))
    }

    pub fn from_value(json: &Value) -> Result<Event> {
        let mut parameters = Map::new();
        if let Some(obj) = json["parameters"].as_object() {
            for (k, v) in obj {
                let s = Schema::from_value(v)?
                    .ok_or_else(|| KIRunError::schema_reference("", "Event expects a schema"))?;
                parameters.insert(k.clone(), s);
            }
        }
        Ok(Event::new(json["name"].as_str().unwrap_or_default(), parameters))
    }

    pub fn to_json(&self) -> Value {
        let params: serde_json::Map<String, Value> = self
            .parameters
            .iter()
            .map(|(k, v)| (k.clone(), v.to_json()))
            .collect();
        serde_json::json!({ "name": self.name, "parameters": params })
    }
}
