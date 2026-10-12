//! Port of kirun-js `function/system/array/BinarySearch.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::primitive_util::PrimitiveUtil;
use crate::{KIRunError, Result, Value};

pub struct BinarySearch {
    signature: FunctionSignature,
}

impl BinarySearch {
    pub fn new() -> Self {
        BinarySearch {
            signature: AbstractArrayFunction::signature(
                "BinarySearch",
                vec![AbstractArrayFunction::parameter_array_source_primitive(), AbstractArrayFunction::parameter_int_source_from(), AbstractArrayFunction::parameter_find_primitive(), AbstractArrayFunction::parameter_int_length()],
                AbstractArrayFunction::event_index(),
            ),
        }
    }
}

impl Default for BinarySearch {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for BinarySearch {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;
        let mut start = arg_i64(context, "srcFrom");
        let find = arg(context, "findPrimitive");
        let mut end = arg_i64(context, "length");
        let len = source.len() as i64;

        if source.is_empty() || start < 0 || start > len {
            return Err(KIRunError::runtime("Search source array cannot be empty"));
        }

        if end == -1 {
            end = len - start;
        }
        end += start;

        if end > len {
            return Err(KIRunError::runtime(
                "End point for array cannot be more than the size of the source array",
            ));
        }

        let mut index: i64 = -1;
        while start <= end {
            let mid = (start + end).div_euclid(2);
            let cmp = PrimitiveUtil::compare(&at(&source, mid), &find);
            if cmp == 0.0 {
                index = mid;
                break;
            } else if cmp > 0.0 {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }

        Ok(output_of(AbstractArrayFunction::EVENT_INDEX_NAME, Value::from(index)))
    }
}
