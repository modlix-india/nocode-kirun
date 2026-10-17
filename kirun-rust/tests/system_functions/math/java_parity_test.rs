//! `System.Math.*` answering as kirun-java 4.9.2 (stored server functions run on it). kirun-js
//! returns plain JS numbers; kirun-java keeps Java types, so these tests compare the JSON
//! exactly (`5.0` is not `5`). Each expected value was measured on kirun-java.

use std::sync::Arc;

use kirun::function::system::math::{Add, Hypotenuse, MathFunctionRepository, Maximum, Minimum};
use kirun::function::Function;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::{Map, Value};
use serde_json::json;

use crate::common::*;

async fn math(name: &str, args: Map<Value>) -> Value {
    let func: Arc<dyn Function> = MathFunctionRepository::new().find(Namespaces::MATH, name).await.unwrap();
    first_value(func.as_ref(), fep_with_args(args), "value").await.unwrap()
}

#[tokio::test]
async fn double_output_functions_always_answer_a_double() {
    // `ofEntryAnyNumberDoubleOutput`: Floor, Ceiling, the trigonometric and log functions,
    // SquareRoot; `AbstractBinaryMathFunction.ofEntryDouble`: Power, ArcTangent2.
    assert_eq!(math("Floor", args! { "value" => 1792233026 }).await, json!(1792233026.0));
    assert_eq!(math("Floor", args! { "value" => 2.7 }).await, json!(2.0));
    assert_eq!(math("Ceiling", args! { "value" => 1.2 }).await, json!(2.0));
    assert_eq!(math("SquareRoot", args! { "value" => 16 }).await, json!(4.0));
    assert_eq!(math("Cosine", args! { "value" => 0 }).await, json!(1.0));
    assert_eq!(math("Power", args! { "value1" => 2, "value2" => 3 }).await, json!(8.0));
    assert_eq!(math("Power", args! { "value1" => 2, "value2" => 62 }).await, json!(4.611686018427388e18));
    assert_eq!(math("ArcTangent2", args! { "value1" => 0, "value2" => 1 }).await, json!(0.0));
    // A result that is not a number (Java `NaN`) is null, as before.
    assert_eq!(math("SquareRoot", args! { "value" => -1 }).await, Value::Null);
}

#[tokio::test]
async fn to_degrees_and_to_radians_are_not_swapped() {
    // kirun-js has the two swapped; kirun-java uses `Math.toDegrees` / `Math.toRadians`.
    assert_eq!(math("ToDegrees", args! { "value" => 1 }).await, json!(57.29577951308232));
    assert_eq!(math("ToRadians", args! { "value" => 180 }).await, json!(std::f64::consts::PI));
    assert_eq!(math("ToRadians", args! { "value" => 1 }).await, json!(0.017453292519943295));
}

#[tokio::test]
async fn absolute_keeps_the_type() {
    assert_eq!(math("Absolute", args! { "value" => -5 }).await, json!(5));
    assert_eq!(math("Absolute", args! { "value" => -2.5 }).await, json!(2.5));
    assert_eq!(math("Absolute", args! { "value" => -9007199254740993i64 }).await, json!(9007199254740993i64));
    // Java `Math.abs` of the smallest int stays negative.
    assert_eq!(math("Absolute", args! { "value" => -2147483648i64 }).await, json!(-2147483648i64));
}

#[tokio::test]
async fn round_is_java_math_round() {
    // Halves go towards positive infinity (JS does the same; Rust's `round` would not).
    assert_eq!(math("Round", args! { "value" => 2.5 }).await, json!(3));
    assert_eq!(math("Round", args! { "value" => -2.5 }).await, json!(-2));
    assert_eq!(math("Round", args! { "value" => -0.5 }).await, json!(0));
    assert_eq!(math("Round", args! { "value" => 5 }).await, json!(5));
    // An `int` goes through `Math.round(float)`: 16777217 is not a float.
    assert_eq!(math("Round", args! { "value" => 16777217 }).await, json!(16777216));
    assert_eq!(math("Round", args! { "value" => 2147483648i64 }).await, json!(2147483648i64));
    assert_eq!(math("Round", args! { "value" => 1e300 }).await, json!(9223372036854775807i64));
}

#[tokio::test]
async fn add_keeps_the_widest_type() {
    let add = |v: Value| async move { first_value(&Add::new(), fep_with_args(args! { "value" => v }), "value").await };
    assert_eq!(add(json!([1, 2])).await.unwrap(), json!(3));
    assert_eq!(add(json!([1.0, 2.0])).await.unwrap(), json!(3.0));
    assert_eq!(add(json!([1, 2.5])).await.unwrap(), json!(3.5));
    // `int` + `int` wraps; a `long` widens.
    assert_eq!(add(json!([2147483647, 1])).await.unwrap(), json!(-2147483648i64));
    assert_eq!(add(json!([2147483648i64, 1])).await.unwrap(), json!(2147483649i64));
    // No values: kirun-java raises no event (kirun-js outputs 0).
    let out = Add::new().execute(&mut fep_with_args(args! { "value" => json!([]) })).await.unwrap();
    assert!(out.all_results().is_empty());
}

#[tokio::test]
async fn hypotenuse_maximum_and_minimum() {
    let run = |f: Arc<dyn Function>, v: Value| async move {
        first_result(f.as_ref(), fep_with_args(args! { "value" => v })).await.unwrap()
    };
    assert_eq!(run(Arc::new(Hypotenuse::new()), json!([3, 4])).await["value"], json!(5.0));
    assert_eq!(run(Arc::new(Hypotenuse::new()), json!([])).await["value"], json!(0));
    // Ties go to the later value; no values give an output without `value` (kirun-js throws).
    assert_eq!(run(Arc::new(Maximum::new()), json!([1, 1.0])).await["value"], json!(1.0));
    assert_eq!(run(Arc::new(Minimum::new()), json!([1.0, 1])).await["value"], json!(1));
    assert!(run(Arc::new(Maximum::new()), json!([])).await.is_empty());
    assert!(run(Arc::new(Minimum::new()), json!([])).await.is_empty());
}
