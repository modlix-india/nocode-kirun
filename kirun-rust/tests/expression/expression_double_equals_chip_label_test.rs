//! Port of `ExpressionDoubleEqualsChipLabelTest.ts`.
//!
//! KIRun's equality operator is `=`, NOT `==`. Writing `==` makes the lexer read two
//! consecutive `=` tokens, which leaves a dangling operator and throws.

use kirun::runtime::expression::ExpressionEvaluator;
use kirun::runtime::FunctionExecutionParameters;
use kirun::{Map, Value};
use serde_json::json;

use crate::common::{args, assert_json_eq, fep};
use crate::steps_map;

fn parameters() -> FunctionExecutionParameters {
    let in_map = args! {
        "status" => "PAID",
        "emptyStatus" => "",
        "value" => "PAID",
        "label" => "Paid",
    };
    fep().set_arguments(Map::new()).set_steps(steps_map(vec![("step1", "output", in_map)]))
}

fn eval(p: &FunctionExecutionParameters, expr: &str) -> Value {
    ExpressionEvaluator::new(expr).evaluate(p.get_values_map()).unwrap()
}

#[test]
fn double_equals_is_not_a_valid_kirun_operator_and_throws() {
    let p = parameters();
    let expr = ExpressionEvaluator::new(
        "((Steps.step1.output.status ?? '') == Steps.step1.output.value ? '● ' : '') + Steps.step1.output.label",
    );
    assert!(expr.evaluate(p.get_values_map()).is_err());
}

#[test]
fn single_equals_evaluates_the_chip_label_correctly() {
    let p = parameters();
    // active chip: status = value -> prefixed with the bullet
    assert_json_eq!(
        eval(
            &p,
            "((Steps.step1.output.status ?? '') = Steps.step1.output.value ? '● ' : '') + Steps.step1.output.label"
        ),
        json!("● Paid")
    );

    // inactive chip: emptyStatus (All) != value -> no prefix
    assert_json_eq!(
        eval(
            &p,
            "((Steps.step1.output.emptyStatus ?? '') = Steps.step1.output.value ? '● ' : '') + Steps.step1.output.label"
        ),
        json!("Paid")
    );
}

#[test]
fn expression_ending_in_a_string_literal_trailing_space_should_not_matter() {
    let p = parameters();
    assert_json_eq!(
        eval(&p, "(Steps.step1.output.status ?? '') = Steps.step1.output.value ? '● ' : ''"),
        json!("● ")
    );
    assert_json_eq!(
        eval(&p, "(Steps.step1.output.status ?? '') = Steps.step1.output.value ? '● ' : '' "),
        json!("● ")
    );
}

#[test]
fn bare_string_literals_at_end_of_expression_with_without_trailing_space() {
    let p = parameters();
    assert_json_eq!(eval(&p, "'hello'"), json!("hello"));
    assert_json_eq!(eval(&p, "'hello' "), json!("hello"));
    assert_json_eq!(eval(&p, "''"), json!(""));
    assert_json_eq!(eval(&p, "'' "), json!(""));
}

#[test]
fn template_expanding_to_a_quoted_literal_with_trailing_spaces_resolves_not_undefined() {
    let p = parameters();
    assert_json_eq!(
        eval(&p, "\"api/ui/personalization/{{Steps.step1.output.label}}/{{Steps.step1.output.value}}\"  "),
        json!("api/ui/personalization/Paid/PAID")
    );
}
