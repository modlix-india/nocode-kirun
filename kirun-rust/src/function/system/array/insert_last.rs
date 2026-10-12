//! Port of kirun-js `function/system/array/InsertLast.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{Result, Value};

pub struct InsertLast {
    signature: FunctionSignature,
}

impl InsertLast {
    pub fn new() -> Self {
        InsertLast {
            signature: AbstractArrayFunction::signature(
                "InsertLast",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_any()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for InsertLast {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for InsertLast {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;
        source.push(arg(context, "element"));
        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}
