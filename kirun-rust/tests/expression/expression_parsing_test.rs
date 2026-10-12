//! Port of `ExpressionParsingTest.ts` (describes 'Expression Parsing Tests' and
//! 'Original Expression Parsing Tests'). Long JS test names are shortened to their leading
//! words; the full JS name is in each doc comment.

use std::sync::Arc;

use kirun::runtime::expression::tokenextractor::token_value_extractor::retrieve_element_from;
use kirun::runtime::expression::tokenextractor::{TokenValueExtractor, ValuesMap};
use kirun::runtime::expression::{Expression, ExpressionEvaluator};
use kirun::{Result, Value};
use serde_json::json;

use crate::common::assert_json_eq;
use crate::values_map;

struct TestTokenValueExtractor {
    prefix: String,
    store: Value,
}

impl TestTokenValueExtractor {
    fn new(prefix: &str, store: Value) -> Arc<dyn TokenValueExtractor> {
        Arc::new(Self { prefix: prefix.to_string(), store })
    }
}

impl TokenValueExtractor for TestTokenValueExtractor {
    fn get_value_internal(&self, token: &str, values_map: Option<&ValuesMap>) -> Result<Value> {
        let parts: Vec<String> = token.split('.').map(String::from).collect();
        retrieve_element_from(token, &parts, 1, &self.store, values_map)
    }
    fn get_prefix(&self) -> &str {
        &self.prefix
    }
    fn get_store(&self) -> Value {
        self.store.clone()
    }
}

fn eval(vm: &ValuesMap, expr: &str) -> Value {
    ExpressionEvaluator::new(expr).evaluate(vm).unwrap()
}

fn one(prefix: &str, store: Value) -> ValuesMap {
    values_map(vec![TestTokenValueExtractor::new(prefix, store)])
}

// ================= describe 'Expression Parsing Tests'

/// 'Parse expression with multiplication and nested template: Steps.floorWeekOne.output.value * {{Page.secondsInDay}}'
#[test]
fn parse_expression_with_multiplication_and_nested_template() {
    let vm = values_map(vec![
        TestTokenValueExtractor::new("Steps.", json!({ "floorWeekOne": { "output": { "value": 7 } } })),
        TestTokenValueExtractor::new("Page.", json!({ "secondsInDay": 86400 })),
    ]);

    let expr = Expression::new("Steps.floorWeekOne.output.value * {{Page.secondsInDay}}").unwrap();
    assert!(!expr.get_operations().is_empty());

    assert_json_eq!(eval(&vm, "Steps.floorWeekOne.output.value * {{Page.secondsInDay}}"), json!(7 * 86400));
}

/// "Parse expression with nullish coalescing: Parent.projectInfo.projectType?? '-'"
#[test]
fn parse_expression_with_nullish_coalescing() {
    let vm = one("Parent.", json!({ "projectInfo": { "projectType": "Commercial" } }));

    assert!(Expression::new("Parent.projectInfo.projectType?? '-'").is_ok());

    let ev = ExpressionEvaluator::new("Parent.projectInfo.projectType ?? '-'");
    assert_json_eq!(ev.evaluate(&vm).unwrap(), json!("Commercial"));

    let ev1 = ExpressionEvaluator::new("Parent.projectInfo.projectType1?? '-'");
    assert_json_eq!(ev1.evaluate(&vm).unwrap(), json!("-"));

    let vm_null = one("Parent.", json!({ "projectInfo": { "projectType": null } }));
    assert_json_eq!(ev.evaluate(&vm_null).unwrap(), json!("-"));

    let vm_missing = one("Parent.", json!({ "projectInfo": {} }));
    assert_json_eq!(ev.evaluate(&vm_missing).unwrap(), json!("-"));
}

