//! Port of kirun-js `function/system/date/LastFirstOf.ts`.

use std::sync::Arc;

#[allow(unused_imports)]
use crate::function::system::date::abstract_date_function::{
    arg_num, arg_str, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
#[allow(unused_imports)]
use crate::function::system::date::common::get_date_time;
#[allow(unused_imports)]
use crate::function::system::date::luxon::{self, DateTime, Duration, Zone};
#[allow(unused_imports)]
use crate::json::schema::Schema;
#[allow(unused_imports)]
use crate::model::{Event, FunctionSignature, Parameter};
#[allow(unused_imports)]
use crate::namespaces::Namespaces;
#[allow(unused_imports)]
use crate::{KIRunError, Map, Value};

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
                let timestamps = a.get(A::PARAMETER_TIMESTAMP_NAME).and_then(Value::as_array).cloned().unwrap_or_default();
                if timestamps.is_empty() {
                    return Err(KIRunError::runtime("No timestamps provided"));
                }
                let mut dts = timestamps
                    .iter()
                    .map(|t| get_date_time(t.as_str().unwrap_or("")))
                    .collect::<crate::Result<Vec<_>>>()?;
                dts.sort_by_key(|d| d.to_millis());
                let pick = if is_last { dts.last() } else { dts.first() };
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(pick.unwrap().to_iso())))
            }),
        ))
    }
}

delegate_date_function!(LastFirstOf);
