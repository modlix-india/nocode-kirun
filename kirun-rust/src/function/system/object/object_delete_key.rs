//! Port of kirun-js `function/system/object/ObjectDeleteKey.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::duplicate::duplicate;
use crate::util::null_check::is_null_value;
use crate::util::primitive::js_to_string;
use crate::{Map, Result, Value};

const VALUE: &str = "value";
const SOURCE: &str = "source";
const KEY: &str = "key";

pub struct ObjectDeleteKey {
    signature: FunctionSignature,
}

impl ObjectDeleteKey {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("ObjectDeleteKey")
                .set_namespace(Namespaces::SYSTEM_OBJECT)
                .set_parameters(Map::from([
                    Parameter::of_entry(SOURCE, Schema::of_any(SOURCE), false),
                    Parameter::of_entry(KEY, Schema::of_string(KEY), false),
                ]))
                .set_events(Map::from([Event::output_event_map_entry(Map::from([(
                    VALUE.to_string(),
                    Schema::of_any(VALUE),
                )]))])),
        }
    }
}

impl Default for ObjectDeleteKey {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for ObjectDeleteKey {
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

        if is_null_value(&source) {
            return Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(
                VALUE.to_string(),
                Value::Null,
            )]))]));
        }

        let mut source = duplicate(&source);
        // JS `delete source[key]`: removes an object key; on an array it leaves a hole
        // (read back as undefined, here null); primitives are untouched.
        match &mut source {
            Value::Object(m) => {
                m.shift_remove(&key);
            }
            Value::Array(a) => {
                if let Ok(i) = key.parse::<usize>() {
                    if i.to_string() == key && i < a.len() {
                        a[i] = Value::Null;
                    }
                }
            }
            _ => {}
        }

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(VALUE.to_string(), source)]))]))
    }
}
