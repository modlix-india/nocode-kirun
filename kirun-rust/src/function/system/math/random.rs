//! Port of kirun-js `function/system/math/Random.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::map_util::MapUtil;
use crate::util::primitive::number_value;
use crate::{Map, Result};

const VALUE: &str = "value";

pub struct Random {
    signature: FunctionSignature,
}

impl Random {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Random")
                .set_namespace(Namespaces::MATH)
                .set_events(Map::from([Event::output_event_map_entry(MapUtil::of(vec![(
                    VALUE,
                    Schema::of_double(VALUE),
                )]))])),
        }
    }
}

impl Default for Random {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Random {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, _context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(
            VALUE.to_string(),
            number_value(rand::random::<f64>()),
        )]))]))
    }
}
