//! Port of kirun-js `function/system/math/Hypotenuse.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::runtime::expression::operators::java_semantics::JNum;
use crate::{Map, Result, Value};

use super::add::number_args;

const VALUE: &str = "value";

pub struct Hypotenuse {
    signature: FunctionSignature,
}

impl Hypotenuse {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Hypotenuse")
                .set_namespace(Namespaces::MATH)
                .set_parameters(Map::from([(
                    VALUE.to_string(),
                    Parameter::new(VALUE, Schema::of_number(VALUE)).set_variable_argument(true),
                )]))
                .set_events(Map::from([Event::output_event_map_entry(Map::from([(
                    VALUE.to_string(),
                    Schema::of(VALUE, &[SchemaType::Double]),
                )]))])),
        }
    }
}

impl Default for Hypotenuse {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Hypotenuse {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let nums = number_args(context, VALUE);
        // kirun-java: no values give the `int` 0, otherwise a `double`.
        let value = if nums.is_empty() {
            Value::from(0)
        } else {
            let sum = nums.iter().fold(0.0, |a, c| {
                let c = c.as_f64().unwrap_or(f64::NAN);
                a + c * c
            });
            JNum::Double(sum.sqrt()).to_value()
        };

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(VALUE.to_string(), value)]))]))
    }
}
