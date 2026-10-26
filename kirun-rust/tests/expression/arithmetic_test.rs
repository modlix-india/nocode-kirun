//! Whole-number arithmetic as kirun-js does it (JavaScript numbers, 2026-10-26): nothing wraps at
//! 32 or 64 bits, `/` is true division, `//` floors and `%` keeps the dividend's sign. A product
//! such as `40000 * 12 * 10000` used to wrap to 505032704 (kirun-java's `int`). The same cases
//! run in kirun-js, kirun-java and kirun-py.

use std::sync::Arc;

use kirun::runtime::expression::tokenextractor::token_value_extractor::{TokenValueExtractor, ValuesMap};
use kirun::runtime::expression::ExpressionEvaluator;
use kirun::runtime::tokenextractor::arguments_token_value_extractor::ArgumentsTokenValueExtractor;
use kirun::{Map, Value};
use serde_json::json;

fn eval(expr: &str) -> Value {
    let args = json!({
        "a": 40000, "b": 12, "c": 10000, "p": 480000, "q": 6000000,
        "n": -40000, "f": 1.5, "s": "ab", "x": 7, "y": 2,
    });
    let store: Map<Value> = args.as_object().unwrap().iter().map(|(k, v)| (k.clone(), v.clone())).collect();
    let ate = ArgumentsTokenValueExtractor::new(store);
    let mut vm: ValuesMap = Map::new();
    vm.insert(ate.get_prefix().to_string(), Arc::new(ate));
    ExpressionEvaluator::new(expr).evaluate(&vm).unwrap_or_else(|e| panic!("{expr}: {e}"))
}

/// A whole result: exact, and still a JSON integer.
fn whole(expr: &str, expected: i64) {
    let v = eval(expr);
    assert_eq!(v, json!(expected), "{expr}");
    assert!(v.is_i64(), "{expr} should be whole, got {v}");
}

fn number(expr: &str, expected: f64) {
    let v = eval(expr);
    assert_eq!(v.as_f64(), Some(expected), "{expr} gave {v}");
}

#[test]
fn big_products_do_not_wrap() {
    whole("40000 * 12 * 10000", 4_800_000_000);
    whole("Arguments.a * Arguments.b * Arguments.c", 4_800_000_000);
    whole("100000 * 100000", 10_000_000_000);
    whole("65536 * 65536", 4_294_967_296);
    whole("2147483647 + 1", 2_147_483_648);
    whole("2147483647 * 2147483647", 4_611_686_014_132_420_609);
    whole("9007199254740993 * 1", 9_007_199_254_740_993);
    // Past the `long` range: a double, as in JS.
    number("9223372036854775807 + 1", 9.223372036854776e18);
    number("9223372036854775807 * 2", 1.8446744073709552e19);
    number("3037000500 * 3037000500", 3037000500.0 * 3037000500.0);
}

#[test]
fn chained_multiply_and_divide() {
    // `*` and `/` group from the right in every KIRun runtime (`a / b * c` is `a / (b * c)`).
    // a * b * (c / q): 480000 * 0.0016666666666666668 is 800 as a double.
    number("Arguments.a * Arguments.b * Arguments.c / Arguments.q", 800.0);
    number("Arguments.a * Arguments.b * Arguments.c * 10000.0 / Arguments.q", 8_000_000.0);
    number("Arguments.p / Arguments.q * 100", 0.0008);
    whole("4800000000 / 1000", 4_800_000);
}

#[test]
fn division_is_true_division() {
    number("480000 / 6000000", 0.08);
    number("Arguments.p / Arguments.q", 0.08);
    number("7 / 2", 3.5);
    number("Arguments.x / Arguments.y", 3.5);
    number("1 / 3", 1.0 / 3.0);
    number("10 / 4", 2.5);
    number("4800000000 / 7", 4_800_000_000.0 / 7.0);
    whole("6 / 3", 2);
    // A whole zero divisor still fails as before (JS would give Infinity, which JSON cannot hold).
    let vm: ValuesMap = Map::new();
    assert!(ExpressionEvaluator::new("1 / 0").evaluate(&vm).is_err());
}

#[test]
fn integer_division_and_modulus() {
    whole("7 // 2", 3);
    whole("-7 // 2", -4);
    whole("7 % 3", 1);
    whole("-7 % 3", -1);
    whole("7 % -3", 1);
    whole("-7 % -3", -1);
    number("7.5 % 2", 1.5);
    number("-7.5 % 2", -1.5);
}

#[test]
fn negative_numbers() {
    whole("-40000 * 12 * 10000", -4_800_000_000);
    whole("Arguments.n * Arguments.b * Arguments.c", -4_800_000_000);
    whole("-65536 * 65536", -4_294_967_296);
    whole("-2147483648 - 1", -2_147_483_649);
    whole("-(-2147483648)", 2_147_483_648);
    whole("2 * -3", -6);
    number("-7 / 2", -3.5);
    number("-9223372036854775807 - 2", -9.223372036854776e18);
}

#[test]
fn mixed_whole_and_decimal() {
    number("1.5 * 4", 6.0);
    number("Arguments.f * 4", 6.0);
    number("Arguments.f * Arguments.a", 60000.0);
    number("7 * 0.5", 3.5);
    number("10 - 2.5", 7.5);
    number("0.1 + 0.2", 0.1 + 0.2);
}

#[test]
fn comparisons_on_results() {
    assert_eq!(eval("40000 * 12 * 10000 > 4000000000"), json!(true));
    assert_eq!(eval("Arguments.a * Arguments.b * Arguments.c = 4800000000"), json!(true));
    assert_eq!(eval("480000 / 6000000 = 0.08"), json!(true));
    assert_eq!(eval("480000 / 6000000 < 0.1"), json!(true));
    assert_eq!(eval("480000 / 6000000 > 0"), json!(true));
    assert_eq!(eval("6 / 3 = 2"), json!(true));
    assert_eq!(eval("-40000 * 12 * 10000 < -4000000000"), json!(true));
}

#[test]
fn string_concatenation_unaffected() {
    assert_eq!(eval("'ab' + 40000 * 12 * 10000"), json!("ab4800000000"));
    assert_eq!(eval("Arguments.s + 1"), json!("ab1"));
    assert_eq!(eval("'x' + 7 / 2"), json!("x3.5"));
    assert_eq!(eval("1 + 2 + 'a'"), json!("3a"));
    assert_eq!(eval("'a' + 1 + 2"), json!("a12"));
    assert_eq!(eval("'Total: ' + Arguments.a * Arguments.b"), json!("Total: 480000"));
    assert_eq!(eval("Arguments.s * 2"), json!("abab"));
}
