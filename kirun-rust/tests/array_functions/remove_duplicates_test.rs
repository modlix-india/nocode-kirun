//! Port of kirun-js `__tests__/engine/function/system/array/RemoveDuplicatesTest.ts`.

use kirun::function::system::array::remove_duplicates::RemoveDuplicates;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn remove_duplicates_test() {
    let remove_duplicates = RemoveDuplicates::new();

    let source = json!([2, 2, 2, 2, 2]);

    let fep1 = crate::common::fep().set_arguments(args! { "source" => source, "srcFrom" => 2, "length" => 4 });
    assert!(first_value(&remove_duplicates, fep1, "result").await.is_err());

    // source.push(6)
    let source = json!([2, 2, 2, 2, 2, 6]);
    let result = json!([2, 2, 2, 6]);

    let fep = crate::common::fep().set_arguments(args! { "source" => source, "srcFrom" => 2, "length" => 4 });
    assert_json_eq!(first_value(&remove_duplicates, fep, "result").await.unwrap(), result);

    let source = json!([{ "name": "Kiran" }, { "name": "Kiran" }, { "name": "Kiran" }, { "name": "Kumar" }]);
    let result = json!([{ "name": "Kiran" }, { "name": "Kumar" }]);

    let fep = crate::common::fep().set_arguments(args! { "source" => source });
    assert_json_eq!(first_value(&remove_duplicates, fep, "result").await.unwrap(), result);
}
