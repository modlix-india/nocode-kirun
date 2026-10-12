//! Port of kirun-js `function/system/array/Disjoint.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::{KIRunError, Result, Value};

/// JS `Set.add`: SameValueZero membership (see `strict_equal`), insertion order kept.
fn set_add(set: &mut Vec<Value>, v: Value) {
    if !set.iter().any(|e| strict_equal(e, &v)) {
        set.push(v);
    }
}

fn set_has(set: &[Value], v: &Value) -> bool {
    set.iter().any(|e| strict_equal(e, v))
}

pub struct Disjoint {
    signature: FunctionSignature,
}

impl Disjoint {
    pub fn new() -> Self {
        Disjoint {
            signature: AbstractArrayFunction::signature(
                "Disjoint",
                vec![AbstractArrayFunction::parameter_array_source(), AbstractArrayFunction::parameter_int_source_from(), AbstractArrayFunction::parameter_array_second_source(), AbstractArrayFunction::parameter_int_second_source_from(), AbstractArrayFunction::parameter_int_length()],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for Disjoint {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Disjoint {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let first_source = arg_array(context, "source")?;
        let first = arg_i64(context, "srcFrom");
        let second_source = arg_array(context, "secondSource")?;
        let second = arg_i64(context, "secondSrcFrom");
        let mut len = arg_i64(context, "length");
        let (l1, l2) = (first_source.len() as i64, second_source.len() as i64);

        if len == -1 {
            len = if l1 <= l2 { l1 - first } else { l2 - second };
        }

        if len > l1 || len > l2 || first + len > l1 || second + len > l2 {
            return Err(KIRunError::runtime(
                "The length which was being requested is more than than the size either source array or second source array",
            ));
        }

        let mut set1: Vec<Value> = Vec::new();
        let mut set2: Vec<Value> = Vec::new();

        for i in 0..len {
            set_add(&mut set1, at(&first_source, i + first));
        }
        for i in 0..len {
            set_add(&mut set2, at(&second_source, i + second));
        }

        let mut set3: Vec<Value> = Vec::new();
        for element in &set1 {
            if let Some(p) = set2.iter().position(|e| strict_equal(e, element)) {
                set2.remove(p);
            } else {
                set_add(&mut set3, element.clone());
            }
        }
        for element in &set2 {
            if !set_has(&set1, element) {
                set_add(&mut set3, element.clone());
            }
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(set3)))
    }
}
