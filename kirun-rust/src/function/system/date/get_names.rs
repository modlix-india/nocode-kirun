//! kirun-java `function/system/date/GetNames.java`: month or weekday names in the default
//! locale (the `locale` parameter only applies to zone names there), or every zone's display
//! name; an unknown unit gives null. The signature declares a `names` event, but the result is
//! raised as `output` (kirun-java's `EventResult.outputOf`).

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    arg_string, delegate_date_function, result_map, DateFunction,
};
use crate::function::system::date::java_format::{month_names, weekday_names, zone_names_list};
use crate::json::schema::Schema;
use crate::model::{Event, Parameter};
use crate::{Map, Value};

pub struct GetNames(DateFunction);

impl GetNames {
    pub const EVENT_NAMES_NAME: &'static str = "names";
    pub const PARAMETER_UNIT_NAME: &'static str = "unit";
    pub const PARAMETER_LOCALE_NAME: &'static str = "locale";

    pub fn new() -> Self {
        Self(DateFunction::new(
            "GetNames",
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
                let names = match arg_string(a, Self::PARAMETER_UNIT_NAME)?.as_str() {
                    "TIMEZONES" => Some(zone_names_list()),
                    "MONTHS" => Some(month_names()),
                    "WEEKDAYS" => Some(weekday_names()),
                    _ => None,
                };
                Ok(result_map(
                    Self::EVENT_NAMES_NAME,
                    names.map_or(Value::Null, |n| Value::Array(n.into_iter().map(Value::String).collect())),
                ))
            }),
        )
        .raising(Event::OUTPUT))
    }
}

impl Default for GetNames {
    fn default() -> Self {
        Self::new()
    }
}

delegate_date_function!(GetNames);
