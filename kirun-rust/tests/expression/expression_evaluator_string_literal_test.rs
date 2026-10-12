//! Port of `ExpressionEvaluatorStringLiteralTest.ts`.

use std::sync::Arc;

use kirun::runtime::expression::tokenextractor::token_value_extractor::{retrieve_element_from, split_path};
use kirun::runtime::expression::tokenextractor::{TokenValueExtractor, ValuesMap};
use kirun::runtime::expression::{Expression, ExpressionEvaluator};
use kirun::runtime::tokenextractor::ArgumentsTokenValueExtractor;
use kirun::{Map, Result, Value};
use serde_json::json;

use crate::common::{args, assert_json_eq};
use crate::values_map;

/// Custom token extractor for `Steps.*` paths.
struct StepsTokenValueExtractor {
    data: Map<Value>,
}

impl TokenValueExtractor for StepsTokenValueExtractor {
    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        let mut parts = split_path(token);
        let mut key = parts[1].clone();
        let mut from_index = 2;
        if let Some(b_index) = key.find('[') {
            key = parts[1][..b_index].to_string();
            parts[1] = parts[1][b_index..].to_string();
            from_index = 1;
        }
        let element = self.data.get(&key).cloned().unwrap_or(Value::Null);
        retrieve_element_from(token, &parts, from_index, &element, values_map)
    }

    fn get_prefix(&self) -> &str {
        "Steps."
    }

    fn get_store(&self) -> Value {
        Value::Object(self.data.iter().map(|(k, v)| (k.clone(), v.clone())).collect())
    }
}

fn eval(vm: &ValuesMap, expr: &str) -> Value {
    ExpressionEvaluator::new(expr).evaluate(vm).unwrap()
}

fn simple_args() -> ValuesMap {
    let atv = ArgumentsTokenValueExtractor::new(args! { "a" => "kirun ", "b" => 2, "c" => true, "d" => 1.5 });
    values_map(vec![Arc::new(atv)])
}

#[test]
fn expression_with_string_literal_1() {
    let ex = Expression::new("'ki/run'+'ab'").unwrap();
    let ev = ExpressionEvaluator::from_expression(ex);
    assert_json_eq!(ev.evaluate(&ValuesMap::new()).unwrap(), json!("ki/runab"));

    let evt = ExpressionEvaluator::new("\"Steps.a");

    let vm = simple_args();

    assert!(evt.evaluate(&vm).is_err());

    assert_json_eq!(eval(&vm, "Arguments.a+'kiran'"), json!("kirun kiran"));
    assert_json_eq!(eval(&vm, "Arguments.b+'kiran'"), json!("2kiran"));
    assert_json_eq!(eval(&vm, "Arguments.c+'k\"ir\"an'"), json!("truek\"ir\"an"));
    assert_json_eq!(eval(&vm, "Arguments.b+\"'kir\" + ' an'"), json!("2'kir an"));
    assert_json_eq!(eval(&vm, "Arguments.a+'kiran'+ Arguments.b"), json!("kirun kiran2"));
}

#[test]
fn testing_for_length_expression_in_string() {
    let atv = ArgumentsTokenValueExtractor::new(args! {
        "a" => "kirun ",
        "b" => 2,
        "c" => json!({ "a": "hello", "b": "" }),
        "d" => 1.5,
    });
    let vm = values_map(vec![Arc::new(atv)]);

    assert_json_eq!(eval(&vm, "Arguments.a.length"), json!(6));
    assert!(ExpressionEvaluator::new("Arguments.b.length").evaluate(&vm).is_err());
    assert_json_eq!(eval(&vm, "Arguments.c.a.length * \"f\""), json!("fffff"));
    assert_json_eq!(eval(&vm, "Arguments.c.b.length ? \"f\" : \"t\""), json!("t"));
}

#[test]
fn expression_with_string_literal_2() {
    let vm = simple_args();

    assert_json_eq!(eval(&vm, "'a' * 10"), json!("aaaaaaaaaa"));
    assert_json_eq!(eval(&vm, "2.5*Arguments.a"), json!("kirun kirun kir"));
    assert_json_eq!(eval(&vm, "-0.5*Arguments.a"), json!("rik"));
    assert_json_eq!(eval(&vm, "'asdf' * -1"), json!("fdsa"));
    assert_json_eq!(eval(&vm, "'asdf' * 0"), json!(""));
    // JS toBe(undefined)
    assert_json_eq!(eval(&vm, "2.val"), Value::Null);
}

