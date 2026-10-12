//! Port of kirun-js `__tests__/engine/function/system/date/IsValidISODateTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'`. Two JS tests share the name
//! `should return false for an invalid ISO date`; the second gets a `_2` suffix.

use kirun::function::system::date::is_valid_iso_date::IsValidISODate;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

async fn is_valid(ts: &str) -> kirun::Value {
    set_default_zone("Asia/Kolkata");
    let is_valid_iso_date = IsValidISODate::new();
    let fep = fep().set_arguments(args! { "isoTimeStamp" => ts });
    first_value(&is_valid_iso_date, fep, "result").await.unwrap()
}

#[tokio::test]
async fn should_return_true_for_a_valid_iso_date() {
    assert_json_eq!(is_valid("2024-01-01T00:00:00.000Z").await, json!(true));
}

#[tokio::test]
async fn should_return_false_for_an_invalid_iso_date() {
    assert_json_eq!(is_valid("invalid").await, json!(false));
}

#[tokio::test]
async fn should_return_false_for_an_invalid_iso_date_2() {
    assert_json_eq!(is_valid("2024-02-31").await, json!(false));
}

#[tokio::test]
async fn should_return_false_for_an_invalid_leap_year_iso_date() {
    assert_json_eq!(is_valid("2022-02-29T00:00:00.000Z").await, json!(false));
}
