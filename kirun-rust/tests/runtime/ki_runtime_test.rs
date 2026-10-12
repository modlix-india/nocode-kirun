//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeTest.ts`.
//! System function signatures (`new Create().getSignature()` etc.) are referenced by their
//! namespace and name.

use std::sync::Arc;
use std::time::Instant;

use async_trait::async_trait;
use kirun::function::{AbstractFunction, Function};
use kirun::json::schema::Schema;
use kirun::model::{
    Event, EventResult, FunctionDefinition, FunctionOutput, FunctionSignature, Parameter, Statement,
};
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use kirun::util::map_util::MapUtil;
use kirun::util::primitive::number_value;
use kirun::Value;
use serde_json::json;

use crate::common::{args, assert_json_eq, fep, schema_repo};
use crate::{expr_ref, hybrid_with, shared_lock, value_ref};

#[tokio::test]
async fn ki_runtime_test_1() {
    let _lock = shared_lock();
    let num: usize = 7000;
    // JS numbers are doubles: from fib(79) on the values lose precision, and from fib(1477)
    // on they are Infinity. JS keeps Infinity in memory (Infinity + x stays Infinity) and only
    // serialises it as null; serde_json::Value cannot hold Infinity, so Rust stores null at
    // 1477, and `null + x` is `x` in KIRun (as in JS). The values therefore diverge after the
    // first non-finite one: compare the length, and the finite prefix by value (see json_eq).
    let mut array: Vec<f64> = Vec::with_capacity(num);
    let (a, b) = (0.0, 1.0);
    array.push(a);
    array.push(b);

    for i in 2..num {
        array.push(array[i - 2] + array[i - 1]);
    }
    let expected = Value::Array(array.iter().map(|&n| number_value(n)).collect());

    let integer_schema = json!({ "name": "EachElement", "type": "INTEGER" });
    let array_of_integer_schema = json!({
        "name": "ArrayType",
        "type": "ARRAY",
        "defaultValue": [],
        "items": integer_schema,
    });
    let create_array = Statement::new("createArray", Namespaces::SYSTEM_CTX, "Create").set_parameter_map(MapUtil::of(vec![
        ("name", value_ref(json!("a"))),
        ("schema", value_ref(array_of_integer_schema)),
    ]));

    let loop_ = Statement::new("loop", Namespaces::SYSTEM_LOOP, "RangeLoop")
        .set_parameter_map(MapUtil::of(vec![("from", value_ref(json!(0))), ("to", expr_ref("Arguments.Count"))]))
        .set_dependent_statements(MapUtil::of(vec![("Steps.createArray.output", true)]));

    let result_obj = json!({ "name": "result", "value": { "isExpression": true, "value": "Context.a" } });

    let output_generate = Statement::new("outputStep", Namespaces::SYSTEM, "GenerateEvent")
        .set_parameter_map(MapUtil::of(vec![("eventName", value_ref(json!("output"))), ("results", value_ref(result_obj))]))
        .set_dependent_statements(MapUtil::of(vec![("Steps.loop.output", true)]));

    let if_step = Statement::new("if", Namespaces::SYSTEM, "If").set_parameter_map(MapUtil::of(vec![(
        "condition",
        expr_ref("Steps.loop.iteration.index = 0 or Steps.loop.iteration.index = 1"),
    )]));

    let set1 = Statement::new("setOnTrue", Namespaces::SYSTEM_CTX, "Set")
        .set_parameter_map(MapUtil::of(vec![
            ("name", value_ref(json!("Context.a[Steps.loop.iteration.index]"))),
            ("value", expr_ref("Steps.loop.iteration.index")),
        ]))
        .set_dependent_statements(MapUtil::of(vec![("Steps.if.true", true)]));
    let set2 = Statement::new("setOnFalse", Namespaces::SYSTEM_CTX, "Set")
        .set_parameter_map(MapUtil::of(vec![
            ("name", value_ref(json!("Context.a[Steps.loop.iteration.index]"))),
            (
                "value",
                expr_ref("Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]"),
            ),
        ]))
        .set_dependent_statements(MapUtil::of(vec![("Steps.if.false", true)]));

    let start = Instant::now();
    let out = KIRuntime::new(
        FunctionDefinition::new("Fibonacci")
            .set_steps(MapUtil::of_entries_array(vec![
                Statement::of_entry(create_array),
                Statement::of_entry(loop_),
                Statement::of_entry(output_generate),
                Statement::of_entry(if_step),
                Statement::of_entry(set1),
                Statement::of_entry(set2),
            ]))
            .set_namespace("Test")
            .set_events(MapUtil::of_entries_array(vec![Event::output_event_map_entry(MapUtil::of(vec![(
                "result",
                Schema::of_array("result", vec![Schema::of_integer("result")]),
            )]))]))
            .set_parameters(MapUtil::of(vec![("Count", Parameter::new("Count", Schema::of_integer("Count")))])),
        false,
    )
    .unwrap()
    .execute(&mut fep().set_arguments(args! { "Count" => num }))
    .await
    .unwrap();
    let out: Vec<EventResult> = out.all_results().clone();
    println!("KIRuntime Logic : {}ms", start.elapsed().as_millis());

    let actual = out[0].get_result().get("result").cloned().unwrap_or_default();
    let finite = array.iter().take_while(|n| n.is_finite()).count();
    assert_eq!(actual.as_array().map(Vec::len), expected.as_array().map(Vec::len));
    assert_json_eq!(
        Value::Array(actual.as_array().unwrap()[..finite].to_vec()),
        Value::Array(expected.as_array().unwrap()[..finite].to_vec())
    );
}

