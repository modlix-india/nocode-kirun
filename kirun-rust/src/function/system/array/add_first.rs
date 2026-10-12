//! Port of kirun-js `function/system/array/AddFirst.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{Result, Value};

pub struct AddFirst {
    signature: FunctionSignature,
}

impl AddFirst {
    pub fn new() -> Self {
        AddFirst {
            signature: AbstractArrayFunction::signature(
                "AddFirst",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_any()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for AddFirst {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for AddFirst {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;
        let input = arg(context, "element");
        source.insert(0, input);
        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}
