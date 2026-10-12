//! Port of kirun-js `function/system/object/ObjectConvert.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::convertor::ConversionMode;
use crate::json::schema::validator::SchemaValidator;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter, ParameterType};
use crate::namespaces::Namespaces;
use crate::repository::Repository;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::map_util::MapUtil;
use crate::{KIRunError, Map, Result, Value};

const SOURCE: &str = "source";
const SCHEMA: &str = "schema";
const VALUE: &str = "value";
const CONVERSION_MODE: &str = "conversionMode";

pub struct ObjectConvert {
    signature: FunctionSignature,
}

impl ObjectConvert {
    pub fn new() -> Self {
        let modes: Vec<Value> =
            ConversionMode::get_conversion_modes().iter().map(|m| Value::from(m.name())).collect();
        Self {
            signature: FunctionSignature::new("ObjectConvert")
                .set_namespace(Namespaces::SYSTEM_OBJECT)
                .set_parameters(Map::from([
                    // JS names the source parameter's schema 'schema'.
                    Parameter::of_entry(SOURCE, Schema::of_any(SCHEMA), false),
                    Parameter::of_entry_typed(SCHEMA, Schema::schema().clone(), false, ParameterType::Constant),
                    Parameter::of_entry(CONVERSION_MODE, Schema::of_string(CONVERSION_MODE).set_enums(modes), false),
                ]))
                .set_events(Map::from([Event::output_event_map_entry(MapUtil::of(vec![(
                    VALUE,
                    Schema::of_any(VALUE),
                )]))])),
        }
    }

    async fn convert_to_schema(
        &self,
        target_schema: &Schema,
        target_schema_repository: &dyn Repository<Schema>,
        element: Value,
        mode: ConversionMode,
    ) -> Result<FunctionOutput> {
        match SchemaValidator::validate_with(
            Some(vec![]),
            Some(target_schema),
            Some(target_schema_repository),
            element,
            true,
            Some(mode),
        )
        .await
        {
            Ok(v) => Ok(FunctionOutput::new(vec![EventResult::output_of(MapUtil::of(vec![(VALUE, v)]))])),
            Err(error) => Err(KIRunError::runtime(error.get_message())),
        }
    }
}

impl Default for ObjectConvert {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for ObjectConvert {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let args = context.get_arguments().cloned().unwrap_or_default();
        let element = args.get(SOURCE).cloned().unwrap_or(Value::Null);
        let schema = Schema::from_value(args.get(SCHEMA).unwrap_or(&Value::Null))?;

        let Some(schema) = schema else {
            return Err(KIRunError::runtime("Schema is not supplied."));
        };

        // JS `genericValueOf(mode) || ConversionMode.STRICT`.
        let mode = args
            .get(CONVERSION_MODE)
            .and_then(Value::as_str)
            .and_then(|m| ConversionMode::generic_value_of(m).ok())
            .unwrap_or(ConversionMode::Strict);

        let repo = context.get_schema_repository().clone();
        self.convert_to_schema(&schema, repo.as_ref(), element, mode).await
    }
}
