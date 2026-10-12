//! Port of kirun-js `__tests__/engine/function/system/array/MinTest.ts`.

use kirun::function::system::array::min::Min;
use serde_json::{json, Value};

use crate::common::*;

async fn run(source: Value) -> kirun::Result<Value> {
    first_value(&Min::new(), fep().set_arguments(args! { "source" => source }), "result").await
}

#[tokio::test]
async fn min_test_1() {
    assert_json_eq!(run(json!([null, 12])).await.unwrap(), json!(12));
}

#[tokio::test]
async fn min_test_2() {
    assert!(run(json!([])).await.is_err());
}

#[tokio::test]
async fn min_test_3() {
    assert_json_eq!(run(json!([12, 15, null, 98, 1])).await.unwrap(), json!(1));
}

#[tokio::test]
async fn min_test_4() {
    assert_json_eq!(run(json!(["nocode", "NoCode", "platform"])).await.unwrap(), json!("NoCode"));
}

#[tokio::test]
async fn min_test_5() {
    assert!(run(Value::Null).await.is_err());
}

#[tokio::test]
async fn min_test_6() {
    assert_json_eq!(run(json!([456, "nocode", "NoCode", "platform", 123, 1])).await.unwrap(), json!(1));
}

#[tokio::test]
async fn min_test_7() {
    assert_json_eq!(run(json!(["c", "r", "d", "s"])).await.unwrap(), json!("c"));
}

#[tokio::test]
async fn min_test_8() {
    assert_json_eq!(run(json!(["surendhar"])).await.unwrap(), json!("surendhar"));
}
