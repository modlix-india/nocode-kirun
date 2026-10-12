//! Port of kirun-js `__tests__/engine/function/system/date/IsBetweenTest.ts`.
//! JS: `Settings.defaultZone = 'UTC+05:30'`.

use kirun::function::system::date::is_between::IsBetween;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

#[tokio::test]
async fn should_return_true_if_the_check_timestamp_is_between_the_start_and_end_timestamps() {
    set_default_zone("UTC+05:30");
    let is_between = IsBetween::new();
    let fep = fep().set_arguments(args! {
        "startTimestamp" => "2024-01-01T00:00:00.000Z",
        "endTimestamp" => "2024-01-02T00:00:00.000Z",
        "checkTimestamp" => "2024-01-01T12:00:00.000Z",
    });

    assert_json_eq!(first_value(&is_between, fep, "result").await.unwrap(), json!(true));
}

#[tokio::test]
async fn should_return_false_if_the_check_timestamp_is_not_between_the_start_and_end_timestamps() {
    set_default_zone("UTC+05:30");
    let is_between = IsBetween::new();
    let fep = fep().set_arguments(args! {
        "startTimestamp" => "2024-01-01T00:00:00.000Z",
        "endTimestamp" => "2024-01-02T00:00:00.000Z",
        "checkTimestamp" => "2024-01-03T00:00:00.000Z",
    });

    assert_json_eq!(first_value(&is_between, fep, "result").await.unwrap(), json!(false));
}
