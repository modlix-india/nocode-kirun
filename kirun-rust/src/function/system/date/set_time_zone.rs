//! Port of kirun-js `function/system/date/SetTimeZone.ts`.

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
                let dt = get_date_time(arg_str(a, A::PARAMETER_TIMESTAMP_NAME))?;
                // luxon setZone to an unknown zone gives an invalid DateTime: toISO() is null.
                let iso = Zone::parse(arg_str(a, Self::PARAMETER_TIMEZONE_NAME))
                    .map_or(Value::Null, |z| Value::String(dt.set_zone(z).to_iso()));
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, iso))
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
