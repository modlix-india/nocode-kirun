//! Port of kirun-js `function/system/date/DateFunctionRepository.ts`.

use std::sync::Arc;

use async_trait::async_trait;

use crate::function::system::date::abstract_date_function::AbstractDateFunction as A;
use crate::function::system::date::common::get_date_time as dt;
use crate::function::system::date::luxon::{self, Duration};
use crate::function::system::date::{
    add_subtract_time::AddSubtractTime, epoch_to_timestamp::EpochToTimestamp, from_date_string::FromDateString,
    from_now::FromNow, get_current::GetCurrentTimestamp, get_names::GetNames, is_between::IsBetween,
    is_valid_iso_date::IsValidISODate, last_first_of::LastFirstOf, set_time_zone::SetTimeZone,
    start_end_of::StartEndOf, time_as::TimeAs, timestamp_to_epoch::TimestampToEpoch, to_date_string::ToDateString,
};
use crate::function::Function;
use crate::json::schema::Schema;
use crate::model::Event;
use crate::namespaces::Namespaces;
use crate::repository::Repository;
use crate::{Map, Value};

pub struct DateFunctionRepository {
    repo_map: Map<Arc<dyn Function>>,
    filterable_names: Vec<String>,
}

fn entry(name: &str, f: impl Function + 'static) -> (String, Arc<dyn Function>) {
    (name.to_string(), Arc::new(f))
}

fn bool_event() -> Event {
    Event::new(Event::OUTPUT, Map::from([(A::EVENT_RESULT_NAME.to_string(), Schema::of_boolean(A::EVENT_RESULT_NAME))]))
}

/// The `set({ unit })` entries: SetDay, SetMonth, ...
fn setter(name: &str, unit: &'static str) -> (String, Arc<dyn Function>) {
    A::of_entry_timestamp_integer_and_timestamp_output(name, move |iso, n| Ok(dt(iso)?.set(&[(unit, n as i64)]).to_iso()))
}

fn compare(name: &str, f: fn(i64, i64) -> bool) -> (String, Arc<dyn Function>) {
    A::of_entry_timestamp_timestamp_and_t_output(
        name,
        bool_event(),
        move |t1, t2, _| Ok(Value::Bool(f(dt(t1)?.to_millis(), dt(t2)?.to_millis()))),
        vec![],
    )
}

