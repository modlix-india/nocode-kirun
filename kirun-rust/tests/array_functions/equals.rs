//! Port of kirun-js `__tests__/engine/function/system/array/Equals.ts`.

use kirun::function::system::array::equals::Equals;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn equals_test() {
    let equals = Equals::new();

    let src_array = json!([30, 31, 32, 33, 34]);
    let mut find_array = json!([30, 31, 32, 33, 34]);

    let fep = crate::common::fep().set_arguments(args! { "source" => src_array.clone(), "find" => find_array.clone() });
    assert_json_eq!(first_value(&equals, fep, "result").await.unwrap(), json!(true));

    find_array[1] = json!(41);

    // JS re-runs with the mutated find array but writes `.toBeFalsy` without calling it,
    // so it asserts nothing. Kept as a run with no assertion.
    let fep = crate::common::fep().set_arguments(args! { "source" => src_array.clone(), "find" => find_array.clone() });
    let _ = first_value(&equals, fep, "result").await.unwrap();

    let fep = crate::common::fep().set_arguments(args! {
        "source" => src_array.clone(),
        "find" => find_array.clone(),
        "srcFrom" => 2,
        "findFrom" => 2,
    });
    assert_json_eq!(first_value(&equals, fep, "result").await.unwrap(), json!(true));

    let src_array = json!([true, true, false]);
    let find_array = json!([true, true, false]);

    let fep = crate::common::fep().set_arguments(args! { "source" => src_array, "find" => find_array });
    assert_json_eq!(first_value(&equals, fep, "result").await.unwrap(), json!(true));
}
