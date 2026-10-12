//! Port of kirun-js `function/system/array/LastIndexOfArray.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

pub struct LastIndexOfArray {
    signature: FunctionSignature,
}

impl LastIndexOfArray {
    pub fn new() -> Self {
        LastIndexOfArray {
            signature: AbstractArrayFunction::signature(
                "LastIndexOfArray",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_array_second_source(), AbstractArrayFunction::parameter_int_find_from()],
                AbstractArrayFunction::event_result_integer(),
            ),
        }
    }
}

impl Default for LastIndexOfArray {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for LastIndexOfArray {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;
        let second_source = arg_array(context, "secondSource")?;
        let from = arg_i64(context, "findFrom");

        if source.is_empty() {
            return Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::from(-1)));
        }

        if from < 0 || from > source.len() as i64 || second_source.len() > source.len() {
            return Err(KIRunError::runtime(
                "Given from index is more than the size of the source array",
            ));
        }

        let second_source_size = second_source.len() as i64;
        let mut index: i64 = -1;

        for i in from..source.len() as i64 {
            let mut j: i64 = 0;
            if compare(&at(&source, i), &at(&second_source, j)) == 0.0 {
                while j < second_source_size {
                    if compare(&at(&source, i + j), &at(&second_source, j)) != 0.0 {
                        break;
                    }
                    j += 1;
                }
                if j == second_source_size {
                    index = i;
                }
            }
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::from(index)))
    }
}
