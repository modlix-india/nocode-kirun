//! Ports of kirun-js `__tests__/engine/json/schema/**`.

#[path = "../common/mod.rs"]
mod common;

mod array_contains_validator_test;
mod array_schema_adapter_type_test;
mod array_schema_type_test;
mod array_validator_test;
mod any_of_all_of_one_of_enforced_test;
mod any_of_all_of_one_of_validator_test;
mod boolean_convertor_test;
mod not_validator_test;
mod null_convertor_test;
mod null_validator_test;
mod number_convertor_test;
mod number_validator_test;
mod object_properties_test;
mod object_validator_test;
mod schema_any_of_validator_test;
mod schema_copy_test;
mod schema_type_spelling_test;
mod schema_util;
mod schema_validator_test;
mod string_convertor_test;
mod string_format_schema_validator_test;
mod string_validator_test;
mod type_util_test;
mod type_validator_test;

// ---- helpers local to this test binary ----

use std::sync::Arc;

use async_trait::async_trait;
use kirun::json::schema::Schema;
use kirun::repository::Repository;
use kirun::{KIRunError, Map, Value};

/// Stand-in for the ad-hoc `Repository<Schema>` objects the JS tests declare inline.
/// `find` returns `None` for an empty namespace, or when `namespace` is set and differs;
/// otherwise looks `name` up in `map`. `filter` (when enabled) returns the full names
/// containing `name`, case-insensitively.
pub struct MapSchemaRepository {
    pub namespace: Option<String>,
    pub map: Map<Arc<Schema>>,
    pub filter_enabled: bool,
}

impl MapSchemaRepository {
    pub fn new(namespace: Option<&str>, entries: Vec<(&str, Schema)>, filter_enabled: bool) -> Self {
        Self {
            namespace: namespace.map(str::to_string),
            map: entries.into_iter().map(|(k, v)| (k.to_string(), Arc::new(v))).collect(),
            filter_enabled,
        }
    }
}

#[async_trait]
impl Repository<Schema> for MapSchemaRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<Schema>> {
        if namespace.is_empty() {
            return None;
        }
        if let Some(ns) = &self.namespace {
            if ns != namespace {
                return None;
            }
        }
        self.map.get(name).cloned()
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        if !self.filter_enabled {
            return vec![];
        }
        let needle = name.to_lowercase();
        self.map
            .values()
            .map(|s| s.get_full_name())
            .filter(|n| n.to_lowercase().contains(&needle))
            .collect()
    }
}

/// JS `new Map<string, Schema>([[k, v], ...])` / `MapUtil.of(k, v, ...)`.
pub fn props(entries: Vec<(&str, Schema)>) -> Map<Schema> {
    entries.into_iter().map(|(k, v)| (k.to_string(), v)).collect()
}

/// `Schema.from(json)!`.
pub fn schema_from(v: Value) -> Schema {
    Schema::from_value(&v).expect("Schema::from_value failed").expect("Schema::from_value returned None")
}

/// JS `toMatchObject`: every property of `expected` is present (recursively) in `actual`.
pub fn match_object(actual: &Value, expected: &Value) -> bool {
    match (actual, expected) {
        (Value::Object(a), Value::Object(e)) => {
            e.iter().all(|(k, ev)| a.get(k).is_some_and(|av| match_object(av, ev)))
        }
        (Value::Array(a), Value::Array(e)) => {
            a.len() == e.len() && a.iter().zip(e).all(|(x, y)| match_object(x, y))
        }
        _ => common::json_eq(actual, expected),
    }
}

/// JS `toThrow(SchemaConversionException)`.
pub fn assert_conversion_error<T: std::fmt::Debug>(result: kirun::Result<T>) {
    match result {
        Err(KIRunError::SchemaConversion { .. }) => {}
        other => panic!("expected a SchemaConversion error, got {other:?}"),
    }
}
