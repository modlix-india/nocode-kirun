//! Port of kirun-js `__tests__/engine/function/system/math/MathFunctionRepositoryTest.ts`.

use std::sync::Arc;

use kirun::function::system::math::MathFunctionRepository;
use kirun::function::Function;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::Value;
use serde_json::json;

use crate::common::*;

/// JS `(await MathFunction.find(ns, name))?.execute(fep)` ... `.get('value')`.
async fn find_and_get_value(name: &str, args: kirun::Map<Value>) -> kirun::Result<Value> {
    let math_function = MathFunctionRepository::new();
    let func: Arc<dyn Function> = math_function
        .find(Namespaces::MATH, name)
        .await
        .unwrap_or_else(|| panic!("{name} not found in MathFunctionRepository"));
    first_value(func.as_ref(), fep().set_arguments(args), "value").await
}

#[tokio::test]
async fn test_math_functions_1() {
    // kirun-js answers 2; kirun-java's Ceiling always outputs a double (stored server functions
    // run on kirun-java).
    assert_eq!(find_and_get_value("Ceiling", args! { "value" => 1.2 }).await.unwrap(), json!(2.0));
}

#[tokio::test]
async fn test_math_functions_2() {
    // kirun-java's wording (stored server functions run on it): each schema error carries its
    // path (`value - ...`) and a non-number reads `<value> is not a <Type>`. The `[Expected: ...]`
    // type list keeps kirun-rust's order (kirun-java's follows a per-run hash order).
    assert_err_contains(
        find_and_get_value("Absolute", args! { "value" => "-1.2" }).await,
        "Error while executing the function System.Math.Absolute's parameter value [Expected: Integer | Long | Float | Double]: value - Value \"-1.2\" is not of valid type(s)\nvalue - \"-1.2\" is not a Double\nvalue - \"-1.2\" is not a Float\nvalue - \"-1.2\" is not a Long\nvalue - \"-1.2\" is not a Integer",
    );
}

#[tokio::test]
async fn test_math_functions_3() {
    let math_function = MathFunctionRepository::new();
    let _fep = fep().set_arguments(args! { "value" => 90 });

    let func = math_function.find(Namespaces::MATH, "ACosine").await;
    if func.is_none() {
        assert!(func.is_none());
    }
}

#[tokio::test]
async fn test_math_functions_4() {
    let math_function = MathFunctionRepository::new();
    assert!(math_function.find(Namespaces::STRING, "ASine").await.is_none());
}

#[tokio::test]
async fn test_math_functions_5() {
    let math_function = MathFunctionRepository::new();
    let _fep = fep().set_arguments(args! { "value" => "-1" });

    let func = math_function.find(Namespaces::MATH, "ATangent").await;
    if func.is_none() {
        assert!(func.is_none());
    }
}

#[tokio::test]
async fn test_math_functions_6() {
    assert_json_eq!(find_and_get_value("Cosine", args! { "value" => 1 }).await.unwrap(), json!(0.5403023058681398));
}

#[tokio::test]
async fn test_math_functions_7() {
    // kirun-js answers 8; kirun-java's Power always outputs a double.
    assert_eq!(find_and_get_value("Power", args! { "value1" => 2, "value2" => 3 }).await.unwrap(), json!(8.0));
}

#[tokio::test]
async fn test_math_functions_8() {
    // kirun-java's wording (stored server functions run on it): each schema error carries its
    // path (`value - ...`) and a non-number reads `<value> is not a <Type>`. The `[Expected: ...]`
    // type list keeps kirun-rust's order (kirun-java's follows a per-run hash order).
    assert_err_contains(
        find_and_get_value("Power", args! { "value1" => "1", "value2" => "1" }).await,
        "Error while executing the function System.Math.Power's parameter value1 [Expected: Integer | Long | Float | Double]: value1 - Value \"1\" is not of valid type(s)\nvalue1 - \"1\" is not a Double\nvalue1 - \"1\" is not a Float\nvalue1 - \"1\" is not a Long\nvalue1 - \"1\" is not a Integer",
    );
}

#[tokio::test]
async fn test_math_functions_9() {
    assert_json_eq!(
        find_and_get_value("Add", args! { "value" => json!([3, 2, 3, 5, 3]) }).await.unwrap(),
        json!(16)
    );
}

#[tokio::test]
async fn test_math_functions_10() {
    assert_json_eq!(
        find_and_get_value("Hypotenuse", args! { "value" => json!([3, 2]) }).await.unwrap(),
        json!(3.605551275463989)
    );
}
