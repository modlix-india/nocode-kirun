//! Port of kirun-js `function/system/string/AbstractStringFunction.ts`: shared parameter and
//! event definitions plus the `ofEntry*` factories behind most `System.String` functions.
//!
//! JS builds an anonymous subclass per entry; Rust uses one `StringFunction` holding the
//! signature and a closure over the validated arguments.

use std::sync::Arc;

use async_trait::async_trait;

use crate::function::{AbstractFunction, Function};
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::number_value;
use crate::{KIRunError, Map, Result, Value};

pub type StringBody = Arc<dyn Fn(&Map<Value>) -> Result<Value> + Send + Sync>;

/// One `System.String` function: a fixed signature and a body producing the `result` value.
pub struct StringFunction {
    signature: FunctionSignature,
    body: StringBody,
    result_name: &'static str,
}

impl StringFunction {
    pub fn new(signature: FunctionSignature, result_name: &'static str, body: StringBody) -> Self {
        Self { signature, body, result_name }
    }
}

#[async_trait]
impl AbstractFunction for StringFunction {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let args = context.get_arguments().cloned().unwrap_or_default();
        let result = (self.body)(&args)?;
        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(self.result_name.to_string(), result)]))]))
    }
}

/// Declares a dedicated string function type (JS: its own class) backed by `StringFunction`.
macro_rules! string_function {
    ($(#[$doc:meta])* $name:ident, $signature:expr, $result:expr, $body:expr) => {
        $(#[$doc])*
        pub struct $name(crate::function::system::string::abstract_string_function::StringFunction);

        impl $name {
            pub fn new() -> Self {
                Self(crate::function::system::string::abstract_string_function::StringFunction::new(
                    $signature,
                    $result,
                    std::sync::Arc::new($body),
                ))
            }
        }

        impl Default for $name {
            fn default() -> Self {
                Self::new()
            }
        }

        #[async_trait::async_trait]
        impl crate::function::AbstractFunction for $name {
            fn get_signature(&self) -> &crate::model::FunctionSignature {
                crate::function::AbstractFunction::get_signature(&self.0)
            }

            async fn internal_execute(
                &self,
                context: &mut crate::runtime::function_execution_parameters::FunctionExecutionParameters,
            ) -> crate::Result<crate::model::FunctionOutput> {
                self.0.internal_execute(context).await
            }
        }
    };
}
pub(crate) use string_function;

/// A string argument (validated by the signature, so missing means empty).
pub fn arg_str<'a>(args: &'a Map<Value>, name: &str) -> &'a str {
    args.get(name).and_then(Value::as_str).unwrap_or("")
}

/// A numeric argument (JS `undefined` → NaN).
pub fn arg_num(args: &Map<Value>, name: &str) -> f64 {
    args.get(name).and_then(Value::as_f64).unwrap_or(f64::NAN)
}

pub fn signature(name: &str, params: Vec<Parameter>, event: Event) -> FunctionSignature {
    FunctionSignature::new(name)
        .set_namespace(Namespaces::STRING)
        .set_parameters(params.into_iter().map(|p| (p.get_parameter_name().to_string(), p)).collect())
        .set_events(Map::from([(event.get_name().to_string(), event)]))
}

pub fn output_event(result: Schema) -> Event {
    Event::new(Event::OUTPUT, Map::from([(result.get_name().unwrap_or_default().to_string(), result)]))
}

pub struct AbstractStringFunction;

impl AbstractStringFunction {
    pub const PARAMETER_STRING_NAME: &'static str = "string";
    pub const PARAMETER_SEARCH_STRING_NAME: &'static str = "searchString";
    pub const PARAMETER_SECOND_STRING_NAME: &'static str = "secondString";
    pub const PARAMETER_THIRD_STRING_NAME: &'static str = "thirdString";
    pub const PARAMETER_INDEX_NAME: &'static str = "index";
    pub const PARAMETER_SECOND_INDEX_NAME: &'static str = "secondIndex";
    pub const EVENT_RESULT_NAME: &'static str = "result";

    fn p_string() -> Parameter {
        Parameter::new(Self::PARAMETER_STRING_NAME, Schema::of_string(Self::PARAMETER_STRING_NAME))
    }
    fn p_second_string() -> Parameter {
        Parameter::new(Self::PARAMETER_SECOND_STRING_NAME, Schema::of_string(Self::PARAMETER_SECOND_STRING_NAME))
    }
    fn p_third_string() -> Parameter {
        Parameter::new(Self::PARAMETER_THIRD_STRING_NAME, Schema::of_string(Self::PARAMETER_THIRD_STRING_NAME))
    }
    fn p_index() -> Parameter {
        Parameter::new(Self::PARAMETER_INDEX_NAME, Schema::of_integer(Self::PARAMETER_INDEX_NAME))
    }
    fn p_second_index() -> Parameter {
        Parameter::new(Self::PARAMETER_SECOND_INDEX_NAME, Schema::of_integer(Self::PARAMETER_SECOND_INDEX_NAME))
    }
    /// JS names this parameter's schema `string`, not `searchString`; kept for parity.
    fn p_search_string() -> Parameter {
        Parameter::new(Self::PARAMETER_SEARCH_STRING_NAME, Schema::of_string(Self::PARAMETER_STRING_NAME))
    }
    fn event_string() -> Event {
        output_event(Schema::of_string(Self::EVENT_RESULT_NAME))
    }
    fn event_boolean() -> Event {
        output_event(Schema::of_boolean(Self::EVENT_RESULT_NAME))
    }
    fn event_int() -> Event {
        output_event(Schema::of_integer(Self::EVENT_RESULT_NAME))
    }

