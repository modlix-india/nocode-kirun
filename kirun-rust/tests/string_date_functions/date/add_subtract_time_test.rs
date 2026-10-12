//! Port of kirun-js `__tests__/engine/function/system/date/AddSubtractTimeTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'`.

use kirun::function::system::date::add_subtract_time::AddSubtractTime;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

#[tokio::test]
async fn should_add_time_with_no_values() {
    set_default_zone("Asia/Kolkata");
    let add_function = AddSubtractTime::new(true);
    let fep = fep().set_arguments(args! { "isoTimeStamp" => "2025-01-01T10:20:35+05:30" });

    assert_json_eq!(
        first_value(&add_function, fep, "isoTimeStamp").await.unwrap(),
        json!("2025-01-01T10:20:35.000+05:30")
    );
}

#[tokio::test]
async fn shoudl_subtract_time_with_no_values() {
    set_default_zone("Asia/Kolkata");
    let subtract_function = AddSubtractTime::new(false);
    let fep = fep().set_arguments(args! { "isoTimeStamp" => "2025-01-01T10:20:35+05:30" });

    assert_json_eq!(
        first_value(&subtract_function, fep, "isoTimeStamp").await.unwrap(),
        json!("2025-01-01T10:20:35.000+05:30")
    );
}

#[tokio::test]
async fn should_add_time_with_all_values() {
    set_default_zone("Asia/Kolkata");
    let add_function = AddSubtractTime::new(true);
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => "2025-01-01T10:20:35+05:30",
        "years" => 1,
        "months" => 1,
        "days" => 1,
        "hours" => 1,
        "minutes" => 1,
        "seconds" => 1,
    });

    assert_json_eq!(
        first_value(&add_function, fep, "isoTimeStamp").await.unwrap(),
        json!("2026-02-02T11:21:36.000+05:30")
    );
}

#[tokio::test]
async fn should_subtract_time_with_all_values() {
    set_default_zone("Asia/Kolkata");
    let subtract_function = AddSubtractTime::new(false);
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => "2025-01-01T10:20:35+05:30",
        "years" => 1,
        "months" => 1,
        "days" => 1,
        "hours" => 1,
        "minutes" => 1,
        "seconds" => 1,
        "milliseconds" => 8,
    });

    assert_json_eq!(
        first_value(&subtract_function, fep, "isoTimeStamp").await.unwrap(),
        json!("2023-11-30T09:19:33.992+05:30")
    );
}
