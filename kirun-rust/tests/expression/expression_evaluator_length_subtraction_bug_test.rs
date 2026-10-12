//! Port of `ExpressionEvaluatorLengthSubtractionBugTest.ts`
//! (describe 'Expression Evaluator - Length Subtraction Bug Reproduction').
//!
//! Reproduces `Page.mainFilterCondition.condition.conditions[0].conditions.length-1` failing
//! with "Cannot evaluate expression [object Object] - 1".

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

fn page(store: Value) -> ValuesMap {
    values_map(vec![Arc::new(TestTokenValueExtractor { store })])
}

/// JS `expect(() => ev.evaluate(..)).not.toThrow(); expect(ev.evaluate(..)).toBe(expected)`.
fn assert_no_throw_then(store: Value, expr: &str, expected: Value) {
    let vm = page(store);
    let ev = ExpressionEvaluator::new(expr);
    assert!(ev.evaluate(&vm).is_ok(), "{expr} threw");
    assert_json_eq!(ev.evaluate(&vm).unwrap(), expected);
}

fn eval(store: Value, expr: &str) -> Value {
    ExpressionEvaluator::new(expr).evaluate(&page(store)).unwrap()
}

// ---- Bug reproduction - nested array length subtraction

#[test]
fn nested_array_access_with_length_1_exact_failing_case_from_bug_report() {
    let store = json!({
        "mainFilterCondition": {
            "condition": {
                "conditions": [ { "conditions": [1, 2, 3, 4] }, { "conditions": [5, 6] } ]
            }
        }
    });
    assert_json_eq!(
        eval(store, "Page.mainFilterCondition.condition.conditions[0].conditions.length-1"),
        json!(3)
    );
}

#[test]
fn nested_array_access_with_length_1_with_spaces() {
    let store = json!({
        "mainFilterCondition": { "condition": { "conditions": [ { "conditions": [1, 2, 3, 4] } ] } }
    });
    assert_no_throw_then(store, "Page.mainFilterCondition.condition.conditions[0].conditions.length - 1", json!(3));
}

// ---- Edge cases - object with length property

#[test]
fn fixed_object_with_length_property_that_is_an_object_should_return_object_keys_length() {
    // obj.length is Object.keys(obj).length = 1, so 1 - 1 = 0
    assert_json_eq!(eval(json!({ "obj": { "length": { "nested": "object" } } }), "Page.obj.length-1"), json!(0));
}

#[test]
fn nested_object_with_length_property_in_path() {
    let store = json!({
        "mainFilterCondition": {
            "condition": {
                "conditions": [ { "conditions": [1, 2, 3, 4], "length": { "some": "object" } } ]
            }
        }
    });
    assert_no_throw_then(store, "Page.mainFilterCondition.condition.conditions[0].conditions.length-1", json!(3));
}

#[test]
fn object_with_length_property_that_is_a_number_should_work() {
    assert_json_eq!(eval(json!({ "obj": { "length": 5 } }), "Page.obj.length-1"), json!(4));
}

#[test]
fn array_length_should_always_work_no_length_property_conflict() {
    assert_json_eq!(eval(json!({ "arr": [1, 2, 3, 4, 5] }), "Page.arr.length-1"), json!(4));
}

// ---- Complex nested scenarios

#[test]
fn deeply_nested_structure_with_multiple_array_accesses() {
    let store = json!({
        "level1": { "level2": { "level3": { "items": [
            { "subItems": ["a", "b", "c", "d", "e"] },
            { "subItems": ["f", "g"] }
        ] } } }
    });
    assert_no_throw_then(store, "Page.level1.level2.level3.items[0].subItems.length-1", json!(4));
}

#[test]
fn fixed_multiple_length_operations_in_same_expression_should_work() {
    // (3-1) + (5-1) = 6
    assert_no_throw_then(
        json!({ "arr1": [1, 2, 3], "arr2": [4, 5, 6, 7, 8] }),
        "Page.arr1.length-1 + Page.arr2.length-1",
        json!(6),
    );
}

#[test]
fn length_1_used_as_array_index() {
    assert_no_throw_then(json!({ "items": [10, 20, 30, 40, 50] }), "Page.items[Page.items.length-1]", json!(50));
}

// ---- Different data types with length

#[test]
fn string_length_subtraction() {
    assert_json_eq!(eval(json!({ "text": "hello world" }), "Page.text.length-1"), json!(10));
}

#[test]
fn empty_array_length_minus_1() {
    assert_json_eq!(eval(json!({ "empty": [] }), "Page.empty.length-1"), json!(-1));
}

#[test]
fn object_with_no_length_property_should_use_object_keys() {
    assert_json_eq!(eval(json!({ "obj": { "a": 1, "b": 2, "c": 3 } }), "Page.obj.length-1"), json!(2));
}

// ---- Specific bug scenarios

#[test]
fn scenario_where_intermediate_object_has_length_property() {
    let store = json!({
        "mainFilterCondition": {
            "condition": {
                "length": { "conflict": true },
                "conditions": [ { "conditions": [1, 2, 3, 4] } ]
            }
        }
    });
    assert_no_throw_then(store, "Page.mainFilterCondition.condition.conditions[0].conditions.length-1", json!(3));
}

#[test]
fn scenario_where_array_element_has_length_property() {
    let store = json!({
        "mainFilterCondition": {
            "condition": {
                "conditions": [ { "conditions": [1, 2, 3, 4], "length": { "nested": "value" } } ]
            }
        }
    });
    assert_no_throw_then(store, "Page.mainFilterCondition.condition.conditions[0].conditions.length-1", json!(3));
}

// ---- Expression parsing edge cases

#[test]
fn length_1_without_spaces_in_complex_path() {
    assert_no_throw_then(
        json!({ "data": { "nested": { "list": [1, 2, 3, 4, 5, 6] } } }),
        "Page.data.nested.list.length-1",
        json!(5),
    );
}

#[test]
fn length_1_with_spaces_in_complex_path() {
    assert_no_throw_then(
        json!({ "data": { "nested": { "list": [1, 2, 3, 4, 5, 6] } } }),
        "Page.data.nested.list.length - 1",
        json!(5),
    );
}

#[test]
fn parentheses_around_length_1() {
    assert_no_throw_then(json!({ "items": [1, 2, 3, 4] }), "(Page.items.length-1)", json!(3));
}
