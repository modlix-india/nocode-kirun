//! Port of kirun-js `function/system/array/Max.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::primitive_util::PrimitiveUtil;
use crate::{KIRunError, Result};

pub struct Max {
    signature: FunctionSignature,
}

impl Max {
    pub fn new() -> Self {
        Max {
            signature: AbstractArrayFunction::signature(
                "Max",
                vec![AbstractArrayFunction::parameter_array_source_primitive()],
                AbstractArrayFunction::event_result_any(),
            ),
        }
    }
}

impl Default for Max {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Max {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;

        if source.is_empty() {
            return Err(KIRunError::runtime("Search source array cannot be empty"));
        }

        let mut max = &source[0];
        for y in source.iter().skip(1) {
            if PrimitiveUtil::compare_primitive(max, y) >= 0.0 {
                continue;
            }
            max = y;
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, max.clone()))
    }
}
