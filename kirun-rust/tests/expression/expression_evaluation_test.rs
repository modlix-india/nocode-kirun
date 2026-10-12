//! Port of `ExpressionEvaluationTest.ts`.

use std::sync::Arc;

use kirun::json::schema::Schema;
use kirun::runtime::expression::tokenextractor::token_value_extractor::retrieve_element_from;
use kirun::runtime::expression::tokenextractor::{TokenValueExtractor, ValuesMap};
use kirun::runtime::expression::ExpressionEvaluator;
use kirun::runtime::function_execution_parameters::ContextMap;
use kirun::runtime::tokenextractor::{ArgumentsTokenValueExtractor, OutputMapTokenValueExtractor};
use kirun::runtime::ContextElement;
use kirun::{shared, Map, Result, Value};
use serde_json::json;

use crate::common::{args, assert_json_eq, fep};
use crate::{is_truthy, steps_map, values_map};

fn eval(vm: &ValuesMap, expr: &str) -> Value {
    ExpressionEvaluator::new(expr).evaluate(vm).unwrap()
}

fn dot_split(token: &str) -> Vec<String> {
    token.split('.').map(String::from).collect()
}

fn student_obj() -> Value {
    let phone = json!({ "phone1": "1234", "phone2": "5678", "phone3": "5678" });
    let address = json!({
        "line1": "Flat 202, PVR Estates",
        "line2": "Nagvara",
        "city": "Benguluru",
        "pin": "560048",
        "phone": phone
    });
    json!({
        "studentName": "Kumar",
        "math": 20,
        "isStudent": true,
        "address": address,
        "array": [10, 20, 30],
        "num": 1
    })
}

#[test]
fn expression_test() {
    let in_map = args! { "name" => "Kiran", "obj" => student_obj() };
    let output = steps_map(vec![("step1", "output", in_map), ("loop", "iteration", args! { "index" => 2 })]);

    let mut context = ContextMap::new();
    context.insert(
        "a".to_string(),
        ContextElement::new(Some(Schema::of_array("numbers", vec![Schema::of_number("number")])), json!([1, 2])),
    );

    let parameters = fep().set_arguments(Map::new()).set_context(context).set_steps(output);
    let vm = parameters.get_values_map();

    assert_json_eq!(
        eval(vm, "Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]"),
        json!(3)
    );

    assert_json_eq!(eval(vm, "3 + 7"), json!(10));
    assert_json_eq!(eval(vm, "\"asdf\"+333"), json!("asdf333"));
    assert_json_eq!(eval(vm, "34 >> 2 = 8 "), json!(true));
    assert_json_eq!(eval(vm, "10*11+12*13*14/7"), json!(422));

    // JS toBeUndefined
    assert_json_eq!(eval(vm, "Steps.step1.output.name1"), Value::Null);

    assert_json_eq!(eval(vm, "\"Kiran\" = Steps.step1.output.name "), json!(true));

    assert_json_eq!(eval(vm, "null = Steps.step1.output.name1 "), json!(true));

    assert_json_eq!(eval(vm, "Steps.step1.output.obj.address.phone.phone2"), json!("5678"));

    assert_json_eq!(
        eval(vm, "Steps.step1.output.obj.address.phone.phone2 = Steps.step1.output.obj.address.phone.phone2 "),
        json!(true)
    );

    assert_json_eq!(
        eval(vm, "Steps.step1.output.obj.address.phone.phone2 != Steps.step1.output.address.obj.phone.phone1 "),
        json!(true)
    );

    assert_json_eq!(eval(vm, "Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2"), json!(32));

    assert_json_eq!(
        eval(
            vm,
            "Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]"
        ),
        json!(60)
    );

    assert_json_eq!(
        eval(
            vm,
            "Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]"
        ),
        json!(60)
    );

    assert_json_eq!(eval(vm, "Steps.step1.output.obj.array[-Steps.step1.output.obj.num + 3]+2"), json!(32));

    // JS toBe(17.3533): exact IEEE double result of 2.43*4.22+7.0987, same in Rust.
    assert_json_eq!(eval(vm, "2.43*4.22+7.0987"), json!(17.3533));
}