/// "Parse expression with nullish coalescing and string concat: (Page.userFirstName??'') +' '+ (Page.userLastName??'')"
#[test]
fn parse_expression_with_nullish_coalescing_and_string_concat() {
    let expr = "(Page.userFirstName??'') +' '+ (Page.userLastName??'')";
    assert!(Expression::new(expr).is_ok());

    let ev = ExpressionEvaluator::new(expr);
    assert_json_eq!(
        ev.evaluate(&one("Page.", json!({ "userFirstName": "John", "userLastName": "Doe" }))).unwrap(),
        json!("John Doe")
    );

    assert_json_eq!(ev.evaluate(&one("Page.", json!({}))).unwrap(), json!(" "));

    assert_json_eq!(ev.evaluate(&one("Page.", json!({ "userFirstName": "Jane" }))).unwrap(), json!("Jane "));

    assert_json_eq!(ev.evaluate(&one("Page.", json!({ "userLastName": "Smith" }))).unwrap(), json!(" Smith"));
}

/// "Parse expression with dynamic array index and dot access: Parent.perCount[Parent.index].value.Percentage + '%'"
#[test]
fn parse_expression_with_dynamic_array_index_and_dot_access() {
    let vm = one(
        "Parent.",
        json!({
            "perCount": [
                { "value": { "Percentage": 10 } },
                { "value": { "Percentage": 25 } },
                { "value": { "Percentage": 50 } }
            ],
            "index": 1
        }),
    );

    assert!(Expression::new("Parent.perCount[Parent.index].value.Percentage + '%'").is_ok());

    assert_json_eq!(eval(&vm, "Parent.perCount[Parent.index].value.Percentage + '%'"), json!("25%"));
}

/// 'Parse expression with nested parent index: Parent.perCount[Parent.Parent.__index].value.Percentage'
#[test]
fn parse_expression_with_nested_parent_index() {
    let vm = one(
        "Parent.",
        json!({
            "perCount": [
                { "value": { "Percentage": 10 } },
                { "value": { "Percentage": 25 } },
                { "value": { "Percentage": 50 } }
            ],
            "Parent": { "__index": 2 }
        }),
    );

    assert!(Expression::new("Parent.perCount[Parent.Parent.__index].value.Percentage + '%'").is_ok());

    // Parent.Parent -> { __index: 2 }; __index -> 2; perCount[2] -> 50
    assert_json_eq!(eval(&vm, "Parent.perCount[Parent.Parent.__index].value.Percentage + '%'"), json!("50%"));
}

/// 'Parse expression with less than comparison: Page.dealData.size < Page.dealData.totalElements'
#[test]
fn parse_expression_with_less_than_comparison() {
    assert!(Expression::new("Page.dealData.size < Page.dealData.totalElements").is_ok());

    let ev = ExpressionEvaluator::new("Page.dealData.size < Page.dealData.totalElements");
    assert_eq!(
        ev.evaluate(&one("Page.", json!({ "dealData": { "size": 10, "totalElements": 100 } }))).unwrap(),
        json!(true)
    );

    assert_eq!(
        ev.evaluate(&one("Page.", json!({ "dealData": { "size": 100, "totalElements": 100 } }))).unwrap(),
        json!(false)
    );

    assert_eq!(
        ev.evaluate(&one("Page.", json!({ "dealData": { "size": 150, "totalElements": 100 } }))).unwrap(),
        json!(false)
    );
}

/// 'Parse expression with extra spaces: Page.dealData.size <  Page.dealData.totalElements'
#[test]
fn parse_expression_with_extra_spaces() {
    let vm = one("Page.", json!({ "dealData": { "size": 5, "totalElements": 20 } }));

    assert!(Expression::new("Page.dealData.size <  Page.dealData.totalElements").is_ok());

    assert_eq!(eval(&vm, "Page.dealData.size <  Page.dealData.totalElements"), json!(true));
}

