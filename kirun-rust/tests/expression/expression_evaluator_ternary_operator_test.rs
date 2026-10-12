//! Port of `ExpressionEvaluatorTernaryOperatorTest.ts`.

use std::sync::Arc;

use kirun::runtime::expression::{Expression, ExpressionEvaluator};
use kirun::runtime::tokenextractor::ArgumentsTokenValueExtractor;
use serde_json::json;

use crate::common::{args, assert_json_eq};
use crate::values_map;

#[test]
fn expression_with_ternary_operator_1() {
    // JS String.replace with a string pattern replaces only the first occurrence.
    let exp = Expression::new(&"a > 10 ?  a - 2 : a + 3".replacen(' ', "", 1)).unwrap();
    assert_eq!(exp.to_string(), "((a>10)?(a-2):(a+3))");

    let exp = Expression::new("a > 10 ?  a - 2 : a + 3").unwrap();
    assert_eq!(exp.to_string(), "((a>10)?(a-2):(a+3))");

    let exp = Expression::new("a > 10 ? a > 15 ? a + 2 : a - 2 : a + 3").unwrap();
    assert_eq!(exp.to_string(), "((a>10)?((a>15)?(a+2):(a-2)):(a+3))");
}

#[test]
fn expression_evaluation_with_ternary_operator_1() {
    let x = json!({ "a": 2, "b": [true, false], "c": { "x": "Arguments.b2" } });
    let atv = ArgumentsTokenValueExtractor::new(args! {
        "a" => "kirun ",
        "b" => 2,
        "b1" => 4,
        "b2" => 4,
        "c" => x.clone(),
        "d" => "c",
    });
    let vm = values_map(vec![Arc::new(atv)]);

    let ev = ExpressionEvaluator::new("Arguments.e = null ? Arguments.c.a : 3 ");
    assert_json_eq!(ev.evaluate(&vm).unwrap(), json!(2));

    let ev = ExpressionEvaluator::new("Arguments.f ? Arguments.c.a : 3 ");
    assert_json_eq!(ev.evaluate(&vm).unwrap(), json!(3));

    let ev = ExpressionEvaluator::new("Arguments.e = null ? Arguments.c : 3 ");
    assert_json_eq!(ev.evaluate(&vm).unwrap(), x);
}
