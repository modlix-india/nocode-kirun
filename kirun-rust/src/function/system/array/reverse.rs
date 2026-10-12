//! Port of kirun-js `function/system/array/Reverse.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

pub struct Reverse {
    signature: FunctionSignature,
}

impl Reverse {
    pub fn new() -> Self {
        Reverse {
            signature: AbstractArrayFunction::signature(
                "Reverse",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_source_from(), AbstractArrayFunction::parameter_int_length()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for Reverse {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Reverse {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;
        let mut st = arg_i64(context, "srcFrom");
        let mut len = arg_i64(context, "length");

        if len == -1 {
            len = source.len() as i64 - st;
        }

        if st + len > source.len() as i64 || len < 0 || st < 0 {
            return Err(KIRunError::runtime(
                "Please provide start point between the start and end indexes or provide the length which was less than the source size ",
            ));
        }

        let mut endpoint = st + len - 1;
        while st <= endpoint {
            source.swap(st as usize, endpoint as usize);
            st += 1;
            endpoint -= 1;
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}
