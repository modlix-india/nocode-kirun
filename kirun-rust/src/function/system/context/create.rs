//! Port of kirun-js `function/system/context/Create.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::r#type::{SchemaType, TypeUtil};
use crate::json::schema::string::StringFormat;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter, ParameterType};
use crate::namespaces::Namespaces;
use crate::runtime::context_element::ContextElement;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::null_check::is_null_opt;
use crate::util::primitive::js_to_string;
use crate::util::string::string_formatter::StringFormatter;
use crate::{KIRunError, Map, Result, Value};

const NAME: &str = "name";
const SCHEMA: &str = "schema";

pub struct Create {
    signature: FunctionSignature,
}

impl Create {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Create")
                .set_namespace(Namespaces::SYSTEM_CTX)
                .set_parameters(Map::from([
                    Parameter::of_entry_typed(
                        NAME,
                        Schema::new()
                            .set_name(NAME)
                            .set_type(TypeUtil::of(&[SchemaType::String]))
                            .set_min_length(1)
                            .set_format(StringFormat::Regex)
                            .set_pattern("^[a-zA-Z_$][a-zA-Z_$0-9]*$".to_string()),
                        false,
                        ParameterType::Constant,
                    ),
                    Parameter::of_entry_typed(SCHEMA, Schema::schema().clone(), false, ParameterType::Constant),
                ]))
                .set_events(Map::from([Event::output_event_map_entry(Map::new())])),
        }
    }
}

impl Default for Create {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Create {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let args = context.get_arguments().cloned().unwrap_or_default();
        let name = match args.get(NAME) {
            Some(Value::String(s)) => s.clone(),
            Some(v) => js_to_string(v),
            None => "undefined".to_string(),
        };

        let ctx = context.get_context().cloned();

        if ctx.as_ref().is_some_and(|c| c.read().contains_key(&name)) {
            return Err(KIRunError::runtime(StringFormatter::format(
                "Context already has an element for '$' ",
                &[&name],
            )));
        }

        let s = Schema::from_value(args.get(SCHEMA).unwrap_or(&Value::Null))?;

        let Some(s) = s else {
            return Err(KIRunError::runtime("Schema is not supplied."));
        };

        let ctx = ctx.ok_or_else(|| KIRunError::runtime("Context is not available"))?;
        let element = if is_null_opt(s.get_default_value()) {
            Value::Null
        } else {
            s.get_default_value().cloned().unwrap_or(Value::Null)
        };
        ctx.write().insert(name, ContextElement::new(Some(s), element));

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}
