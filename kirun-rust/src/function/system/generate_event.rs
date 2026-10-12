//! Port of kirun-js `function/system/GenerateEvent.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::expression::ExpressionEvaluator;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::null_check::is_null_value;
use crate::{KIRunError, Map, Result, Value};

const VALUE: &str = "value";
const EVENT_NAME: &str = "eventName";
const RESULTS: &str = "results";

pub struct GenerateEvent {
    signature: FunctionSignature,
}

impl GenerateEvent {
    pub fn new() -> Self {
        let mut props = Map::new();
        props.insert("name".to_string(), Schema::of_string("name"));
        props.insert(VALUE.to_string(), Parameter::expression().clone());
        Self {
            signature: FunctionSignature::new("GenerateEvent")
                .set_namespace(Namespaces::SYSTEM)
                .set_parameters(Map::from([
                    Parameter::of_entry(
                        EVENT_NAME,
                        Schema::of_string(EVENT_NAME).set_default_value(Value::from("output")),
                        false,
                    ),
                    Parameter::of_entry(RESULTS, Schema::of_object(RESULTS).set_properties(props), true),
                ]))
                .set_events(Map::from([Event::output_event_map_entry(Map::new())])),
        }
    }
}

impl Default for GenerateEvent {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for GenerateEvent {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let args = context.get_arguments().cloned().unwrap_or_default();

        let event_name = match args.get(EVENT_NAME) {
            Some(Value::String(s)) => s.clone(),
            Some(v) => crate::util::primitive::js_to_string(v),
            None => "undefined".to_string(),
        };

        let mut map: Map<Value> = Map::new();
        if let Some(Value::Array(results)) = args.get(RESULTS) {
            for e in results {
                let je = &e[VALUE];

                if is_null_value(je) {
                    return Err(KIRunError::runtime("Expect a value object"));
                }

                let mut v = je[VALUE].clone();
                if crate::util::primitive::js_truthy(&je["isExpression"]) {
                    let expression = match &v {
                        Value::String(s) => s.clone(),
                        other => crate::util::primitive::js_to_string(other),
                    };
                    v = ExpressionEvaluator::new(&expression).evaluate(context.get_values_map())?;
                }
                let name = match &e["name"] {
                    Value::String(s) => s.clone(),
                    other => crate::util::primitive::js_to_string(other),
                };
                map.insert(name, v);
            }
        }

        if let Some(events) = context.get_events() {
            events.write().entry(event_name).or_default().push(map);
        }

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}
