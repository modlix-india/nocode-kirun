//! Port of kirun-js `function/system/date/IsBetween.ts`.

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
                let mut start = get_date_time(arg_str(a, Self::PARAMETER_START_TIMESTAMP_NAME))?.to_millis();
                let mut end = get_date_time(arg_str(a, Self::PARAMETER_END_TIMESTAMP_NAME))?.to_millis();
                let check = get_date_time(arg_str(a, Self::PARAMETER_CHECK_TIMESTAMP_NAME))?.to_millis();
                if start > end {
                    std::mem::swap(&mut start, &mut end);
                }
                Ok(result_map(A::EVENT_RESULT_NAME, Value::Bool(start <= check && check <= end)))
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
