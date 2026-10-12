//! Port of kirun-js `function/system/array/Concatenate.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{Result, Value};

pub struct Concatenate {
    signature: FunctionSignature,
}

impl Concatenate {
    pub fn new() -> Self {
        Concatenate {
            signature: AbstractArrayFunction::signature(
                "Concatenate",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_array_second_source()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for Concatenate {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Concatenate {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;
        let second_source = arg_array(context, "secondSource")?;
        source.extend(second_source);
        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}
