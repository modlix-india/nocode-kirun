//! Port of kirun-js `__tests__/engine/function/system/date/FromNowTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'`. Strings come from luxon `toRelative`, which
//! uses `Intl.RelativeTimeFormat` (en and fr locale data).

use kirun::function::system::date::from_now::FromNow;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

#[tokio::test]
async fn should_return_the_relative_date() {
    set_default_zone("Asia/Kolkata");
    let fep = fep().set_arguments(args! { "isoTimeStamp" => "2025-01-01", "base" => "2024-04-25" });

    let result = first_value(&FromNow::new(), fep, "result").await.unwrap();

    assert_json_eq!(result, json!("in 8 months"));
}

#[tokio::test]
async fn should_return_the_relative_date_with_units() {
    set_default_zone("Asia/Kolkata");
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => "2025-01-01",
        "base" => "2023-04-25",
        "unit" => json!(["MONTHS", "DAYS"]),
        "format" => "SHORT",
    });

    let ret_value = first_value(&FromNow::new(), fep, "result").await.unwrap();

    assert!(ret_value == json!("in 20 mo") || ret_value == json!("in 20 mo."), "got {ret_value}");
}

#[tokio::test]
async fn should_return_the_relative_date_with_locale() {
    set_default_zone("Asia/Kolkata");
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => "2025-01-01",
        "base" => "2023-04-25",
        "unit" => json!(["DAYS"]),
        "format" => "LONG",
        "locale" => "fr",
    });

    let result = first_value(&FromNow::new(), fep, "result").await.unwrap();

    assert_json_eq!(result, json!("dans 617 jours"));
}
