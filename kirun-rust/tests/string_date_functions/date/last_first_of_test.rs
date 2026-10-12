//! Port of kirun-js `__tests__/engine/function/system/date/LastFirstOfTest.ts`.
//! JS: `Settings.defaultZone = 'UTC+05:30'`.

use kirun::function::system::date::last_first_of::LastFirstOf;
use kirun::function::Function;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

#[tokio::test]
async fn should_return_the_last_timestamp() {
    set_default_zone("UTC+05:30");
    let last_of = LastFirstOf::new(true);
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => json!([
            "2024-01-03T05:50:00.000+05:30",
            "2024-01-02T00:00:00.000Z",
            "2024-01-03T10:00:00.000Z"
        ]),
    });

    assert_json_eq!(
        first_value(&last_of, fep, "isoTimeStamp").await.unwrap(),
        json!("2024-01-03T10:00:00.000Z")
    );
}

#[tokio::test]
async fn should_return_the_first_timestamp() {
    set_default_zone("UTC+05:30");
    let first_of = LastFirstOf::new(false);
    let fep = fep().set_arguments(args! {
        "isoTimeStamp" => json!([
            "2024-01-03T05:50:00.000+05:30",
            "2024-01-02T00:00:00.000Z",
            "2024-01-03T10:00:00.000Z"
        ]),
    });

    assert_json_eq!(
        first_value(&first_of, fep, "isoTimeStamp").await.unwrap(),
        json!("2024-01-02T00:00:00.000Z")
    );
}

#[tokio::test]
async fn should_throw_an_error_if_no_timestamps_are_provided() {
    set_default_zone("UTC+05:30");
    let last_of = LastFirstOf::new(true);
    let first_of = LastFirstOf::new(false);
    let fep = fep().set_arguments(args! { "isoTimeStamp" => json!([]) });

    assert_err_contains(last_of.execute(&mut fep.clone()).await, "No timestamps provided");
    assert_err_contains(first_of.execute(&mut fep.clone()).await, "No timestamps provided");
}
