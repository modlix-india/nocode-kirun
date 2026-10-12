//! Port of kirun-js `function/system/array/SubArray.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::model::EventResult;
use crate::{KIRunError, Map, Result, Value};

pub struct SubArray {
    signature: FunctionSignature,
}

impl SubArray {
    pub fn new() -> Self {
        SubArray {
            signature: AbstractArrayFunction::signature(
                "SubArray",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_find_from(), AbstractArrayFunction::parameter_int_length()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for SubArray {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for SubArray {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;
        let start = arg_i64(context, "findFrom");
        let mut len = arg_i64(context, "length");
        let size = source.len() as i64;

        if len == -1 {
            len = size - start;
        }

        if len <= 0 {
            return Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]));
        }

        if !(start >= 0 && start < size) || start + len > size {
            return Err(KIRunError::runtime(
                "Given find from point is more than the source size array or the Requested length for the subarray was more than the source size",
            ));
        }

        let sliced: Vec<Value> = source[start as usize..(start + len) as usize].to_vec();

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(sliced)))
    }
}