#[test]
fn expression_evaluation_with_square_access_bracket() {
    let in_map = args! { "name" => "Kiran", "obj" => student_obj() };
    let output = steps_map(vec![("step1", "output", in_map)]);

    let parameters = fep().set_arguments(Map::new()).set_context(ContextMap::new()).set_steps(output);
    let vm = parameters.get_values_map();

    assert_json_eq!(
        eval(vm, "Steps.step1.output.obj.phone.phone2 = Steps.step1.output.obj[\"phone\"][\"phone2\"]"),
        json!(true)
    );

    assert_json_eq!(
        eval(vm, "Steps.step1.output.obj[\"phone\"][\"phone2\"] = Steps.step1.output.obj[\"phone\"][\"phone2\"]"),
        json!(true)
    );

    assert_json_eq!(
        eval(
            vm,
            "Steps.step1.output.obj[\"address\"].phone[\"phone2\"] != Steps.step1.output.address.obj.phone.phone1"
        ),
        json!(true)
    );

    assert_json_eq!(
        eval(
            vm,
            "Steps.step1.output.obj[\"address\"][\"phone\"][\"phone2\"] != Steps.step1.output.address.obj.phone.phone1"
        ),
        json!(true)
    );

    assert_json_eq!(
        eval(
            vm,
            "Steps.step1.output.obj[\"address\"][\"phone\"][\"phone2\"] != Steps.step1.output[\"address\"][\"phone\"][\"phone2\"]"
        ),
        json!(true)
    );

    assert_json_eq!(
        eval(vm, "Steps.step1.output.obj.array[Steps.step1.output.obj[\"num\"] + 1] + 2"),
        json!(32)
    );

    assert_json_eq!(
        eval(
            vm,
            "Steps.step1.output.obj.array[Steps.step1.output.obj[\"num\"] + 1] + Steps.step1.output.obj.array[Steps.step1.output.obj[\"num\"] + 1]"
        ),
        json!(60)
    );

    assert_json_eq!(
        eval(
            vm,
            "Steps.step1.output.obj.array[Steps.step1.output.obj.num + 1] + Steps.step1.output.obj.array[Steps.step1.output.obj.num + 1]"
        ),
        json!(60)
    );
}

#[test]
fn expression_evaluation_deep_tests() {
    let atv = ArgumentsTokenValueExtractor::new(args! {
        "a" => "kirun ",
        "b" => 2,
        "c" => json!({ "a": 2, "b": [true, false], "c": { "x": "kiran" } }),
        "d" => json!({ "a": 2, "b": [true, false], "c": { "x": "kiran" } }),
    });
    let vm = values_map(vec![Arc::new(atv)]);

    assert!(!is_truthy(&eval(&vm, "Arguments.a = Arugments.b")));
    assert!(is_truthy(&eval(&vm, "Arguments.c = Arguments.d")));
    assert!(is_truthy(&eval(&vm, "Arguments.e = null")));
    assert!(!is_truthy(&eval(&vm, "Arguments.e != null")));
    assert!(is_truthy(&eval(&vm, "Arguments.e = false")));
    assert!(is_truthy(&eval(&vm, "Arguments.c != null")));
}

#[test]
fn expression_evaluation_nullish_coalescing() {
    let atv = ArgumentsTokenValueExtractor::new(args! {
        "a" => "kirun ",
        "b" => 2,
        "b1" => 4,
        "c" => json!({ "a": 2, "b": [true, false], "c": { "x": "kiran" } }),
        "d" => json!({ "a": 2, "b": [true, false], "c": { "x": "kiran" } }),
    });
    let vm = values_map(vec![Arc::new(atv)]);

    assert_json_eq!(eval(&vm, "(Arguments.e ?? Arguments.b ?? Arguments.b1) + 4"), json!(6));
    assert_json_eq!(eval(&vm, "(Arguments.e ?? Arguments.b2 ?? Arguments.b1) + 4"), json!(8));
}

