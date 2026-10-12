//! Port of kirun-js `function/system/array/DeleteFrom.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

pub struct DeleteFrom {
    signature: FunctionSignature,
}

impl DeleteFrom {
    pub fn new() -> Self {
        DeleteFrom {
            signature: AbstractArrayFunction::signature(
                "DeleteFrom",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_source_from(), AbstractArrayFunction::parameter_int_length()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for DeleteFrom {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for DeleteFrom {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;
        let start = arg_i64(context, "srcFrom");
        let mut len = arg_i64(context, "length");

        if source.is_empty() {
            return Err(KIRunError::runtime("There are no elements to be deleted"));
        }

        let size = source.len() as i64;
        if start >= size || start < 0 {
            return Err(KIRunError::runtime(
                "The int source for the array should be in between 0 and length of the array ",
            ));
        }

        if len == -1 {
            len = size - start;
        }

        if start + len > size {
            return Err(KIRunError::runtime(
                "Requested length to be deleted is more than the size of array ",
            ));
        }

        // JS splice treats a negative delete count as 0.
        if len > 0 {
            source.drain(start as usize..(start + len) as usize);
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}
