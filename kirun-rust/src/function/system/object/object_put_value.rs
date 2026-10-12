//! Port of kirun-js `function/system/object/ObjectPutValue.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::expression::tokenextractor::object_value_setter_extractor::ObjectValueSetterExtractor;
use crate::runtime::expression::tokenextractor::token_value_extractor::TokenValueExtractor;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::{js_to_string, js_truthy};
use crate::{Map, Result, Value};

const VALUE: &str = "value";
const SOURCE: &str = "source";
const KEY: &str = "key";
const OVERWRITE: &str = "overwrite";
const DELETE_KEY_ON_NULL: &str = "deleteKeyOnNull";

pub struct ObjectPutValue {
    signature: FunctionSignature,
}

impl ObjectPutValue {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("ObjectPutValue")
                .set_namespace(Namespaces::SYSTEM_OBJECT)
                .set_parameters(Map::from([
                    Parameter::of_entry(SOURCE, Schema::of_object(SOURCE), false),
                    Parameter::of_entry(KEY, Schema::of_string(KEY), false),
                    Parameter::of_entry(VALUE, Schema::of_any(VALUE), false),
                    Parameter::of_entry(
                        OVERWRITE,
                        Schema::of_boolean(OVERWRITE).set_default_value(Value::Bool(true)),
                        false,
                    ),
                    Parameter::of_entry(
                        DELETE_KEY_ON_NULL,
                        Schema::of_boolean(DELETE_KEY_ON_NULL).set_default_value(Value::Bool(false)),
                        false,
                    ),
                ]))
                .set_events(Map::from([Event::output_event_map_entry(Map::from([(
                    VALUE.to_string(),
                    Schema::of_object(VALUE),
                )]))])),
        }
    }
}

impl Default for ObjectPutValue {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for ObjectPutValue {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let args = context.get_arguments().cloned().unwrap_or_default();
        let source = args.get(SOURCE).cloned().unwrap_or(Value::Null);
        let key = match args.get(KEY) {
            Some(Value::String(s)) => s.clone(),
            Some(v) => js_to_string(v),
            None => "undefined".to_string(),
        };
        let value = args.get(VALUE).cloned().unwrap_or(Value::Null);
        let overwrite = args.get(OVERWRITE).map_or(true, js_truthy);
        let delete_key_on_null = args.get(DELETE_KEY_ON_NULL).is_some_and(js_truthy);

        let mut ove = ObjectValueSetterExtractor::new(source, "Data.");
        ove.set_value(&key, value, overwrite, delete_key_on_null)?;

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(VALUE.to_string(), ove.get_store())]))]))
    }
}
