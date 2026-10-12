//! Port of kirun-js `function/system/array/LastIndexOf.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

pub struct LastIndexOf {
    signature: FunctionSignature,
}

impl LastIndexOf {
    pub fn new() -> Self {
        LastIndexOf {
            signature: AbstractArrayFunction::signature(
                "LastIndexOf",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_any_element_object(), AbstractArrayFunction::parameter_int_find_from()],
                AbstractArrayFunction::event_result_integer(),
            ),
        }
    }
}

impl Default for LastIndexOf {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for LastIndexOf {
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
                "The value of length shouldn't the exceed the size of the array or shouldn't be in terms",
            ));
        }

        let mut index: i64 = -1;
        let mut i = source.len() as i64 - 1;
        while i >= len {
            if compare(&source[i as usize], &find) == 0.0 {
                index = i;
                break;
            }
            i -= 1;
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::from(index)))
    }
}
