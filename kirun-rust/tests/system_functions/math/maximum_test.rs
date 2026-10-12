//! Port of kirun-js `__tests__/engine/function/system/math/MaximumTest.ts`
//! (the JS tests are named "Minimum Test n" although they exercise Maximum).

use kirun::function::system::math::Maximum;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn minimum_test_1() {
    let max = Maximum::new();
    let fep = fep().set_arguments(args! { "value" => json!([3, 2, 3, 5, 3]) });

    assert_json_eq!(first_value(&max, fep, "value").await.unwrap(), json!(5));
}

#[tokio::test]
async fn minimum_test_2() {
    let max = Maximum::new();
    let fep = fep().set_arguments(args! { "value" => json!([-1, -1, 0, -1, -2]) });

    assert_json_eq!(first_value(&max, fep, "value").await.unwrap(), json!(0));
}

#[tokio::test]
async fn minimum_test_3() {
    let max = Maximum::new();
    let fep = fep().set_arguments(args! { "value" => json!(["-1", -1, 0, -1, -2]) });

    assert_err_contains(
        first_value(&max, fep, "value").await,
        "Error while executing the function System.Math.Maximum's parameter value [Expected: Integer | Long | Float | Double]: Value \"-1\" is not of valid type(s)\nExpected a Double but found \"-1\"\nExpected a Float but found \"-1\"\nExpected a Long but found \"-1\"\nExpected a Integer but found \"-1\"",
    );
}
