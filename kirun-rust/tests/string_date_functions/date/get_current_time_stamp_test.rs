//! Port of kirun-js `__tests__/engine/function/system/date/GetCurrentTimeStampTest.ts`.
//! JS: `Settings.defaultZone = 'Asia/Kolkata'`.

use kirun::function::system::date::date_function_repository::DateFunctionRepository;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;

use super::set_default_zone;
use crate::common::*;

#[tokio::test]
async fn should_return_the_current_timestamp() {
    set_default_zone("Asia/Kolkata");
    let fun = DateFunctionRepository::new()
        .find(Namespaces::DATE, "GetCurrentTimestamp")
        .await
        .unwrap();
    let fep = fep().set_arguments(args! {});

    let result = first_value(fun.as_ref(), fep, "isoTimeStamp").await.unwrap();

    // JS `toBeDefined()`; undefined and null are both Value::Null in Rust.
    assert!(!result.is_null());
}
