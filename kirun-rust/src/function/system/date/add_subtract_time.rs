//! Port of kirun-js `function/system/date/AddSubtractTime.ts`.

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

pub struct AddSubtractTime(DateFunction);

impl AddSubtractTime {
    pub const PARAMETER_YEARS_NAME: &'static str = "years";
    pub const PARAMETER_MONTHS_NAME: &'static str = "months";
    pub const PARAMETER_DAYS_NAME: &'static str = "days";
    pub const PARAMETER_HOURS_NAME: &'static str = "hours";
    pub const PARAMETER_MINUTES_NAME: &'static str = "minutes";
    pub const PARAMETER_SECONDS_NAME: &'static str = "seconds";
    pub const PARAMETER_MILLISECONDS_NAME: &'static str = "milliseconds";

    const UNITS: [&'static str; 7] = ["years", "months", "days", "hours", "minutes", "seconds", "milliseconds"];

    pub fn new(is_add: bool) -> Self {
        let mut params = vec![A::parameter_timestamp()];
        for u in Self::UNITS {
            params.push(Parameter::of(u, Schema::of_number(u).set_default_value(Value::from(0)), false));
        }
        Self(DateFunction::new(
            if is_add { "AddTime" } else { "SubtractTime" },
            A::event_timestamp(),
            params,
            Arc::new(move |a| {
                let dt = get_date_time(arg_str(a, A::PARAMETER_TIMESTAMP_NAME))?;
                let pairs: Vec<(&str, f64)> =
                    Self::UNITS.iter().map(|u| (*u, a.get(*u).and_then(Value::as_f64).unwrap_or(0.0))).collect();
                let dur = Duration::from_pairs(&pairs);
                let out = if is_add { dt.plus(&dur) } else { dt.minus(&dur) };
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(out.to_iso())))
            }),
        ))
    }
}

delegate_date_function!(AddSubtractTime);
