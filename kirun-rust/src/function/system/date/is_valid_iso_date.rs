//! Port of kirun-js `function/system/date/IsValidISODate.ts`.

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

pub struct IsValidISODate(DateFunction);

impl IsValidISODate {
    pub fn new() -> Self {
        let signature = FunctionSignature::new("IsValidISODate")
            .set_namespace(Namespaces::DATE)
            .set_parameters(Map::from([Parameter::of_entry(
                A::PARAMETER_TIMESTAMP_NAME,
                Schema::of_string(A::PARAMETER_TIMESTAMP_NAME),
                false,
            )]))
            .set_events(Map::from([Event::output_event_map_entry(Map::from([(
                A::EVENT_RESULT_NAME.to_string(),
                Schema::of_boolean(A::EVENT_RESULT_NAME),
            )]))]));
        Self(DateFunction::with_signature(
            signature,
            Event::OUTPUT,
            Arc::new(|a| {
                let valid = DateTime::from_iso(arg_str(a, A::PARAMETER_TIMESTAMP_NAME), false).is_some();
                Ok(result_map(A::EVENT_RESULT_NAME, Value::Bool(valid)))
            }),
        ))
    }
}

impl Default for IsValidISODate {
    fn default() -> Self {
        Self::new()
    }
}

delegate_date_function!(IsValidISODate);
