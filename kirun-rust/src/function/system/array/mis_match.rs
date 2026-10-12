//! Port of kirun-js `function/system/array/MisMatch.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

pub struct MisMatch {
    signature: FunctionSignature,
}

impl MisMatch {
    pub fn new() -> Self {
        MisMatch {
            signature: AbstractArrayFunction::signature(
                "MisMatch",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_find_from(), AbstractArrayFunction::parameter_array_second_source(), AbstractArrayFunction::parameter_int_second_source_from(), AbstractArrayFunction::parameter_int_length()],
                AbstractArrayFunction::event_result_integer(),
            ),
        }
    }
}

impl Default for MisMatch {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for MisMatch {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let first_source = arg_array(context, "source")?;
        let first_find = arg_i64(context, "findFrom");
        let second_source = arg_array(context, "secondSource")?;
        let second_find = arg_i64(context, "secondSrcFrom");
        let length = arg_i64(context, "length");
        let (l1, l2) = (first_source.len() as i64, second_source.len() as i64);

        let first = if first_find < l1 && first_find > 0 { first_find } else { 0 };
        let second = if second_find < l2 && second_find > 0 { second_find } else { 0 };

        if first + length >= l1 || second + length > l2 {
            return Err(KIRunError::runtime(
                "The size of the array for first and second which was being requested is more than size of the given array",
            ));
        }

        let mut index: i64 = -1;
        for i in 0..length {
            if !loose_equal(&at(&first_source, first + i), &at(&second_source, second + i)) {
                index = i;
                break;
            }
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::from(index)))
    }
}
