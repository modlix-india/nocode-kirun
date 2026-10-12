//! Port of `ExpressionEvaluatorLengthSubtractionTest.ts`.

use std::sync::Arc;

use kirun::runtime::expression::tokenextractor::token_value_extractor::retrieve_element_from;
use kirun::runtime::expression::tokenextractor::{TokenValueExtractor, ValuesMap};
use kirun::runtime::expression::ExpressionEvaluator;
use kirun::{Result, Value};
use serde_json::json;

use crate::common::assert_json_eq;
use crate::values_map;

struct TestTokenValueExtractor {
    store: Value,
}

impl TokenValueExtractor for TestTokenValueExtractor {
    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        let parts: Vec<String> = token.split('.').map(String::from).collect();
        retrieve_element_from(token, &parts, 1, &self.store, values_map)
    }
    fn get_prefix(&self) -> &str {
        "Page."
    }
    fn get_store(&self) -> Value {
        self.store.clone()
    }
}

fn eval(store: Value, expr: &str) -> Value {
    let vm = values_map(vec![Arc::new(TestTokenValueExtractor { store })]);
    ExpressionEvaluator::new(expr).evaluate(&vm).unwrap()
}

fn nested_conditions() -> Value {
    json!({
        "mainFilterCondition": {
            "condition": {
                "conditions": [ { "conditions": [1, 2, 3, 4] }, { "conditions": [5, 6] } ]
            }
        }
    })
}

// ---- Array length with subtraction (working patterns)

#[test]
fn array_length_alone_should_work() {
    assert_json_eq!(eval(json!({ "conditions": [1, 2, 3, 4, 5] }), "Page.conditions.length"), json!(5));
}

#[test]
fn array_length_1_with_spaces_should_work() {
    assert_json_eq!(eval(json!({ "conditions": [1, 2, 3, 4, 5] }), "Page.conditions.length - 1"), json!(4));
}

#[test]
fn array_length_1_without_spaces_should_work() {
    assert_json_eq!(eval(json!({ "conditions": [1, 2, 3, 4, 5] }), "Page.conditions.length-1"), json!(4));
}

#[test]
fn nested_array_access_with_length_1_without_spaces() {
    assert_json_eq!(
        eval(nested_conditions(), "Page.mainFilterCondition.condition.conditions[0].conditions.length-1"),
        json!(3)
    );
}

#[test]
fn nested_array_access_with_length_1_with_spaces() {
    assert_json_eq!(
        eval(nested_conditions(), "Page.mainFilterCondition.condition.conditions[0].conditions.length - 1"),
        json!(3)
    );
}

#[test]
fn array_length_with_addition() {
    assert_json_eq!(eval(json!({ "items": [1, 2, 3] }), "Page.items.length+2"), json!(5));
}

#[test]
fn array_length_with_multiplication() {
    assert_json_eq!(eval(json!({ "items": [1, 2, 3] }), "Page.items.length*2"), json!(6));
}

#[test]
fn array_length_in_complex_expression() {
    // 3 + 4 - 1 = 6
    assert_json_eq!(
        eval(json!({ "arr1": [1, 2, 3], "arr2": [4, 5, 6, 7] }), "Page.arr1.length + Page.arr2.length - 1"),
        json!(6)
    );
}

#[test]
fn using_length_1_as_array_index() {
    assert_json_eq!(eval(json!({ "items": [10, 20, 30, 40, 50] }), "Page.items[Page.items.length - 1]"), json!(50));
}

#[test]
fn using_length_1_without_spaces_as_array_index() {
    assert_json_eq!(eval(json!({ "items": [10, 20, 30, 40, 50] }), "Page.items[Page.items.length-1]"), json!(50));
}

#[test]
fn nested_object_with_array_length_subtraction() {
    assert_json_eq!(
        eval(json!({ "data": { "nested": { "list": ["a", "b", "c", "d"] } } }), "Page.data.nested.list.length-1"),
        json!(3)
    );
}

