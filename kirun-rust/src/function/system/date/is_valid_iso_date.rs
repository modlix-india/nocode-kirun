//! kirun-java `function/system/date/IsValidISODate.java`: whether `DateUtil.getDateTime` reads it.

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    arg_string, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_time::get_date_time;
use crate::json::schema::Schema;
use crate::model::{Event, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::{Map, Value};

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
                let valid = arg_string(a, A::PARAMETER_TIMESTAMP_NAME).and_then(|t| get_date_time(&t)).is_ok();
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
