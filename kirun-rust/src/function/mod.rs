//! Port of kirun-js `function/Function.ts` and `function/AbstractFunction.ts`.

pub mod system;

use std::any::Any;

use async_trait::async_trait;

use crate::json::schema::Schema;
use crate::json::schema::validator::SchemaValidator;
use crate::model::{Event, FunctionOutput, FunctionSignature};
use crate::repository::Repository;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::runtime::statement_execution::StatementExecution;
use crate::util::error_message_formatter::ErrorMessageFormatter;
use crate::{KIRunError, Map, Result, Value};

/// JS `Function`.
#[async_trait]
pub trait Function: Send + Sync {
    fn get_signature(&self) -> &FunctionSignature;

    fn get_probable_event_signature(&self, _probable_parameters: &Map<Vec<Schema>>) -> Map<Event> {
        self.get_signature().get_events().clone()
    }

    async fn execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput>;

    /// Stand-in for JS `fun instanceof KIRuntime` (and other downcasts).
    fn as_any(&self) -> Option<&dyn Any> {
        None
    }
}

/// JS `AbstractFunction`. Implement this (not `Function`) for anything whose arguments
/// should be validated against its signature; `Function` comes from the blanket impl.
#[async_trait]
pub trait AbstractFunction: Send + Sync {
    fn get_signature(&self) -> &FunctionSignature;

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput>;

    fn get_probable_event_signature(&self, _probable_parameters: &Map<Vec<Schema>>) -> Map<Event> {
        self.get_signature().get_events().clone()
    }

    fn as_any(&self) -> Option<&dyn Any> {
        None
    }
}

#[async_trait]
impl<T: AbstractFunction> Function for T {
    fn get_signature(&self) -> &FunctionSignature {
        AbstractFunction::get_signature(self)
    }

    fn get_probable_event_signature(&self, probable_parameters: &Map<Vec<Schema>>) -> Map<Event> {
        AbstractFunction::get_probable_event_signature(self, probable_parameters)
    }

    async fn execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let signature = AbstractFunction::get_signature(self);
        let args = validate_arguments(
            signature,
            context.get_arguments().cloned().unwrap_or_default(),
            context.get_schema_repository().as_ref(),
            context.get_statement_execution().map(|s| s.as_ref()),
        )
        .await?;
        context.set_arguments_in_place(args);
        // kirun-java's `AbstractReactiveFunction.execute` names the function and parameter only
        // for an argument that fails validation; an error the function raises while it runs
        // reaches the caller as it is (kirun-js wraps it in "Error while executing the function
        // <name> in statement '<step>': ...").
        self.internal_execute(context).await
    }

    fn as_any(&self) -> Option<&dyn Any> {
        AbstractFunction::as_any(self)
    }
}

/// JS `AbstractFunction.validateArguments`, with kirun-java's
/// `ReactiveArgumentsValidationUtil.validateArguments` where they differ.
///
/// kirun-java tells an argument that is not there (Java `null`; a step's argument whose
/// expression came to nothing is left out) from one that is JSON `null`: an absent argument
/// whose parameter schema has no `type` (a bare `$ref`, `{}`) and nothing that answers before
/// the type check (default, constant, enums, format) makes `ReactiveSchemaValidator` call
/// `Mono.just(null)`, and the run fails with Reactor's `NullPointerException` "value", outside
/// the wrapper that names the parameter. kirun-js validates `undefined` like `null`.
pub async fn validate_arguments(
    signature: &FunctionSignature,
    args: Map<Value>,
    schema_repository: &dyn Repository<Schema>,
    statement_execution: Option<&StatementExecution>,
) -> Result<Map<Value>> {
    let mut retmap = Map::new();
    for (key, param) in signature.get_parameters() {
        if !param.is_variable_argument() && !args.contains_key(key) && java_null_reaches_mono_just(param.get_schema()) {
            return Err(KIRunError::uncaught("value"));
        }
        let element = args.get(key).cloned().unwrap_or(Value::Null);
        let validated: Result<Value> = async {
            if !param.is_variable_argument() {
                return SchemaValidator::validate(
                    None,
                    Some(param.get_schema()),
                    Some(schema_repository),
                    element,
                )
                .await;
            }
            let mut array = match element {
                Value::Array(a) => a,
                Value::Null => match param.get_schema().get_default_value() {
                    Some(d) if !d.is_null() => vec![d.clone()],
                    _ => vec![],
                },
                other => vec![other],
            };
            for item in array.iter_mut() {
                *item = SchemaValidator::validate(
                    None,
                    Some(param.get_schema()),
                    Some(schema_repository),
                    std::mem::take(item),
                )
                .await?;
            }
            Ok(Value::Array(array))
        }
        .await;

        match validated {
            Ok(v) => {
                retmap.insert(key.clone(), v);
            }
            Err(err) => {
                return Err(KIRunError::runtime(ErrorMessageFormatter::build_function_execution_error(
                    &ErrorMessageFormatter::format_function_name(signature.get_namespace(), signature.get_name()),
                    &ErrorMessageFormatter::format_statement_name(
                        statement_execution.map(|s| s.get_statement().get_statement_name()),
                    ),
                    &ErrorMessageFormatter::format_error_message(&err),
                    Some(param.get_parameter_name()),
                    Some(param.get_schema()),
                )));
            }
        }
    }
    Ok(retmap)
}

/// kirun-java `ReactiveSchemaValidator.validateSchema` with a Java `null` element: a default
/// value, a constant, enums or a format answer first; otherwise `typeValidation` returns
/// `Mono.just(element)` for a schema without a `type`, which throws on `null`.
fn java_null_reaches_mono_just(schema: &Schema) -> bool {
    schema.get_default_value().is_none()
        && schema.get_constant().is_none()
        && schema.get_enums().is_none_or(|e| e.is_empty())
        && schema.get_format().is_none()
        && schema.get_type().is_none()
}
