//! Port of kirun-js `__tests__/engine/function/system/date/SetTimeZoneTest.ts`.
//! The JS file sets no default zone; the results depend only on the requested zone.

use kirun::function::system::date::set_time_zone::SetTimeZone;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn should_set_the_time_zone() {
    let set_time_zone = SetTimeZone::new();
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => "2024-01-01T00:00:00.000Z",
        "timezone" => "Asia/Tokyo",
    });

    assert_json_eq!(
        first_value(&set_time_zone, fep, "isoTimeStamp").await.unwrap(),
        json!("2024-01-01T09:00:00.000+09:00")
    );
}

#[tokio::test]
async fn shoud_set_the_time_zone_with_offset() {
    let set_time_zone = SetTimeZone::new();
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => "2024-01-01T00:00:00.000Z",
        "timezone" => "UTC+05:30",
    });

    assert_json_eq!(
        first_value(&set_time_zone, fep, "isoTimeStamp").await.unwrap(),
        json!("2024-01-01T05:30:00.000+05:30")
    );
}
