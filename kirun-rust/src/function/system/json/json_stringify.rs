//! Port of kirun-js `function/system/json/JSONStringify.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{EventResult, Event, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::js_stringify;
use crate::{Map, Result, Value};

const VALUE: &str = "value";
const SOURCE: &str = "source";

pub struct JSONStringify {
    signature: FunctionSignature,
}

impl JSONStringify {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("JSONStringify")
                .set_namespace(Namespaces::SYSTEM_JSON)
                .set_parameters(Map::from([Parameter::of_entry(SOURCE, Schema::of_any(SOURCE), false)]))
                .set_events(Map::from([Event::output_event_map_entry(Map::from([(
                    VALUE.to_string(),
                    Schema::of_string(VALUE),
                )]))])),
        }
    }
}

impl Default for JSONStringify {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for JSONStringify {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = context.get_arguments().and_then(|a| a.get(SOURCE)).cloned().unwrap_or(Value::Null);
        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(
            VALUE.to_string(),
            Value::String(js_stringify(&source)),
        )]))]))
    }
}
