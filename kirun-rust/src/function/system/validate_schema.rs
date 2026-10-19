//! Port of kirun-js `function/system/ValidateSchema.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
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
const IS_VALID: &str = "isValid";

pub struct ValidateSchema {
    signature: FunctionSignature,
}

impl ValidateSchema {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("ValidateSchema")
                .set_namespace(Namespaces::SYSTEM_OBJECT)
                .set_parameters(Map::from([
                    // JS names the source parameter's schema 'schema'.
                    Parameter::of_entry(SOURCE, Schema::of_any(SCHEMA), false),
                    Parameter::of_entry_typed(SCHEMA, Schema::schema().clone(), false, ParameterType::Constant),
                ]))
                .set_events(Map::from([Event::output_event_map_entry(MapUtil::of(vec![(
                    IS_VALID,
                    Schema::of_boolean(IS_VALID),
                )]))])),
        }
    }

    async fn validate_schema(
        &self,
        target_schema: &Schema,
        target_schema_repository: &dyn Repository<Schema>,
        element: Value,
    ) -> Result<FunctionOutput> {
        let valid = SchemaValidator::validate_with(
            Some(vec![]),
            Some(target_schema),
            Some(target_schema_repository),
            element,
            true,
            None,
        )
        .await
        .is_ok();

        Ok(FunctionOutput::new(vec![EventResult::output_of(MapUtil::of(vec![(IS_VALID, Value::Bool(valid))]))]))
    }
}

impl ValidateSchema {
    /// The source as kirun-java leaves it after a successful validation: converted in place
    /// (a number where the schema says STRING becomes a string, and so on). `None` when the
    /// arguments carry no schema or the source does not validate.
    pub async fn converted_source(args: &Map<Value>, repository: &dyn Repository<Schema>) -> Option<Value> {
        let schema = Schema::from_value(args.get(SCHEMA)?).ok()??;
        let element = args.get(SOURCE).cloned().unwrap_or(Value::Null);
        SchemaValidator::validate_with(Some(vec![]), Some(&schema), Some(repository), element, true, None)
            .await
            .ok()
    }
}

impl Default for ValidateSchema {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for ValidateSchema {
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

        let repo = context.get_schema_repository().clone();
        self.validate_schema(&schema, repo.as_ref(), element).await
    }
}
