//! Parity with kirun-js `ExpressionNullishDataTest.ts`: `??` returns data as it is.
//!
//! kirun-js and kirun-py used to parse a `??` text result again when it held an operator character, so a date
//! '2026-11-20' became 1995 (fixed there 2026-11-07). The server never did; these cases keep it that way.

use kirun::runtime::expression::ExpressionEvaluator;
use kirun::runtime::FunctionExecutionParameters;
use kirun::{Map, Value};
use serde_json::json;

use crate::common::{args, assert_json_eq, fep};
use crate::steps_map;

fn parameters() -> FunctionExecutionParameters {
    let out = args! {
        "date" => "2026-11-20",
        "dmy" => "27/11/2026",
        "time" => "10:00",
        "phone" => "+919876543210",
        "dashed" => "98450-12345",
        "invoice" => "INV/2026-27/1/3",
        "path" => "Steps.s.output.other",
        "other" => "WRONG",
        "stored" => json!({ "location": { "expression": "'A' + 'B'" } }),
    };
    fep().set_arguments(Map::new()).set_steps(steps_map(vec![("s", "output", out)]))
}

fn eval(p: &FunctionExecutionParameters, expr: &str) -> Value {
    ExpressionEvaluator::new(expr).evaluate(p.get_values_map()).unwrap()
}

#[test]
fn left_side_data_is_returned_as_it_is() {
    let p = parameters();
    for (key, value) in [
        ("date", "2026-11-20"),
        ("dmy", "27/11/2026"),
        ("time", "10:00"),
        ("phone", "+919876543210"),
        ("dashed", "98450-12345"),
        ("invoice", "INV/2026-27/1/3"),
        ("path", "Steps.s.output.other"),
    ] {
        assert_json_eq!(eval(&p, &format!("Steps.s.output.{key} ?? ''")), json!(value));
    }
}

#[test]
fn fallbacks_are_returned_as_they_are() {
    let p = parameters();
    assert_json_eq!(eval(&p, "Steps.s.output.missing ?? Steps.s.output.date"), json!("2026-11-20"));
    assert_json_eq!(eval(&p, "Steps.s.output.missing ?? '-'"), json!("-"));
    assert_json_eq!(eval(&p, "Steps.s.output.missing ?? '1/2'"), json!("1/2"));
    assert_json_eq!(eval(&p, "'On ' + (Steps.s.output.date ?? '')"), json!("On 2026-11-20"));
}

#[test]
fn a_stored_expression_is_text_on_the_server() {
    // kirun-js and kirun-py evaluate this one (a page title's location.expression, browser only) to "AB".
    let p = parameters();
    assert_json_eq!(eval(&p, "Steps.s.output.stored.location.expression ?? ''"), json!("'A' + 'B'"));
}
