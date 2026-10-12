//! Port of kirun-js `function/system/date/GetNames.ts`.

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

pub struct GetNames(DateFunction);

impl GetNames {
    pub const EVENT_NAMES_NAME: &'static str = "names";
    pub const PARAMETER_UNIT_NAME: &'static str = "unit";
    pub const PARAMETER_LOCALE_NAME: &'static str = "locale";

    pub fn new() -> Self {
        Self(DateFunction::new(
            "GetNames",
            // JS raises an event named "names" (not "output").
            Event::new(
                Self::EVENT_NAMES_NAME,
                Map::from([(
                    Self::EVENT_NAMES_NAME.to_string(),
                    Schema::of_array(Self::EVENT_NAMES_NAME, vec![Schema::of_string(Self::EVENT_NAMES_NAME)]),
                )]),
            ),
            vec![
                Parameter::new(
                    Self::PARAMETER_UNIT_NAME,
                    Schema::of_string(Self::PARAMETER_UNIT_NAME).set_enums(vec![
                        Value::from("TIMEZONES"),
                        Value::from("MONTHS"),
                        Value::from("WEEKDAYS"),
                    ]),
                ),
                Parameter::new(
                    Self::PARAMETER_LOCALE_NAME,
                    Schema::of_string(Self::PARAMETER_LOCALE_NAME).set_default_value(Value::from("system")),
                ),
            ],
            Arc::new(|a| {
                let lang = luxon::Lang::of(arg_str(a, Self::PARAMETER_LOCALE_NAME));
                let names: Vec<String> = match arg_str(a, Self::PARAMETER_UNIT_NAME) {
                    "TIMEZONES" => luxon::time_zone_names(),
                    "MONTHS" => (1..=12).map(|m| luxon::month_name(lang, m, true).to_string()).collect(),
                    "WEEKDAYS" => (1..=7).map(|d| luxon::weekday_name(lang, d, true).to_string()).collect(),
                    _ => Vec::new(),
                };
                Ok(result_map(Self::EVENT_NAMES_NAME, Value::Array(names.into_iter().map(Value::String).collect())))
            }),
        ))
    }
}

impl Default for GetNames {
    fn default() -> Self {
        Self::new()
    }
}

delegate_date_function!(GetNames);
