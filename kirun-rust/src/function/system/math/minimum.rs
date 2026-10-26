//! Port of kirun-js `function/system/math/Minimum.ts`, answering as kirun-java's `Minimum`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{Map, Result};

use super::add::number_args;

const VALUE: &str = "value";

pub struct Minimum {
    signature: FunctionSignature,
}

impl Minimum {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Minimum")
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

impl Default for Minimum {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Minimum {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let nums = number_args(context, VALUE);

        // kirun-java: no values output an empty result; otherwise the last value that is not
        // larger than the one kept so far (ties go to the later value, `[1, 1.0]` gives `1.0`).
        let mut iter = nums.into_iter();
        let Some(mut element) = iter.next() else {
            return Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]));
        };
        let mut first = element.as_f64().unwrap_or(f64::NAN);
        for other in iter {
            let o = other.as_f64().unwrap_or(f64::NAN);
            if first < o {
                continue;
            }
            first = o;
            element = other;
        }

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(VALUE.to_string(), element)]))]))
    }
}
