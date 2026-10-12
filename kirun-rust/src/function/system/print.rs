//! Port of kirun-js `function/system/Print.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::js_stringify;
use crate::{Map, Result, Value};

const VALUES: &str = "values";
const STREAM: &str = "stream";

const LOG: &str = "LOG";
const ERROR: &str = "ERROR";

pub struct Print {
    signature: FunctionSignature,
}

impl Print {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Print")
                .set_namespace(Namespaces::SYSTEM)
                .set_parameters(Map::from([
                    Parameter::of_entry(VALUES, Schema::of_any(VALUES), true),
                    Parameter::of_entry(
                        STREAM,
                        Schema::of_string(STREAM)
                            .set_enums(vec![Value::from(LOG), Value::from(ERROR)])
                            .set_default_value(Value::from(LOG)),
                        false,
                    ),
                ]))
                .set_events(Map::from([Event::output_event_map_entry(Map::new())])),
        }
    }
}

impl Default for Print {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Print {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let args = context.get_arguments();
        let values = args.and_then(|a| a.get(VALUES)).cloned().unwrap_or(Value::Array(vec![]));
        let stream = args.and_then(|a| a.get(STREAM)).cloned().unwrap_or(Value::Null);

        // JS `console.log(...values)`: arguments separated by a space, strings printed raw.
        let line = match &values {
            Value::Array(a) => a
                .iter()
                .map(|v| match v {
                    Value::String(s) => s.clone(),
                    other => js_stringify(other),
                })
                .collect::<Vec<_>>()
                .join(" "),
            other => js_stringify(other),
        };

        if stream == Value::from(LOG) {
            println!("{line}");
        } else {
            eprintln!("{line}");
        }

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}
