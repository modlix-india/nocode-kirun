//! Port of kirun-js `function/system/array/Delete.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

pub struct Delete {
    signature: FunctionSignature,
}

impl Delete {
    pub fn new() -> Self {
        Delete {
            signature: AbstractArrayFunction::signature(
                "Delete",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_any_var_args()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for Delete {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Delete {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;
        let received_args = match arg(context, "element") {
            Value::Null => {
                return Err(KIRunError::runtime(
                    "The deletable var args are empty. So cannot be proceeded further.",
                ))
            }
            Value::Array(a) => a,
            other => vec![other],
        };

        if source.is_empty() || received_args.is_empty() {
            return Err(KIRunError::runtime(
                "Expected a source or deletable for an array but not found any",
            ));
        }

        let mut indexes = vec![false; source.len()];
        for i in (0..source.len()).rev() {
            for a in &received_args {
                if !indexes[i] && compare(&source[i], a) == 0.0 {
                    indexes[i] = true;
                }
            }
        }

        let result: Vec<Value> = source
            .into_iter()
            .enumerate()
            .filter(|(i, _)| !indexes[*i])
            .map(|(_, v)| v)
            .collect();

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(result)))
    }
}
