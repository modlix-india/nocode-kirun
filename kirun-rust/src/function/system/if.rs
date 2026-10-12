//! Port of kirun-js `function/system/If.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::js_truthy;
use crate::{Map, Result, Value};

const CONDITION: &str = "condition";

pub struct If {
    signature: FunctionSignature,
}

impl If {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("If")
                .set_namespace(Namespaces::SYSTEM)
                .set_parameters(Map::from([Parameter::of_entry(CONDITION, Schema::of_any(CONDITION), false)]))
                .set_events(Map::from([
                    Event::event_map_entry(Event::TRUE, Map::new()),
                    Event::event_map_entry(Event::FALSE, Map::new()),
                    Event::output_event_map_entry(Map::new()),
                ])),
        }
    }
}

impl Default for If {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for If {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let condition = context.get_arguments().and_then(|a| a.get(CONDITION)).cloned().unwrap_or(Value::Null);

        // JS `!!condition || condition === ''`
        let condition_value = js_truthy(&condition) || condition == Value::String(String::new());

        Ok(FunctionOutput::new(vec![
            EventResult::of(if condition_value { Event::TRUE } else { Event::FALSE }, Map::new()),
            EventResult::output_of(Map::new()),
        ]))
    }
}
