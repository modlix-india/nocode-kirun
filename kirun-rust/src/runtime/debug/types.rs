//! Port of kirun-js `runtime/debug/types.ts`.

use std::sync::Arc;

use serde::Serialize;

use crate::{Map, Value};

/// Log entry for a single step execution. Times are milliseconds since the epoch (JS `Date.now()`).
#[derive(Debug, Clone, PartialEq, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct LogEntry {
    pub step_id: String,
    pub timestamp: i64,
    pub function_name: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub statement_name: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub kirun_function_name: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub duration: Option<i64>,
    /// JS `arguments` (a serialized plain object; `Null` when absent).
    pub arguments: Value,
    pub result: Value,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub event_name: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub error: Option<String>,
    pub children: Vec<LogEntry>,
}

/// Complete execution log.
#[derive(Debug, Clone, PartialEq)]
pub struct ExecutionLog {
    pub execution_id: String,
    pub start_time: i64,
    pub end_time: Option<i64>,
    pub errored: bool,
    pub logs: Vec<LogEntry>,
    /// Function full name → definition JSON (JS stores `fd.toJSON()`).
    pub definitions: Map<Value>,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum DebugEventType {
    ExecutionStart,
    ExecutionEnd,
    StepStart,
    StepEnd,
    ExecutionErrored,
}

impl DebugEventType {
    /// The JS string value (`executionStart`, ...).
    pub fn name(&self) -> &'static str {
        match self {
            DebugEventType::ExecutionStart => "executionStart",
            DebugEventType::ExecutionEnd => "executionEnd",
            DebugEventType::StepStart => "stepStart",
            DebugEventType::StepEnd => "stepEnd",
            DebugEventType::ExecutionErrored => "executionErrored",
        }
    }
}

/// JS `{ type, executionId, data }`. `data` is the JS object as JSON (`Null` when absent);
/// for `stepEnd` it is `{ "log": <LogEntry as camelCase JSON> }`.
#[derive(Debug, Clone, PartialEq)]
pub struct DebugEvent {
    pub event_type: DebugEventType,
    pub execution_id: String,
    pub data: Value,
}

pub type DebugEventListener = Arc<dyn Fn(&DebugEvent) + Send + Sync>;
