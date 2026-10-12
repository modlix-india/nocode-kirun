//! Port of kirun-js `function/system/array/Fill.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::duplicate::duplicate;
use crate::{KIRunError, Result, Value};

pub struct Fill {
    signature: FunctionSignature,
}

impl Fill {
    pub fn new() -> Self {
        Fill {
            signature: AbstractArrayFunction::signature(
                "Fill",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_source_from(), AbstractArrayFunction::parameter_int_length(), AbstractArrayFunction::parameter_any()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for Fill {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Fill {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;
        let srcfrom = arg_i64(context, "srcFrom");
        let mut length = arg_i64(context, "length");
        let element = arg(context, "element");

        if srcfrom < 0 {
            return Err(KIRunError::runtime(format!(
                "Arrays out of bound trying to access {} index",
                srcfrom
            )));
        }

        if length == -1 {
            length = source.len() as i64 - srcfrom;
        }

        let add = srcfrom + length - source.len() as i64;
        for _ in 0..add.max(0) {
            source.push(Value::Null);
        }

        for i in srcfrom..srcfrom + length {
            source[i as usize] = if element.is_null() { Value::Null } else { duplicate(&element) };
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}
