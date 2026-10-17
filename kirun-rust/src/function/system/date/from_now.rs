//! kirun-java `function/system/date/FromNow.java`: `DateUtil.toRelative(base, timestamp, ...)`
//! (the base first, so a past timestamp reads "in ..."), units by `ChronoUnit.valueOf`, always
//! the long unit names (kirun-java reads the format with its JSON quotes).

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    arg_string, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_format::to_relative;
use crate::function::system::date::java_time::{get_date_time, java_error, Unit, ZonedDateTime};
use crate::json::schema::Schema;
use crate::model::Parameter;
use crate::Value;

pub struct FromNow(DateFunction);

impl FromNow {
    pub const PARAMETER_BASE_NAME: &'static str = "base";
    pub const PARAMETER_LOCALE_NAME: &'static str = "locale";
    pub const PARAMETER_FORMAT_NAME: &'static str = "format";
    pub const PARAMETER_ROUND_NAME: &'static str = "round";

    pub fn new() -> Self {
        Self(DateFunction::new(
            "FromNow",
            A::event_string(),
            vec![
                A::parameter_timestamp(),
                Parameter::new(
                    Self::PARAMETER_FORMAT_NAME,
                    Schema::of_string(Self::PARAMETER_FORMAT_NAME)
                        .set_enums(vec![Value::from("LONG"), Value::from("SHORT"), Value::from("NARROW")])
                        .set_default_value(Value::from("LONG")),
                ),
                Parameter::new(Self::PARAMETER_BASE_NAME, A::timestamp_schema().set_default_value(Value::from(""))),
                A::parameter_variable_unit(),
                Parameter::new(
                    Self::PARAMETER_ROUND_NAME,
                    Schema::of_boolean(Self::PARAMETER_ROUND_NAME).set_default_value(Value::Bool(true)),
                ),
                Parameter::new(
                    Self::PARAMETER_LOCALE_NAME,
                    Schema::of_string(Self::PARAMETER_LOCALE_NAME).set_default_value(Value::from("system")),
                ),
            ],
            Arc::new(|a| {
                let base = arg_string(a, Self::PARAMETER_BASE_NAME)?;
                let base = if base.is_empty() { ZonedDateTime::now() } else { get_date_time(&base)? };
                let given = get_date_time(&arg_string(a, A::PARAMETER_TIMESTAMP_NAME)?)?;
                let units = a
                    .get(A::PARAMETER_UNIT_NAME)
                    .and_then(Value::as_array)
                    .map(|u| u.iter().map(|e| Unit::chrono_value_of(e.as_str().unwrap_or(""))).collect())
                    .unwrap_or_else(|| Ok(Vec::new()))?;
                let round = match a.get(Self::PARAMETER_ROUND_NAME) {
                    Some(Value::Bool(b)) => *b,
                    Some(Value::String(s)) => s.eq_ignore_ascii_case("true"),
                    Some(Value::Null) | None => return Err(java_error("JsonNull")),
                    _ => false,
                };
                Ok(result_map(A::EVENT_RESULT_NAME, Value::String(to_relative(&base, &given, &units, round))))
            }),
        ))
    }
}

impl Default for FromNow {
    fn default() -> Self {
        Self::new()
    }
}

delegate_date_function!(FromNow);
