//! Port of kirun-js `runtime/expression/tokenextractor/ExpressionInternalValueExtractor.ts`.

use parking_lot::RwLock;

use super::token_value_extractor::{key_and_parts_ref, retrieve_element_from, TokenValueExtractor, ValuesMap};
use crate::{Map, Result, Value};

/// `values` sits behind a lock because the evaluator adds values while the extractor is
/// shared in the values map.
#[derive(Debug, Default)]
pub struct ExpressionInternalValueExtractor {
    values: RwLock<Map<Value>>,
}

impl Clone for ExpressionInternalValueExtractor {
    fn clone(&self) -> Self {
        Self { values: RwLock::new(self.values.read().clone()) }
    }
}

impl ExpressionInternalValueExtractor {
    pub const PREFIX: &'static str = "_internal.";

    pub fn new() -> Self {
        Self::default()
    }

    pub fn add_value(&self, key: impl Into<String>, value: Value) {
        self.values.write().insert(key.into(), value);
    }
}

impl TokenValueExtractor for ExpressionInternalValueExtractor {
    fn get_prefix(&self) -> &str {
        Self::PREFIX
    }

    fn get_store(&self) -> Value {
        Value::Null
    }

    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        let (key, parts, from_index) = key_and_parts_ref(token);
        let values = self.values.read_recursive();
        retrieve_element_from(token, &parts, from_index, values.get(key).unwrap_or(&Value::Null), values_map)
    }
}
