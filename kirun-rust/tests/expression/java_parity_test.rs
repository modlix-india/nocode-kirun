//! Expressions as kirun-java evaluates them (stored server functions run on kirun-java):
//! `java_expression_cases.jsonl` holds 500 expressions with their arguments and kirun-java 4.9.2's
//! answer (a value, or its error message): every operator against nulls, missing values,
//! numbers, strings, booleans, arrays and objects, and literals and bare words (K5). Values
//! compare exactly (a whole number and a double apart).
//!
//! Not in it, because kirun-rust cannot reproduce them with JSON values: kirun-java computes a
//! decimal it parsed from request text or an expression literal as a 32-bit `float`
//! (`Arguments.n * 0.1` is `0.3` there), and keeps NaN and Infinity results.

use std::sync::Arc;

use kirun::runtime::expression::tokenextractor::token_value_extractor::{TokenValueExtractor, ValuesMap};
use kirun::runtime::expression::ExpressionEvaluator;
use kirun::runtime::tokenextractor::arguments_token_value_extractor::ArgumentsTokenValueExtractor;
use kirun::{Map, Value};
use serde_json::json;

const CASES: &str = include_str!("java_expression_cases.jsonl");

fn values(args: &Value) -> ValuesMap {
    let store: Map<Value> = args.as_object().map(|o| o.iter().map(|(k, v)| (k.clone(), v.clone())).collect()).unwrap_or_default();
    let ate = ArgumentsTokenValueExtractor::new(store);
    let mut vm: ValuesMap = Map::new();
    vm.insert(ate.get_prefix().to_string(), Arc::new(ate));
    vm
}

#[test]
fn expressions_evaluate_as_kirun_java() {
    let mut failures = Vec::new();
    let mut count = 0;
    for line in CASES.lines().filter(|l| !l.trim().is_empty()) {
        count += 1;
        let case: Value = serde_json::from_str(line).unwrap();
        let expr = case["expr"].as_str().unwrap();
        let got = ExpressionEvaluator::new(expr).evaluate(&values(&case["args"])).map_err(|e| e.to_string());
        let ok = match (&got, case.get("ok"), case.get("err")) {
            (Ok(v), Some(expected), _) => v == expected,
            (Err(e), _, Some(Value::String(expected))) => e == expected,
            _ => false,
        };
        if !ok {
            failures.push(format!("{expr}\n    java {}\n    rust {got:?}", case.get("ok").or(case.get("err")).unwrap()));
        }
    }
    assert_eq!(count, 500);
    assert!(failures.is_empty(), "{} of {count} differ:\n{}", failures.len(), failures.join("\n"));
}

#[test]
fn bare_words_are_text() {
    // K5: kirun-java reads a token that is not a keyword, a quoted string or a Java number as
    // its text; kirun-js fails ("Unable to parse the literal or expression").
    let vm = values(&json!({ "s": "paymentDetails" }));
    let eval = |e: &str| ExpressionEvaluator::new(e).evaluate(&vm).unwrap();
    assert_eq!(eval("paymentDetails"), json!("paymentDetails"));
    assert_eq!(eval("abc + 1"), json!("abc1"));
    assert_eq!(eval("Arguments.s = paymentDetails"), json!(true));
    assert_eq!(eval("1e5"), json!("1e5"));
    assert_eq!(eval("0x10"), json!("0x10"));
    assert_eq!(eval("Infinity"), json!("Infinity"));
    assert_eq!(eval("2.val"), json!("2.val"));
    // Whole number literals are exact (a Java long), not doubles.
    assert_eq!(eval("9007199254740993"), json!(9007199254740993_i64));
}

#[test]
fn missing_and_null_are_told_apart() {
    // kirun-java's evaluator returns Java null for a plain path to nothing and JSON null for a
    // path to a null value or any expression with an operator.
    let vm = values(&json!({ "a": { "b": null } }));
    let eval = |e: &str| ExpressionEvaluator::new(e).evaluate_or_missing(&vm).unwrap();
    assert_eq!(eval("Arguments.a.b"), Some(Value::Null));
    assert_eq!(eval("Arguments.a.c"), None);
    assert_eq!(eval("Arguments.x ?? Arguments.y"), Some(Value::Null));
    assert_eq!(eval("1 + 1"), Some(json!(2)));
}

#[test]
fn whole_decimal_literals_stay_decimal() {
    // kirun-java reads `1000000000.0` as a float, so the arithmetic is floating point: Java gives
    // 3.0E9 for `3 * 1000000000.0`, where 32-bit integer maths wraps to -1294967296.
    let vm = values(&json!({ "w": 3 }));
    let eval = |e: &str| ExpressionEvaluator::new(e).evaluate(&vm).unwrap();
    assert_eq!(eval("5.0"), json!(5.0));
    assert!(eval("5.0").is_f64());
    assert_eq!(eval("0.0"), json!(0.0));
    assert!(eval("Arguments.w * 1000000000.0").is_f64());
    assert_eq!(eval("Arguments.w * 1000000000.0"), json!(3.0e9));
    assert_eq!(eval("Arguments.w * 604800000.0"), json!(1814400000.0));
    assert_eq!(eval("Arguments.w * 1000000000"), json!(-1294967296));
    assert_eq!(eval("5.0 = 5"), json!(true));
    assert_eq!(eval("2.5"), json!(2.5));
    assert_eq!(eval("7"), json!(7));
}
