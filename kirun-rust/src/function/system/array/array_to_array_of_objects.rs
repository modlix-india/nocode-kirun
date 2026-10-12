//! Port of kirun-js `function/system/array/ArrayToArrayOfObjects.ts`.

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::json::schema::Schema;
use crate::model::Parameter;
use crate::util::primitive::js_to_string;
use crate::{Result, Value};

const KEY_NAME: &str = "keyName";

pub struct ArrayToArrayOfObjects {
    signature: FunctionSignature,
}

impl ArrayToArrayOfObjects {
    pub fn new() -> Self {
        ArrayToArrayOfObjects {
            signature: AbstractArrayFunction::signature(
                "ArrayToArrayOfObjects",
                vec![AbstractArrayFunction::parameter_array_source(), Parameter::of(KEY_NAME, Schema::of_string(KEY_NAME), true)],
                AbstractArrayFunction::event_result_array(),
            ),
        }
    }
}

impl Default for ArrayToArrayOfObjects {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for ArrayToArrayOfObjects {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg(context, "source");
        let keys: Vec<String> = match arg(context, KEY_NAME) {
            Value::Array(a) => a.iter().map(js_to_string).collect(),
            Value::Null => Vec::new(),
            other => vec![js_to_string(&other)],
        };

        let source = match source {
            Value::Array(a) if !a.is_empty() => a,
            _ => return Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(vec![]))),
        };

        let result: Vec<Value> = source
            .into_iter()
            .map(|e| {
                let mut obj = serde_json::Map::new();
                match e {
                    Value::Array(arr) => {
                        if !keys.is_empty() {
                            for (index, key) in keys.iter().enumerate() {
                                obj.insert(key.clone(), arr.get(index).cloned().unwrap_or(Value::Null));
                            }
                        } else {
                            for (i, v) in arr.into_iter().enumerate() {
                                obj.insert(format!("value{}", i + 1), v);
                            }
                        }
                    }
                    other => {
                        let key = keys.first().cloned().unwrap_or_else(|| "value".to_string());
                        obj.insert(key, other);
                    }
                }
                Value::Object(obj)
            })
            .collect();

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Array(result)))
    }
}