/// 'Parse expression with space before dot: Parent.perCount[Parent.Parent.__index].value.percentage'
///
/// JS wraps the body in try/catch: either it evaluates to '10%' or it throws.
#[test]
fn parse_expression_with_space_before_dot() {
    let vm = one(
        "Parent.",
        json!({
            "perCount": [ { "value": { "Percentage": 10 } }, { "value": { "Percentage": 25 } } ],
            "Parent": { "__index": 0 }
        }),
    );

    let expr = "Parent.perCount[Parent.Parent.__index].value.percentage + '%'";
    if Expression::new(expr).is_err() {
        return;
    }
    // JS's `expect(result).toBe('10%')` sits inside the same try/catch, so its failure is
    // swallowed and the test passes whatever happens. kirun-js actually evaluates this to "%"
    // (`percentage` is undefined and `undefined + '%'` is '%'); assert that instead.
    match ExpressionEvaluator::new(expr).evaluate(&vm) {
        Ok(result) => assert_json_eq!(result, json!("%")),
        Err(_) => {}
    }
}

/// 'Expression toString() preserves structure'
#[test]
fn expression_to_string_preserves_structure() {
    let expr1 = Expression::new("Page.dealData.size").unwrap().to_string();
    assert!(expr1.contains("Page"));
    assert!(expr1.contains("dealData"));

    assert!(Expression::new("Page.dealData.size < Page.dealData.totalElements").unwrap().to_string().contains('<'));

    assert!(Expression::new("Steps.floorWeekOne.output.value * 86400").unwrap().to_string().contains('*'));

    assert!(Expression::new("Parent.projectInfo.projectType ?? '-'").unwrap().to_string().contains("??"));
}

/// 'Parse complex expression with array access and string concatenation'
#[test]
fn parse_complex_expression_with_array_access_and_string_concatenation() {
    let vm = one(
        "Parent.",
        json!({
            "perCount": [
                { "value": { "Percentage": 15 } },
                { "value": { "Percentage": 30 } },
                { "value": { "Percentage": 45 } }
            ],
            "index": 2
        }),
    );

    assert_json_eq!(eval(&vm, "Parent.perCount[Parent.index].value.Percentage + '%'"), json!("45%"));
}

// ================= describe 'Original Expression Parsing Tests'

const EXPRESSION_5: &str = "10 - {{{{Parent.id}} < 10 ? 1 : {{Parent.id}} < 100 ? 2 : {{Parent.id}} < 1000 ? 3 : {{Parent.id}} < 10000 ? 4 : {{Parent.id}} < 100000 ? 5 : {{Parent.id}} < 1000000 ? 6 : {{Parent.id}} < 10000000 ? 7 : {{Parent.id}} < 100000000 ? 8 : {{Parent.id}} < 1000000000 ? 9 :10}}";

const EXPRESSION_8: &str = "Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? {{Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.location.expression}}";

/// 'Expression 1: Steps.floorWeekOne.output.value * {{Page.secondsInDay}}'
#[test]
fn expression_1_multiplication_with_nested_template() {
    let expr = Expression::new("Steps.floorWeekOne.output.value * {{Page.secondsInDay}}").unwrap();
    assert!(!expr.get_operations().is_empty());
    assert!(expr.to_string().contains("Page.secondsInDay"));
}

/// "Expression 2: Parent.projectInfo.projectType?? '-'"
#[test]
fn expression_2_nullish_coalescing_without_space() {
    let expr = Expression::new("Parent.projectInfo.projectType?? '-'").unwrap();
    assert!(expr.to_string().contains("??"));
}

/// 'Expression 4: Page.dealData.size <  Page.dealData.totalElements'
#[test]
fn expression_4_less_than_with_extra_space() {
    let expr = Expression::new("Page.dealData.size <  Page.dealData.totalElements").unwrap();
    assert!(expr.to_string().contains('<'));
}

