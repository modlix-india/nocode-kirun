//! Port of kirun-js `function/system/date/TimeAs.ts`.

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
                let dt = get_date_time(arg_str(a, A::PARAMETER_TIMESTAMP_NAME))?;
                let value = if is_array {
                    let c = dt.c;
                    serde_json::json!([c.year, c.month, c.day, c.hour, c.minute, c.second, c.millisecond])
                } else {
                    dt.to_object()
                };
                Ok(result_map(key, value))
            }),
        ))
    }
}

delegate_date_function!(TimeAs);
