//! kirun-java `function/system/date/StartEndOf.java`: the unit by `ChronoUnit.valueOf` (so
//! `QUARTERS` and `MILLISECONDS` fail), weeks starting on the default locale's first day; the
//! end is the next start less a millisecond.

use std::sync::Arc;

use chrono::{Datelike, Duration, NaiveDate};

use crate::function::system::date::abstract_date_function::{
    arg_string, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_format::first_day_of_week;
use crate::function::system::date::java_time::{get_date_time, Unit, ZonedDateTime};
use crate::Value;

pub struct StartEndOf(DateFunction);

fn floor(z: &ZonedDateTime, unit: Unit) -> ZonedDateTime {
    let d = z.local.date();
    let date = match unit {
        Unit::Weeks => {
            let back = (d.weekday().number_from_monday() as i64 - first_day_of_week() as i64).rem_euclid(7);
            d - Duration::days(back)
        }
        Unit::Months => NaiveDate::from_ymd_opt(d.year(), d.month(), 1).unwrap_or(d),
        Unit::Years => NaiveDate::from_ymd_opt(d.year(), 1, 1).unwrap_or(d),
        _ => return z.truncated_to(unit),
    };
    z.with_date(date).truncated_to(Unit::Days)
}

impl StartEndOf {
    pub fn new(is_start: bool) -> Self {
        Self(DateFunction::new(
            if is_start { "StartOf" } else { "EndOf" },
            A::event_timestamp(),
            vec![A::parameter_timestamp(), A::parameter_unit()],
            Arc::new(move |a| {
                let dt = get_date_time(&arg_string(a, A::PARAMETER_TIMESTAMP_NAME)?)?;
                let unit = Unit::chrono_value_of(&arg_string(a, A::PARAMETER_UNIT_NAME)?.to_uppercase())?;
                let start = floor(&dt, unit);
                let out = if is_start { start } else { start.plus(1, unit)?.plus(-1, Unit::Millis)? };
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(out.format_iso())))
            }),
        ))
    }
}

delegate_date_function!(StartEndOf);