/// 'Original expression parsing - verify all parse without throwing'
#[test]
fn original_expression_parsing_verify_all_parse_without_throwing() {
    let expressions = [
        "Steps.floorWeekOne.output.value * {{Page.secondsInDay}}",
        "Parent.projectInfo.projectType?? '-'",
        "Page.dealData.size <  Page.dealData.totalElements",
        EXPRESSION_8,
    ];
    for expression in expressions {
        assert!(Expression::new(expression).is_ok(), "{expression} threw");
    }
}

/// 'Expression 5: Complex nested ternary with multiple template expressions'
#[test]
fn expression_5_complex_nested_ternary_with_multiple_template_expressions() {
    assert!(Expression::new(EXPRESSION_5).is_ok());

    let expr = Expression::new(EXPRESSION_5).unwrap();
    assert!(!expr.get_operations().is_empty());
}

/// 'Expression 5 Evaluation: Complex nested ternary returns correct values'
#[test]
fn expression_5_evaluation_complex_nested_ternary_returns_correct_values() {
    let test_cases: [(i64, i64); 10] = [
        (5, 10 - 1),
        (50, 10 - 2),
        (500, 10 - 3),
        (5000, 10 - 4),
        (50000, 10 - 5),
        (500000, 10 - 6),
        (5000000, 10 - 7),
        (50000000, 10 - 8),
        (500000000, 10 - 9),
        (5000000000, 10 - 10),
    ];

    for (id, expected) in test_cases {
        let vm = one("Parent.", json!({ "id": id }));
        let result = ExpressionEvaluator::new(EXPRESSION_5).evaluate(&vm).unwrap();
        assert_json_eq!(result, json!(expected));
    }
}

/// 'Expression 5 Debug: Log parsing structure of complex nested ternary'
#[test]
fn expression_5_debug_log_parsing_structure_of_complex_nested_ternary() {
    let expr = Expression::new(EXPRESSION_5).unwrap();

    println!("Expression parsed successfully");
    println!("Expression toString: {expr}");
    println!("Operations empty? {}", expr.get_operations().is_empty());

    let vm = one("Parent.", json!({ "id": 12345 }));
    let result = ExpressionEvaluator::new(EXPRESSION_5).evaluate(&vm).unwrap();

    println!("Evaluation result for id=12345: {result}");
    // 12345 < 100000, so 10 - 5
    assert_json_eq!(result, json!(10 - 5));
}

/// 'Expression 5 Edge Cases: Test boundary values'
#[test]
fn expression_5_edge_cases_test_boundary_values() {
    let boundary_test_cases: [(i64, i64); 20] = [
        (0, 10 - 1),
        (9, 10 - 1),
        (10, 10 - 2),
        (99, 10 - 2),
        (100, 10 - 3),
        (999, 10 - 3),
        (1000, 10 - 4),
        (9999, 10 - 4),
        (10000, 10 - 5),
        (99999, 10 - 5),
        (100000, 10 - 6),
        (999999, 10 - 6),
        (1000000, 10 - 7),
        (9999999, 10 - 7),
        (10000000, 10 - 8),
        (99999999, 10 - 8),
        (100000000, 10 - 9),
        (999999999, 10 - 9),
        (1000000000, 10 - 10),
        (10000000000, 10 - 10),
    ];

    for (id, expected) in boundary_test_cases {
        let vm = one("Parent.", json!({ "id": id }));
        let result = ExpressionEvaluator::new(EXPRESSION_5).evaluate(&vm).unwrap();
        assert_json_eq!(result, json!(expected));
    }
}

/// 'Expression 6: Dynamic bracket index with Parent.__index'
#[test]
fn expression_6_dynamic_bracket_index_with_parent_index() {
    let vm = values_map(vec![
        TestTokenValueExtractor::new("Parent.", json!({ "__index": 2 })),
        TestTokenValueExtractor::new("Page.", json!({ "items": ["first", "second", "third", "fourth"] })),
    ]);
    assert_json_eq!(eval(&vm, "Page.items[Parent.__index]"), json!("third"));
}

