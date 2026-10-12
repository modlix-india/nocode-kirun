//! Port of kirun-js `model/Position.ts`.

use crate::Value;

#[derive(Debug, Clone, Copy, PartialEq)]
pub struct Position {
    pub left: f64,
    pub top: f64,
}

impl Position {
    pub const SCHEMA_NAME: &'static str = "Position";

    pub fn new(left: f64, top: f64) -> Self {
        Self { left, top }
    }
    pub fn get_left(&self) -> f64 {
        self.left
    }
    pub fn set_left(mut self, left: f64) -> Self {
        self.left = left;
        self
    }
    pub fn get_top(&self) -> f64 {
        self.top
    }
    pub fn set_top(mut self, top: f64) -> Self {
        self.top = top;
        self
    }
    pub fn from_value(json: &Value) -> Option<Position> {
        if json.is_null() {
            return None;
        }
        Some(Position::new(
            json["left"].as_f64().unwrap_or(0.0),
            json["top"].as_f64().unwrap_or(0.0),
        ))
    }
    pub fn to_json(&self) -> Value {
        serde_json::json!({ "left": crate::util::primitive::number_value(self.left), "top": crate::util::primitive::number_value(self.top) })
    }
}
