//! Port of kirun-js `function/system/array/Shuffle.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use rand::Rng;

use crate::{Result, Value};

pub struct Shuffle {
    signature: FunctionSignature,
}

impl Shuffle {
    pub fn new() -> Self {
        Shuffle {
            signature: AbstractArrayFunction::signature(
                "Shuffle",
                vec![AbstractArrayFunction::parameter_array_source()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for Shuffle {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Shuffle {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;

        if source.len() <= 1 {
            return Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)));
        }

        let size = source.len();
        let mut x: usize = 0;
        let mut rng = rand::thread_rng();
        for _ in 0..size {
            let y: usize = rng.gen_range(0..size);
            source.swap(x, y);
            x = y;
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}
