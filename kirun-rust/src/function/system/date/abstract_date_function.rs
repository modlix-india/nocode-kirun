//! Port of kirun-js `function/system/date/AbstractDateFunction.ts`: shared parameters, events and
//! the `ofEntry*` factories behind most `System.Date` functions.
//!
//! JS builds an anonymous subclass per entry; Rust uses one `DateFunction` holding the signature
//! and a closure from the validated arguments to the output event's result map.

use std::sync::Arc;

use async_trait::async_trait;

use crate::function::system::date::common::get_date_time;
use crate::function::{AbstractFunction, Function};
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::number_value;
use crate::{Map, Result, Value};

pub type DateBody = Arc<dyn Fn(&Map<Value>) -> Result<Map<Value>> + Send + Sync>;

/// One `System.Date` function: a signature, the event it raises and its body.
pub struct DateFunction {
    signature: FunctionSignature,
    event_name: String,
    body: DateBody,
}

impl DateFunction {
    /// JS `AbstractDateFunction` constructor: namespace System.Date, one event, parameters.
    pub fn new(name: &str, event: Event, params: Vec<Parameter>, body: DateBody) -> Self {
        let event_name = event.get_name().to_string();
        let mut signature = FunctionSignature::new(name)
            .set_namespace(Namespaces::DATE)
            .set_events(Map::from([(event_name.clone(), event)]));
        if !params.is_empty() {
            signature = signature.set_parameters(params.into_iter().map(|p| (p.get_parameter_name().to_string(), p)).collect());
        }
        Self { signature, event_name, body }
    }

    /// A function whose signature does not come from the abstract constructor.
    pub fn with_signature(signature: FunctionSignature, event_name: &str, body: DateBody) -> Self {
        Self { signature, event_name: event_name.to_string(), body }
    }
}

#[async_trait]
impl AbstractFunction for DateFunction {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let args = context.get_arguments().cloned().unwrap_or_default();
        let result = (self.body)(&args)?;
        Ok(FunctionOutput::new(vec![EventResult::of(&self.event_name, result)]))
    }
}

/// Implements `AbstractFunction` for a newtype over `DateFunction`.
macro_rules! delegate_date_function {
    ($name:ident) => {
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
pub(crate) use delegate_date_function;

pub fn arg_str<'a>(args: &'a Map<Value>, name: &str) -> &'a str {
    args.get(name).and_then(Value::as_str).unwrap_or("")
}

pub fn arg_num(args: &Map<Value>, name: &str) -> f64 {
    args.get(name).and_then(Value::as_f64).unwrap_or(f64::NAN)
}

pub fn result_map(key: &str, value: Value) -> Map<Value> {
    Map::from([(key.to_string(), value)])
}

pub struct AbstractDateFunction;

impl AbstractDateFunction {
    pub const PARAMETER_TIMESTAMP_NAME: &'static str = "isoTimeStamp";
    pub const PARAMETER_TIMESTAMP_NAME_ONE: &'static str = "isoTimeStamp1";
    pub const PARAMETER_TIMESTAMP_NAME_TWO: &'static str = "isoTimeStamp2";
    pub const PARAMETER_UNIT_NAME: &'static str = "unit";
    pub const PARAMETER_NUMBER_NAME: &'static str = "number";
    pub const EVENT_RESULT_NAME: &'static str = "result";
    pub const EVENT_TIMESTAMP_NAME: &'static str = "isoTimeStamp";

    pub fn timestamp_schema() -> Schema {
        Schema::of_ref(&format!("{}.Timestamp", Namespaces::DATE))
    }

    pub fn parameter_timestamp() -> Parameter {
        Parameter::new(Self::PARAMETER_TIMESTAMP_NAME, Self::timestamp_schema())
    }
    pub fn parameter_timestamp_one() -> Parameter {
        Parameter::new(Self::PARAMETER_TIMESTAMP_NAME_ONE, Self::timestamp_schema())
    }
    pub fn parameter_timestamp_two() -> Parameter {
        Parameter::new(Self::PARAMETER_TIMESTAMP_NAME_TWO, Self::timestamp_schema())
    }
    pub fn parameter_variable_unit() -> Parameter {
        Parameter::new(Self::PARAMETER_UNIT_NAME, Schema::of_ref(&format!("{}.Timeunit", Namespaces::DATE)))
            .set_variable_argument(true)
    }
    pub fn parameter_unit() -> Parameter {
        Parameter::new(Self::PARAMETER_UNIT_NAME, Schema::of_ref(&format!("{}.Timeunit", Namespaces::DATE)))
    }
    pub fn parameter_number() -> Parameter {
        Parameter::new(Self::PARAMETER_NUMBER_NAME, Schema::of_integer(Self::PARAMETER_NUMBER_NAME))
    }

