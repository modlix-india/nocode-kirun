//! Port of kirun-js `__tests__/engine/function/system/date/FromDateStringTest.ts`, answering as
//! kirun-java's `FromDateString` (stored server functions run on it): the result is under
//! `isoTimeStamp` (kirun-js emits `result`), and fields the format leaves out default to today's
//! year, month and day and a zero time (kirun-js defaults the day to 1).
//! The JS file sets no `Settings.defaultZone`; its `+05:30` expectations assume the process zone
//! was Asia/Kolkata, so the Rust tests set that zone explicitly.

use chrono::{DateTime, Datelike, Duration, TimeZone, Utc};
use kirun::function::system::date::from_date_string::FromDateString;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

#[tokio::test]
async fn should_return_the_timestamp() {
    set_default_zone("Asia/Kolkata");
    let from_date_string = FromDateString::new();
    let fep = fep().set_arguments(args! { "timestampString" => "2024-01-01", "format" => "yyyy-MM-dd" });

    let result = first_value(&from_date_string, fep, "isoTimeStamp").await.unwrap();

    assert_json_eq!(result, json!("2024-01-01T00:00:00.000+05:30"));
}

#[tokio::test]
async fn should_return_the_timestamp_with_the_format() {
    set_default_zone("Asia/Kolkata");
    let from_date_string = FromDateString::new();
    let fep = fep().set_arguments(args! { "timestampString" => "2024-05", "format" => "yyyy-MM" });

    let result = first_value(&from_date_string, fep, "isoTimeStamp").await.unwrap();

    // kirun-java defaults the missing day to today's (kirun-js: the 1st).
    let today = Utc::now().with_timezone(&chrono_tz::Asia::Kolkata).day();
    assert_json_eq!(result, json!(format!("2024-05-{today:02}T00:00:00.000+05:30")));
}

#[tokio::test]
async fn should_return_the_timestamp_with_the_only_few_fields() {
    set_default_zone("Asia/Kolkata");
    let from_date_string = FromDateString::new();
    let fep = fep().set_arguments(args! { "timestampString" => "03 12 123", "format" => "dd ss SSS" });

    let result = first_value(&from_date_string, fep, "isoTimeStamp").await.unwrap();

    // JS: d = new Date(); d.setDate(3); d.setHours(0); d.setMinutes(0); d.setSeconds(12);
    // d.setMilliseconds(123) — current year and month, local (IST) time. kirun-java gives the
    // same instant.
    let tz = chrono_tz::Asia::Kolkata;
    let now = Utc::now().with_timezone(&tz);
    let d = tz.with_ymd_and_hms(now.year(), now.month(), 3, 0, 0, 12).unwrap() + Duration::milliseconds(123);

    let actual = DateTime::parse_from_rfc3339(result.as_str().expect("result is a string")).unwrap();
    assert_eq!(actual.timestamp_millis(), d.timestamp_millis());
}
