//! Port of kirun-js `function/system/date/FromDateString.ts`.

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

pub struct FromDateString(DateFunction);

impl FromDateString {
    pub const PARAMETER_FORMAT_NAME: &'static str = "format";
    pub const PARAMETER_TIMESTAMP_STRING_NAME: &'static str = "timestampString";

    pub fn new() -> Self {
        Self(DateFunction::new(
            "FromDateString",
            A::event_timestamp(),
            vec![
                Parameter::of(
                    Self::PARAMETER_TIMESTAMP_STRING_NAME,
                    Schema::of_string(Self::PARAMETER_TIMESTAMP_STRING_NAME),
                    false,
                ),
                Parameter::of(Self::PARAMETER_FORMAT_NAME, Schema::of_string(Self::PARAMETER_FORMAT_NAME), false),
            ],
            Arc::new(|a| {
                // An unparseable string is an invalid DateTime in luxon, whose toISO() is null.
                let iso = luxon::from_format(
                    arg_str(a, Self::PARAMETER_TIMESTAMP_STRING_NAME),
                    arg_str(a, Self::PARAMETER_FORMAT_NAME),
                )
                .map_or(Value::Null, |d| Value::String(d.to_iso()));
                Ok(result_map(A::EVENT_RESULT_NAME, iso))
            }),
        ))
    }
}

impl Default for FromDateString {
    fn default() -> Self {
        Self::new()
    }
}

delegate_date_function!(FromDateString);