/// 'Expression 7: Nested dynamic bracket index'
#[test]
fn expression_7_nested_dynamic_bracket_index() {
    let vm = values_map(vec![
        TestTokenValueExtractor::new("Parent.", json!({ "index": 1 })),
        TestTokenValueExtractor::new(
            "Page.",
            json!({ "matrix": [["a", "b", "c"], ["d", "e", "f"], ["g", "h", "i"]] }),
        ),
    ]);
    assert_json_eq!(eval(&vm, "Page.matrix[Parent.index][Parent.index]"), json!("e"));
}

/// 'Expression 8: Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? {{...location.expression}}'
#[test]
fn expression_8_dynamic_page_key_nullish_with_location_expression() {
    let expr = Expression::new(EXPRESSION_8).unwrap();
    assert!(!expr.get_operations().is_empty());

    let vm_with_value = one(
        "Store.",
        json!({
            "application": { "properties": { "title": "My Application Title" } },
            "urlDetails": { "pageName": "home" },
            "pageDefinition": {
                "home": {
                    "properties": {
                        "title": {
                            "name": {
                                "location": { "expression": "'Application : ' + Store.application.properties.title" }
                            }
                        }
                    }
                }
            }
        }),
    );

    let ev = ExpressionEvaluator::new(EXPRESSION_8);
    assert_json_eq!(ev.evaluate(&vm_with_value).unwrap(), json!("Application : My Application Title"));

    // value null: falls back to location.expression (a quoted literal once substituted)
    let vm_value_null = one(
        "Store.",
        json!({
            "urlDetails": { "pageName": "home" },
            "pageDefinition": {
                "home": {
                    "properties": {
                        "title": {
                            "name": {
                                "value": null,
                                "location": { "expression": "'Expression Fallback Title'" }
                            }
                        }
                    }
                }
            }
        }),
    );
    assert_json_eq!(ev.evaluate(&vm_value_null).unwrap(), json!("Expression Fallback Title"));

    // value missing: fresh evaluator to avoid any cached expansion
    let vm_value_missing = one(
        "Store.",
        json!({
            "urlDetails": { "pageName": "home" },
            "pageDefinition": {
                "home": {
                    "properties": {
                        "title": {
                            "name": {
                                "location": { "expression": "'Expression Fallback Title'" }
                            }
                        }
                    }
                }
            }
        }),
    );
    let ev_fresh = ExpressionEvaluator::new(EXPRESSION_8);
    assert_json_eq!(ev_fresh.evaluate(&vm_value_missing).unwrap(), json!("Expression Fallback Title"));
}

/// 'Expression 9a: Simplified nested ternary to isolate issue'
#[test]
fn expression_9a_simplified_nested_ternary_to_isolate_issue() {
    let vm = one("Page.", json!({ "a": "test" }));
    assert_json_eq!(
        eval(&vm, "Page.a != undefined ? 'A' : Page.b != undefined ? 'B' : Page.c != undefined ? 'C' : '-'"),
        json!("A")
    );
}

/// 'Expression 9b: Nested ternary with dynamic keys'
#[test]
fn expression_9b_nested_ternary_with_dynamic_keys() {
    let vm = values_map(vec![
        TestTokenValueExtractor::new("Page.", json!({ "kycs": { "123": { "a": "test" } } })),
        TestTokenValueExtractor::new("Parent.", json!({ "id": "123" })),
    ]);
    assert_json_eq!(
        eval(&vm, "Page.kycs.{{Parent.id}}.a != undefined ? 'A' : Page.kycs.{{Parent.id}}.b != undefined ? 'B' : '-'"),
        json!("A")
    );
}

/// 'Expression 9c1: Debug - Simplest failing case'
#[test]
fn expression_9c1_debug_simplest_failing_case() {
    assert_json_eq!(
        eval(&ValuesMap::new(), "true ? 'a' +' '+ 'b' : false ? 'c' +' '+ 'd' : '-'"),
        json!("a b")
    );
}

