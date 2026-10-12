//! Port of kirun-js `__tests__/engine/dsl/ReadableLayout.spec.ts`.
//!
//! The decompiler's layout: `output` chains stay at one level, long calls take one argument
//! per line, expressions in event results print bare, and large objects move to a VALUES
//! section referenced as `@name`. None of it may change the compiled JSON.

use kirun::dsl::DSLCompiler;
use kirun::Value;
use regex::Regex;
use serde_json::json;

/// Steps with ParameterReference keys dropped and refs listed in order: keys are random per compile.
fn shape(func: &Value) -> Value {
    let mut func = func.clone();
    if let Some(steps) = func["steps"].as_object_mut() {
        for step in steps.values_mut() {
            if let Some(pm) = step.get_mut("parameterMap").and_then(Value::as_object_mut) {
                for refs in pm.values_mut() {
                    let mut list: Vec<Value> = refs.as_object().map(|o| o.values().cloned().collect()).unwrap_or_default();
                    list.sort_by_key(|r| r["order"].as_i64().unwrap_or(0));
                    for r in list.iter_mut() {
                        r.as_object_mut().unwrap().remove("key");
                    }
                    *refs = Value::Array(list);
                }
            }
        }
    }
    func
}

/// The first (or only) ref of a parameter.
fn first_ref<'a>(func: &'a Value, step: &str, param: &str) -> &'a Value {
    func["steps"][step]["parameterMap"][param].as_object().unwrap().values().next().unwrap()
}

fn round_trip(text: &str) -> String {
    let json = DSLCompiler::compile(text).unwrap();
    let printed = DSLCompiler::decompile(&json).unwrap();
    assert_eq!(shape(&DSLCompiler::compile(&printed).unwrap()), shape(&json), "printed:\n{printed}");
    printed
}

const SOURCE: &str = r#"FUNCTION activity
    NAMESPACE Test
    LOGIC
        c: Test.context()
        auth: System.If(condition = Steps.c.output.ctx.isAuthenticated = true) AFTER Steps.c.output
            true
                dg: Test.rowGet(ctx = Steps.c.output.ctx, ref = Arguments.id, storage = "deal", entity = "Ticket", mode = "strict") AFTER Steps.auth.true
                    error
                        dgE: System.GenerateEvent(eventName = "error", results = {"name": "status", "value": {"isExpression": true, "value": "Steps.dg.error.status"}}) AFTER Steps.dg.error
                    output
                        cfg: System.Make(resultShape = {"rels": [{"field": "taskId", "storage": "task", "spec": "task"}, {"field": "noteId", "storage": "note", "spec": "note"}]}) AFTER Steps.dg.output
                            output
                                done: System.GenerateEvent(results = {"name": "result", "value": {"isExpression": true, "value": "Steps.cfg.output.value"}}) AFTER Steps.cfg.output
            false
                authE: System.GenerateEvent(eventName = "error") AFTER Steps.auth.false"#;

#[test]
fn output_chains_print_at_one_level_branches_indent() {
    let printed = round_trip(SOURCE);
    let indent_of = |step: &str| {
        let line = printed.lines().find(|l| l.trim_start().starts_with(&format!("{step}:"))).unwrap();
        line.len() - line.trim_start().len()
    };
    assert_eq!(indent_of("auth"), indent_of("c"));
    assert_eq!(indent_of("cfg"), indent_of("dg"));
    assert_eq!(indent_of("done"), indent_of("dg"));
    assert_eq!(indent_of("dg"), indent_of("auth") + 8);
    assert_eq!(indent_of("dgE"), indent_of("dg") + 8);
    assert_eq!(indent_of("authE"), indent_of("dg"));
}

