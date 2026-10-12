//! Port of kirun-js `function/system/array/Equals.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use super::compare::Compare;
use crate::function::Function;
use crate::{Result, Value};

pub struct Equals {
    signature: FunctionSignature,
}

impl Equals {
    pub fn new() -> Self {
        Equals {
            signature: AbstractArrayFunction::signature(
                "Equals",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_source_from(), AbstractArrayFunction::parameter_array_find(), AbstractArrayFunction::parameter_int_find_from(), AbstractArrayFunction::parameter_int_length()],
                AbstractArrayFunction::event_result_boolean(),
            ),
        }
    }
}

impl Default for Equals {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Equals {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let compare = Compare::new();
        let fo = Function::execute(&compare, context).await?;
        let v = fo
            .all_results()
            .first()
            .and_then(|r| r.get_result().get(AbstractArrayFunction::EVENT_RESULT_NAME))
            .and_then(Value::as_f64);
        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Bool(v == Some(0.0))))
    }
}
