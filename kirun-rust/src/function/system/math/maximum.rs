//! Port of kirun-js `function/system/math/Maximum.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Map, Result, Value};

use super::add::number_args;

const VALUE: &str = "value";

pub struct Maximum {
    signature: FunctionSignature,
}

impl Maximum {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Maximum")
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

impl Default for Maximum {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Maximum {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let nums = number_args(context, VALUE);

        // JS `nums.reduce((a, c) => ((!a && a !== 0) || c > a ? c : a))` (no initial value).
        let mut iter = nums.into_iter();
        let first = iter
            .next()
            .ok_or_else(|| KIRunError::runtime("Reduce of empty array with no initial value"))?;
        let result = iter.fold(first, |a: Value, c: Value| {
            let af = a.as_f64();
            let a_falsy = af.map_or(true, |f| f == 0.0 || f.is_nan());
            let a_is_zero = af == Some(0.0);
            let take_c = (a_falsy && !a_is_zero) || matches!((c.as_f64(), af), (Some(cf), Some(af)) if cf > af);
            if take_c {
                c
            } else {
                a
            }
        });

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(VALUE.to_string(), result)]))]))
    }
}
