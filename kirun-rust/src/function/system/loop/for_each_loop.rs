//! Port of kirun-js `function/system/loop/ForEachLoop.ts`.

use async_trait::async_trait;

use crate::function::system::r#loop::{break_requested, clear_break};
use crate::function::AbstractFunction;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{Map, Result, Value};

const SOURCE: &str = "source";
const EACH: &str = "each";
const INDEX: &str = "index";
const VALUE: &str = "value";

pub struct ForEachLoop {
    signature: FunctionSignature,
}

impl ForEachLoop {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("ForEachLoop")
                .set_namespace(Namespaces::SYSTEM_LOOP)
                .set_parameters(Map::from([Parameter::of_entry(
                    SOURCE,
                    Schema::of_array(SOURCE, vec![Schema::of_any(SOURCE)]),
                    false,
                )]))
                .set_events(Map::from([
                    Event::event_map_entry(
                        Event::ITERATION,
                        Map::from([
                            (INDEX.to_string(), Schema::of(INDEX, &[SchemaType::Integer])),
                            (EACH.to_string(), Schema::of_any(EACH)),
                        ]),
                    ),
                    Event::output_event_map_entry(Map::from([(
                        VALUE.to_string(),
                        Schema::of(VALUE, &[SchemaType::Integer]),
                    )])),
                ])),
        }
    }
}

impl Default for ForEachLoop {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for ForEachLoop {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source: Vec<Value> = match context.get_arguments().and_then(|a| a.get(SOURCE)) {
            Some(Value::Array(a)) => a.clone(),
            _ => Vec::new(),
        };
        let statement_name = context.get_statement_execution().map(|s| s.get_statement().get_statement_name().to_string());
        let execution_context = context.get_execution_context().clone();
        let mut current: usize = 0;

        // Like JS, this keeps yielding `output` once the source is exhausted (no done flag).
        Ok(FunctionOutput::from_generator(move || {
            let name = statement_name.as_deref();
            if current >= source.len() || break_requested(&execution_context, name) {
                clear_break(&execution_context, name);
                return Some(EventResult::output_of(Map::from([(VALUE.to_string(), Value::from(current))])));
            }
            let eve = EventResult::of(
                Event::ITERATION,
                Map::from([
                    (INDEX.to_string(), Value::from(current)),
                    (EACH.to_string(), source[current].clone()),
                ]),
            );
            current += 1;
            Some(eve)
        }))
    }
}
