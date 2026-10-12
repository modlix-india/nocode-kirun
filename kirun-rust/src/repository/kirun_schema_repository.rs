//! Port of kirun-js `repository/KIRunSchemaRepository.ts`.

use std::sync::Arc;

use async_trait::async_trait;

use crate::json::schema::{AdditionalType, Schema};
use crate::namespaces::Namespaces;
use crate::repository::Repository;
use crate::{Map, Value};

pub struct KIRunSchemaRepository {
    map: Map<Arc<Schema>>,
    filterable_names: Vec<String>,
}

fn props(entries: Vec<(&str, Schema)>) -> Map<Schema> {
    entries.into_iter().map(|(k, v)| (k.to_string(), v)).collect()
}

fn numbers(names: &[&str]) -> Map<Schema> {
    props(names.iter().map(|n| (*n, Schema::of_number(n))).collect())
}

/// JS `Parameter.EXPRESSION` (built here so the repository does not depend on the model layer).
fn parameter_expression() -> Schema {
    Schema::of_object("ParameterExpression").set_namespace(Namespaces::SYSTEM).set_properties(props(vec![
        ("isExpression", Schema::of_boolean("isExpression").set_default_value(Value::Bool(true))),
        ("value", Schema::of_any("value")),
    ]))
}

impl KIRunSchemaRepository {
    pub fn new() -> Self {
        let entries: Vec<(&str, Schema)> = vec![
            ("any", Schema::of_any("any").set_namespace(Namespaces::SYSTEM)),
            ("boolean", Schema::of_boolean("boolean").set_namespace(Namespaces::SYSTEM)),
            ("double", Schema::of_double("double").set_namespace(Namespaces::SYSTEM)),
            ("float", Schema::of_float("float").set_namespace(Namespaces::SYSTEM)),
            ("integer", Schema::of_integer("integer").set_namespace(Namespaces::SYSTEM)),
            ("long", Schema::of_long("long").set_namespace(Namespaces::SYSTEM)),
            ("number", Schema::of_number("number").set_namespace(Namespaces::SYSTEM)),
            ("string", Schema::of_string("string").set_namespace(Namespaces::SYSTEM)),
            ("Timestamp", Schema::of_string("Timestamp").set_namespace(Namespaces::DATE)),
            (
                "Timeunit",
                Schema::of_string("Timeunit").set_namespace(Namespaces::DATE).set_enums(
                    ["YEARS", "QUARTERS", "MONTHS", "WEEKS", "DAYS", "HOURS", "MINUTES", "SECONDS", "MILLISECONDS"]
                        .iter()
                        .map(|s| Value::String((*s).to_string()))
                        .collect(),
                ),
            ),
            (
                "Duration",
                Schema::of_object("Duration")
                    .set_namespace(Namespaces::DATE)
                    .set_properties(numbers(&[
                        "years",
                        "quarters",
                        "months",
                        "weeks",
                        "days",
                        "hours",
                        "minutes",
                        "seconds",
                        "milliseconds",
                    ]))
                    .set_additional_items(AdditionalType::new().set_boolean_value(false)),
            ),
            (
                "TimeObject",
                Schema::of_object("TimeObject")
                    .set_namespace(Namespaces::DATE)
                    .set_properties(numbers(&["year", "month", "day", "hour", "minute", "second", "millisecond"]))
                    .set_additional_items(AdditionalType::new().set_boolean_value(false)),
            ),
            ("ParameterExpression", parameter_expression()),
            ("Null", Schema::null().clone()),
            ("Schema", Schema::schema().clone()),
        ];

        let map: Map<Arc<Schema>> = entries.into_iter().map(|(k, v)| (k.to_string(), Arc::new(v))).collect();
        let filterable_names = map.values().map(|s| s.get_full_name()).collect();
        Self { map, filterable_names }
    }
}

impl Default for KIRunSchemaRepository {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl Repository<Schema> for KIRunSchemaRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<Schema>> {
        if namespace != Namespaces::SYSTEM && namespace != Namespaces::DATE {
            return None;
        }
        self.map.get(name).cloned()
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        let needle = name.to_lowercase();
        self.filterable_names.iter().filter(|e| e.to_lowercase().contains(&needle)).cloned().collect()
    }
}