    fn output(key: &str, schema: Schema) -> Event {
        Event::new(Event::OUTPUT, Map::from([(key.to_string(), schema)]))
    }
    pub fn event_int() -> Event {
        Self::output(Self::EVENT_RESULT_NAME, Schema::of_integer(Self::EVENT_RESULT_NAME))
    }
    pub fn event_string() -> Event {
        Self::output(Self::EVENT_RESULT_NAME, Schema::of_string(Self::EVENT_RESULT_NAME))
    }
    pub fn event_long() -> Event {
        Self::output(Self::EVENT_RESULT_NAME, Schema::of_long(Self::EVENT_RESULT_NAME))
    }
    pub fn event_boolean() -> Event {
        Self::output(Self::EVENT_RESULT_NAME, Schema::of_boolean(Self::EVENT_RESULT_NAME))
    }
    pub fn event_timestamp() -> Event {
        Self::output(Self::EVENT_TIMESTAMP_NAME, Self::timestamp_schema())
    }

    fn entry(name: &str, event: Event, params: Vec<Parameter>, body: DateBody) -> (String, Arc<dyn Function>) {
        let f: Arc<dyn Function> = Arc::new(DateFunction::new(name, event, params, body));
        (name.to_string(), f)
    }

    pub fn of_entry_timestamp_and_integer_output(
        name: &str,
        fun: impl Fn(&str) -> Result<f64> + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            Self::event_int(),
            vec![Self::parameter_timestamp()],
            Arc::new(move |a| {
                Ok(result_map(Self::EVENT_RESULT_NAME, number_value(fun(arg_str(a, Self::PARAMETER_TIMESTAMP_NAME))?)))
            }),
        )
    }

    pub fn of_entry_timestamp_and_boolean_output(
        name: &str,
        fun: impl Fn(&str) -> Result<bool> + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            Self::event_boolean(),
            vec![Self::parameter_timestamp()],
            Arc::new(move |a| {
                Ok(result_map(Self::EVENT_RESULT_NAME, Value::Bool(fun(arg_str(a, Self::PARAMETER_TIMESTAMP_NAME))?)))
            }),
        )
    }

    pub fn of_entry_timestamp_and_string_output(
        name: &str,
        fun: impl Fn(&str) -> Result<String> + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            Self::event_string(),
            vec![Self::parameter_timestamp()],
            Arc::new(move |a| {
                Ok(result_map(Self::EVENT_RESULT_NAME, Value::String(fun(arg_str(a, Self::PARAMETER_TIMESTAMP_NAME))?)))
            }),
        )
    }

    /// JS declares the `isoTimeStamp` event key but emits the value under `result`; kept.
    pub fn of_entry_timestamp_integer_and_timestamp_output(
        name: &str,
        fun: impl Fn(&str, f64) -> Result<String> + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            Self::event_timestamp(),
            vec![Self::parameter_timestamp(), Self::parameter_number()],
            Arc::new(move |a| {
                Ok(result_map(
                    Self::EVENT_RESULT_NAME,
                    Value::String(fun(arg_str(a, Self::PARAMETER_TIMESTAMP_NAME), arg_num(a, Self::PARAMETER_NUMBER_NAME))?),
                ))
            }),
        )
    }

    /// Two timestamps (plus `extra` parameters, passed in order) to a result of any type.
    pub fn of_entry_timestamp_timestamp_and_t_output(
        name: &str,
        event: Event,
        fun: impl Fn(&str, &str, &[Value]) -> Result<Value> + Send + Sync + 'static,
        extra: Vec<Parameter>,
    ) -> (String, Arc<dyn Function>) {
        let extra_names: Vec<String> = extra.iter().map(|p| p.get_parameter_name().to_string()).collect();
        let mut params = vec![Self::parameter_timestamp_one(), Self::parameter_timestamp_two()];
        params.extend(extra);
        Self::entry(
            name,
            event,
            params,
            Arc::new(move |a| {
                let extra_values: Vec<Value> =
                    extra_names.iter().map(|n| a.get(n).cloned().unwrap_or(Value::Null)).collect();
                Ok(result_map(
                    Self::EVENT_RESULT_NAME,
                    fun(arg_str(a, Self::PARAMETER_TIMESTAMP_NAME_ONE), arg_str(a, Self::PARAMETER_TIMESTAMP_NAME_TWO), &extra_values)?,
                ))
            }),
        )
    }

    /// `get_date_time` for closures.
    pub fn dt(iso: &str) -> Result<crate::function::system::date::luxon::DateTime> {
        get_date_time(iso)
    }
}
