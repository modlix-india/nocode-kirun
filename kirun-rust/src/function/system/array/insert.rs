//! Port of kirun-js `function/system/array/Insert.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

pub struct Insert {
    signature: FunctionSignature,
}

impl Insert {
    pub fn new() -> Self {
        Insert {
            signature: AbstractArrayFunction::signature(
                "Insert",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_offset(), AbstractArrayFunction::parameter_any()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for Insert {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Insert {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;
        let offset_value = arg(context, "offset");
        let output = arg(context, "element");
        let offset = offset_value.as_f64().map(|f| f as i64).unwrap_or(0);

        if output.is_null() || offset_value.is_null() || offset > source.len() as i64 {
            return Err(KIRunError::runtime(
                "Please provide valid resources to insert at the correct location",
            ));
        }

        if source.is_empty() {
            if offset == 0 {
                source.push(output);
            }
            return Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)));
        }

        if offset >= 0 {
            source.insert(offset as usize, output);
        } else {
            // JS swaps the element down past index 0 into the non-index property `-1`,
            // leaving `undefined` at index 0.
            source.insert(0, Value::Null);
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}
