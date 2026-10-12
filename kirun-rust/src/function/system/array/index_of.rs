//! Port of kirun-js `function/system/array/IndexOf.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

pub struct IndexOf {
    signature: FunctionSignature,
}

impl IndexOf {
    pub fn new() -> Self {
        IndexOf {
            signature: AbstractArrayFunction::signature(
                "IndexOf",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_any_element_object(), AbstractArrayFunction::parameter_int_find_from()],
                AbstractArrayFunction::event_result_integer(),
            ),
        }
    }
}

impl Default for IndexOf {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for IndexOf {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;
        let find = arg(context, "elementObject");
        let len = arg_i64(context, "findFrom");

        if source.is_empty() {
            return Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::from(-1)));
        }

        if len < 0 || len > source.len() as i64 {
            return Err(KIRunError::runtime(
                "The size of the search index of the array is greater than the size of the array",
            ));
        }

        let mut index: i64 = -1;
        for i in len..source.len() as i64 {
            if compare(&source[i as usize], &find) == 0.0 {
                index = i;
                break;
            }
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::from(index)))
    }
}
