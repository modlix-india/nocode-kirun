//! Port of kirun-js `function/system/object/ObjectKeys.ts`.

use async_trait::async_trait;

use super::abstract_object_function::{locale_compare, object_entries, AbstractObjectFunction};
use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{EventResult, FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::duplicate::duplicate;
use crate::util::null_check::is_null_value;
use crate::{Map, Result, Value};

const VALUE: &str = "value";

pub struct ObjectKeys {
    signature: FunctionSignature,
}

impl ObjectKeys {
    pub fn new() -> Self {
        Self {
            signature: AbstractObjectFunction::signature(
                "ObjectKeys",
                Schema::of_array(VALUE, vec![Schema::of_string(VALUE)]),
            ),
        }
    }
}

impl Default for ObjectKeys {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for ObjectKeys {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = context.get_arguments().and_then(|a| a.get("source")).cloned().unwrap_or(Value::Null);

        if is_null_value(&source) {
            return Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(
                VALUE.to_string(),
                Value::Array(vec![]),
            )]))]));
        }

        let mut keys: Vec<String> = object_entries(&duplicate(&source)).into_iter().map(|(k, _)| k).collect();
        keys.sort_by(|a, b| locale_compare(a, b));

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(
            VALUE.to_string(),
            Value::Array(keys.into_iter().map(Value::String).collect()),
        )]))]))
    }
}
