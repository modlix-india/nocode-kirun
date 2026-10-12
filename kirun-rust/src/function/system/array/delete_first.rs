//! Port of kirun-js `function/system/array/DeleteFirst.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

pub struct DeleteFirst {
    signature: FunctionSignature,
}

impl DeleteFirst {
    pub fn new() -> Self {
        DeleteFirst {
            signature: AbstractArrayFunction::signature(
                "DeleteFirst",
                vec![AbstractArrayFunction::parameter_array_source()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for DeleteFirst {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for DeleteFirst {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;
        if source.is_empty() {
            return Err(KIRunError::runtime("Given source array is empty"));
        }
        source.remove(0);
        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}