fn surendhar_args() -> ValuesMap {
    let json_obj = json!({ "greeting": "hello", "name": "surendhar" });
    let atv = ArgumentsTokenValueExtractor::new(args! {
        "a" => "surendhar ",
        "b" => 2,
        "c" => true,
        "d" => 1.5,
        "obj" => json_obj,
    });
    values_map(vec![Arc::new(atv)])
}

#[test]
fn testing_for_string_length_with_object() {
    let vm = surendhar_args();

    assert_json_eq!(eval(&vm, "Arguments.a.length"), json!(10));
    assert!(ExpressionEvaluator::new("Arguments.b.length").evaluate(&vm).is_err());
    assert_json_eq!(eval(&vm, "Arguments.obj.greeting.length * \"S\""), json!("SSSSS"));
    assert_json_eq!(eval(&vm, "Arguments.obj.greeting.length * \"SP\""), json!("SPSPSPSPSP"));
    assert_json_eq!(eval(&vm, "Arguments.obj.name.length ? \"fun\" : \"not Fun\""), json!("fun"));
}

#[test]
fn testing_for_string_length_with_square_brackets() {
    let vm = surendhar_args();

    assert_json_eq!(eval(&vm, "Arguments.a[\"length\"]"), json!(10));
    assert!(ExpressionEvaluator::new("Arguments.b[\"length\"]").evaluate(&vm).is_err());
    assert_json_eq!(eval(&vm, "Arguments.obj.greeting[\"length\"] * \"S\""), json!("SSSSS"));
    assert_json_eq!(eval(&vm, "Arguments.obj.greeting[\"length\"] * \"SP\""), json!("SPSPSPSPSP"));
    assert_json_eq!(eval(&vm, "Arguments.obj[\"greeting\"][\"length\"] * \"S\""), json!("SSSSS"));
    assert_json_eq!(eval(&vm, "Arguments.obj[\"greeting\"][\"length\"] * \"SP\""), json!("SPSPSPSPSP"));
    assert_json_eq!(eval(&vm, "Arguments.obj.name[\"length\"] ? \"fun\" : \"not Fun\""), json!("fun"));
}

#[test]
fn testing_string_literal_with_template_interpolation_double_curly_braces() {
    let mut steps_data = Map::<Value>::new();
    steps_data.insert("countLoop".to_string(), json!({ "iteration": { "index": 1 } }));
    steps_data.insert("index".to_string(), json!(5));
    let steps_atv: Arc<dyn TokenValueExtractor> = Arc::new(StepsTokenValueExtractor { data: steps_data });

    let args_atv: Arc<dyn TokenValueExtractor> =
        Arc::new(ArgumentsTokenValueExtractor::new(args! { "a" => "test", "b" => 10, "c" => 15 }));

    let mut vm = ValuesMap::new();
    vm.insert("Steps.".to_string(), steps_atv);
    vm.insert("Arguments.".to_string(), args_atv);

    // {{}} inside string literals is evaluated
    assert_json_eq!(
        eval(&vm, "'Page.appDefinitions.content[{{Steps.countLoop.iteration.index}}].stringValue'"),
        json!("Page.appDefinitions.content[1].stringValue")
    );

    assert_json_eq!(
        eval(&vm, "\"Page.appDefinitions.content[{{Steps.countLoop.iteration.index}}].stringValue\""),
        json!("Page.appDefinitions.content[1].stringValue")
    );

    assert_json_eq!(eval(&vm, "Arguments.a + ' - ' + 'Path: {{Steps.index}}'"), json!("test - Path: 5"));

    assert_json_eq!(eval(&vm, "'{{Arguments.a}} + {{Arguments.b}} = {{Arguments.c}}'"), json!("test + 10 = 15"));

    assert_json_eq!(eval(&vm, "'Result: {{Arguments.b + Arguments.c}}!'"), json!("Result: 25!"));

    assert_json_eq!(
        eval(&vm, "'Item {{Steps.countLoop.iteration.index}} of {{Arguments.c}}'"),
        json!("Item 1 of 15")
    );
}
