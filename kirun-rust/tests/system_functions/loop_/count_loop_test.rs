//! Port of kirun-js `__tests__/engine/function/system/loop/CountLoopTest.ts`.

use kirun::function::system::r#loop::CountLoop;
use kirun::function::Function;
use kirun::model::{EventResult, FunctionOutput};
use kirun::runtime::FunctionExecutionParameters;
use kirun::{Map, Value};
use serde_json::json;

use crate::common::*;

fn test_fep(args: Map<Value>) -> FunctionExecutionParameters {
    FunctionExecutionParameters::with_execution_id(function_repo(), schema_repo(), "Test").set_arguments(args)
}

/// JS `while ((er = loop1.next())?.getName() !== 'output') iterations.push(er?.getResult().get('index'))`.
/// The JS loop would spin forever if the generator ended without `output`; here that panics.
fn drain(loop1: &mut FunctionOutput) -> (Vec<Value>, EventResult) {
    let mut iterations = Vec::new();
    loop {
        let er = loop1.next().expect("generator ended before the output event");
        if er.get_name() == "output" {
            return (iterations, er);
        }
        iterations.push(er.get_result().get("index").cloned().unwrap_or(Value::Null));
    }
}

#[tokio::test]
async fn count_loop1() {
    let mut loop1 = CountLoop::new().execute(&mut test_fep(args! { "count" => 10 })).await.unwrap();

    let (iterations, er) = drain(&mut loop1);

    assert_json_eq!(Value::Array(iterations), json!([0, 1, 2, 3, 4, 5, 6, 7, 8, 9]));
    assert_eq!(er.get_name(), "output");
    assert_json_eq!(er.get_result().get("value").cloned().unwrap_or(Value::Null), json!(10));
}

#[tokio::test]
async fn count_loop2() {
    let mut loop1 = CountLoop::new().execute(&mut test_fep(args! { "count" => -1 })).await.unwrap();

    let (iterations, er) = drain(&mut loop1);

    assert_json_eq!(Value::Array(iterations), json!([]));
    assert_eq!(er.get_name(), "output");
    assert_json_eq!(er.get_result().get("value").cloned().unwrap_or(Value::Null), json!(0));
}

#[tokio::test]
async fn count_loop3() {
    let mut loop1 = CountLoop::new().execute(&mut test_fep(args! { "count" => 0 })).await.unwrap();

    let (iterations, er) = drain(&mut loop1);

    assert_json_eq!(Value::Array(iterations), json!([]));
    assert_eq!(er.get_name(), "output");
    assert_json_eq!(er.get_result().get("value").cloned().unwrap_or(Value::Null), json!(0));
}

#[tokio::test]
async fn count_loop4() {
    let result = CountLoop::new().execute(&mut test_fep(args! {})).await;

    // JS `rejects.toThrow()` with no message: any error.
    assert!(result.is_err());
}