#[test]
fn string_length_with_subtraction() {
    assert_json_eq!(eval(json!({ "text": "hello" }), "Page.text.length-1"), json!(4));
}

#[test]
fn object_keys_length_with_subtraction() {
    assert_json_eq!(eval(json!({ "obj": { "a": 1, "b": 2, "c": 3, "d": 4 } }), "Page.obj.length-1"), json!(3));
}

#[test]
fn empty_array_length_minus_1() {
    assert_json_eq!(eval(json!({ "empty": [] }), "Page.empty.length-1"), json!(-1));
}

#[test]
fn multiple_length_operations_in_same_expression() {
    // (3-1) * (2-1) = 2
    assert_json_eq!(
        eval(json!({ "list1": [1, 2, 3], "list2": [4, 5] }), "(Page.list1.length-1) * (Page.list2.length-1)"),
        json!(2)
    );
}

#[test]
fn length_with_division() {
    assert_json_eq!(eval(json!({ "items": [1, 2, 3, 4, 5, 6] }), "Page.items.length/2"), json!(3));
}

#[test]
fn length_with_modulus() {
    assert_json_eq!(eval(json!({ "items": [1, 2, 3, 4, 5] }), "Page.items.length%3"), json!(2));
}

#[test]
fn simple_greater_than_comparison_no_subtraction() {
    assert_eq!(eval(json!({ "items": [1, 2, 3] }), "Page.items.length > 0"), json!(true));
}

// ---- Alternative patterns with parentheses

#[test]
fn alternative_length_comparison_with_parentheses_and_spaces() {
    assert_eq!(eval(json!({ "items": [1, 2, 3] }), "(Page.items.length - 1) > 0"), json!(true));
}

#[test]
fn alternative_length_comparison_with_parentheses_no_spaces() {
    assert_eq!(eval(json!({ "items": [1, 2, 3] }), "(Page.items.length-1) > 0"), json!(true));
}

#[test]
fn alternative_literal_subtraction_and_comparison_with_parentheses() {
    assert_eq!(eval(json!({}), "(5 - 1) > 0"), json!(true));
}

#[test]
fn alternative_chained_subtraction_with_parentheses() {
    assert_json_eq!(eval(json!({}), "(5 - 1) - 2"), json!(2));
}

#[test]
fn alternative_length_in_ternary_with_parentheses() {
    assert_json_eq!(
        eval(json!({ "items": [1, 2, 3, 4] }), "(Page.items.length - 1) > 2 ? 'big' : 'small'"),
        json!("big")
    );
}

#[test]
fn alternative_subtraction_with_equality_using_parentheses() {
    assert_eq!(eval(json!({ "items": [1, 2, 3] }), "(Page.items.length - 1) = 2"), json!(true));
}

// ---- Previously known issues (now fixed)

#[test]
fn fixed_length_comparison_without_parentheses_now_works() {
    assert_eq!(eval(json!({ "items": [1, 2, 3] }), "Page.items.length - 1 > 0"), json!(true));
}

#[test]
fn fixed_simple_subtraction_and_comparison_without_parentheses_now_works() {
    assert_eq!(eval(json!({}), "5 - 1 > 0"), json!(true));
}

#[test]
fn fixed_chained_subtraction_now_has_correct_associativity() {
    // (5-1)-2 = 2
    assert_json_eq!(eval(json!({}), "5 - 1 - 2"), json!(2));
}

#[test]
fn fixed_subtraction_then_equality_without_parentheses_now_works() {
    assert_eq!(eval(json!({ "items": [1, 2, 3] }), "Page.items.length - 1 = 2"), json!(true));
}

#[test]
fn fixed_length_in_ternary_without_parentheses_now_works() {
    assert_json_eq!(
        eval(json!({ "items": [1, 2, 3, 4] }), "Page.items.length - 1 > 2 ? 'big' : 'small'"),
        json!("big")
    );
}
