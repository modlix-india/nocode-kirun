//! Port of kirun-js `function/system/date/TimestampToEpoch.ts`.

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
use crate::util::primitive::number_value;

pub struct TimestampToEpoch(DateFunction);

impl TimestampToEpoch {
    pub fn new(name: &str, is_seconds: bool) -> Self {
        let signature = FunctionSignature::new(name)
            .set_namespace(Namespaces::DATE)
            .set_parameters(Map::from([(A::PARAMETER_TIMESTAMP_NAME.to_string(), A::parameter_timestamp())]))
            .set_events(Map::from([(Event::OUTPUT.to_string(), A::event_long())]));
        Self(DateFunction::with_signature(
            signature,
            Event::OUTPUT,
            Arc::new(move |a| {
                // JS DateTime.fromISO(ts).toMillis() is NaN (→ null) for an invalid timestamp.
                let epoch = DateTime::from_iso(arg_str(a, A::PARAMETER_TIMESTAMP_NAME), false)
                    .map(|d| if is_seconds { d.to_millis() as f64 / 1000.0 } else { d.to_millis() as f64 })
                    .unwrap_or(f64::NAN);
                Ok(result_map(A::EVENT_RESULT_NAME, number_value(epoch)))
            }),
        ))
    }
}

delegate_date_function!(TimestampToEpoch);
