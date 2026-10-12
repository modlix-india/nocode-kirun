//! Port of kirun-js `runtime/tokenextractor/ArgumentsTokenValueExtractor.ts`.

use crate::runtime::expression::tokenextractor::token_value_extractor::{
    key_and_parts_ref, retrieve_element_from, TokenValueExtractor, ValuesMap,
};
use crate::{Result, Value};
#[allow(unused_imports)]
use crate::{Map, Shared};
#[allow(unused_imports)]
use crate::runtime::function_execution_parameters::{ContextMap, StepsMap};

pub struct ArgumentsTokenValueExtractor {
    store: Map<Value>,
}

impl ArgumentsTokenValueExtractor {
    pub const PREFIX: &'static str = "Arguments.";

    pub fn new(store: Map<Value>) -> Self {
        Self { store }
    }
}

impl TokenValueExtractor for ArgumentsTokenValueExtractor {
    fn get_prefix(&self) -> &str {
        Self::PREFIX
    }

    fn get_store(&self) -> Value {
        Value::Object(self.store.iter().map(|(k, v)| (k.clone(), v.clone())).collect())
    }

    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        let (key, parts, from_index) = key_and_parts_ref(token);
        let element = self.store.get(key).unwrap_or(&Value::Null);
        retrieve_element_from(token, &parts, from_index, element, values_map)
    }
}
