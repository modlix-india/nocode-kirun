//! kirun-java `function/system/date/TimeAs.java`: the parts of the local date-time.

use std::sync::Arc;

use chrono::{Datelike, Timelike};

use crate::function::system::date::abstract_date_function::{
    arg_string, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_time::get_date_time;
use crate::json::schema::Schema;
use crate::model::Event;
use crate::namespaces::Namespaces;
use crate::{Map, Value};

pub struct TimeAs(DateFunction);

impl TimeAs {
    pub const EVENT_TIME_OBJECT_NAME: &'static str = "object";
    pub const EVENT_TIME_ARRAY_NAME: &'static str = "array";

    pub fn new(is_array: bool) -> Self {
        let key = if is_array { Self::EVENT_TIME_ARRAY_NAME } else { Self::EVENT_TIME_OBJECT_NAME };
        let schema = if is_array {
            Schema::of_array(Self::EVENT_TIME_ARRAY_NAME, vec![Schema::of_integer("timeParts")])
        } else {
            Schema::of_ref(&format!("{}.TimeObject", Namespaces::DATE))
        };
        Self(DateFunction::new(
            if is_array { "TimeAsArray" } else { "TimeAsObject" },
            Event::new(Event::OUTPUT, Map::from([(key.to_string(), schema)])),
            vec![A::parameter_timestamp()],
            Arc::new(move |a| {
                let dt = get_date_time(&arg_string(a, A::PARAMETER_TIMESTAMP_NAME)?)?;
                let l = dt.local;
                let parts: [(&str, i64); 7] = [
                    ("year", l.year() as i64),
                    ("month", l.month() as i64),
                    ("day", l.day() as i64),
                    ("hour", l.hour() as i64),
                    ("minute", l.minute() as i64),
                    ("second", l.second() as i64),
                    ("millisecond", (dt.nano() / 1_000_000) as i64),
                ];
                let value = if is_array {
                    Value::Array(parts.iter().map(|(_, v)| Value::from(*v)).collect())
                } else {
                    Value::Object(parts.iter().map(|(k, v)| (k.to_string(), Value::from(*v))).collect())
                };
                Ok(result_map(key, value))
            }),
        ))
    }
}

delegate_date_function!(TimeAs);
