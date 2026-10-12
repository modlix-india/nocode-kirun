//! Port of kirun-js `function/system/array/RemoveDuplicates.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::deep_equal::deep_equal;
use crate::{KIRunError, Result, Value};

pub struct RemoveDuplicates {
    signature: FunctionSignature,
}

impl RemoveDuplicates {
    pub fn new() -> Self {
        RemoveDuplicates {
            signature: AbstractArrayFunction::signature(
                "RemoveDuplicates",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_source_from(), AbstractArrayFunction::parameter_int_length()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for RemoveDuplicates {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for RemoveDuplicates {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;
        let srcfrom = arg_i64(context, "srcFrom");
        let mut length = arg_i64(context, "length");
        let size = source.len() as i64;

        if length == -1 {
            length = size - srcfrom;
        }

        if srcfrom + length > size {
            return Err(KIRunError::runtime(format!(
                "Array has no elements from {} to {} as the array size is {}",
                srcfrom,
                srcfrom + length,
                size
            )));
        }

        let mut ja = source;
        let to = srcfrom + length;

        let mut i = to - 1;
        while i >= srcfrom {
            let mut j = i - 1;
            while j >= srcfrom {
                if deep_equal(&at(&ja, i), &at(&ja, j)) {
                    if (i as usize) < ja.len() {
                        ja.remove(i as usize);
                    }
                    break;
                }
                j -= 1;
            }
            i -= 1;
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(ja)))
    }
}
