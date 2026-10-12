//! Port of kirun-js `function/system/array/ArrayToObject.ts` (JS function name `ArrayToObjects`).

use async_trait::async_trait;

use super::abstract_array_function::*;
use crate::function::AbstractFunction;
use crate::model::{FunctionOutput, FunctionSignature};
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::model::Parameter;
use crate::util::primitive::{js_to_string, js_truthy};
use crate::{Result, Value};

const KEY_PATH: &str = "keyPath";
const VALUE_PATH: &str = "valuePath";
const IGNORE_NULL_VALUES: &str = "ignoreNullValues";
const IGNORE_NULL_KEYS: &str = "ignoreNullKeys";
const IGNORE_DUPLICATE_KEYS: &str = "ignoreDuplicateKeys";

pub struct ArrayToObject {
    signature: FunctionSignature,
}

impl ArrayToObject {
    pub fn new() -> Self {
        ArrayToObject {
            signature: AbstractArrayFunction::signature(
                "ArrayToObjects",
                vec![AbstractArrayFunction::parameter_array_source(),
                    Parameter::of(KEY_PATH, Schema::of_string(KEY_PATH), false),
                    Parameter::of(VALUE_PATH, Schema::of(VALUE_PATH, &[SchemaType::String, SchemaType::Null]), false),
                    Parameter::of(
                        IGNORE_NULL_VALUES,
                        Schema::of_boolean(IGNORE_NULL_VALUES).set_default_value(Value::Bool(false)),
                        false,
                    ),
                    Parameter::of(
                        IGNORE_NULL_KEYS,
                        Schema::of_boolean(IGNORE_NULL_KEYS).set_default_value(Value::Bool(true)),
                        false,
                    ),
                    Parameter::of(
                        IGNORE_DUPLICATE_KEYS,
                        Schema::of_boolean(IGNORE_DUPLICATE_KEYS).set_default_value(Value::Bool(false)),
                        false,
                    ),],
                AbstractArrayFunction::event_result_any(),
            ),
        }
    }
}

impl Default for ArrayToObject {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for ArrayToObject {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, "source")?;
        let key_path = js_to_string(&arg(context, KEY_PATH));
        let value_path = match arg(context, VALUE_PATH) {
            Value::Null => String::new(),
            v => js_to_string(&v),
        };

        let ignore_null_values = js_truthy(&arg(context, IGNORE_NULL_VALUES));
        let ignore_null_keys = js_truthy(&arg(context, IGNORE_NULL_KEYS));
        let ignore_duplicate_keys = js_truthy(&arg(context, IGNORE_DUPLICATE_KEYS));

        let mut acc = serde_json::Map::new();
        for cur in source.into_iter().filter(|e| !e.is_null()) {
            // JS `new ObjectValueSetterExtractor(cur, "Data.").getValue("Data." + path)`.
            let key = get_path_value(&cur, &key_path);
            if ignore_null_keys && key.is_null() {
                continue;
            }

            let value = if value_path.is_empty() { cur.clone() } else { get_path_value(&cur, &value_path) };
            if ignore_null_values && value.is_null() {
                continue;
            }

            // JS object keys are strings: `acc[key]` stringifies the key.
            let key = js_to_string(&key);
            if ignore_duplicate_keys && acc.contains_key(&key) {
                continue;
            }

            acc.insert(key, value);
        }

        Ok(output_of(AbstractArrayFunction::EVENT_RESULT_NAME, Value::Object(acc)))
    }
}
