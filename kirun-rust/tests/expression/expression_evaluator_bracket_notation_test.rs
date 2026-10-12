//! Port of `ExpressionEvaluatorBracketNotationTest.ts` (describe 'ExpressionEvaluator with Bracket Notation').

use std::sync::Arc;

use kirun::runtime::expression::tokenextractor::token_value_extractor::{retrieve_element_from, split_path};
use kirun::runtime::expression::tokenextractor::{TokenValueExtractor, ValuesMap};
use kirun::runtime::expression::ExpressionEvaluator;
use kirun::{Result, Value};
use serde_json::json;

use crate::common::assert_json_eq;

struct TestExtractor {
    data: Value,
}

impl TokenValueExtractor for TestExtractor {
    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        let path = &token[self.get_prefix().len()..];
        let parts = split_path(path);
        retrieve_element_from(token, &parts, 0, &self.data, values_map)
    }

    fn get_prefix(&self) -> &str {
        "Context."
    }

    fn get_store(&self) -> Value {
        self.data.clone()
    }
}

/// The outer `beforeEach` test data.
fn test_data() -> Value {
    json!({
        "obj": {
            "mail.props.port": 587,
            "mail.props.host": "smtp.example.com",
            "api.key.secret": "secret123",
            "simple": "value",
            "count": 100
        },
        "arr": [10, 20, 30, 40, 50],
        "nested": {
            "field.with.dots": "nestedValue",
            "regular": "regularValue"
        }
    })
}

/// The inner `beforeEach` of describe 'Keys containing operator characters'.
fn operator_key_data() -> Value {
    let mut d = test_data();
    let obj = &mut d["obj"];
    obj["key-with-hyphen"] = json!("hyphenValue");
    obj["key with space"] = json!("spaceValue");
    obj["key+plus"] = json!("plusValue");
    obj["key:colon"] = json!("colonValue");
    obj["key/slash"] = json!("slashValue");
    obj["a-b"] = json!("literalAMinusB");
    obj["a"] = json!(10);
    obj["b"] = json!(3);
    d["metas"] = json!({ "m1": { "http-equiv": "X-UA-Compatible", "content": "IE=edge" } });
    d["k"] = json!("m1");
    d
}

/// JS `new Map([['Context.', new TestExtractor(data)]])`.
fn evaluator_map(data: Value) -> ValuesMap {
    let mut m = ValuesMap::new();
    m.insert("Context.".to_string(), Arc::new(TestExtractor { data }) as Arc<dyn TokenValueExtractor>);
    m
}

fn eval(vm: &ValuesMap, expr: &str) -> Value {
    ExpressionEvaluator::new(expr).evaluate(vm).unwrap()
}

fn eval_default(expr: &str) -> Value {
    eval(&evaluator_map(test_data()), expr)
}

// ---- Basic bracket notation with dotted keys

#[test]
fn should_access_property_with_double_quotes() {
    assert_json_eq!(eval_default("Context.obj[\"mail.props.port\"]"), json!(587));
}

#[test]
fn should_access_property_with_single_quotes() {
    assert_json_eq!(eval_default("Context.obj['mail.props.host']"), json!("smtp.example.com"));
}

#[test]
fn should_access_nested_property_with_dotted_key() {
    assert_json_eq!(eval_default("Context.nested['field.with.dots']"), json!("nestedValue"));
}

// ---- Comparison operators with bracket notation

#[test]
fn should_work_with_equality_operator() {
    assert_eq!(eval_default("Context.obj[\"mail.props.port\"] = 587"), json!(true));
}

#[test]
fn should_work_with_not_equal_operator() {
    assert_eq!(eval_default("Context.obj[\"mail.props.port\"] != 500"), json!(true));
}

#[test]
fn should_work_with_greater_than_operator() {
    assert_eq!(eval_default("Context.obj[\"mail.props.port\"] > 500"), json!(true));
}

#[test]
fn should_work_with_greater_than_or_equal_operator() {
    assert_eq!(eval_default("Context.obj[\"mail.props.port\"] >= 587"), json!(true));
}

#[test]
fn should_work_with_less_than_operator() {
    assert_eq!(eval_default("Context.obj[\"mail.props.port\"] < 600"), json!(true));
}

#[test]
fn should_work_with_less_than_or_equal_operator() {
    assert_eq!(eval_default("Context.obj[\"mail.props.port\"] <= 587"), json!(true));
}

