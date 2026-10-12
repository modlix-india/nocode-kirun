//! Port of kirun-js `function/system/loop/CountLoop.ts`.

use async_trait::async_trait;

use crate::function::system::r#loop::{break_requested, clear_break};
use crate::function::AbstractFunction;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{Map, Result, Value};

const COUNT: &str = "count";
const VALUE: &str = "value";
const INDEX: &str = "index";

pub struct CountLoop {
    signature: FunctionSignature,
}

impl CountLoop {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("CountLoop")
                .set_namespace(Namespaces::SYSTEM_LOOP)
                .set_parameters(Map::from([Parameter::of_entry(
                    COUNT,
                    Schema::of(COUNT, &[SchemaType::Integer]),
                    false,
                )]))
                .set_events(Map::from([
                    Event::event_map_entry(
                        Event::ITERATION,
                        Map::from([(INDEX.to_string(), Schema::of(INDEX, &[SchemaType::Integer]))]),
                    ),
                    Event::output_event_map_entry(Map::from([(
                        VALUE.to_string(),
                        Schema::of(VALUE, &[SchemaType::Integer]),
                    )])),
                ])),
        }
    }
}

impl Default for CountLoop {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for CountLoop {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let count = context.get_arguments().and_then(|a| a.get(COUNT)).and_then(Value::as_f64).unwrap_or(0.0);
        let statement_name = context.get_statement_execution().map(|s| s.get_statement().get_statement_name().to_string());
        let execution_context = context.get_execution_context().clone();
        let mut current: i64 = 0;

        // Like JS, this keeps yielding `output` once the count is reached (no done flag).
        Ok(FunctionOutput::from_generator(move || {
            let name = statement_name.as_deref();
            if current as f64 >= count || break_requested(&execution_context, name) {
                clear_break(&execution_context, name);
                return Some(EventResult::output_of(Map::from([(VALUE.to_string(), Value::from(current))])));
            }
            let eve = EventResult::of(Event::ITERATION, Map::from([(INDEX.to_string(), Value::from(current))]));
            current += 1;
            Some(eve)
        }))
    }
}