impl DateFunctionRepository {
    pub fn new() -> Self {
        let entries: Vec<(String, Arc<dyn Function>)> = vec![
            entry("EpochSecondsToTimestamp", EpochToTimestamp::new("EpochSecondsToTimestamp", true)),
            entry("EpochMillisecondsToTimestamp", EpochToTimestamp::new("EpochMillisecondsToTimestamp", false)),
            A::of_entry_timestamp_and_integer_output("GetDay", |i| Ok(dt(i)?.c.day as f64)),
            A::of_entry_timestamp_and_integer_output("GetDayOfWeek", |i| Ok(dt(i)?.weekday() as f64)),
            A::of_entry_timestamp_and_integer_output("GetMonth", |i| Ok(dt(i)?.c.month as f64)),
            A::of_entry_timestamp_and_integer_output("GetYear", |i| Ok(dt(i)?.c.year as f64)),
            A::of_entry_timestamp_and_integer_output("GetHours", |i| Ok(dt(i)?.c.hour as f64)),
            A::of_entry_timestamp_and_integer_output("GetMinutes", |i| Ok(dt(i)?.c.minute as f64)),
            A::of_entry_timestamp_and_integer_output("GetSeconds", |i| Ok(dt(i)?.c.second as f64)),
            A::of_entry_timestamp_and_integer_output("GetMilliseconds", |i| Ok(dt(i)?.c.millisecond as f64)),
            A::of_entry_timestamp_and_integer_output("GetDaysInMonth", |i| Ok(dt(i)?.days_in_month() as f64)),
            A::of_entry_timestamp_and_integer_output("GetDaysInYear", |i| Ok(dt(i)?.days_in_year() as f64)),
            entry("TimestampToEpochSeconds", TimestampToEpoch::new("TimestampToEpochSeconds", true)),
            entry("TimestampToEpochMilliseconds", TimestampToEpoch::new("TimestampToEpochMilliseconds", false)),
            A::of_entry_timestamp_and_string_output("GetTimeZoneName", |i| Ok(dt(i)?.zone.name())),
            A::of_entry_timestamp_and_string_output("GetTimeZoneOffsetLong", |i| Ok(dt(i)?.offset_name_long())),
            A::of_entry_timestamp_and_string_output("GetTimeZoneOffsetShort", |i| Ok(dt(i)?.offset_name_short())),
            A::of_entry_timestamp_and_integer_output("GetTimeZoneOffset", |i| Ok(dt(i)?.o as f64)),
            entry("ToDateString", ToDateString::new()),
            entry("AddTime", AddSubtractTime::new(true)),
            entry("SubtractTime", AddSubtractTime::new(false)),
            entry("GetCurrentTimestamp", GetCurrentTimestamp::new()),
            A::of_entry_timestamp_timestamp_and_t_output(
                "Difference",
                Event::new(
                    Event::OUTPUT,
                    Map::from([(
                        A::EVENT_RESULT_NAME.to_string(),
                        Schema::of_ref(&format!("{}.Duration", Namespaces::DATE)),
                    )]),
                ),
                |t1, t2, extra| {
                    let (d1, d2) = (dt(t1)?, dt(t2)?);
                    let units: Vec<&'static str> = extra
                        .first()
                        .and_then(Value::as_array)
                        .map(|u| u.iter().filter_map(|e| e.as_str().and_then(luxon::normalize_unit)).collect())
                        .unwrap_or_default();
                    let duration = Duration::from_pairs(&[("milliseconds", (d1.to_millis() - d2.to_millis()) as f64)]);
                    Ok(if units.is_empty() { duration } else { duration.shift_to(&units) }.to_object())
                },
                vec![A::parameter_variable_unit()],
            ),
            setter("SetDay", "day"),
            setter("SetMonth", "month"),
            setter("SetYear", "year"),
            setter("SetHours", "hour"),
            setter("SetMinutes", "minute"),
            setter("SetSeconds", "second"),
            setter("SetMilliseconds", "millisecond"),
            entry("SetTimeZone", SetTimeZone::new()),
            compare("IsBefore", |a, b| a < b),
            compare("IsAfter", |a, b| a > b),
            // Same instant (Java ZonedDateTime.isEqual).
            compare("IsSame", |a, b| a == b),
            compare("IsSameOrBefore", |a, b| a <= b),
            compare("IsSameOrAfter", |a, b| a >= b),
            A::of_entry_timestamp_and_boolean_output("IsInLeapYear", |i| Ok(dt(i)?.is_in_leap_year())),
            A::of_entry_timestamp_and_boolean_output("IsInDST", |i| Ok(dt(i)?.is_in_dst())),
            entry("IsBetween", IsBetween::new()),
            entry("LastOf", LastFirstOf::new(true)),
            entry("FirstOf", LastFirstOf::new(false)),
            entry("StartOf", StartEndOf::new(true)),
            entry("EndOf", StartEndOf::new(false)),
            entry("TimeAsObject", TimeAs::new(false)),
            entry("TimeAsArray", TimeAs::new(true)),
            entry("GetNames", GetNames::new()),
            entry("IsValidISODate", IsValidISODate::new()),
            entry("FromNow", FromNow::new()),
            entry("FromDateString", FromDateString::new()),
        ];
        let repo_map: Map<Arc<dyn Function>> = entries.into_iter().collect();
        let filterable_names = repo_map.values().map(|f| f.get_signature().get_full_name()).collect();
        Self { repo_map, filterable_names }
    }
}

impl Default for DateFunctionRepository {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl Repository<dyn Function> for DateFunctionRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<dyn Function>> {
        if namespace != Namespaces::DATE {
            return None;
        }
        self.repo_map.get(name).cloned()
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        let name = name.to_lowercase();
        self.filterable_names.iter().filter(|e| e.to_lowercase().contains(&name)).cloned().collect()
    }
}
