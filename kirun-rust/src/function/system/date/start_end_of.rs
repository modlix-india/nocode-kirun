//! Port of kirun-js `function/system/date/StartEndOf.ts`.

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

pub struct StartEndOf(DateFunction);

impl StartEndOf {
    pub fn new(is_start: bool) -> Self {
        Self(DateFunction::new(
            if is_start { "StartOf" } else { "EndOf" },
            A::event_timestamp(),
            vec![A::parameter_timestamp(), A::parameter_unit()],
            Arc::new(move |a| {
                let dt = get_date_time(arg_str(a, A::PARAMETER_TIMESTAMP_NAME))?;
                // JS lower-cases the Timeunit and drops the plural "s": YEARS → year.
                let unit = arg_str(a, A::PARAMETER_UNIT_NAME).to_lowercase();
                let unit = &unit[..unit.len().saturating_sub(1)];
                let out = if is_start { dt.start_of(unit) } else { dt.end_of(unit) };
                let out = out.ok_or_else(|| KIRunError::runtime(format!("Invalid unit {unit}")))?;
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(out.to_iso())))
            }),
        ))
    }
}

delegate_date_function!(StartEndOf);
