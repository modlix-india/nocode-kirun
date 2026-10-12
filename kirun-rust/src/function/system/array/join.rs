//! Port of kirun-js `function/system/array/Join.ts`.

use async_trait::async_trait;

use super::abstract_array_function::{arg, arg_array, output_of};
use crate::function::AbstractFunction;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::model::{Event, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::js_to_string;
use crate::{Map, Result, Value};

const VALUE: &str = "source";
const DELIMITTER: &str = "delimiter";
const OUTPUT: &str = "result";

pub struct Join {
    signature: FunctionSignature,
}

impl Join {
    pub fn new() -> Self {
        let mut parameters = Map::new();
        parameters.insert(
            VALUE.to_string(),
            Parameter::new(
                VALUE,
                Schema::of_array(
                    VALUE,
                    vec![Schema::of(
                        "each",
                        &[
                            SchemaType::String,
                            SchemaType::Integer,
                            SchemaType::Long,
                            SchemaType::Double,
                            SchemaType::Float,
                            SchemaType::Null,
                        ],
                    )],
                ),
            ),
        );
        parameters.insert(
            DELIMITTER.to_string(),
            Parameter::new(DELIMITTER, Schema::of_string(DELIMITTER).set_default_value(Value::from(""))),
        );

        let mut out = Map::new();
        out.insert(OUTPUT.to_string(), Schema::of_string(OUTPUT));
        let mut events = Map::new();
        let (k, e) = Event::output_event_map_entry(out);
        events.insert(k, e);

        Join {
            signature: FunctionSignature::new("Join")
                .set_namespace(Namespaces::SYSTEM_ARRAY)
                .set_parameters(parameters)
                .set_events(events),
        }
    }
}

impl Default for Join {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for Join {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = arg_array(context, VALUE)?;
        // JS `source.join(undefined)` uses ","; the validated default is "".
        let delimitter = match arg(context, DELIMITTER) {
            Value::Null => ",".to_string(),
            v => js_to_string(&v),
        };

        // JS Array.join renders null and undefined as "".
        let joined = source
            .iter()
            .map(|e| if e.is_null() { String::new() } else { js_to_string(e) })
            .collect::<Vec<_>>()
            .join(&delimitter);

        Ok(output_of(OUTPUT, Value::String(joined)))
    }
}
