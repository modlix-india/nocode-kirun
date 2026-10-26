//! kirun-java `function/system/date/GetCurrentTimestamp.java`: now, in the system zone.

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_time::ZonedDateTime;
use crate::Value;

pub struct GetCurrentTimestamp(DateFunction);

impl GetCurrentTimestamp {
    pub fn new() -> Self {
        Self(DateFunction::new(
            "GetCurrentTimestamp",
            A::event_timestamp(),
            vec![],
            Arc::new(|_| Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(ZonedDateTime::now().format_iso())))),
        ))
    }
}

impl Default for GetCurrentTimestamp {
    fn default() -> Self {
        Self::new()
    }
}

delegate_date_function!(GetCurrentTimestamp);
