//! Port of kirun-js `__tests__/engine/function/system/date/EpochToTimestampTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'`. kirun-js prints UTC `Z` strings; kirun-java (which
//! stored server functions run on) places the instant in the system zone, so the expectations
//! are the same instants at +05:30, and a string epoch must be a plain `long` (Gson `getAsLong`).

use kirun::function::system::date::date_function_repository::DateFunctionRepository;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::{Map, Value};
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

async fn run(name: &str, args: Map<Value>) -> kirun::Result<Value> {
    set_default_zone("Asia/Kolkata");
    let fun = DateFunctionRepository::new().find(Namespaces::DATE, name).await.unwrap();
    first_value(fun.as_ref(), fep().set_arguments(args), "isoTimeStamp").await
}

#[tokio::test]
async fn epoch_to_date() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => 1694072117_i64 }).await.unwrap(),
        json!("2023-09-07T13:05:17.000+05:30")
    );
}

#[tokio::test]
async fn epoch_to_date_2() {
    assert_json_eq!(
        run("EpochMillisecondsToTimestamp", args! { "epochMilliseconds" => 1694072117000_i64 })
            .await
            .unwrap(),
        json!("2023-09-07T13:05:17.000+05:30")
    );
}

#[tokio::test]
async fn epoch_to_date_3() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => 169407211700_i64 }).await.unwrap(),
        json!("7338-04-20T20:18:20.000+05:30")
    );
}

#[tokio::test]
async fn epoch_string_to_date_1() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => "1696489387" }).await.unwrap(),
        json!("2023-10-05T12:33:07.000+05:30")
    );
}

#[tokio::test]
async fn epoch_string_to_date_2() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => "1696489387" }).await.unwrap(),
        json!("2023-10-05T12:33:07.000+05:30")
    );
}

#[tokio::test]
async fn epoch_string_to_date_3() {
    assert_json_eq!(
        run("EpochMillisecondsToTimestamp", args! { "epochMilliseconds" => "1696489386" })
            .await
            .unwrap(),
        json!("1970-01-20T20:44:49.386+05:30")
    );
}

#[tokio::test]
async fn epoch_string_to_date_4_all_chars() {
    // kirun-js meant "Please provide a valid value for epochSeconds." (an unawaited jest check);
    // kirun-java fails in `Long.parseLong`, and that message is what core answers.
    assert_err_contains(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => "abcdef" }).await,
        "For input string: \"abcdef\"",
    );
}

#[tokio::test]
async fn epoch_test_for_large_string_function() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => "1696494131" }).await.unwrap(),
        json!("2023-10-05T13:52:11.000+05:30")
    );
}

#[tokio::test]
async fn epoch_test_for_small_string_function() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => "169640" }).await.unwrap(),
        json!("1970-01-03T04:37:20.000+05:30")
    );
}
