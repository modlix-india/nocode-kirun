//! Port of kirun-js `__tests__/engine/function/system/math/MinimumTest.ts`.

use kirun::function::system::math::Minimum;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn minimum_test_1() {
    let min = Minimum::new();
    let fep = fep().set_arguments(args! { "value" => json!([3, 2, 3, 5, 3]) });

    assert_json_eq!(first_value(&min, fep, "value").await.unwrap(), json!(2));
}

#[tokio::test]
async fn minimum_test_2() {
    let min = Minimum::new();
    let fep = fep().set_arguments(args! { "value" => json!(["3", 2, 3, 5, 3]) });

    assert_err_contains(
        first_value(&min, fep, "value").await,
        "Error while executing the function System.Math.Minimum's parameter value [Expected: Integer | Long | Float | Double]: Value \"3\" is not of valid type(s)\nExpected a Double but found \"3\"\nExpected a Float but found \"3\"\nExpected a Long but found \"3\"\nExpected a Integer but found \"3\"",
    );
}

#[tokio::test]
async fn minimum_test_3() {
    let min = Minimum::new();
    let fep = fep().set_arguments(args! { "value" => json!([-1, -2, -3, -5, -3]) });

    assert_json_eq!(first_value(&min, fep, "value").await.unwrap(), json!(-5));
}
