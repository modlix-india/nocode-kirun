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

    // kirun-java's wording (stored server functions run on it): each schema error carries its
    // path (`value - ...`) and a non-number reads `<value> is not a <Type>`. The `[Expected: ...]`
    // type list keeps kirun-rust's order (kirun-java's follows a per-run hash order).
    assert_err_contains(
        first_value(&max, fep, "value").await,
        "Error while executing the function System.Math.Maximum's parameter value [Expected: Integer | Long | Float | Double]: value - Value \"-1\" is not of valid type(s)\nvalue - \"-1\" is not a Double\nvalue - \"-1\" is not a Float\nvalue - \"-1\" is not a Long\nvalue - \"-1\" is not a Integer",
    );
}
