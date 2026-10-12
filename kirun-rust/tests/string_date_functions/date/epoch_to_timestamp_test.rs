//! Port of kirun-js `__tests__/engine/function/system/date/EpochToTimestampTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'` (outputs are UTC `Z` strings regardless).

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
        json!("2023-09-07T07:35:17.000Z")
    );
}

#[tokio::test]
async fn epoch_to_date_2() {
    assert_json_eq!(
        run("EpochMillisecondsToTimestamp", args! { "epochMilliseconds" => 1694072117000_i64 })
            .await
            .unwrap(),
        json!("2023-09-07T07:35:17.000Z")
    );
}

#[tokio::test]
async fn epoch_to_date_3() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => 169407211700_i64 }).await.unwrap(),
        json!("7338-04-20T14:48:20.000Z")
    );
}

#[tokio::test]
async fn epoch_string_to_date_1() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => "1696489387" }).await.unwrap(),
        json!("2023-10-05T07:03:07.000Z")
    );
}

#[tokio::test]
async fn epoch_string_to_date_2() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => "1696489387" }).await.unwrap(),
        json!("2023-10-05T07:03:07.000Z")
    );
}

#[tokio::test]
async fn epoch_string_to_date_3() {
    assert_json_eq!(
        run("EpochMillisecondsToTimestamp", args! { "epochMilliseconds" => "1696489386" })
            .await
            .unwrap(),
        json!("1970-01-20T15:14:49.386Z")
    );
}

#[tokio::test]
async fn epoch_string_to_date_4_all_chars() {
    // JS does not await this `rejects` expectation, so jest never actually checked it;
    // the intent (an error with this message) is asserted here.
    assert_err_contains(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => "abcdef" }).await,
        "Please provide a valid value for epochSeconds.",
    );
}

#[tokio::test]
async fn epoch_test_for_large_string_function() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => "1696494131" }).await.unwrap(),
        json!("2023-10-05T08:22:11.000Z")
    );
}

#[tokio::test]
async fn epoch_test_for_small_string_function() {
    assert_json_eq!(
        run("EpochSecondsToTimestamp", args! { "epochSeconds" => "169640" }).await.unwrap(),
        json!("1970-01-02T23:07:20.000Z")
    );
}
