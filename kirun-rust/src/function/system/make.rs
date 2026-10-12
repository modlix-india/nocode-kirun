//! Port of kirun-js `function/system/Make.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::expression::tokenextractor::token_value_extractor::ValuesMap;
use crate::runtime::expression::ExpressionEvaluator;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::map_util::MapUtil;
use crate::{Map, Result, Value};

const RESULT_SHAPE: &str = "resultShape";
const VALUE: &str = "value";

pub struct Make {
    signature: FunctionSignature,
}

impl Make {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Make")
                .set_namespace(Namespaces::SYSTEM)
                .set_parameters(Map::from([Parameter::of_entry(RESULT_SHAPE, Schema::of_any(RESULT_SHAPE), false)]))
                .set_events(Map::from([Event::output_event_map_entry(MapUtil::of(vec![(
                    VALUE,
                    Schema::of_any(VALUE),
                )]))])),
        }
    }

    fn process_value(&self, value: &Value, values_map: &ValuesMap) -> Result<Value> {
        match value {
            Value::Null => Ok(Value::Null),
            Value::String(s) => self.evaluate_expression(s, values_map),
            Value::Array(a) => Ok(Value::Array(
                a.iter().map(|item| self.process_value(item, values_map)).collect::<Result<Vec<_>>>()?,
            )),
            Value::Object(o) => {
                let mut result = serde_json::Map::new();
                for (key, val) in o {
                    result.insert(key.clone(), self.process_value(val, values_map)?);
                }
                Ok(Value::Object(result))
            }
            other => Ok(other.clone()),
        }
    }

    fn evaluate_expression(&self, expression: &str, values_map: &ValuesMap) -> Result<Value> {
        if expression.is_empty() || !expression.starts_with("{{") || !expression.ends_with("}}") {
            return Ok(Value::String(expression.to_string()));
        }

        // JS `expression.substring(2, expression.length - 2)` ("{{}" style overlaps give "").
        let inner = if expression.len() >= 4 { &expression[2..expression.len() - 2] } else { "" };
        ExpressionEvaluator::new(inner).evaluate(values_map)
    }
}

impl Default for Make {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Make {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let result_shape = context.get_arguments().and_then(|a| a.get(RESULT_SHAPE)).cloned().unwrap_or(Value::Null);

        let result = self.process_value(&result_shape, context.get_values_map())?;

        Ok(FunctionOutput::new(vec![EventResult::output_of(MapUtil::of(vec![(VALUE, result)]))]))
    }
}
