//! Port of kirun-js `model/EventResult.ts`.

use crate::model::Event;
use crate::{Map, Value};

#[derive(Debug, Clone, PartialEq)]
pub struct EventResult {
    pub name: String,
    pub result: Map<Value>,
}

impl EventResult {
    pub fn new(name: impl Into<String>, result: Map<Value>) -> Self {
        Self { name: name.into(), result }
    }
    pub fn get_name(&self) -> &str {
        &self.name
    }
    pub fn set_name(mut self, name: impl Into<String>) -> Self {
        self.name = name.into();
        self
    }
    pub fn get_result(&self) -> &Map<Value> {
        &self.result
    }
    pub fn set_result(mut self, result: Map<Value>) -> Self {
        self.result = result;
        self
    }
    pub fn output_of(result: Map<Value>) -> Self {
        EventResult::of(Event::OUTPUT, result)
    }
    pub fn of(event_name: &str, result: Map<Value>) -> Self {
        EventResult::new(event_name, result)
    }
}
