//! Port of kirun-js `function/system/context/Get.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::r#type::{SchemaType, TypeUtil};
use crate::json::schema::string::StringFormat;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter, ParameterType};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::js_to_string;
use crate::util::string::string_formatter::StringFormatter;
use crate::{KIRunError, Map, Result, Value};

const NAME: &str = "name";
const VALUE: &str = "value";

pub struct Get {
    signature: FunctionSignature,
}

impl Get {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Get")
                .set_namespace(Namespaces::SYSTEM_CTX)
                .set_parameters(Map::from([Parameter::of_entry_typed(
                    NAME,
                    Schema::new()
                        .set_name(NAME)
                        .set_type(TypeUtil::of(&[SchemaType::String]))
                        .set_min_length(1)
                        .set_format(StringFormat::Regex)
                        .set_pattern("^[a-zA-Z_$][a-zA-Z_$0-9]*$".to_string()),
                    false,
                    ParameterType::Constant,
                )]))
                .set_events(Map::from([Event::output_event_map_entry(Map::from([(
                    VALUE.to_string(),
                    Schema::of_any(VALUE),
                )]))])),
        }
    }
}

impl Default for Get {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Get {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let name = match context.get_arguments().and_then(|a| a.get(NAME)) {
            Some(Value::String(s)) => s.clone(),
            Some(v) => js_to_string(v),
            None => "undefined".to_string(),
        };

        let element = context.get_context().and_then(|c| c.read().get(&name).map(|ce| ce.get_element().clone()));

        let Some(element) = element else {
            return Err(KIRunError::runtime(StringFormatter::format(
                "Context don't have an element for '$' ",
                &[&name],
            )));
        };

        // JS builds `new Map([VALUE, element])` (a malformed Map literal); the intent and the
        // Java port return `{ value: element }`.
        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(VALUE.to_string(), element)]))]))
    }
}
