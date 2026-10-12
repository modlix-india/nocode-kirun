//! Port of kirun-js `function/system/date/FromNow.ts`.

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
                let base = match arg_str(a, Self::PARAMETER_BASE_NAME) {
                    "" => Some(DateTime::now(Zone::default_zone())),
                    b => DateTime::from_iso(b, false),
                };
                let given = DateTime::from_iso(arg_str(a, A::PARAMETER_TIMESTAMP_NAME), false);
                // luxon toRelative on an invalid DateTime is null → 'Unknown'.
                let (Some(base), Some(given)) = (base, given) else {
                    return Ok(result_map(A::EVENT_RESULT_NAME, Value::from("Unknown")));
                };
                let units: Vec<&'static str> = a
                    .get(A::PARAMETER_UNIT_NAME)
                    .and_then(Value::as_array)
                    .map(|u| u.iter().filter_map(|e| e.as_str().and_then(luxon::normalize_unit)).collect())
                    .unwrap_or_default();
                let style = luxon::RelStyle::of(arg_str(a, Self::PARAMETER_FORMAT_NAME));
                let round = a.get(Self::PARAMETER_ROUND_NAME).and_then(Value::as_bool).unwrap_or(true);
                let text = luxon::to_relative(&given, &base, &units, style, round, arg_str(a, Self::PARAMETER_LOCALE_NAME));
                Ok(result_map(A::EVENT_RESULT_NAME, Value::String(text)))
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
