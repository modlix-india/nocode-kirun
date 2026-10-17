//! kirun-java `function/system/date/FromDateString.java`: the luxon format turned into a Java
//! pattern (`DateUtil.toDateTimeFormat`), parsed with today's year, month and day and a zero time
//! as defaults, in the system zone; the result is `isoTimeStamp` (kirun-js emits `result`).

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    arg_string, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_format::{parse_with_defaults, to_date_time_format};
use crate::function::system::date::java_time::ZonedDateTime;
use crate::json::schema::Schema;
use crate::model::Parameter;
use crate::Value;

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
                let text = arg_string(a, Self::PARAMETER_TIMESTAMP_STRING_NAME)?;
                let format = arg_string(a, Self::PARAMETER_FORMAT_NAME)?;
                let dt = parse_with_defaults(&text, &to_date_time_format(&format), &ZonedDateTime::now())?;
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(dt.format_iso())))
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
