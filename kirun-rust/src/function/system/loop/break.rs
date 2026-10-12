//! Port of kirun-js `function/system/loop/Break.ts`: marks a loop step for breaking via the
//! shared execution context, which the loop's generator checks before its next iteration.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::js_to_string;
use crate::{Map, Result, Value};

const STEP_NAME: &str = "stepName";

pub struct Break {
    signature: FunctionSignature,
}

impl Break {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Break")
                .set_namespace(Namespaces::SYSTEM_LOOP)
                .set_parameters(Map::from([Parameter::of_entry(
                    STEP_NAME,
                    Schema::of(STEP_NAME, &[SchemaType::String]),
                    false,
                )]))
                .set_events(Map::from([Event::output_event_map_entry(Map::new())])),
        }
    }
}

impl Default for Break {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Break {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let step_name = context.get_arguments().and_then(|a| a.get(STEP_NAME)).cloned().unwrap_or(Value::Null);
        context.get_execution_context().write().insert(js_to_string(&step_name), Value::Bool(true));
        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}
