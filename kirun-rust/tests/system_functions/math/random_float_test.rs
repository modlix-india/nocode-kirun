//! Port of kirun-js `__tests__/engine/function/system/math/RandomFloatTest.ts`.

use kirun::function::system::math::MathFunctionRepository;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::Map;
use kirun::Value;

use crate::common::*;

async fn random_float(args: Map<Value>) -> f64 {
    let rand = MathFunctionRepository::new().find(Namespaces::MATH, "RandomFloat").await.unwrap();
    let num = first_value(rand.as_ref(), fep().set_arguments(args), "value").await.unwrap();
    num.as_f64().unwrap_or_else(|| panic!("not a number: {num}"))
}

#[tokio::test]
async fn rand_float_1() {
    let (min, max) = (1, 10);
    let num = random_float(args! { "minValue" => min, "maxValue" => max }).await;

    assert!(num <= max as f64);
    assert!(num >= min as f64);
}

#[tokio::test]
async fn rand_float_2() {
    let (min, max) = (0.1, 0.9);
    let num = random_float(args! { "minValue" => min, "maxValue" => max }).await;

    assert!(num <= max as f64);
    assert!(num >= min as f64);
}

#[tokio::test]
async fn rand_float_3() {
    let (min, max) = (1, 2147483647);
    let num = random_float(args! { "minValue" => min }).await;

    assert!(num <= max as f64);
    assert!(num >= min as f64);
}

#[tokio::test]
async fn rand_float_4() {
    let (min, max) = (1.1, 1.2);
    let num = random_float(args! { "minValue" => min, "maxValue" => max }).await;

    assert!(num <= max as f64);
    assert!(num >= min as f64);
}
