//! kirun-java `function/system/date/TimestampToEpoch.java`: epoch seconds or milliseconds (floor).

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    arg_string, delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_time::get_date_time;
use crate::model::{Event, FunctionSignature};
use crate::namespaces::Namespaces;
use crate::{Map, Value};

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
                let dt = get_date_time(&arg_string(a, A::PARAMETER_TIMESTAMP_NAME)?)?;
                let epoch = if is_seconds { dt.epoch_second() } else { dt.to_epoch_milli() };
                Ok(result_map(A::EVENT_RESULT_NAME, Value::from(epoch)))
            }),
        ))
    }
}

delegate_date_function!(TimestampToEpoch);
