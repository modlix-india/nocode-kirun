//! Port of kirun-js `function/system/object/ObjectValues.ts`.

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

pub struct ObjectValues {
    signature: FunctionSignature,
}

impl ObjectValues {
    pub fn new() -> Self {
        Self {
            signature: AbstractObjectFunction::signature(
                "ObjectValues",
                Schema::of_array(VALUE, vec![Schema::of_any(VALUE)]),
            ),
        }
    }
}

impl Default for ObjectValues {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for ObjectValues {
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

        let mut entries = object_entries(&duplicate(&source));
        entries.sort_by(|a, b| locale_compare(&a.0, &b.0));
        let values: Vec<Value> = entries.into_iter().map(|(_, v)| v).collect();

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(
            VALUE.to_string(),
            Value::Array(values),
        )]))]))
    }
}
