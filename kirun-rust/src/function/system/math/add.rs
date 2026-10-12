//! Port of kirun-js `function/system/math/Add.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::number_value;
use crate::{Map, Result, Value};

const VALUE: &str = "value";

pub struct Add {
    signature: FunctionSignature,
}

impl Add {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Add")
                .set_namespace(Namespaces::MATH)
                .set_parameters(Map::from([(
                    VALUE.to_string(),
                    Parameter::new(VALUE, Schema::of_number(VALUE)).set_variable_argument(true),
                )]))
                .set_events(Map::from([Event::output_event_map_entry(Map::from([(
                    VALUE.to_string(),
                    Schema::of_number(VALUE),
                )]))])),
        }
    }
}

impl Default for Add {
    fn default() -> Self {
        Self::new()
    }
}

/// The variable-argument `value` array as JS numbers.
pub(crate) fn number_args(context: &FunctionExecutionParameters, name: &str) -> Vec<Value> {
    match context.get_arguments().and_then(|a| a.get(name)) {
        Some(Value::Array(a)) => a.clone(),
        _ => vec![],
    }
}

#[async_trait]
impl AbstractFunction for Add {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let nums = number_args(context, VALUE);
        let sum = nums.iter().fold(0.0, |a, c| a + c.as_f64().unwrap_or(f64::NAN));

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(VALUE.to_string(), number_value(sum))]))]))
    }
}
