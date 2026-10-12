//! Port of kirun-js `__tests__/engine/function/system/date/TimestampToEpochTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'`.

use kirun::function::system::date::timestamp_to_epoch::TimestampToEpoch;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

#[tokio::test]
async fn should_return_the_epoch_time_in_seconds() {
    set_default_zone("Asia/Kolkata");
    let fep = fep().set_arguments(args! { "isoTimeStamp" => "2024-01-01T00:00:00.000+05:30" });

    let result = first_value(&TimestampToEpoch::new("TimestampToEpoch", true), fep, "result").await.unwrap();

    assert_json_eq!(result, json!(1704047400_i64));
}

#[tokio::test]
async fn should_return_the_epoch_time_in_milliseconds() {
    set_default_zone("Asia/Kolkata");
    let fep = fep().set_arguments(args! { "isoTimeStamp" => "2024-01-01T00:00:00.000+05:30" });

    let result = first_value(&TimestampToEpoch::new("TimestampToEpoch", false), fep, "result").await.unwrap();

    assert_json_eq!(result, json!(1704047400000_i64));
}
