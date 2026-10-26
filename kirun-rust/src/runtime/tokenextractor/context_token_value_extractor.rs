//! Port of kirun-js `runtime/tokenextractor/ContextTokenValueExtractor.ts`.

use crate::runtime::expression::tokenextractor::token_value_extractor::{
    key_and_parts_ref, plain_path_segments, retrieve_element_from, split_path_ref, value_at_mut, TokenValueExtractor,
    ValuesMap,
};
use crate::{Result, Value};
#[allow(unused_imports)]
use crate::{Map, Shared};
#[allow(unused_imports)]
use crate::runtime::function_execution_parameters::{ContextMap, StepsMap};

pub struct ContextTokenValueExtractor {
    store: Shared<ContextMap>,
}

impl ContextTokenValueExtractor {
    pub const PREFIX: &'static str = "Context.";

    pub fn new(store: Shared<ContextMap>) -> Self {
        Self { store }
    }
}

impl TokenValueExtractor for ContextTokenValueExtractor {
    fn get_prefix(&self) -> &str {
        Self::PREFIX
    }

    fn get_store(&self) -> Value {
        Value::Object(self.store.read().iter().map(|(k, v)| (k.clone(), v.get_element().clone())).collect())
    }

    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        let (key, parts, from_index) = key_and_parts_ref(token);
        // Walk the stored value in place; a recursive read lets a dynamic index such as
        // `Context.a[Context.i]` read the store again without deadlocking on a waiting writer.
        let store = self.store.read_recursive();
        let element = store.get(key).map_or(&Value::Null, |e| e.get_element());
        retrieve_element_from(token, &parts, from_index, element, values_map)
    }

    fn write_back(&self, token: &str, value: Value) -> bool {
        let parts = split_path_ref(token);
        let Some(segments) = parts.get(1..).and_then(plain_path_segments) else { return false };
        let Some((key, rest)) = segments.split_first() else { return false };
        let mut store = self.store.write();
        let Some(el) = store.get_mut(key) else { return false };
        match value_at_mut(&mut el.element, rest) {
            Some(slot) => {
                *slot = value;
                true
            }
            None => false,
        }
    }
}
