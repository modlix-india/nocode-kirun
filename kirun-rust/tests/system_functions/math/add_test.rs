//! Port of kirun-js `__tests__/engine/function/system/math/AddTest.ts`.

use kirun::function::system::math::Add;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn add_test_1() {
    let add = Add::new();
    let fep = fep().set_arguments(args! { "value" => json!([1, 2, 3, 4, 5, 6, 5.5]) });

    assert_json_eq!(first_value(&add, fep, "value").await.unwrap(), json!(26.5));
}