#[test]
fn expression_evaluation_nesting_expression() {
    let atv = ArgumentsTokenValueExtractor::new(args! {
        "a" => "kirun ",
        "b" => 2,
        "b1" => 4,
        "b2" => 4,
        "c" => json!({ "a": 2, "b": [true, false], "c": { "x": "Arguments.b2" } }),
        "d" => "c",
    });
    let vm = values_map(vec![Arc::new(atv)]);

    assert_json_eq!(eval(&vm, "Arguments.{{Arguments.d}}.a + {{Arguments.{{Arguments.d}}.c.x}}"), json!(6));

    assert_json_eq!(
        eval(&vm, "'There are {{{{Arguments.{{Arguments.d}}.c.x}}}} boys in the class room...' * Arguments.b"),
        json!("There are 4 boys in the class room...There are 4 boys in the class room...")
    );
}

#[test]
fn partial_path_evaluation() {
    let atv = ArgumentsTokenValueExtractor::new(args! {
        "a" => "kirun ",
        "b" => 1,
        "b1" => 4,
        "b2" => 4,
        "c" => json!({ "a": 0, "b": [true, false], "c": { "x": "Arguments.b2" }, "keys": ["a", "e", { "val": 5 }] }),
        "d" => "c",
        "e" => json!([{ "name": "Kiran", "num": 1 }, { "name": "Good", "num": 2 }]),
    });
    let vm = values_map(vec![Arc::new(atv)]);

    assert_json_eq!(eval(&vm, "Arguments.c.keys[2].val + 3"), json!(8));
    assert_json_eq!(eval(&vm, "(Arguments.f ?? Arguments.e)[1+1-1].num"), json!(2));
}

// The JS file has two commented-out tests ('Expression with consecutive negative operators',
// 'Expression with multiple coalesce operator'); they are not ported.

#[test]
fn expression_with_logical_operators_and_all_value_types_including_object() {
    // JS `undefined` entries become Value::Null. In JS `{ e: undefined }` still has 5 keys and
    // `[.., undefined]` still has 5 elements; serde_json keeps both as null, so the
    // `.length` assertions below are unchanged.
    let atv = ArgumentsTokenValueExtractor::new(args! {
        "string" => "kirun ",
        "stringEmpty" => "",
        "number" => 122.2,
        "number0" => 0,
        "booleanTrue" => true,
        "booleanFalse" => false,
        "null" => Value::Null,
        "undefined" => Value::Null,
        "object" => json!({ "a": 1, "b": "2", "c": true, "d": null, "e": null }),
        "array" => json!([1, "2", true, null, null]),
        "array2" => json!([1, "2", true, null, null]),
        "emptyArray" => json!([]),
    });
    let vm = values_map(vec![Arc::new(atv)]);

    assert!(is_truthy(&eval(&vm, "not not Arguments.object")));
    assert!(is_truthy(&eval(&vm, "not not Arguments.stringEmpty")));
    assert!(is_truthy(&eval(&vm, "not not Arguments.number")));
    assert!(!is_truthy(&eval(&vm, "not not Arguments.number0")));
    assert!(is_truthy(&eval(&vm, "not not Arguments.booleanTrue")));
    assert!(!is_truthy(&eval(&vm, "not not Arguments.booleanFalse")));
    assert!(!is_truthy(&eval(&vm, "not not Arguments.null")));
    assert!(!is_truthy(&eval(&vm, "not not Arguments.undefined")));
    assert!(is_truthy(&eval(&vm, "not not Arguments.array")));
    assert!(is_truthy(&eval(&vm, "not not Arguments.emptyArray")));

    assert!(!is_truthy(&eval(&vm, "Arguments.object = true")));
    assert!(is_truthy(&eval(&vm, "Arguments.object != true")));
    assert!(!is_truthy(&eval(&vm, "Arguments.stringEmpty = true")));
    assert!(is_truthy(&eval(&vm, "Arguments.stringEmpty != false")));
    assert!(!is_truthy(&eval(&vm, "Arguments.number0 = true")));
    assert!(!is_truthy(&eval(&vm, "Arguments.number0 = false")));

    assert_json_eq!(eval(&vm, "Arguments.array.length"), json!(5));
    assert_json_eq!(eval(&vm, "Arguments.object.length"), json!(5));

    assert!(is_truthy(&eval(&vm, "Arguments.object and Arguments.array")));
    assert!(is_truthy(&eval(&vm, "Arguments.object or Arguments.null")));
    assert!(!is_truthy(&eval(&vm, "Arguments.object and Arguments.null")));

    assert_json_eq!(eval(&vm, "Arguments.object ? 3 : 4"), json!(3));
    assert_json_eq!(eval(&vm, "not Arguments.object ? 3 : 4"), json!(4));

    assert!(is_truthy(&eval(&vm, "Arguments.array = Arguments.array2")));

    assert_json_eq!(eval(&vm, "Arguments.number0 ? 3 : 4"), json!(4));
}

