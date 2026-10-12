//! Port of kirun-js `__tests__/engine/function/system/date/ToDateStringTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'`. Format tokens are luxon's (`DDD` = localized
//! full date via Intl).

use kirun::function::system::date::to_date_string::ToDateString;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

#[tokio::test]
async fn should_return_the_date_string_in_the_given_format() {
    set_default_zone("Asia/Kolkata");
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => "2024-01-01T00:00:00.000+05:30",
        "format" => "yyyy-MM-dd",
    });

    let result = first_value(&ToDateString::new(), fep, "result").await.unwrap();

    assert_json_eq!(result, json!("2024-01-01"));
}

#[tokio::test]
async fn should_return_the_date_string_in_the_given_format_with_locale() {
    set_default_zone("Asia/Kolkata");
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => "2024-01-01T00:00:00.000+05:30",
        "format" => "DDD",
        "locale" => "fr-CA",
    });

    let result = first_value(&ToDateString::new(), fep, "result").await.unwrap();

    assert_json_eq!(result, json!("1 janvier 2024"));
}

#[tokio::test]
async fn should_return_the_date_string_in_the_given_format_and_no_locale() {
    set_default_zone("Asia/Kolkata");
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => "2024-01-01T00:00:00.000+05:30",
        "format" => "DDD",
    });

    let result = first_value(&ToDateString::new(), fep, "result").await.unwrap();

    assert!(result == json!("1 January 2024") || result == json!("January 1, 2024"), "got {result}");
}
