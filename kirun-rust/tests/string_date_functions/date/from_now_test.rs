//! Port of kirun-js `__tests__/engine/function/system/date/FromNowTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'`. kirun-js prints luxon's `toRelative`; kirun-java
//! (which stored server functions run on) calls `DateUtil.toRelative(base, timestamp)`, base
//! first, so a timestamp after the base reads "... ago", always in English long units (it ignores
//! `locale`, and reads `format` with its JSON quotes so `SHORT` never applies).

use kirun::function::system::date::from_now::FromNow;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

#[tokio::test]
async fn should_return_the_relative_date() {
    set_default_zone("Asia/Kolkata");
    let fep = fep().set_arguments(args! { "isoTimeStamp" => "2025-01-01", "base" => "2024-04-25" });

    let result = first_value(&FromNow::new(), fep, "result").await.unwrap();

    assert_json_eq!(result, json!("8 months ago"));
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

    assert_json_eq!(ret_value, json!("20 months ago"));
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

    assert_json_eq!(result, json!("617 days ago"));
}