#[tokio::test]
async fn ki_runtime_test_2() {
    let _lock = shared_lock();
    let expression = json!({ "isExpression": true, "value": "Steps.first.output.value" });

    let result_obj = json!({ "name": "result", "value": expression });

    let out = KIRuntime::new(
        FunctionDefinition::new("SingleCall")
            .set_namespace("Test")
            .set_parameters(MapUtil::of(vec![("Value", Parameter::new("Value", Schema::of_integer("Value")))]))
            .set_steps(MapUtil::of_entries_array(vec![
                Statement::of_entry(
                    Statement::new("first", Namespaces::MATH, "Absolute")
                        .set_parameter_map(MapUtil::of(vec![("value", expr_ref("Arguments.Value"))])),
                ),
                Statement::of_entry(Statement::new("second", Namespaces::SYSTEM, "GenerateEvent").set_parameter_map(
                    MapUtil::of(vec![("eventName", value_ref(json!("output"))), ("results", value_ref(result_obj))]),
                )),
            ])),
        false,
    )
    .unwrap()
    .execute(&mut fep().set_arguments(args! { "Value" => -10 }))
    .await
    .unwrap();

    assert_json_eq!(out.all_results()[0].get_result().get("result").cloned().unwrap_or_default(), json!(10));
}

#[tokio::test]
async fn ki_runtime_test_3() {
    let _lock = shared_lock();
    let expression = json!({ "isExpression": true, "value": "Steps.first.output.value" });

    let result_obj = json!({ "name": "result", "value": expression });

    let out = KIRuntime::new(
        FunctionDefinition::new("SingleCall")
            .set_namespace("Test")
            .set_parameters(MapUtil::of(vec![("Value", Parameter::new("Value", Schema::of_integer("Value")))]))
            .set_steps(MapUtil::of_entries_array(vec![
                Statement::of_entry(
                    Statement::new("first", Namespaces::MATH, "CubeRoot")
                        .set_parameter_map(MapUtil::of(vec![("value", expr_ref("Arguments.Value"))])),
                ),
                Statement::of_entry(Statement::new("second", Namespaces::SYSTEM, "GenerateEvent").set_parameter_map(
                    MapUtil::of(vec![("eventName", value_ref(json!("output"))), ("results", value_ref(result_obj))]),
                )),
            ])),
        false,
    )
    .unwrap()
    .execute(&mut fep().set_arguments(args! { "Value" => 27 }))
    .await
    .unwrap();

    assert_json_eq!(out.all_results()[0].get_result().get("result").cloned().unwrap_or_default(), json!(3));
}

struct FibFunction {
    signature: FunctionSignature,
}

#[async_trait]
impl AbstractFunction for FibFunction {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> kirun::Result<FunctionOutput> {
        let count = context.get_arguments().and_then(|a| a.get("value")).and_then(Value::as_i64).unwrap_or(0) as usize;
        let mut a: Vec<i64> = vec![0; count];
        for i in 0..count {
            a[i] = if i < 2 { i as i64 } else { a[i - 1] + a[i - 2] };
        }
        Ok(FunctionOutput::new(vec![EventResult::output_of(MapUtil::of(vec![("value", json!(a))]))]))
    }
}

/// JS `class X`: finds the fib function whatever the name.
struct X(Arc<dyn Function>);

#[async_trait]
impl Repository<dyn Function> for X {
    async fn find(&self, _namespace: &str, _name: &str) -> Option<Arc<dyn Function>> {
        Some(self.0.clone())
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        [self.0.get_signature().get_full_name()]
            .into_iter()
            .filter(|e| e.to_lowercase().contains(&name.to_lowercase()))
            .collect()
    }
}

#[tokio::test]
async fn ki_runtime_test_4() {
    let _lock = shared_lock();
    let num = 7usize;
    let mut array = vec![0i64; num];
    let (a, b) = (0, 1);
    array[0] = a;
    array[1] = b;

    for i in 2..num {
        array[i] = array[i - 2] + array[i - 1];
    }

    let fib_function_signature = FunctionSignature::new("FibFunction")
        .set_namespace("FibSpace")
        .set_parameters(MapUtil::of(vec![("value", Parameter::new("value", Schema::of_integer("value")))]))
        .set_events(MapUtil::of_entries_array(vec![Event::output_event_map_entry(MapUtil::of(vec![(
            "value",
            Schema::of_array("value", vec![Schema::of_integer("value")]),
        )]))]));

    let fib_namespace = fib_function_signature.get_namespace().to_string();
    let fib_function: Arc<dyn Function> = Arc::new(FibFunction { signature: fib_function_signature });

    let expression = json!({ "isExpression": true, "value": "Steps.fib.output.value" });
    let result_obj = json!({ "name": "result", "value": expression });

    let hybrid = hybrid_with(Arc::new(X(fib_function)));

    let out = KIRuntime::new(
        FunctionDefinition::new("CustomFunction")
            .set_namespace("Test")
            .set_parameters(MapUtil::of(vec![("Value", Parameter::new("Value", Schema::of_integer("Value")))]))
            .set_steps(MapUtil::of_entries_array(vec![
                Statement::of_entry(
                    Statement::new("fib", fib_namespace, "asdf")
                        .set_parameter_map(MapUtil::of(vec![("value", expr_ref("Arguments.Value"))])),
                ),
                Statement::of_entry(Statement::new("fiboutput", Namespaces::SYSTEM, "GenerateEvent").set_parameter_map(
                    MapUtil::of(vec![("eventName", value_ref(json!("output"))), ("results", value_ref(result_obj))]),
                )),
            ])),
        false,
    )
    .unwrap()
    .execute(&mut FunctionExecutionParameters::new(hybrid, schema_repo()).set_arguments(args! { "Value" => num }))
    .await
    .unwrap();

    assert_json_eq!(out.all_results()[0].get_result().get("result").cloned().unwrap_or_default(), json!(array));
}
