//! kirun-java `function/system/date/SetTimeZone.java`: `withZoneSameInstant(ZoneId.of(zone))`,
//! with Java's zone IDs and errors (`local` is not one).

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    arg_string, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_time::{get_date_time, ZoneId};
use crate::json::schema::Schema;
use crate::model::Parameter;
use crate::Value;

pub struct SetTimeZone(DateFunction);

impl SetTimeZone {
    pub const PARAMETER_TIMEZONE_NAME: &'static str = "timezone";

    pub fn new() -> Self {
        Self(DateFunction::new(
            "SetTimeZone",
            A::event_timestamp(),
            vec![
                A::parameter_timestamp(),
                Parameter::of(Self::PARAMETER_TIMEZONE_NAME, Schema::of_string(Self::PARAMETER_TIMEZONE_NAME), false),
            ],
            Arc::new(|a| {
                let dt = get_date_time(&arg_string(a, A::PARAMETER_TIMESTAMP_NAME)?)?;
                let zone = ZoneId::of(&arg_string(a, Self::PARAMETER_TIMEZONE_NAME)?)?;
                let out = dt.with_zone_same_instant(zone)?;
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(out.format_iso())))
            }),
        ))
    }
}

impl Default for SetTimeZone {
    fn default() -> Self {
        Self::new()
    }
}

delegate_date_function!(SetTimeZone);
