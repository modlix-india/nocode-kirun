//! Port of kirun-js `function/system/loop/RangeLoop.ts`.

use async_trait::async_trait;

use crate::function::system::r#loop::{break_requested, clear_break};
use crate::function::AbstractFunction;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::number_value;
use crate::{Map, Result, Value};

const FROM: &str = "from";
const TO: &str = "to";
const STEP: &str = "step";
const VALUE: &str = "value";
const INDEX: &str = "index";

const NUMBERS: [SchemaType; 4] = [SchemaType::Integer, SchemaType::Long, SchemaType::Float, SchemaType::Double];

pub struct RangeLoop {
    signature: FunctionSignature,
}

impl RangeLoop {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("RangeLoop")
                .set_namespace(Namespaces::SYSTEM_LOOP)
                .set_parameters(Map::from([
                    Parameter::of_entry(FROM, Schema::of(FROM, &NUMBERS).set_default_value(Value::from(0)), false),
                    Parameter::of_entry(TO, Schema::of(TO, &NUMBERS).set_default_value(Value::from(1)), false),
                    Parameter::of_entry(
                        STEP,
                        Schema::of(STEP, &NUMBERS)
                            .set_default_value(Value::from(1))
                            .set_not(Schema::new().set_constant(Value::from(0))),
                        false,
                    ),
                ]))
                .set_events(Map::from([
                    Event::event_map_entry(
                        Event::ITERATION,
                        Map::from([(INDEX.to_string(), Schema::of(INDEX, &NUMBERS))]),
                    ),
                    Event::output_event_map_entry(Map::from([(VALUE.to_string(), Schema::of(VALUE, &NUMBERS))])),
                ])),
        }
    }
}

impl Default for RangeLoop {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for RangeLoop {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let arg = |k: &str| context.get_arguments().and_then(|a| a.get(k)).and_then(Value::as_f64);
        let from = arg(FROM).unwrap_or(0.0);
        let to = arg(TO).unwrap_or(1.0);
        let step = arg(STEP).unwrap_or(1.0);

        let forward = step > 0.0;
        let mut current = from;
        let mut done = false;
        let statement_name = context.get_statement_execution().map(|s| s.get_statement().get_statement_name().to_string());
        let execution_context = context.get_execution_context().clone();

        Ok(FunctionOutput::from_generator(move || {
            if done {
                return None;
            }
            let name = statement_name.as_deref();
            if (forward && current >= to) || (!forward && current <= to) || break_requested(&execution_context, name) {
                done = true;
                clear_break(&execution_context, name);
                return Some(EventResult::output_of(Map::from([(VALUE.to_string(), number_value(current))])));
            }
            let eve = EventResult::of(Event::ITERATION, Map::from([(INDEX.to_string(), number_value(current))]));
            current += step;
            Some(eve)
        }))
    }
}
