//! Port of kirun-js `__tests__/engine/function/system/math/RandomIntTest.ts`.

use kirun::function::system::math::MathFunctionRepository;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::Map;
use kirun::Value;

use crate::common::*;

async fn random_int(args: Map<Value>) -> f64 {
    let rand = MathFunctionRepository::new().find(Namespaces::MATH, "RandomInt").await.unwrap();
    let num = first_value(rand.as_ref(), fep().set_arguments(args), "value").await.unwrap();
    num.as_f64().unwrap_or_else(|| panic!("not a number: {num}"))
}

#[tokio::test]
async fn rand_int_1() {
    let (min, max) = (100, 1000123);
    let num = random_int(args! { "minValue" => min, "maxValue" => max }).await;

    assert!(num <= max as f64);
    assert!(num >= min as f64);
}

#[tokio::test]
async fn rand_int_2() {
    let min = 100;
    let num = random_int(args! { "minValue" => min }).await;

    assert!(num <= 2147483647.0);
    assert!(num >= min as f64);
}

#[tokio::test]
async fn rand_int_3() {
    let (min, max) = (100, 101);
    let num = random_int(args! { "minValue" => min, "maxValue" => max }).await;

    assert!(num <= max as f64);
    assert!(num >= min as f64);
}
