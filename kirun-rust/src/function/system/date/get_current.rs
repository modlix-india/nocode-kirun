//! Port of kirun-js `function/system/date/GetCurrent.ts`.

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

pub struct GetCurrentTimestamp(DateFunction);

impl GetCurrentTimestamp {
    pub fn new() -> Self {
        Self(DateFunction::new(
            "GetCurrentTimestamp",
            A::event_timestamp(),
            vec![],
            Arc::new(|_| {
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(DateTime::now(Zone::default_zone()).to_iso())))
            }),
        ))
    }
}

impl Default for GetCurrentTimestamp {
    fn default() -> Self {
        Self::new()
    }
}

delegate_date_function!(GetCurrentTimestamp);
