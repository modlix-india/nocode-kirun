//! Port of `ExpressionArrayStringIndexing.ts`.

use std::sync::Arc;

use kirun::runtime::expression::tokenextractor::ValuesMap;
use kirun::runtime::expression::ExpressionEvaluator;
use kirun::runtime::tokenextractor::ArgumentsTokenValueExtractor;
use kirun::Value;
use serde_json::json;

use crate::common::{args, assert_json_eq};
use crate::values_map;

fn eval(vm: &ValuesMap, expr: &str) -> Value {
    ExpressionEvaluator::new(expr).evaluate(vm).unwrap()
}

#[test]
fn expression_string_index() {
    let atv = ArgumentsTokenValueExtractor::new(args! {
        "a" => "kirun ",
        "b" => 2,
        "c" => json!({ "a": 2, "b": [true, false], "c": { "x": "kiran" } }),
        "d" => json!({ "a": 2, "b": [true, false], "c": { "x": "kiran" } }),
        "arr" => json!([0, 1, 2, 3, 4, 5, 6]),
    });
    let vm = values_map(vec![Arc::new(atv)]);

    assert_json_eq!(eval(&vm, "Arguments.a"), json!("kirun "));
    assert_json_eq!(eval(&vm, "Arguments.a[2]"), json!("r"));
    assert_json_eq!(eval(&vm, "Arguments.a[-2]"), json!("n"));
    assert_json_eq!(eval(&vm, "Arguments.arr[2..4]"), json!([2, 3]));
    assert_json_eq!(eval(&vm, "Arguments.a[2..4]"), json!("ru"));
    assert_json_eq!(eval(&vm, "Arguments.a[(4-2)..(6-2)]"), json!("ru"));
    assert_json_eq!(eval(&vm, "Arguments.a[..4]"), json!("kiru"));
    assert_json_eq!(eval(&vm, "Arguments.a[2..]"), json!("run "));
    assert_json_eq!(eval(&vm, "Arguments.arr[..4]"), json!([0, 1, 2, 3]));
    assert_json_eq!(eval(&vm, "Arguments.arr[(4-2)..7]"), json!([2, 3, 4, 5, 6]));
    assert_json_eq!(eval(&vm, "Arguments.a[..-4]"), json!("ki"));
    assert_json_eq!(eval(&vm, "Arguments.a[..(-8+4)]"), json!("ki"));
    assert_json_eq!(eval(&vm, "Arguments.a[-4..-1]"), json!("run"));
    assert_json_eq!(eval(&vm, "Arguments.a[-4..]"), json!("run "));
    assert_json_eq!(eval(&vm, "Arguments.arr[..-1]"), json!([0, 1, 2, 3, 4, 5]));
    assert_json_eq!(eval(&vm, "Arguments.arr[-2..]"), json!([5, 6]));
}
