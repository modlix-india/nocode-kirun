//! Port of kirun-js `__tests__/engine/function/system/IfTest.ts`.

use kirun::function::system::r#if::If;
use kirun::function::Function;
use kirun::Value;
use serde_json::json;

use crate::common::*;

async fn event_name(if_function: &If, condition: Value) -> String {
    let mut fep = fep().set_arguments(args! { "condition" => condition });
    let mut fo = if_function.execute(&mut fep).await.unwrap();
    fo.next().unwrap().get_name().to_string()
}

#[tokio::test]
async fn if_test() {
    let if_function = If::new();

    assert_eq!(event_name(&if_function, json!(true)).await, "true");

    assert_eq!(event_name(&if_function, json!(false)).await, "false");

    // JS passes `undefined` here; in Rust undefined and null are both Value::Null.
    assert_eq!(event_name(&if_function, Value::Null).await, "false");

    assert_eq!(event_name(&if_function, Value::Null).await, "false");

    // empty string
    assert_eq!(event_name(&if_function, json!("")).await, "true");

    // space
    assert_eq!(event_name(&if_function, json!(" ")).await, "true");

    assert_eq!(event_name(&if_function, json!("abc")).await, "true");

    assert_eq!(event_name(&if_function, json!(0)).await, "false");

    assert_eq!(event_name(&if_function, json!(1)).await, "true");

    assert_eq!(event_name(&if_function, json!(-1)).await, "true");

    assert_eq!(event_name(&if_function, json!({})).await, "true");

    assert_eq!(event_name(&if_function, json!([])).await, "true");
}