struct TestTokenValueExtractor {
    store: Value,
}

impl TestTokenValueExtractor {
    fn new(store: Value) -> Arc<dyn TokenValueExtractor> {
        Arc::new(Self { store })
    }
}

impl TokenValueExtractor for TestTokenValueExtractor {
    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        retrieve_element_from(token, &dot_split(token), 1, &self.store, values_map)
    }
    fn get_prefix(&self) -> &str {
        "Test."
    }
    fn get_store(&self) -> Value {
        self.store.clone()
    }
}

#[test]
fn full_store_test() {
    let atv = ArgumentsTokenValueExtractor::new(args! {
        "a" => "kirun ",
        "b" => 2,
        "c" => json!({ "a": 2, "b": [true, false], "c": { "x": "kiran" } }),
        "d" => json!({ "a": 2, "b": [true, false], "c": { "x": "kiran" } }),
    });

    // JS toMatchObject: the store is exactly these keys here.
    assert_json_eq!(
        atv.get_store(),
        json!({
            "a": "kirun ",
            "b": 2,
            "c": { "a": 2, "b": [true, false], "c": { "x": "kiran" } },
            "d": { "a": 2, "b": [true, false], "c": { "x": "kiran" } }
        })
    );

    let obv = OutputMapTokenValueExtractor::new(shared(steps_map(vec![
        ("step1", "output", args! { "name" => "Kiran" }),
        ("loop", "iteration", args! { "index" => 2 }),
    ])));

    assert_json_eq!(
        obv.get_store(),
        json!({
            "step1": { "output": { "name": "Kiran" } },
            "loop": { "iteration": { "index": 2 } }
        })
    );

    let ttv = TestTokenValueExtractor::new(json!({
        "a": "kirun",
        "b": 2,
        "c": { "a": 2, "b": [true, false], "c": { "x": "kiran" } },
        "d": { "a": 2, "b": [true, false], "c": { "x": "kiran" } }
    }));

    assert_json_eq!(eval(&values_map(vec![ttv]), "Test.a"), json!("kirun"));

    let ttv = TestTokenValueExtractor::new(json!(20));

    assert_json_eq!(eval(&values_map(vec![ttv.clone()]), "Test"), json!(20));

    // JS toStrictEqual(true)
    assert_eq!(eval(&values_map(vec![ttv]), "Test > 10"), json!(true));
}

#[test]
fn full_store_test_when_undefined() {
    // JS store `undefined` -> Value::Null
    let ttv = TestTokenValueExtractor::new(Value::Null);
    // JS toBeUndefined
    assert_json_eq!(eval(&values_map(vec![ttv]), "Test"), Value::Null);
}

