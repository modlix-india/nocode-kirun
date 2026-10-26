//! kirun-java `function/system/date/LastFirstOf.java`: the timestamps sorted by
//! `ZonedDateTime.compareTo` (instant, then local time, then zone ID), last or first.

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_time::{get_date_time, java_error};
use crate::model::{Event, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::{Map, Value};

pub struct LastFirstOf(DateFunction);

impl LastFirstOf {
    pub fn new(is_last: bool) -> Self {
        let signature = FunctionSignature::new(if is_last { "LastOf" } else { "FirstOf" })
            .set_namespace(Namespaces::DATE)
            .set_parameters(Map::from([(
                A::PARAMETER_TIMESTAMP_NAME.to_string(),
                Parameter::new(A::PARAMETER_TIMESTAMP_NAME, A::timestamp_schema()).set_variable_argument(true),
            )]))
            .set_events(Map::from([(Event::OUTPUT.to_string(), A::event_timestamp())]));
        Self(DateFunction::with_signature(
            signature,
            Event::OUTPUT,
            Arc::new(move |a| {
                let list = a.get(A::PARAMETER_TIMESTAMP_NAME).and_then(Value::as_array).cloned().unwrap_or_default();
                if list.is_empty() {
                    return Err(java_error("No timestamps provided"));
                }
                let mut times = list
                    .iter()
                    .map(|t| get_date_time(t.as_str().unwrap_or_default()))
                    .collect::<crate::Result<Vec<_>>>()?;
                times.sort_by(|x, y| x.java_compare(y));
                let pick = if is_last { times.last() } else { times.first() };
                let iso = pick.map(|t| t.format_iso()).unwrap_or_default();
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(iso)))
            }),
        ))
    }
}

delegate_date_function!(LastFirstOf);
