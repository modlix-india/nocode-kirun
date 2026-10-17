//! kirun-java `function/system/date/ToDateString.java`: the luxon format turned into a Java
//! pattern (`DateUtil.toDateTimeFormat`) and printed in the default locale (the `locale`
//! parameter is not used, as in kirun-java).

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    arg_string, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_format::{format_pattern, to_date_time_format};
use crate::function::system::date::java_time::get_date_time;
use crate::json::schema::Schema;
use crate::model::Parameter;
use crate::Value;

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
                    Schema::of_string(Self::PARAMETER_LOCALE_NAME).set_default_value(Value::from("en")),
                    false,
                ),
            ],
            Arc::new(|a| {
                let dt = get_date_time(&arg_string(a, A::PARAMETER_TIMESTAMP_NAME)?)?;
                let format = arg_string(a, Self::PARAMETER_FORMAT_NAME)?;
                let text = format_pattern(&dt, &to_date_time_format(&format))?;
                Ok(result_map(A::EVENT_RESULT_NAME, Value::String(text)))
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
