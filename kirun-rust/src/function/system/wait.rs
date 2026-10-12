//! Port of kirun-js `function/system/Wait.ts`.

use std::time::Duration;

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{Map, Result, Value};

const MILLIS: &str = "millis";

pub struct Wait {
    signature: FunctionSignature,
}

impl Wait {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Wait")
                .set_namespace(Namespaces::SYSTEM)
                .set_parameters(Map::from([Parameter::of_entry(
                    MILLIS,
                    Schema::of_number(MILLIS).set_minimum(0.0).set_default_value(Value::from(0)),
                    false,
                )]))
                .set_events(Map::from([Event::output_event_map_entry(Map::new())])),
        }
    }
}

impl Default for Wait {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Wait {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let millis = context.get_arguments().and_then(|a| a.get(MILLIS)).and_then(Value::as_f64).unwrap_or(0.0);

        // JS `setTimeout(resolve, millis)`: non-finite or negative delays fire immediately.
        let millis = if millis.is_finite() && millis > 0.0 { millis } else { 0.0 };
        if millis > 0.0 {
            tokio::time::sleep(Duration::from_secs_f64(millis / 1000.0)).await;
        }

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}
