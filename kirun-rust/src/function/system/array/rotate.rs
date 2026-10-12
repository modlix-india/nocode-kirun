//! Port of kirun-js `function/system/array/Rotate.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{Result, Value};

fn rotate(elements: &mut [Value], mut start: i64, mut end: i64) {
    while start < end {
        elements.swap(start as usize, end as usize);
        start += 1;
        end -= 1;
    }
}

pub struct Rotate {
    signature: FunctionSignature,
}

impl Rotate {
    pub fn new() -> Self {
        Rotate {
            signature: AbstractArrayFunction::signature(
                "Rotate",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_rotate_length()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for Rotate {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Rotate {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut source = arg_array(context, "source")?;
        let rot_len = arg_i64(context, "rotateLength");

        if source.is_empty() {
            return Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)));
        }

        let size = source.len() as i64;
        // JS `%` keeps the sign of the dividend (Rust `%` does too).
        let rot_len = rot_len % size;

        rotate(&mut source, 0, rot_len - 1);
        rotate(&mut source, rot_len, size - 1);
        rotate(&mut source, 0, size - 1);

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(source)))
    }
}
