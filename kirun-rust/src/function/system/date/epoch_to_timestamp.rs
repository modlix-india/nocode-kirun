//! Port of kirun-js `function/system/date/EpochToTimestamp.ts`.

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
use crate::json::schema::r#type::{SchemaType, TypeUtil};

pub struct EpochToTimestamp(DateFunction);

impl EpochToTimestamp {
    pub fn new(name: &str, is_seconds: bool) -> Self {
        let param_name = format!("epoch{}", if is_seconds { "Seconds" } else { "Milliseconds" });
        let signature = FunctionSignature::new(name)
            .set_namespace(Namespaces::DATE)
            .set_parameters(Map::from([(
                param_name.clone(),
                Parameter::new(
                    &param_name,
                    Schema::new()
                        .set_name(&param_name)
                        .set_type(TypeUtil::of(&[SchemaType::Long, SchemaType::Integer, SchemaType::String])),
                ),
            )]))
            .set_events(Map::from([(Event::OUTPUT.to_string(), A::event_timestamp())]));
        Self(DateFunction::with_signature(
            signature,
            Event::OUTPUT,
            Arc::new(move |a| {
                // JS parseInt(value): leading integer digits of the number or string.
                let epoch = match a.get(&param_name) {
                    Some(Value::Number(n)) => n.as_f64().map(f64::trunc),
                    Some(Value::String(s)) => crate::runtime::expression::js::parse_int(s),
                    _ => None,
                };
                let ts = epoch.map(|e| if is_seconds { e * 1000.0 } else { e });
                let iso = ts
                    .filter(|t| !t.is_nan())
                    .and_then(|t| luxon::js_date_to_iso_string(t as i64))
                    .ok_or_else(|| KIRunError::runtime(format!("Please provide a valid value for {param_name}.")))?;
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(iso)))
            }),
        ))
    }
}

delegate_date_function!(EpochToTimestamp);
