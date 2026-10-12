//! Port of kirun-js `__tests__/engine/function/system/date/TimeAsTest.ts`.
//! JS: `Settings.defaultZone = 'UTC+05:30'` (the result keeps the input's -05:00 offset).

use kirun::function::system::date::time_as::TimeAs;
use serde_json::json;

use super::set_default_zone;
use crate::common::*;

#[tokio::test]
async fn should_return_the_time_as_an_array() {
    set_default_zone("UTC+05:30");
    let time_as_array = TimeAs::new(true);
    let _time_as_object = TimeAs::new(false); // constructed in JS too, unused there.
    let fep = fep().set_arguments(args! { "isoTimeStamp" => "2024-11-10T10:10:10.100-05:00" });

    assert_json_eq!(
        first_value(&time_as_array, fep, "array").await.unwrap(),
        json!([2024, 11, 10, 10, 10, 10, 100])
    );
}
