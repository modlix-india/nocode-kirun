//! kirun-java `function/system/date/IsBetween.java`: `equals` either end (the same local time,
//! offset and zone, not just the same instant), or strictly after the start and before the end.

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    arg_string, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_time::get_date_time;
use crate::model::Parameter;
use crate::Value;

pub struct IsBetween(DateFunction);

impl IsBetween {
    pub const PARAMETER_START_TIMESTAMP_NAME: &'static str = "startTimestamp";
    pub const PARAMETER_END_TIMESTAMP_NAME: &'static str = "endTimestamp";
    pub const PARAMETER_CHECK_TIMESTAMP_NAME: &'static str = "checkTimestamp";

    pub fn new() -> Self {
        Self(DateFunction::new(
            "IsBetween",
            A::event_boolean(),
            vec![
                Parameter::of(Self::PARAMETER_START_TIMESTAMP_NAME, A::timestamp_schema(), false),
                Parameter::of(Self::PARAMETER_END_TIMESTAMP_NAME, A::timestamp_schema(), false),
                Parameter::of(Self::PARAMETER_CHECK_TIMESTAMP_NAME, A::timestamp_schema(), false),
            ],
            Arc::new(|a| {
                let start = get_date_time(&arg_string(a, Self::PARAMETER_START_TIMESTAMP_NAME)?)?;
                let end = get_date_time(&arg_string(a, Self::PARAMETER_END_TIMESTAMP_NAME)?)?;
                let check = get_date_time(&arg_string(a, Self::PARAMETER_CHECK_TIMESTAMP_NAME)?)?;
                let between = check.java_equals(&start)
                    || check.java_equals(&end)
                    || (check.is_after(&start) && check.is_before(&end));
                Ok(result_map(A::EVENT_RESULT_NAME, Value::Bool(between)))
            }),
        ))
    }
}

impl Default for IsBetween {
    fn default() -> Self {
        Self::new()
    }
}

delegate_date_function!(IsBetween);