#[test]
fn index_retrieval() {
    let ttv = TestTokenValueExtractor::new(json!({
        "a": "kirun",
        "b": 2,
        "c": { "a": 2, "b": [true, false], "c": { "x": "kiran" } },
        "d": { "a": 2, "b": [true, false], "c": { "x": "kiran" } }
    }));
    let vm = values_map(vec![ttv]);

    assert_json_eq!(eval(&vm, "Test.a"), json!("kirun"));
    assert_json_eq!(eval(&vm, "Test.b.__index"), json!("b"));
    assert_json_eq!(eval(&vm, "Test.c.c.x.__index"), json!("x"));
    assert_json_eq!(eval(&vm, "Test.c.b[1].__index"), json!(1));
    assert_json_eq!(eval(&vm, "Test.x.c.__index"), json!("c"));
}

#[test]
fn backslash_escape() {
    // JS source "'\\maza'" is the 7-char expression '\maza'.
    assert_json_eq!(eval(&ValuesMap::new(), "'\\maza'"), json!("\\maza"));
}

#[test]
fn ternary_expression_with_display_value_equality_check() {
    let run = |display_value: &str, expr: &str| -> Value {
        let ttv = TestTokenValueExtractor::new(json!({ "displayValue": display_value }));
        eval(&values_map(vec![ttv]), expr)
    };

    let e = "(Test.displayValue = '0') ? '1' : (Test.displayValue + '1')";
    assert_json_eq!(run("0", e), json!("1"));
    assert_json_eq!(run("1", e), json!("11"));
    assert_json_eq!(run("5", e), json!("51"));
    assert_json_eq!(run("10", e), json!("101"));

    assert_json_eq!(run("1", "((Test.displayValue = '0') ? '1' : (Test.displayValue + '1'))"), json!("11"));
    assert_json_eq!(run("1", "(((Test.displayValue = '0') ? '1' : (Test.displayValue + '1')))"), json!("11"));
    assert_json_eq!(
        run("1", "(((Test.displayValue = '0')) ? '1' : (((Test.displayValue) + '1')))"),
        json!("11")
    );
}

struct StoreTokenValueExtractor {
    store: Value,
}

impl TokenValueExtractor for StoreTokenValueExtractor {
    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        retrieve_element_from(token, &dot_split(token), 1, &self.store, values_map)
    }
    fn get_prefix(&self) -> &str {
        "Store."
    }
    fn get_store(&self) -> Value {
        self.store.clone()
    }
}

#[test]
fn string_vs_number_equality_with_nested_expression_substitution() {
    let stv = StoreTokenValueExtractor { store: json!({ "strNumber": "413", "number": 413 }) };
    let vm = values_map(vec![Arc::new(stv)]);

    // "413" (string) != 413 (number): different types
    assert_json_eq!(eval(&vm, "Store.strNumber = Store.number"), json!(false));

    // {{Store.strNumber}} is substituted textually, giving "413 = Store.number"
    assert_json_eq!(eval(&vm, "{{Store.strNumber}} = Store.number"), json!(true));

    assert_json_eq!(eval(&vm, "Store.strNumber"), json!("413"));
    assert_json_eq!(eval(&vm, "Store.number"), json!(413));
    assert_json_eq!(eval(&vm, "Store.strNumber = '413'"), json!(true));
    assert_json_eq!(eval(&vm, "Store.number = 413"), json!(true));
}

#[test]
fn unary_minus_operator() {
    let empty = ValuesMap::new();
    assert_json_eq!(eval(&empty, "-5"), json!(-5));

    let ttv = TestTokenValueExtractor::new(json!({ "value": 10 }));
    assert_json_eq!(eval(&values_map(vec![ttv]), "0 - Test.value"), json!(-10));

    let ttv = TestTokenValueExtractor::new(json!({ "a": 5, "b": 3 }));
    assert_json_eq!(eval(&values_map(vec![ttv]), "Test.a + (0 - Test.b)"), json!(2));

    let ttv = TestTokenValueExtractor::new(json!({ "a": 5 }));
    assert_json_eq!(eval(&values_map(vec![ttv]), "Test.a * -1"), json!(-5));

    assert_json_eq!(eval(&empty, "(-5)"), json!(-5));

    assert_json_eq!(eval(&empty, "10 + (-5) * 2"), json!(0));
}
