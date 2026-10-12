//! Port of kirun-js `runtime/tokenextractor/OutputMapTokenValueExtractor.ts`.

use crate::runtime::expression::tokenextractor::token_value_extractor::{
    retrieve_element_from, split_path_ref, TokenValueExtractor, ValuesMap,
};
use crate::{Result, Value};
#[allow(unused_imports)]
use crate::{Map, Shared};
#[allow(unused_imports)]
use crate::runtime::function_execution_parameters::{ContextMap, StepsMap};

pub struct OutputMapTokenValueExtractor {
    store: Shared<StepsMap>,
}

impl OutputMapTokenValueExtractor {
    pub const PREFIX: &'static str = "Steps.";

    pub fn new(store: Shared<StepsMap>) -> Self {
        Self { store }
    }
}

impl TokenValueExtractor for OutputMapTokenValueExtractor {
    fn get_prefix(&self) -> &str {
        Self::PREFIX
    }

    fn get_store(&self) -> Value {
        let steps = self.store.read();
        Value::Object(
            steps
                .iter()
                .map(|(step, events)| {
                    let ev: serde_json::Map<String, Value> = events
                        .iter()
                        .map(|(event, results)| {
                            (event.clone(), Value::Object(results.iter().map(|(k, v)| (k.clone(), v.clone())).collect()))
                        })
                        .collect();
                    (step.clone(), Value::Object(ev))
                })
                .collect(),
        )
    }

    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        let mut parts = split_path_ref(token);
        let steps = self.store.read_recursive();

        let mut ind = 1;
        let Some(events) = parts.get(ind).and_then(|p| steps.get(*p)) else { return Ok(Value::Null) };
        ind += 1;
        if ind >= parts.len() {
            return Ok(Value::Null);
        }

        let Some(each_event) = events.get(parts[ind]) else { return Ok(Value::Null) };
        ind += 1;
        if ind == parts.len() {
            return Ok(Value::Object(each_event.iter().map(|(k, v)| (k.clone(), v.clone())).collect()));
        }

        let part = parts[ind];
        match part.find('[') {
            None => {
                let element = each_event.get(part).unwrap_or(&Value::Null);
                retrieve_element_from(token, &parts, ind + 1, element, values_map)
            }
            Some(bracket) => {
                // `result[2]`: start from `result` and walk the bracket tail (JS wraps the
                // value in a one-key object and walks the whole part; the result is the same).
                let element = each_event.get(&part[..bracket]).unwrap_or(&Value::Null);
                parts[ind] = &part[bracket..];
                retrieve_element_from(token, &parts, ind, element, values_map)
            }
        }
    }
}
