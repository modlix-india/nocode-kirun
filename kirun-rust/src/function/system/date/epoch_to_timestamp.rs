//! kirun-java `function/system/date/EpochToTimestamp.java`: Gson `getAsLong` of the epoch (a
//! string must be a plain `long`), placed in the system zone.

use std::sync::Arc;

use crate::function::system::date::abstract_date_function::{
    delegate_date_function, result_map, AbstractDateFunction as A, DateFunction,
};
use crate::function::system::date::java_time::{java_error, system_default_zone, ZonedDateTime};
use crate::json::schema::r#type::{SchemaType, TypeUtil};
use crate::json::schema::Schema;
use crate::model::{Event, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::{Map, Value};

pub struct EpochToTimestamp(DateFunction);

/// Gson `JsonElement.getAsLong()`.
fn as_long(v: Option<&Value>) -> crate::Result<i64> {
    match v {
        Some(Value::Number(n)) => Ok(n.as_i64().unwrap_or_else(|| n.as_f64().map_or(0, |f| f as i64))),
        Some(Value::String(s)) => s.parse::<i64>().map_err(|_| java_error(format!("For input string: \"{s}\""))),
        Some(Value::Bool(_)) => Err(java_error("For input string: \"true\"")),
        _ => Err(java_error("JsonNull")),
    }
}

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
                let epoch = as_long(a.get(&param_name))?;
                let (secs, nanos) = if is_seconds {
                    (epoch, 0)
                } else {
                    (epoch.div_euclid(1000), (epoch.rem_euclid(1000) * 1_000_000) as u32)
                };
                let dt = ZonedDateTime::of_instant(secs, nanos, system_default_zone())?;
                Ok(result_map(A::EVENT_TIMESTAMP_NAME, Value::String(dt.format_iso())))
            }),
        ))
    }
}

delegate_date_function!(EpochToTimestamp);
