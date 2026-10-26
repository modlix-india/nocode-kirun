//! kirun-js `__tests__/engine/dsl/GenerateEventLiteral.spec.ts`: a literal `value` in a
//! GenerateEvent result compiles to `{isExpression: false, value}`, prints back as the literal,
//! and runs.

use kirun::dsl::DSLCompiler;
use kirun::model::FunctionDefinition;
use kirun::runtime::KIRuntime;
use kirun::Value;
use serde_json::json;

use crate::common::{args, fep_with_args, first_result};

const SOURCE: &str = r#"FUNCTION Parity
    NAMESPACE Test
    PARAMETERS
        n AS INTEGER
    EVENTS
        output
            kind AS STRING
            n AS INTEGER
            tags AS ARRAY OF STRING
            sum AS INTEGER
    LOGIC
        out: System.GenerateEvent(results = {name: "kind", value: "odd"}, results = {name: "n", value: 5}, results = {name: "tags", value: ["a", "b"]}, results = {name: "sum", value: Arguments.n + 1})"#;

fn results_of(func: &Value) -> Vec<Value> {
    let mut refs: Vec<&Value> = func["steps"]["out"]["parameterMap"]["results"].as_object().unwrap().values().collect();
    refs.sort_by_key(|r| r["order"].as_i64());
    refs.into_iter().map(|r| r["value"].clone()).collect()
}

#[test]
fn a_literal_compiles_to_a_value_object() {
    let json = DSLCompiler::compile(SOURCE).unwrap();
    assert_eq!(
        results_of(&json),
        vec![
            json!({"name": "kind", "value": {"isExpression": false, "value": "odd"}}),
            json!({"name": "n", "value": {"isExpression": false, "value": 5}}),
            json!({"name": "tags", "value": {"isExpression": false, "value": ["a", "b"]}}),
            json!({"name": "sum", "value": {"isExpression": true, "value": "Arguments.n + 1"}}),
        ]
    );
}

#[test]
fn prints_back_as_the_literal_and_round_trips() {
    let json = DSLCompiler::compile(SOURCE).unwrap();
    let printed = DSLCompiler::decompile(&json).unwrap();
    assert!(printed.contains(r#"{name: "kind", value: "odd"}"#), "{printed}");
    assert!(printed.contains(r#"{name: "n", value: 5}"#), "{printed}");
    assert!(printed.contains(r#"{name: "tags", value: ["a", "b"]}"#), "{printed}");
    assert_eq!(results_of(&DSLCompiler::compile(&printed).unwrap()), results_of(&json));
}

#[tokio::test]
async fn runs_and_returns_every_named_value() {
    let def = FunctionDefinition::from_value(&DSLCompiler::compile(SOURCE).unwrap()).unwrap();
    let runtime = KIRuntime::new(def, false).unwrap();
    let result = first_result(&runtime, fep_with_args(args! { "n" => 2 })).await.unwrap();
    assert_eq!(result.get("kind"), Some(&json!("odd")));
    assert_eq!(result.get("n").and_then(|v| v.as_i64()), Some(5));
    assert_eq!(result.get("tags"), Some(&json!(["a", "b"])));
    assert_eq!(result.get("sum").and_then(|v| v.as_f64()), Some(3.0));
}
