//! Port of kirun-js `function/system/array/Frequency.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

pub struct Frequency {
    signature: FunctionSignature,
}

impl Frequency {
    pub fn new() -> Self {
        Frequency {
            signature: AbstractArrayFunction::signature(
                "Frequency",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_any(), AbstractArrayFunction::parameter_int_source_from(), AbstractArrayFunction::parameter_int_length()],
                AbstractArrayFunction::event_result_integer(),
            ),
        }
    }
}

impl Default for Frequency {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Frequency {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;
        let find = arg(context, "element");
        let start = arg_i64(context, "srcFrom");
        let length = arg_i64(context, "length");
        let size = source.len() as i64;

        if source.is_empty() {
            return Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::from(0)));
        }

        if start > size {
            return Err(KIRunError::runtime("Given start point is more than the size of source"));
        }

        let mut end = start + length;
        if length == -1 {
            end = size - start;
        }

        if end > size {
            return Err(KIRunError::runtime("Given length is more than the size of source"));
        }

        let mut frequency: i64 = 0;
        let mut i = start;
        while i < end && i < size {
            if compare(&at(&source, i), &find) == 0.0 {
                frequency += 1;
            }
            i += 1;
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::from(frequency)))
    }
}
