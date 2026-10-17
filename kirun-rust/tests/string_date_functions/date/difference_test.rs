//! Port of kirun-js `__tests__/engine/function/system/date/DifferenceTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'`. `toMatchObject` → `assert_match_object`.

use kirun::function::system::date::date_function_repository::DateFunctionRepository;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::{Map, Value};
use serde_json::json;

use super::{assert_match_object, set_default_zone};
use crate::common::*;

async fn difference(args: Map<Value>) -> Value {
    set_default_zone("Asia/Kolkata");
    let fun = DateFunctionRepository::new().find(Namespaces::DATE, "Difference").await.unwrap();
    first_value(fun.as_ref(), fep().set_arguments(args), "result").await.unwrap()
}

#[tokio::test]
async fn should_return_the_difference_in_default() {
    let result = difference(args! {
        "isoTimeStamp1" => "2025-01-01",
        "isoTimeStamp2" => "2024-04-25",
    })
    .await;
    assert_match_object(&result, &json!({ "milliseconds": 21686400000_i64 }));
}

#[tokio::test]
async fn should_return_the_difference_in_days() {
    let result = difference(args! {
        "isoTimeStamp1" => "2025-01-01T10:20:30+05:30",
        "isoTimeStamp2" => "2024-04-25T10:20:30-05:00",
        "unit" => json!(["DAYS"]),
    })
    .await;
    assert_match_object(&result, &json!({ "days": 250.5625 }));
}

#[tokio::test]
async fn should_return_the_difference_in_months_days_hours_minutes() {
    let result = difference(args! {
        "isoTimeStamp1" => "2025-01-01T10:20:30+05:30",
        "isoTimeStamp2" => "2024-04-25T10:20:30-05:00",
        "unit" => json!(["MONTHS", "DAYS", "HOURS", "MINUTES"]),
    })
    .await;
    assert_match_object(&result, &json!({ "months": 8, "days": 10, "hours": 13, "minutes": 30 }));
}

#[tokio::test]
async fn should_return_the_difference_in_negative_months_days_hours_minutes() {
    let result = difference(args! {
        "isoTimeStamp1" => "2024-01-01T10:20:30+05:30",
        "isoTimeStamp2" => "2025-04-25T10:20:30-05:00",
        "unit" => json!(["MONTHS", "DAYS", "HOURS", "MINUTES"]),
    })
    .await;
    // kirun-java splits the millisecond gap over fixed-length units (a month is 30 days) and
    // skips a unit larger than what is left, so there is no `days` key; the last unit is a
    // double. kirun-js (luxon) answered { months: -16, days: 0, hours: -10, minutes: -30 }.
    assert_json_eq!(result, json!({ "months": -16, "hours": -10, "minutes": -30.0 }));
}

#[tokio::test]
async fn should_return_the_difference_in_fractions() {
    let result = difference(args! {
        "isoTimeStamp1" => "2024-01-01T10:20:30+05:30",
        "isoTimeStamp2" => "2025-04-25T10:20:30-05:00",
        "unit" => json!(["DAYS"]),
    })
    .await;
    assert_match_object(&result, &json!({ "days": -480.4375 }));
}

#[tokio::test]
async fn should_return_the_difference_in_multiple_fractions() {
    let result = difference(args! {
        "isoTimeStamp1" => "2024-01-01T10:20:30+05:30",
        "isoTimeStamp2" => "2025-04-25T09:17:23-05:00",
        "unit" => json!(["DAYS", "HOURS"]),
    })
    .await;
    // kirun-java's float (the remaining milliseconds over an hour); luxon's remainder gave
    // -9.448055555554674.
    assert_match_object(&result, &json!({ "days": -480, "hours": -9.448055555555555 }));
}
