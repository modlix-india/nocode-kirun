//! Helpers shared by every integration test binary. Include from `tests/<area>/main.rs` with
//! `#[path = "../common/mod.rs"] mod common;`. Do not put area-specific helpers here.
#![allow(dead_code, unused_macros, unused_imports)]

use std::sync::Arc;

use kirun::function::Function;
use kirun::json::schema::Schema;
use kirun::model::FunctionOutput;
use kirun::repository::{KIRunFunctionRepository, KIRunSchemaRepository, Repository};
use kirun::runtime::FunctionExecutionParameters;
use kirun::{Map, Value};

pub fn function_repo() -> Arc<dyn Repository<dyn Function>> {
    Arc::new(KIRunFunctionRepository::new())
}

pub fn schema_repo() -> Arc<dyn Repository<Schema>> {
    Arc::new(KIRunSchemaRepository::new())
}

/// JS `new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())`.
pub fn fep() -> FunctionExecutionParameters {
    FunctionExecutionParameters::new(function_repo(), schema_repo())
}

/// The usual JS test setup: `fep().setArguments(args).setSteps(new Map()).setContext(new Map())`.
pub fn fep_with_args(args: Map<Value>) -> FunctionExecutionParameters {
    fep().set_arguments(args).set_steps(Map::new()).set_context(Map::new())
}

/// Builds a `Map<Value>` from `"key" => value` pairs. A value is any `Serialize` expression;
/// use `json!(..)` for arrays/objects: `args! { "source" => json!([1, 2.0]), "n" => 3 }`.
macro_rules! args {
    () => { kirun::Map::<kirun::Value>::new() };
    ($($k:expr => $v:expr),+ $(,)?) => {{
        let mut m = kirun::Map::<kirun::Value>::new();
        $( m.insert(($k).to_string(), serde_json::to_value($v).unwrap()); )+
        m
    }};
}
pub(crate) use args;

/// Runs a function and returns the first event's result map
/// (JS `(await f.execute(fep)).allResults()[0].getResult()`).
pub async fn first_result(f: &dyn Function, mut fep: FunctionExecutionParameters) -> kirun::Result<Map<Value>> {
    let mut out: FunctionOutput = f.execute(&mut fep).await?;
    let er = out
        .next()
        .ok_or_else(|| kirun::KIRunError::runtime("function returned no events"))?;
    Ok(er.get_result().clone())
}

/// Runs a function and returns `result[key]` of the first event.
pub async fn first_value(f: &dyn Function, fep: FunctionExecutionParameters, key: &str) -> kirun::Result<Value> {
    Ok(first_result(f, fep).await?.get(key).cloned().unwrap_or(Value::Null))
}

/// JSON equality where numbers compare by value (`3 == 3.0`), like JS `toStrictEqual`/`toBe`.
pub fn json_eq(a: &Value, b: &Value) -> bool {
    match (a, b) {
        (Value::Number(x), Value::Number(y)) => match (x.as_f64(), y.as_f64()) {
            (Some(x), Some(y)) => x == y || (x - y).abs() <= f64::EPSILON * x.abs().max(y.abs()),
            _ => x == y,
        },
        (Value::Array(x), Value::Array(y)) => x.len() == y.len() && x.iter().zip(y).all(|(a, b)| json_eq(a, b)),
        (Value::Object(x), Value::Object(y)) => {
            x.len() == y.len() && x.iter().all(|(k, v)| y.get(k).is_some_and(|w| json_eq(v, w)))
        }
        _ => a == b,
    }
}

/// `assert_json_eq!(actual, json!(expected))`.
macro_rules! assert_json_eq {
    ($actual:expr, $expected:expr $(,)?) => {{
        let actual: kirun::Value = $actual;
        let expected: kirun::Value = $expected;
        assert!(
            crate::common::json_eq(&actual, &expected),
            "JSON mismatch\n  actual: {}\nexpected: {}",
            actual,
            expected
        );
    }};
}
pub(crate) use assert_json_eq;

/// JS `toBeCloseTo(expected, digits)` (default 2 digits): |a - e| < 10^-digits / 2.
pub fn assert_close_to(actual: &Value, expected: f64, digits: i32) {
    let a = actual.as_f64().unwrap_or_else(|| panic!("not a number: {actual}"));
    assert!(
        (a - expected).abs() < 10f64.powi(-digits) / 2.0,
        "{a} is not close to {expected} ({digits} digits)"
    );
}

/// Asserts an error whose message contains `needle` (JS `rejects.toThrow(needle)`).
pub fn assert_err_contains<T: std::fmt::Debug>(result: kirun::Result<T>, needle: &str) {
    match result {
        Ok(v) => panic!("expected an error containing {needle:?}, got Ok({v:?})"),
        Err(e) => {
            let msg = e.to_string();
            assert!(msg.contains(needle), "error {msg:?} does not contain {needle:?}");
        }
    }
}
