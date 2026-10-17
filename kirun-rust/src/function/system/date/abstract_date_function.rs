//! Port of kirun-js `function/system/date/AbstractDateFunction.ts`: shared parameters, events and
//! the `ofEntry*` factories behind most `System.Date` functions, which read timestamps with
//! kirun-java's `DateUtil.getDateTime`.
//!
//! JS builds an anonymous subclass per entry; Rust uses one `DateFunction` holding the signature
//! and a closure from the validated arguments to the output event's result map.

use std::sync::Arc;

use async_trait::async_trait;

use crate::function::system::date::java_time::{get_date_time, java_error, ZonedDateTime};
use crate::function::{AbstractFunction, Function};
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
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

    /// Raise the result under `event_name` whatever the signature declares.
    pub fn raising(mut self, event_name: &str) -> Self {
        self.event_name = event_name.to_string();
        self
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

/// Gson `JsonElement.getAsString()` of an argument (validated arguments are always present).
pub fn arg_string(args: &Map<Value>, name: &str) -> Result<String> {
    match args.get(name) {
        Some(Value::String(s)) => Ok(s.clone()),
        Some(Value::Number(n)) => Ok(n.to_string()),
        Some(Value::Bool(b)) => Ok(b.to_string()),
        Some(Value::Object(_)) => Err(java_error("JsonObject")),
        Some(Value::Array(a)) if a.len() == 1 => {
            let one = Map::from([(name.to_string(), a[0].clone())]);
            arg_string(&one, name)
        }
        Some(Value::Array(a)) => Err(java_error(format!("Array must have size 1, but has size {}", a.len()))),
        Some(Value::Null) => Err(java_error("JsonNull")),
        None => Err(java_error(
            "Cannot invoke \"com.google.gson.JsonElement.getAsString()\" because the return value of \"java.util.Map.get(Object)\" is null",
        )),
    }
}

/// Gson `JsonElement.getAsInt()`: a fraction is cut off, a `long` keeps its low 32 bits.
pub fn arg_int(args: &Map<Value>, name: &str) -> i32 {
    match args.get(name) {
        Some(Value::Number(n)) => match n.as_i64() {
            Some(i) => i as i32,
            None => n.as_f64().map_or(0, |f| f as i32),
        },
        Some(Value::String(s)) => s.trim().parse::<i64>().map_or(0, |i| i as i32),
        _ => 0,
    }
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
        fun: impl Fn(&ZonedDateTime) -> i64 + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            Self::event_int(),
            vec![Self::parameter_timestamp()],
            Arc::new(move |a| {
                let dt = get_date_time(&arg_string(a, Self::PARAMETER_TIMESTAMP_NAME)?)?;
                Ok(result_map(Self::EVENT_RESULT_NAME, Value::from(fun(&dt))))
            }),
        )
    }

    pub fn of_entry_timestamp_and_boolean_output(
        name: &str,
        fun: impl Fn(&ZonedDateTime) -> bool + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            Self::event_boolean(),
            vec![Self::parameter_timestamp()],
            Arc::new(move |a| {
                let dt = get_date_time(&arg_string(a, Self::PARAMETER_TIMESTAMP_NAME)?)?;
                Ok(result_map(Self::EVENT_RESULT_NAME, Value::Bool(fun(&dt))))
            }),
        )
    }

    pub fn of_entry_timestamp_and_string_output(
        name: &str,
        fun: impl Fn(&ZonedDateTime) -> String + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            Self::event_string(),
            vec![Self::parameter_timestamp()],
            Arc::new(move |a| {
                let dt = get_date_time(&arg_string(a, Self::PARAMETER_TIMESTAMP_NAME)?)?;
                Ok(result_map(Self::EVENT_RESULT_NAME, Value::String(fun(&dt))))
            }),
        )
    }

    /// kirun-java declares the `isoTimeStamp` event key but emits the value under `result`; kept.
    pub fn of_entry_timestamp_integer_and_timestamp_output(
        name: &str,
        fun: impl Fn(&ZonedDateTime, i64) -> Result<ZonedDateTime> + Send + Sync + 'static,
    ) -> (String, Arc<dyn Function>) {
        Self::entry(
            name,
            Self::event_timestamp(),
            vec![Self::parameter_timestamp(), Self::parameter_number()],
            Arc::new(move |a| {
                let dt = get_date_time(&arg_string(a, Self::PARAMETER_TIMESTAMP_NAME)?)?;
                let out = fun(&dt, arg_int(a, Self::PARAMETER_NUMBER_NAME) as i64)?;
                Ok(result_map(Self::EVENT_RESULT_NAME, Value::String(out.format_iso())))
            }),
        )
    }

    /// Two timestamps (plus `extra` parameters, passed in order) to a result of any type.
    pub fn of_entry_timestamp_timestamp_and_t_output(
        name: &str,
        event: Event,
        fun: impl Fn(&ZonedDateTime, &ZonedDateTime, &[Value]) -> Result<Value> + Send + Sync + 'static,
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
                let t1 = get_date_time(&arg_string(a, Self::PARAMETER_TIMESTAMP_NAME_ONE)?)?;
                let t2 = get_date_time(&arg_string(a, Self::PARAMETER_TIMESTAMP_NAME_TWO)?)?;
                Ok(result_map(Self::EVENT_RESULT_NAME, fun(&t1, &t2, &extra_values)?))
            }),
        )
    }

    /// kirun-java `DateUtil.getDateTime`, for closures.
    pub fn dt(iso: &str) -> Result<ZonedDateTime> {
        get_date_time(iso)
    }
}