// ---- Arithmetic operators with bracket notation

#[test]
fn should_work_with_addition() {
    assert_json_eq!(eval_default("Context.obj[\"mail.props.port\"] + 13"), json!(600));
}

#[test]
fn should_work_with_subtraction() {
    assert_json_eq!(eval_default("Context.obj[\"mail.props.port\"] - 87"), json!(500));
}

#[test]
fn should_work_with_multiplication() {
    assert_json_eq!(eval_default("Context.obj[\"count\"] * 2"), json!(200));
}

#[test]
fn should_work_with_division() {
    assert_json_eq!(eval_default("Context.obj[\"count\"] / 4"), json!(25));
}

// ---- Ternary operator with bracket notation

#[test]
fn should_work_with_ternary_operator() {
    assert_json_eq!(eval_default("Context.obj[\"mail.props.port\"] > 500 ? \"high\" : \"low\""), json!("high"));
}

#[test]
fn should_return_false_branch_of_ternary() {
    assert_json_eq!(eval_default("Context.obj[\"mail.props.port\"] < 500 ? \"high\" : \"low\""), json!("low"));
}

// ---- Logical operators with bracket notation

#[test]
fn should_work_with_logical_and() {
    assert_eq!(
        eval_default("Context.obj[\"mail.props.port\"] > 500 and Context.obj[\"count\"] = 100"),
        json!(true)
    );
}

#[test]
fn should_work_with_logical_or() {
    assert_eq!(
        eval_default("Context.obj[\"mail.props.port\"] < 500 or Context.obj[\"count\"] = 100"),
        json!(true)
    );
}

#[test]
fn should_work_with_logical_not() {
    assert_eq!(eval_default("not Context.obj[\"mail.props.port\"] < 500"), json!(true));
}

// ---- Mixed bracket and dot notation

#[test]
fn should_work_with_bracket_notation_followed_by_dot_notation() {
    let mut data = test_data();
    data["obj"]["mail.props.port"] = json!({ "subfield": "subvalue" });
    assert_json_eq!(
        eval(&evaluator_map(data), "Context.obj[\"mail.props.port\"].subfield"),
        json!("subvalue")
    );
}

#[test]
fn should_work_with_dot_notation_followed_by_bracket_notation() {
    assert_json_eq!(eval_default("Context.nested['field.with.dots']"), json!("nestedValue"));
}

// ---- Array bracket notation (pre-existing functionality)

#[test]
fn should_work_with_array_index_access() {
    assert_json_eq!(eval_default("Context.arr[0]"), json!(10));
}

#[test]
fn should_work_with_array_index_and_comparison() {
    assert_eq!(eval_default("Context.arr[0] = 10"), json!(true));
}

#[test]
fn should_work_with_array_index_and_arithmetic() {
    assert_json_eq!(eval_default("Context.arr[1] + Context.arr[2]"), json!(50));
}

// ---- Complex expressions

#[test]
fn should_work_with_multiple_bracket_notations_in_one_expression() {
    assert_json_eq!(eval_default("Context.obj[\"mail.props.port\"] + Context.obj[\"count\"]"), json!(687));
}

#[test]
fn should_work_with_bracket_notation_in_nested_expression() {
    assert_eq!(
        eval_default("(Context.obj[\"mail.props.port\"] > 500) and (Context.obj[\"count\"] < 200)"),
        json!(true)
    );
}

#[test]
fn should_work_with_string_concatenation() {
    assert_json_eq!(eval_default("Context.obj[\"mail.props.host\"] + \":587\""), json!("smtp.example.com:587"));
}

// ---- Keys containing operator characters

#[test]
fn hyphen_in_a_double_quoted_key() {
    let vm = evaluator_map(operator_key_data());
    assert_json_eq!(eval(&vm, "Context.obj[\"key-with-hyphen\"]"), json!("hyphenValue"));
}

#[test]
fn hyphen_in_a_single_quoted_key() {
    let vm = evaluator_map(operator_key_data());
    assert_json_eq!(eval(&vm, "Context.obj['key-with-hyphen']"), json!("hyphenValue"));
}

#[test]
fn http_equiv_the_app_definition_meta_attribute() {
    let vm = evaluator_map(operator_key_data());
    assert_json_eq!(eval(&vm, "Context.metas.m1[\"http-equiv\"]"), json!("X-UA-Compatible"));
}

