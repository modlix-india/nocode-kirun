//! Port of kirun-js `function/system/array/Min.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::primitive_util::PrimitiveUtil;
use crate::{KIRunError, Result, Value};

pub struct Min {
    signature: FunctionSignature,
}

impl Min {
    pub fn new() -> Self {
        Min {
            signature: AbstractArrayFunction::signature(
                "Min",
                vec![AbstractArrayFunction::parameter_array_source_primitive()],
                AbstractArrayFunction::event_result_any(),
            ),
        }
    }
}

impl Default for Min {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Min {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;

        if source.is_empty() {
            return Err(KIRunError::runtime("Search source array cannot be empty"));
        }

        let mut min: Option<&Value> = None;
        for v in source.iter() {
            if v.is_null() {
                continue;
            }
            if min.map_or(true, |m| PrimitiveUtil::compare_primitive(v, m) < 0.0) {
                min = Some(v);
            }
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, min.cloned().unwrap_or(Value::Null)))
    }
}
