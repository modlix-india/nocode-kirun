//! Port of kirun-js `function/system/math/RandomAny.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::map_util::MapUtil;
use crate::util::primitive::number_value;
use crate::{Map, Result, Value};

/// JS `randomFunction: (min: number, max: number) => number`.
pub type RandomFunction = fn(f64, f64) -> f64;

pub struct RandomAny {
    signature: FunctionSignature,
    random_function: RandomFunction,
}

impl RandomAny {
    pub const MIN_VALUE: &'static str = "minValue";
    pub const MAX_VALUE: &'static str = "maxValue";
    pub const VALUE: &'static str = "value";

    pub fn new(
        name: &str,
        min_parameter: Parameter,
        max_parameter: Parameter,
        output_schema: Schema,
        random_function: RandomFunction,
    ) -> Self {
        Self {
            signature: FunctionSignature::new(name)
                .set_parameters(MapUtil::of(vec![
                    (Self::MIN_VALUE, min_parameter),
                    (Self::MAX_VALUE, max_parameter),
                ]))
                .set_namespace(Namespaces::MATH)
                .set_events(Map::from([Event::output_event_map_entry(MapUtil::of(vec![(
                    Self::VALUE,
                    output_schema,
                )]))])),
            random_function,
        }
    }
}

#[async_trait]
impl AbstractFunction for RandomAny {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let get = |k: &str| context.get_arguments().and_then(|a| a.get(k)).and_then(Value::as_f64).unwrap_or(f64::NAN);
        let min = get(Self::MIN_VALUE);
        let max = get(Self::MAX_VALUE);

        let num = (self.random_function)(min, max);

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(
            Self::VALUE.to_string(),
            number_value(num),
        )]))]))
    }
}