#[test]
fn space_plus_colon_and_slash_in_a_key() {
    let vm = evaluator_map(operator_key_data());
    assert_json_eq!(eval(&vm, "Context.obj[\"key with space\"]"), json!("spaceValue"));
    assert_json_eq!(eval(&vm, "Context.obj[\"key+plus\"]"), json!("plusValue"));
    assert_json_eq!(eval(&vm, "Context.obj[\"key:colon\"]"), json!("colonValue"));
    assert_json_eq!(eval(&vm, "Context.obj[\"key/slash\"]"), json!("slashValue"));
}

#[test]
fn a_quoted_key_wins_over_the_arithmetic_it_looks_like() {
    let vm = evaluator_map(operator_key_data());
    assert_json_eq!(eval(&vm, "Context.obj[\"a-b\"]"), json!("literalAMinusB"));
}

#[test]
fn the_same_key_unquoted_is_arithmetic_and_throws() {
    let vm = evaluator_map(operator_key_data());
    assert!(ExpressionEvaluator::new("Context.obj.key-with-hyphen").evaluate(&vm).is_err());
    assert!(ExpressionEvaluator::new("Context.metas.m1.http-equiv").evaluate(&vm).is_err());
}

#[test]
fn chained_brackets_both_keys_hyphenated_or_dotted() {
    let vm = evaluator_map(operator_key_data());
    assert_json_eq!(eval(&vm, "Context.metas['m1']['http-equiv']"), json!("X-UA-Compatible"));
}

#[test]
fn a_variable_key_resolves_including_into_a_hyphenated_leaf() {
    let vm = evaluator_map(operator_key_data());
    assert_json_eq!(eval(&vm, "Context.metas[Context.k].content"), json!("IE=edge"));
    assert_json_eq!(eval(&vm, "Context.metas[Context.k]['http-equiv']"), json!("X-UA-Compatible"));
}

/// LIMITATION, asserted so a fix shows up as a failing test: a bracket segment is a literal
/// or a token path, never an expression; a concatenation inside brackets silently misses.
#[test]
fn an_expression_inside_brackets_is_not_evaluated_documented_gap() {
    let vm = evaluator_map(operator_key_data());
    // JS toBeUndefined
    assert_json_eq!(eval(&vm, "Context.metas['m' + '1'].content"), Value::Null);
}

// ---- Root level bracket access

#[test]
fn numeric_index_directly_on_the_extractor_root() {
    let vm = evaluator_map(json!(["zero", "one", "two"]));
    assert_json_eq!(eval(&vm, "Context[0]"), json!("zero"));
    assert_json_eq!(eval(&vm, "Context[2]"), json!("two"));
}

#[test]
fn the_dotted_form_keeps_working() {
    let vm = evaluator_map(json!(["zero", "one", "two"]));
    assert_json_eq!(eval(&vm, "Context.0"), json!("zero"));
}

#[test]
fn quoted_key_directly_on_the_extractor_root() {
    let vm = evaluator_map(json!({ "a.b": "dotted", "plain": "p" }));
    assert_json_eq!(eval(&vm, "Context[\"a.b\"]"), json!("dotted"));
    assert_json_eq!(eval(&vm, "Context[\"plain\"]"), json!("p"));
    assert_json_eq!(eval(&vm, "Context['plain']"), json!("p"));
}

#[test]
fn a_bracket_after_a_dotted_path_is_untouched() {
    let vm = evaluator_map(json!({ "arr": [1, 2, 3] }));
    assert_json_eq!(eval(&vm, "Context.arr[1]"), json!(2));
}

// ---- Edge cases

#[test]
fn should_handle_property_with_multiple_dots() {
    let mut data = test_data();
    data["obj"]["a.b.c.d"] = json!("deepValue");
    assert_json_eq!(eval(&evaluator_map(data), "Context.obj[\"a.b.c.d\"]"), json!("deepValue"));
}

#[test]
fn should_handle_empty_string_key_though_unusual() {
    let mut data = test_data();
    data["obj"][""] = json!("emptyKey");
    assert_json_eq!(eval(&evaluator_map(data), "Context.obj[\"\"]"), json!("emptyKey"));
}

#[test]
fn should_handle_key_with_special_characters() {
    let mut data = test_data();
    data["obj"]["key@#$%"] = json!("specialValue");
    assert_json_eq!(eval(&evaluator_map(data), "Context.obj[\"key@#$%\"]"), json!("specialValue"));
}
