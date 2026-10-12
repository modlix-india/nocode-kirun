//! Port of kirun-js `function/system/date/ToDateString.ts`.

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

pub struct ToDateString(DateFunction);

impl ToDateString {
    pub const PARAMETER_FORMAT_NAME: &'static str = "format";
    pub const PARAMETER_LOCALE_NAME: &'static str = "locale";

    pub fn new() -> Self {
        Self(DateFunction::new(
            "ToDateString",
            A::event_string(),
            vec![
                A::parameter_timestamp(),
                Parameter::of(Self::PARAMETER_FORMAT_NAME, Schema::of_string(Self::PARAMETER_FORMAT_NAME), false),
                Parameter::of(
                    Self::PARAMETER_LOCALE_NAME,
                    Schema::of_string(Self::PARAMETER_LOCALE_NAME).set_default_value(Value::from("")),
                    false,
                ),
            ],
            Arc::new(|a| {
                let dt = get_date_time(arg_str(a, A::PARAMETER_TIMESTAMP_NAME))?;
                let locale = match arg_str(a, Self::PARAMETER_LOCALE_NAME) {
                    "" => "system",
                    l => l,
                };
                Ok(result_map(
                    A::EVENT_RESULT_NAME,
                    Value::String(luxon::to_format(&dt, arg_str(a, Self::PARAMETER_FORMAT_NAME), locale)),
                ))
            }),
        ))
    }
}

impl Default for ToDateString {
    fn default() -> Self {
        Self::new()
    }
}

delegate_date_function!(ToDateString);
