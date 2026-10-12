//! Port of `ExpressionEqualityTest.ts`.

use kirun::runtime::expression::ExpressionEvaluator;
use kirun::runtime::FunctionExecutionParameters;
use kirun::{Map, Value};
use serde_json::json;

use crate::common::{args, assert_json_eq, fep};
use crate::steps_map;

fn parameters() -> FunctionExecutionParameters {
    // JS also has `undefinedValue: undefined`, which JSON drops from the object; Rust has no
    // undefined, so it is left out (a missing key reads as Value::Null either way).
    let obj = json!({
        "number": 20,
        "zero": 0,
        "booleanTrue": true,
        "booleanFalse": false,
        "string": "Hello",
        "emptyString": "",
        "nullValue": null,
        "emptyObject": {},
        "emptyArray": []
    });
    let in_map = args! { "name" => "Kiran", "obj" => obj };
    fep().set_arguments(Map::new()).set_steps(steps_map(vec![("step1", "output", in_map)]))
}

fn eval(p: &FunctionExecutionParameters, expr: &str) -> Value {
    ExpressionEvaluator::new(expr).evaluate(p.get_values_map()).unwrap()
}

#[test]
fn expression_equality_test() {
    let p = parameters();

    assert_json_eq!(eval(&p, "Steps.step1.output.obj.number = Steps.step1.output.obj.zero"), json!(false));

    assert_json_eq!(
        eval(&p, "Steps.step1.output.obj.booleanFalse = (not Steps.step1.output.obj.number)"),
        json!(true)
    );

    assert_json_eq!(
        eval(&p, "Steps.step1.output.obj.booleanFalse = (not Steps.step1.output.obj.emptyString)"),
        json!(true)
    );

    assert_json_eq!(
        eval(&p, "Steps.step1.output.obj.booleanTrue = (not Steps.step1.output.obj.zero)"),
        json!(true)
    );

    assert_json_eq!(eval(&p, "Steps.step1.output.obj.emptyString = ''"), json!(true));

    assert_json_eq!(eval(&p, "Steps.step1.output.obj.emptyString != ''"), json!(false));

    assert_json_eq!(eval(&p, "Steps.step1.output.obj.string != ''"), json!(true));

    assert_json_eq!(eval(&p, "Steps.step1.output.obj.string = ''"), json!(false));
}
