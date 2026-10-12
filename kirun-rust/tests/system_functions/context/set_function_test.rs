//! Port of kirun-js `__tests__/engine/function/system/context/SetFunctionTest.ts`.

use kirun::function::system::context::SetFunction;
use kirun::function::Function;
use kirun::json::schema::Schema;
use kirun::runtime::context_element::ContextElement;
use kirun::runtime::function_execution_parameters::ContextMap;
use kirun::{shared, Value};
use serde_json::json;

use crate::common::*;

/// The context map JS shares by reference with `fep.setContext(contextMap)`.
fn element_of(context_map: &kirun::Shared<ContextMap>, key: &str) -> Value {
    context_map.read().get(key).map(|ce| ce.get_element().clone()).unwrap_or(Value::Null)
}

#[tokio::test]
async fn set_function_test_1() {
    let set_function = SetFunction::new();

    let mut map = ContextMap::new();
    map.insert("a".to_string(), ContextElement::new(Some(Schema::of_any("test")), json!({})));
    let context_map = shared(map);

    let mut fep = fep()
        .set_context_shared(context_map.clone())
        .set_arguments(args! { "name" => "Context.a.b", "value" => 20 });

    set_function.execute(&mut fep).await.unwrap();
    assert_json_eq!(element_of(&context_map, "a")["b"].clone(), json!(20));

    fep.set_arguments_in_place(args! { "name" => "Context.a.c[2].d", "value" => 25 });

    set_function.execute(&mut fep).await.unwrap();
    assert_json_eq!(element_of(&context_map, "a")["c"][2]["d"].clone(), json!(25));
}

#[tokio::test]
async fn set_function_test_2() {
    let set_function = SetFunction::new();

    let mut map = ContextMap::new();
    map.insert("a".to_string(), ContextElement::new(Some(Schema::of_any("test")), json!([])));
    let context_map = shared(map);

    let mut fep = fep()
        .set_context_shared(context_map.clone())
        .set_arguments(args! { "name" => "Context.a[1]", "value" => 240 });

    set_function.execute(&mut fep).await.unwrap();
    assert_json_eq!(element_of(&context_map, "a")[1].clone(), json!(240));
}

#[tokio::test]
async fn set_function_test_3() {
    let set_function = SetFunction::new();

    let mut map = ContextMap::new();
    map.insert("a".to_string(), ContextElement::new(Some(Schema::of_any("test")), json!({})));
    let context_map = shared(map);

    let mut fep = fep()
        .set_context_shared(context_map.clone())
        .set_arguments(args! { "name" => "Context.a.b[1]", "value" => 240 });

    set_function.execute(&mut fep).await.unwrap();
    assert_json_eq!(element_of(&context_map, "a")["b"][1].clone(), json!(240));
}
