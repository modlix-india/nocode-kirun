//! Port of kirun-js `__tests__/engine/function/system/date/StartEndOfTest.ts`.
//! JS: `Settings.defaultZone = 'UTC+05:30'` (results keep the input's own offset).

use kirun::function::system::date::start_end_of::StartEndOf;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

async fn run(is_start: bool, ts: &str, unit: &str) -> kirun::Value {
    set_default_zone("UTC+05:30");
    let f = StartEndOf::new(is_start);
    let fep = fep().set_arguments(args! { "isoTimeStamp" => ts, "unit" => unit });
    first_value(&f, fep, "isoTimeStamp").await.unwrap()
}

#[tokio::test]
async fn should_return_the_start_of_the_year() {
    assert_json_eq!(run(true, "2023-12-31T22:00:00.000Z", "YEARS").await, json!("2023-01-01T00:00:00.000Z"));
}

#[tokio::test]
async fn should_return_the_end_of_the_year() {
    assert_json_eq!(run(false, "2023-12-31T22:00:00.000Z", "YEARS").await, json!("2023-12-31T23:59:59.999Z"));
}

#[tokio::test]
async fn should_return_the_end_of_day() {
    assert_json_eq!(run(false, "2023-12-31T22:00:00.000Z", "DAYS").await, json!("2023-12-31T23:59:59.999Z"));
}

#[tokio::test]
async fn should_return_the_end_of_day_in_a_different_timezone() {
    assert_json_eq!(
        run(false, "2024-12-31T22:00:00.000-06:00", "DAYS").await,
        json!("2024-12-31T23:59:59.999-06:00")
    );
}