#[test]
fn expressions_in_event_results_print_bare() {
    let printed = round_trip(SOURCE);
    assert!(printed.contains(r#"results = {name: "status", value: Steps.dg.error.status}"#), "{printed}");
    assert!(!printed.contains("isExpression"));
}

#[test]
fn a_large_object_moves_to_values_and_compiles_to_the_inline_value() {
    let printed = round_trip(SOURCE);
    assert!(printed.contains("cfg: System.Make(resultShape = @cfg.resultShape) AFTER Steps.dg.output"), "{printed}");
    assert!(Regex::new(r"\n {4}VALUES\n {8}@cfg\.resultShape = \{").unwrap().is_match(&printed));
    let back = DSLCompiler::compile(&printed).unwrap();
    assert_eq!(
        first_ref(&back, "cfg", "resultShape")["value"],
        json!({"rels": [
            {"field": "taskId", "storage": "task", "spec": "task"},
            {"field": "noteId", "storage": "note", "spec": "note"}
        ]})
    );
}

#[test]
fn a_long_call_takes_one_argument_per_line() {
    let printed = round_trip(SOURCE);
    assert!(Regex::new(r"dg: Test\.rowGet\(\n\s+ctx = Steps\.c\.output\.ctx,\n").unwrap().is_match(&printed), "{printed}");
    assert!(Regex::new(r#"\n\s+mode = "strict"\n\s+\) AFTER Steps\.auth\.true"#).unwrap().is_match(&printed));
}

#[test]
fn the_last_argument_before_a_closing_paren_on_its_own_line_does_not_keep_the_line_break() {
    let json = DSLCompiler::compile("FUNCTION f\n    LOGIC\n        a: System.Print(\n            values = Arguments.x\n        )").unwrap();
    assert_eq!(first_ref(&json, "a", "values")["expression"], "Arguments.x");
}

#[test]
fn trailing_spaces_on_one_line_are_kept() {
    let func = json!({"name": "f", "steps": {"a": {"statementName": "a", "namespace": "System", "name": "Print",
        "parameterMap": {"values": {"k": {"key": "k", "type": "EXPRESSION", "expression": " Arguments.x  ", "order": 1}}}}}});
    let back = DSLCompiler::compile(&DSLCompiler::decompile(&func).unwrap()).unwrap();
    assert_eq!(first_ref(&back, "a", "values")["expression"], " Arguments.x  ");
}

#[test]
fn an_event_result_expression_the_parser_would_misread_goes_in_backticks() {
    let results = [
        json!({"name": "a", "value": {"isExpression": true, "value": "'x' + Arguments.y"}}),
        json!({"name": "b", "value": {"isExpression": true, "value": "0 + 400"}}),
        json!({"name": "c", "value": {"isExpression": true, "value": "max(1, 2)"}}),
        json!({"name": "d", "value": {"isExpression": true, "value": "Arguments.a, Arguments.b"}}),
        json!({"name": "e", "value": {"isExpression": true, "value": "true"}}),
    ];
    let refs: serde_json::Map<String, Value> = results
        .iter()
        .enumerate()
        .map(|(i, v)| (format!("k{i}"), json!({"key": format!("k{i}"), "type": "VALUE", "value": v, "order": i + 1})))
        .collect();
    let func = json!({"name": "f", "steps": {"a": {"statementName": "a", "namespace": "System", "name": "GenerateEvent",
        "parameterMap": {"results": refs}}}});
    let printed = DSLCompiler::decompile(&func).unwrap();
    assert!(printed.contains(r#"{name: "a", value: `'x' + Arguments.y`}"#), "{printed}");
    assert!(printed.contains(r#"{name: "b", value: `0 + 400`}"#));
    assert!(printed.contains(r#"{name: "c", value: max(1, 2)}"#));
    assert!(printed.contains(r#"{name: "d", value: `Arguments.a, Arguments.b`}"#));
    assert!(printed.contains(r#"{name: "e", value: `true`}"#));
    let back = shape(&DSLCompiler::compile(&printed).unwrap());
    let values: Vec<Value> = back["steps"]["a"]["parameterMap"]["results"].as_array().unwrap().iter().map(|r| r["value"].clone()).collect();
    assert_eq!(values, results);
}

#[test]
fn an_expression_object_outside_event_results_stays_written_out() {
    // The runtime does not evaluate it there, so it must not look like a live expression
    let value = json!({"title": {"isExpression": true, "value": "Page.t"}, "done": true});
    let func = json!({"name": "f", "steps": {"a": {"statementName": "a", "namespace": "CoreServices.Storage", "name": "Create",
        "parameterMap": {"dataObject": {"k": {"key": "k", "type": "VALUE", "value": value, "order": 1}}}}}});
    let printed = DSLCompiler::decompile(&func).unwrap();
    assert!(printed.contains(r#"dataObject = {title: {isExpression: true, value: "Page.t"}, done: true}"#), "{printed}");
    assert_eq!(first_ref(&DSLCompiler::compile(&printed).unwrap(), "a", "dataObject")["value"], value);
}

#[test]
fn a_value_used_twice_is_copied() {
    let json = DSLCompiler::compile(
        "FUNCTION f\n    LOGIC\n        a: System.Make(resultShape = @shape)\n        b: System.Make(resultShape = @shape)\n    VALUES\n        @shape = {list: [1, 2]}",
    )
    .unwrap();
    assert_eq!(first_ref(&json, "a", "resultShape")["value"], json!({"list": [1, 2]}));
    assert_eq!(first_ref(&json, "b", "resultShape")["value"], json!({"list": [1, 2]}));
}

#[test]
fn any_value_name_compiles_and_decompiling_renames_it_to_step_parameter() {
    // Over INLINE_VALUE_WIDTH (100) on one line, so the printer moves it to VALUES
    let shape_text = r#"{list: [1, 2], label: "a value long enough to be moved out of the step line", note: "by the printer"}"#;
    let printed = DSLCompiler::decompile(
        &DSLCompiler::compile(&format!(
            "FUNCTION f\n    LOGIC\n        a: System.Make(resultShape = @shape)\n        b: System.Make(resultShape = @shape)\n    VALUES\n        @shape = {shape_text}"
        ))
        .unwrap(),
    )
    .unwrap();
    assert!(printed.contains("a: System.Make(resultShape = @a.resultShape)"), "{printed}");
    assert!(printed.contains("b: System.Make(resultShape = @b.resultShape)"));
    assert!(Regex::new(r"\n {8}@a\.resultShape = \{").unwrap().is_match(&printed));
    assert!(Regex::new(r"\n {8}@b\.resultShape = \{").unwrap().is_match(&printed));
    assert!(!printed.contains("@shape"));

    let back = DSLCompiler::compile(&printed).unwrap();
    let expected =
        json!({"list": [1, 2], "label": "a value long enough to be moved out of the step line", "note": "by the printer"});
    assert_eq!(first_ref(&back, "a", "resultShape")["value"], expected);
    assert_eq!(first_ref(&back, "b", "resultShape")["value"], expected);
}

#[test]
fn an_undefined_value_name_is_an_error() {
    let err = DSLCompiler::compile("FUNCTION f\n    LOGIC\n        a: System.Make(resultShape = @missing)").unwrap_err();
    assert!(err.to_string().contains("Value @missing is not defined in VALUES"), "{err}");
}

#[test]
fn values_is_still_an_ordinary_name_where_it_is_not_followed_by_at() {
    let json = DSLCompiler::compile("FUNCTION f\n    LOGIC\n        VALUES: System.Print(values = Arguments.x)").unwrap();
    assert_eq!(json["steps"]["VALUES"]["name"], "Print");
}
