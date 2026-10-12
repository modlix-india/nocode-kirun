//! Rust runtime for KIRun (Kinetic Instructions). Port of kirun-js; see DESIGN.md.

pub mod dsl;
pub mod exception;
pub mod function;
pub mod hybrid_repository;
pub mod json;
pub mod model;
pub mod namespaces;
pub mod repository;
pub mod runtime;
pub mod util;

use std::sync::Arc;

pub use exception::{KIRunError, Result};

/// Every JSON value in the engine. JS `undefined` and `null` are both `Value::Null`.
pub type Value = serde_json::Value;

/// Insertion-ordered map, the Rust stand-in for a JS `Map<string, V>`.
pub type Map<V> = indexmap::IndexMap<String, V>;

/// State that JS shares by reference between executions (context, steps, events).
pub type Shared<T> = Arc<parking_lot::RwLock<T>>;

pub fn shared<T>(value: T) -> Shared<T> {
    Arc::new(parking_lot::RwLock::new(value))
}
