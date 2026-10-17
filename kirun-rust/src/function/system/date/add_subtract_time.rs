//! kirun-java `function/system/date/AddSubtractTime.java`: years, months and days on the local
//! date-time, then hours, minutes, seconds and milliseconds on the instant, each amount read
//! with Gson's `getAsInt` (a fraction is cut off).

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    arg_int, arg_string, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_time::{get_date_time, Unit};
use crate::json::schema::Schema;
use crate::model::Parameter;
use crate::Value;

pub struct AddSubtractTime(DateFunction);

impl AddSubtractTime {
    pub const PARAMETER_YEARS_NAME: &'static str = "years";
    pub const PARAMETER_MONTHS_NAME: &'static str = "months";
    pub const PARAMETER_DAYS_NAME: &'static str = "days";
    pub const PARAMETER_HOURS_NAME: &'static str = "hours";
    pub const PARAMETER_MINUTES_NAME: &'static str = "minutes";
    pub const PARAMETER_SECONDS_NAME: &'static str = "seconds";
    pub const PARAMETER_MILLISECONDS_NAME: &'static str = "milliseconds";

    const UNITS: [(&'static str, Unit); 7] = [
        ("years", Unit::Years),
        ("months", Unit::Months),
        ("days", Unit::Days),
        ("hours", Unit::Hours),
        ("minutes", Unit::Minutes),
        ("seconds", Unit::Seconds),
        ("milliseconds", Unit::Millis),
    ];

    pub fn new(is_add: bool) -> Self {
        let mut params = vec![A::parameter_timestamp()];
        for (u, _) in Self::UNITS {
            params.push(Parameter::of(u, Schema::of_number(u).set_default_value(Value::from(0)), false));
        }
        Self(DateFunction::new(
            if is_add { "AddTime" } else { "SubtractTime" },
            A::event_timestamp(),
            params,
            Arc::new(move |a| {
                let mut dt = get_date_time(&arg_string(a, A::PARAMETER_TIMESTAMP_NAME)?)?;
                let amounts: Vec<i64> = Self::UNITS.iter().map(|(u, _)| arg_int(a, u) as i64).collect();
                for ((_, unit), amount) in Self::UNITS.iter().zip(amounts) {
                    dt = dt.plus(if is_add { amount } else { -amount }, *unit)?;
                }
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(dt.format_iso())))
            }),
        ))
    }
}

delegate_date_function!(AddSubtractTime);
