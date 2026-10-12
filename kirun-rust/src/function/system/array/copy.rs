//! Port of kirun-js `function/system/array/Copy.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::duplicate::duplicate;
use crate::util::primitive::js_truthy;
use crate::{KIRunError, Result, Value};

pub struct Copy {
    signature: FunctionSignature,
}

impl Copy {
    pub fn new() -> Self {
        Copy {
            signature: AbstractArrayFunction::signature(
                "Copy",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_source_from(), AbstractArrayFunction::parameter_int_length(), AbstractArrayFunction::parameter_boolean_deep_copy()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for Copy {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Copy {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;
        let srcfrom = arg_i64(context, "srcFrom");
        let mut length = arg_i64(context, "length");
        let len = source.len() as i64;

        if length == -1 {
            length = len - srcfrom;
        }

        if srcfrom + length > len {
            return Err(KIRunError::runtime(format!(
                "Array has no elements from {} to {} as the array size is {}",
                srcfrom,
                srcfrom + length,
                len
            )));
        }

        let deep = js_truthy(&arg(context, "deepCopy"));

        // JS `new Array(length)`: unfilled slots read as undefined (null here).
        let mut ja: Vec<Value> = vec![Value::Null; length.max(0) as usize];
        for i in srcfrom.max(0)..srcfrom + length {
            let v = at(&source, i);
            if !v.is_null() {
                // Rust values are owned, so the shallow copy is a clone too.
                ja[(i - srcfrom) as usize] = if deep { duplicate(&v) } else { v };
            }
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(ja)))
    }
}
