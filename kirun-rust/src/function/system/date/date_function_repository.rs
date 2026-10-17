//! Port of kirun-js `function/system/date/DateFunctionRepository.ts` (the same functions), each
//! answering as kirun-java's: its `DateFunctionRepository` entries and the package-scanned
//! `Difference`, `FromNow`, `GetNames`, ... classes.

use std::sync::Arc;

use async_trait::async_trait;

use chrono::{Datelike, Timelike};

use crate::function::system::date::abstract_date_function::AbstractDateFunction as A;
use crate::function::system::date::java_format::zone_display_name;
use crate::function::system::date::java_time::{is_leap, java_error, month_length, SetField, ZonedDateTime};
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

/// `SetDay`, `SetMonth`, ...: `with(ChronoField, number)`.
fn setter(name: &str, field: SetField) -> (String, Arc<dyn Function>) {
    A::of_entry_timestamp_integer_and_timestamp_output(name, move |dt, n| dt.with(field, n))
}

fn compare(name: &str, f: fn(&ZonedDateTime, &ZonedDateTime) -> bool) -> (String, Arc<dyn Function>) {
    A::of_entry_timestamp_timestamp_and_t_output(name, bool_event(), move |t1, t2, _| Ok(Value::Bool(f(t1, t2))), vec![])
}

/// kirun-java `DateUnit`: the `Difference` units, largest first, with their fixed lengths.
const DATE_UNITS: [(&str, i64); 9] = [
    ("YEARS", 365 * 86_400_000),
    ("QUARTERS", 3 * 30 * 86_400_000),
    ("MONTHS", 30 * 86_400_000),
    ("WEEKS", 7 * 86_400_000),
    ("DAYS", 86_400_000),
    ("HOURS", 3_600_000),
    ("MINUTES", 60_000),
    ("SECONDS", 1000),
    ("MILLISECONDS", 1),
];

/// kirun-java `Difference`: the millisecond gap split over the units (largest first); units
/// larger than what is left are skipped, every unit but the last is whole (a `long`), the last
/// keeps the fraction (a `double`).
fn difference(t1: &ZonedDateTime, t2: &ZonedDateTime, units: &[Value]) -> crate::Result<Value> {
    let mut list: Vec<(&str, i64)> = Vec::new();
    for u in units {
        let name = u.as_str().unwrap_or_default().to_uppercase();
        let Some(unit) = DATE_UNITS.iter().find(|(n, _)| *n == name) else {
            return Err(java_error(format!(
                "No enum constant com.fincity.nocode.kirun.engine.function.system.date.DateUnit.{name}"
            )));
        };
        list.push(*unit);
    }
    // Sorted by `DateUnit` order, largest first (a stable sort, as Java's).
    list.sort_by_key(|(n, _)| DATE_UNITS.iter().position(|(m, _)| m == n));
    if list.is_empty() {
        list.push(("MILLISECONDS", 1));
    }

    // `Duration.between(t2, t1).toMillis()`.
    let nanos = (t1.epoch_second() as i128 - t2.epoch_second() as i128) * 1_000_000_000
        + (t1.nano() as i128 - t2.nano() as i128);
    let mut millis = nanos.div_euclid(1_000_000) as i64;
    let factor = if millis < 0 { -1 } else { 1 };
    millis *= factor;

    let mut out = serde_json::Map::new();
    for (i, (name, len)) in list.iter().enumerate() {
        if millis < *len {
            continue;
        }
        let value = millis as f64 / *len as f64;
        let key = name.to_lowercase();
        if i + 1 != list.len() {
            let whole = value.floor() as i64;
            out.insert(key, Value::from(whole * factor));
            millis -= whole * len;
        } else {
            out.insert(key, Value::from(value * factor as f64));
        }
    }
    Ok(Value::Object(out))
}

impl DateFunctionRepository {
    pub fn new() -> Self {
        let entries: Vec<(String, Arc<dyn Function>)> = vec![
            entry("EpochSecondsToTimestamp", EpochToTimestamp::new("EpochSecondsToTimestamp", true)),
            entry("EpochMillisecondsToTimestamp", EpochToTimestamp::new("EpochMillisecondsToTimestamp", false)),
            A::of_entry_timestamp_and_integer_output("GetDay", |d| d.local.day() as i64),
            A::of_entry_timestamp_and_integer_output("GetDayOfWeek", |d| d.local.weekday().number_from_monday() as i64),
            A::of_entry_timestamp_and_integer_output("GetMonth", |d| d.local.month() as i64),
            A::of_entry_timestamp_and_integer_output("GetYear", |d| d.local.year() as i64),
            A::of_entry_timestamp_and_integer_output("GetHours", |d| d.local.hour() as i64),
            A::of_entry_timestamp_and_integer_output("GetMinutes", |d| d.local.minute() as i64),
            A::of_entry_timestamp_and_integer_output("GetSeconds", |d| d.local.second() as i64),
            A::of_entry_timestamp_and_integer_output("GetMilliseconds", |d| (d.nano() / 1_000_000) as i64),
            A::of_entry_timestamp_and_integer_output("GetDaysInMonth", |d| {
                month_length(d.local.year(), d.local.month()) as i64
            }),
            A::of_entry_timestamp_and_integer_output("GetDaysInYear", |d| if is_leap(d.local.year()) { 366 } else { 365 }),
            entry("TimestampToEpochSeconds", TimestampToEpoch::new("TimestampToEpochSeconds", true)),
            entry("TimestampToEpochMilliseconds", TimestampToEpoch::new("TimestampToEpochMilliseconds", false)),
            A::of_entry_timestamp_and_string_output("GetTimeZoneName", |d| zone_display_name(&d.zone, false)),
            A::of_entry_timestamp_and_string_output("GetTimeZoneOffsetLong", |d| zone_display_name(&d.zone, false)),
            A::of_entry_timestamp_and_string_output("GetTimeZoneOffsetShort", |d| zone_display_name(&d.zone, true)),
            // Java `getOffset().get(OFFSET_SECONDS) / 60`: integer division towards zero.
            A::of_entry_timestamp_and_integer_output("GetTimeZoneOffset", |d| (d.offset / 60) as i64),
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
                    let units = extra.first().and_then(Value::as_array).cloned().unwrap_or_default();
                    difference(t1, t2, &units)
                },
                vec![A::parameter_variable_unit()],
            ),
            setter("SetDay", SetField::DayOfMonth),
            setter("SetMonth", SetField::MonthOfYear),
            setter("SetYear", SetField::Year),
            setter("SetHours", SetField::HourOfDay),
            setter("SetMinutes", SetField::MinuteOfHour),
            setter("SetSeconds", SetField::SecondOfMinute),
            setter("SetMilliseconds", SetField::MilliOfSecond),
            entry("SetTimeZone", SetTimeZone::new()),
            compare("IsBefore", |a, b| a.is_before(b)),
            compare("IsAfter", |a, b| a.is_after(b)),
            // Same instant (Java ZonedDateTime.isEqual).
            compare("IsSame", |a, b| a.is_equal(b)),
            compare("IsSameOrBefore", |a, b| a.is_before(b) || a.is_equal(b)),
            compare("IsSameOrAfter", |a, b| a.is_after(b) || a.is_equal(b)),
            A::of_entry_timestamp_and_boolean_output("IsInLeapYear", |d| is_leap(d.local.year())),
            A::of_entry_timestamp_and_boolean_output("IsInDST", |d| d.is_daylight_savings()),
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