/// 'Expression 9c2: Debug - With property access'
#[test]
fn expression_9c2_debug_with_property_access() {
    let vm = one("Page.", json!({ "a": "Hello", "b": "World" }));
    assert_json_eq!(
        eval(&vm, "true ? Page.a +' '+ Page.b : false ? Page.c +' '+ Page.d : '-'"),
        json!("Hello World")
    );
}

/// 'Expression 9c3: Debug - With deeper property paths like original'
#[test]
fn expression_9c3_debug_with_deeper_property_paths_like_original() {
    let vm = one("Page.", json!({ "x": { "a": { "first": "John", "last": "Doe" } } }));
    assert_json_eq!(eval(&vm, "true ? Page.x.a.first +' '+ Page.x.a.last : '-'"), json!("John Doe"));
}

/// 'Expression 9c4: Debug - Nested ternary with deep paths'
#[test]
fn expression_9c4_debug_nested_ternary_with_deep_paths() {
    let vm = one("Page.", json!({ "x": { "a": { "first": "John", "last": "Doe" } } }));
    assert_json_eq!(
        eval(&vm, "true ? Page.x.a.first +' '+ Page.x.a.last : false ? Page.x.b.first +' '+ Page.x.b.last : '-'"),
        json!("John Doe")
    );
}

/// 'Expression 9c5: Debug - With NUMERIC property path segment'
#[test]
fn expression_9c5_debug_with_numeric_property_path_segment() {
    let vm = one("Page.", json!({ "x": { "123": { "a": { "first": "John", "last": "Doe" } } } }));
    assert_json_eq!(
        eval(&vm, "true ? Page.x.123.a.first +' '+ Page.x.123.a.last : '-'"),
        json!("John Doe")
    );
}

/// 'Expression 9c6: Debug - With ObjectId-like property path segment'
#[test]
fn expression_9c6_debug_with_object_id_like_property_path_segment() {
    let vm = one(
        "Page.",
        json!({ "x": { "507f1f77bcf86cd799439011": { "a": { "first": "Jane", "last": "Smith" } } } }),
    );
    assert_json_eq!(
        eval(
            &vm,
            "true ? Page.x.507f1f77bcf86cd799439011.a.first +' '+ Page.x.507f1f77bcf86cd799439011.a.last : '-'"
        ),
        json!("Jane Smith")
    );
}

/// 'Expression 9c7: Debug - With underscore in property path segment'
#[test]
fn expression_9c7_debug_with_underscore_in_property_path_segment() {
    let vm = one("Page.", json!({ "x": { "123_abc": { "a": { "first": "Bob", "last": "Johnson" } } } }));
    assert_json_eq!(
        eval(&vm, "true ? Page.x.123_abc.a.first +' '+ Page.x.123_abc.a.last : '-'"),
        json!("Bob Johnson")
    );
}

/// 'Expression 9c: Debug - Parse expanded expression without {{}}'
#[test]
fn expression_9c_debug_parse_expanded_expression_without_templates() {
    let vm = one("Page.", json!({ "kycs": { "123": { "a": { "first": "John", "last": "Doe" } } } }));
    assert_json_eq!(
        eval(
            &vm,
            "Page.kycs.123.a != undefined ? Page.kycs.123.a.first +' '+ Page.kycs.123.a.last : Page.kycs.123.b != undefined ? Page.kycs.123.b.first +' '+ Page.kycs.123.b.last : '-'"
        ),
        json!("John Doe")
    );
}