    fn entry(name: &str, params: Vec<Parameter>, event: Event, body: StringBody) -> (String, Arc<dyn Function>) {
        let f: Arc<dyn Function> =
            Arc::new(StringFunction::new(signature(name, params, event), Self::EVENT_RESULT_NAME, body));
        (name.to_string(), f)
    }

    pub fn of_entry_string_string_and_boolean_output(
        name: &str,
        fun: impl Fn(&str, &str) -> Result<bool> + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            vec![Self::p_string(), Self::p_search_string()],
            Self::event_boolean(),
            Arc::new(move |a| {
                Ok(Value::Bool(fun(arg_str(a, Self::PARAMETER_STRING_NAME), arg_str(a, Self::PARAMETER_SEARCH_STRING_NAME))?))
            }),
        )
    }

    pub fn of_entry_string_integer_and_string_output(
        name: &str,
        fun: impl Fn(&str, f64) -> Result<String> + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            vec![Self::p_string(), Self::p_index()],
            Self::event_string(),
            Arc::new(move |a| {
                Ok(Value::String(fun(arg_str(a, Self::PARAMETER_STRING_NAME), arg_num(a, Self::PARAMETER_INDEX_NAME))?))
            }),
        )
    }

    pub fn of_entry_string_string_and_integer_output(
        name: &str,
        fun: impl Fn(&str, &str) -> f64 + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            vec![Self::p_string(), Self::p_search_string()],
            Self::event_int(),
            Arc::new(move |a| {
                Ok(number_value(fun(arg_str(a, Self::PARAMETER_STRING_NAME), arg_str(a, Self::PARAMETER_SEARCH_STRING_NAME))))
            }),
        )
    }

    pub fn of_entry_string_and_string_output(
        name: &str,
        fun: impl Fn(&str) -> String + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            vec![Self::p_string()],
            Self::event_string(),
            Arc::new(move |a| Ok(Value::String(fun(arg_str(a, Self::PARAMETER_STRING_NAME))))),
        )
    }

    pub fn of_entry_string_and_boolean_output(
        name: &str,
        fun: impl Fn(&str) -> bool + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            vec![Self::p_string()],
            Self::event_boolean(),
            Arc::new(move |a| Ok(Value::Bool(fun(arg_str(a, Self::PARAMETER_STRING_NAME))))),
        )
    }

    pub fn of_entry_string_string_integer_and_integer_output(
        name: &str,
        fun: impl Fn(&str, &str, f64) -> f64 + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            vec![Self::p_string(), Self::p_search_string(), Self::p_index()],
            Self::event_int(),
            Arc::new(move |a| {
                Ok(number_value(fun(
                    arg_str(a, Self::PARAMETER_STRING_NAME),
                    arg_str(a, Self::PARAMETER_SEARCH_STRING_NAME),
                    arg_num(a, Self::PARAMETER_INDEX_NAME),
                )))
            }),
        )
    }

    pub fn of_entry_string_integer_integer_and_string_output(
        name: &str,
        fun: impl Fn(&str, f64, f64) -> String + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            vec![Self::p_string(), Self::p_index(), Self::p_second_index()],
            Self::event_string(),
            Arc::new(move |a| {
                Ok(Value::String(fun(
                    arg_str(a, Self::PARAMETER_STRING_NAME),
                    arg_num(a, Self::PARAMETER_INDEX_NAME),
                    arg_num(a, Self::PARAMETER_SECOND_INDEX_NAME),
                )))
            }),
        )
    }

    pub fn of_entry_string_string_string_and_string_output(
        name: &str,
        fun: impl Fn(&str, &str, &str) -> String + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            vec![Self::p_string(), Self::p_second_string(), Self::p_third_string()],
            Self::event_string(),
            Arc::new(move |a| {
                Ok(Value::String(fun(
                    arg_str(a, Self::PARAMETER_STRING_NAME),
                    arg_str(a, Self::PARAMETER_SECOND_STRING_NAME),
                    arg_str(a, Self::PARAMETER_THIRD_STRING_NAME),
                )))
            }),
        )
    }

    pub fn of_entry_string_and_integer_output(
        name: &str,
        fun: impl Fn(&str) -> f64 + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            vec![Self::p_string()],
            Self::event_int(),
            Arc::new(move |a| Ok(number_value(fun(arg_str(a, Self::PARAMETER_STRING_NAME))))),
        )
    }
}

/// JS `new RegExp(pattern)`; an invalid pattern is a SyntaxError in JS.
pub fn js_regex(pattern: &str) -> Result<std::sync::Arc<fancy_regex::Regex>> {
    crate::util::regex_cache::cached_regex(pattern)
        .map_err(|e| KIRunError::runtime(format!("Invalid regular expression: /{pattern}/: {e}")))
}