/// 'Expression 9c: Nested ternary with string concatenation'
#[test]
fn expression_9c_nested_ternary_with_string_concatenation() {
    let vm = values_map(vec![
        TestTokenValueExtractor::new("Page.", json!({ "kycs": { "123": { "a": { "first": "John", "last": "Doe" } } } })),
        TestTokenValueExtractor::new("Parent.", json!({ "id": "123" })),
    ]);
    assert_json_eq!(
        eval(
            &vm,
            "Page.kycs.{{Parent.id}}.a != undefined ? Page.kycs.{{Parent.id}}.a.first +' '+ Page.kycs.{{Parent.id}}.a.last : Page.kycs.{{Parent.id}}.b != undefined ? Page.kycs.{{Parent.id}}.b.first +' '+ Page.kycs.{{Parent.id}}.b.last : '-'"
        ),
        json!("John Doe")
    );
}

/// 'Expression 9: Complex nested ternary with dynamic keys and string concatenation'
#[test]
fn expression_9_complex_nested_ternary_with_dynamic_keys_and_string_concatenation() {
    let expression = "Page.kycs.{{Parent.kycAccountId}}.individual != undefined ? Page.kycs.{{Parent.kycAccountId}}.individual.basic.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.individual.basic.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.joint != undefined ? Page.kycs.{{Parent.kycAccountId}}.joint.mainApplicant.individual.basic.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.joint.mainApplicant.individual.basic.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.llp != undefined ? Page.kycs.{{Parent.kycAccountId}}.llp.authorizePerson.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.llp.authorizePerson.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.partnership != undefined ? Page.kycs.{{Parent.kycAccountId}}.partnership.authorizePerson.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.partnership.authorizePerson.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.private != undefined ? Page.kycs.{{Parent.kycAccountId}}.private.authorizePerson.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.private.authorizePerson.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.trust != undefined ? Page.kycs.{{Parent.kycAccountId}}.trust.authorizePerson.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.trust.authorizePerson.personalInformation.lastName : Page.kycs.{{Parent.kycAccountId}}.huf != undefined ? Page.kycs.{{Parent.kycAccountId}}.huf.kartaInformation.personalInformation.firstName +' '+ Page.kycs.{{Parent.kycAccountId}}.huf.kartaInformation.personalInformation.lastName : '-'";

    let vm_individual = values_map(vec![
        TestTokenValueExtractor::new(
            "Page.",
            json!({
                "kycs": {
                    "123": {
                        "individual": {
                            "basic": { "personalInformation": { "firstName": "John", "lastName": "Doe" } }
                        }
                    }
                }
            }),
        ),
        TestTokenValueExtractor::new("Parent.", json!({ "kycAccountId": "123" })),
    ]);

    let ev = ExpressionEvaluator::new(expression);
    assert_json_eq!(ev.evaluate(&vm_individual).unwrap(), json!("John Doe"));

    let vm_fallback = values_map(vec![
        TestTokenValueExtractor::new("Page.", json!({ "kycs": { "456": {} } })),
        TestTokenValueExtractor::new("Parent.", json!({ "kycAccountId": "456" })),
    ]);

    let ev_fallback = ExpressionEvaluator::new(expression);
    assert_json_eq!(ev_fallback.evaluate(&vm_fallback).unwrap(), json!("-"));
}

/// 'Expression trimming: Leading and trailing whitespace should be trimmed'
#[test]
fn expression_trimming_leading_and_trailing_whitespace_should_be_trimmed() {
    let expr1 = Expression::new("   Page.value   ").unwrap();
    assert_eq!(expr1.get_expression(), "Page.value");

    let expr2 = Expression::new("  true ? \"yes\" : \"no\"  ").unwrap();
    assert_eq!(expr2.get_expression(), "true ? \"yes\" : \"no\"");

    let expr3 = Expression::new("\t\nPage.x + 5\n\t").unwrap();
    assert_eq!(expr3.get_expression(), "Page.x + 5");

    let vm = one("Page.", json!({ "value": "test", "x": 10 }));

    assert_json_eq!(eval(&vm, "   Page.value   "), json!("test"));
    assert_json_eq!(eval(&vm, "  Page.x + 5  "), json!(15));
}
