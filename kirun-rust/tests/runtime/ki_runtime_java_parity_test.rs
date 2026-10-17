//! How a definition runs where kirun-java (which stored server functions run on) differs from
//! kirun-js. Every expected answer here was measured on kirun-java 4.9.2 with the same
//! definition and arguments.

use std::sync::Arc;

use kirun::function::Function;
use kirun::model::FunctionDefinition;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use kirun::{Map, Value};
use serde_json::{json, Value as J};

use crate::common::schema_repo;
use crate::{hybrid_with, shared_lock, ListRepository};

fn reference(t: &str, extra: J) -> J {
    let mut r = json!({ "key": "k", "type": t });
    for (k, v) in extra.as_object().unwrap() {
        r[k] = v.clone();
    }
    json!({ "k": r })
}

fn value(v: J) -> J {
    reference("VALUE", json!({ "value": v }))
}

fn step(name: &str, ns: &str, fun: &str, params: J) -> J {
    json!({ "statementName": name, "namespace": ns, "name": fun, "parameterMap": params })
}

fn result(name: &str, expr: &str) -> J {
    json!({ "name": name, "value": { "isExpression": true, "value": expr } })
}

fn generate(results: J) -> J {
    step("ge", "System", "GenerateEvent", json!({ "results": value(results) }))
}

async fn run(def: J, args: J, extra: Vec<J>) -> Result<J, String> {
    let _lock = shared_lock();
    let functions: Vec<Arc<dyn Function>> = extra
        .iter()
        .map(|d| Arc::new(KIRuntime::new(FunctionDefinition::from_value(d).unwrap(), false).unwrap()) as Arc<dyn Function>)
        .collect();
    let repo = hybrid_with(Arc::new(ListRepository(functions)));
    let runtime = KIRuntime::new(FunctionDefinition::from_value(&def).map_err(|e| e.to_string())?, false).unwrap();
    let args: Map<Value> = args.as_object().unwrap().iter().map(|(k, v)| (k.clone(), v.clone())).collect();
    let mut fep = FunctionExecutionParameters::new(repo, schema_repo()).set_arguments(args);
    let mut out = runtime.execute(&mut fep).await.map_err(|e| e.to_string())?;
    let mut events = Vec::new();
    while let Some(er) = out.next() {
        let r: serde_json::Map<String, Value> = er.get_result().iter().map(|(k, v)| (k.clone(), v.clone())).collect();
        events.push(json!({ "name": er.get_name(), "result": r }));
    }
    Ok(J::Array(events))
}

fn def(steps: J, parameters: J) -> J {
    json!({ "name": "a", "namespace": "T", "parameters": parameters, "steps": steps })
}

fn output(result: J) -> Result<J, String> {
    Ok(json!([{ "name": "output", "result": result }]))
}

#[tokio::test]
async fn variable_argument_drops_references_that_come_to_null() {
    // K1: kirun-java leaves out a reference whose expression reads nothing (or is blank).
    let results = json!({
        "a": { "key": "a", "type": "EXPRESSION", "expression": "Arguments.nothing", "order": 1 },
        "b": { "key": "b", "type": "EXPRESSION", "expression": "", "order": 2 },
        "c": { "key": "c", "type": "VALUE", "order": 3, "value": result("s", "1") }
    });
    let d = def(json!({ "ge": step("ge", "System", "GenerateEvent", json!({ "results": results })) }), json!({}));
    assert_eq!(run(d, json!({}), vec![]).await, output(json!({ "s": 1 })));
}

#[tokio::test]
async fn an_absent_argument_with_a_typeless_schema_is_reactors_npe() {
    // K6: `Mono.just(null)` in kirun-java's validator ("value"), raised before the steps run.
    let params = json!({ "status": { "parameterName": "status", "schema": {} } });
    let d = def(json!({ "ge": generate(result("s", "Arguments.status")) }), params.clone());
    assert_eq!(run(d.clone(), json!({}), vec![]).await, Err("value".to_string()));
    // A JSON null is a value: it passes and reads as null.
    assert_eq!(run(d, json!({ "status": null }), vec![]).await, output(json!({ "s": null })));
    // A bare `$ref` has no type either.
    let refd = def(
        json!({ "ge": generate(result("s", "Arguments.t")) }),
        json!({ "t": { "parameterName": "t", "schema": { "ref": "System.Date.Timestamp" } } }),
    );
    assert_eq!(run(refd, json!({}), vec![]).await, Err("value".to_string()));
}

#[tokio::test]
async fn a_generated_value_that_reads_nothing_is_reactors_npe() {
    // K6: `Tuples.of(name, null)` fails with "t2" when the expression reads a missing key.
    let params = json!({ "x": { "parameterName": "x", "schema": { "type": "OBJECT" } } });
    let missing = def(json!({ "ge": generate(result("s", "Arguments.x.z")) }), params.clone());
    assert_eq!(run(missing, json!({ "x": { "y": null } }), vec![]).await, Err("t2".to_string()));
    let present_null = def(json!({ "ge": generate(result("s", "Arguments.x.y")) }), params.clone());
    assert_eq!(run(present_null, json!({ "x": { "y": null } }), vec![]).await, output(json!({ "s": null })));
    let in_parentheses = def(json!({ "ge": generate(result("s", "(Arguments.x.z)")) }), params);
    assert_eq!(run(in_parentheses, json!({ "x": {} }), vec![]).await, Err("t2".to_string()));
    // An expression with an operator reads JSON null, which is kept.
    let nullish = def(json!({ "ge": generate(result("s", "Arguments.a ?? Arguments.b")) }), json!({}));
    assert_eq!(run(nullish, json!({}), vec![]).await, output(json!({ "s": null })));
    // `Context.<name>` of an unknown name is JSON null in kirun-java.
    let context = def(json!({ "ge": generate(result("s", "Context.nothing")) }), json!({}));
    assert_eq!(run(context, json!({}), vec![]).await, output(json!({ "s": null })));
    // A missing `value` with isExpression false is Java null too.
    let no_value = def(json!({ "ge": generate(json!({ "name": "s", "value": { "isExpression": false } })) }), json!({}));
    assert_eq!(run(no_value, json!({}), vec![]).await, Err("t2".to_string()));
}

#[tokio::test]
async fn generate_event_fails_as_gson_and_collectors_do() {
    let npe = |method: &str, receiver: &str| {
        Err(format!("Cannot invoke \"com.google.gson.JsonElement.{method}\" because {receiver} is null"))
    };
    let get = "the return value of \"com.google.gson.JsonObject.get(String)\"";
    // Read while planning, even for a step that would never run.
    let no_is_expression = def(json!({ "ge": generate(json!({ "name": "s", "value": { "value": 5 } })) }), json!({}));
    assert_eq!(run(no_is_expression, json!({}), vec![]).await, npe("getAsBoolean()", get));
    let no_expression = def(json!({ "ge": generate(json!({ "name": "s", "value": { "isExpression": true } })) }), json!({}));
    assert_eq!(run(no_expression, json!({}), vec![]).await, npe("getAsString()", get));
    let no_name = def(json!({ "ge": generate(json!({ "value": { "isExpression": false, "value": 1 } })) }), json!({}));
    assert_eq!(run(no_name, json!({}), vec![]).await, npe("getAsString()", get));
    let no_value = def(json!({ "ge": generate(json!({ "name": "s" })) }), json!({}));
    assert_eq!(run(no_value, json!({}), vec![]).await, npe("isJsonObject()", "\"je\""));
    let duplicate = def(
        json!({ "ge": generate(json!([
            { "name": "s", "value": { "isExpression": false, "value": 1 } },
            { "name": "s", "value": { "isExpression": false, "value": 2 } }
        ])) }),
        json!({}),
    );
    assert_eq!(
        run(duplicate, json!({}), vec![]).await,
        Err("Duplicate key s (attempted merging values 1 and 2)".to_string())
    );
}

#[tokio::test]
async fn planning_fails_where_kirun_java_reads_without_a_null_check() {
    // A parameter the function does not have.
    let unknown = def(
        json!({
            "abs": step("abs", "System.Math", "Absolute", json!({ "value": value(json!(-3)), "nope": value(json!(1)) })),
            "ge": generate(result("s", "Steps.abs.output.value"))
        }),
        json!({}),
    );
    assert_eq!(
        run(unknown, json!({}), vec![]).await,
        Err("Cannot invoke \"com.fincity.nocode.kirun.engine.model.Parameter.isVariableArgument()\" because \"p\" is null".to_string())
    );

    // A VALUE object for an OBJECT parameter whose schema has no name.
    let inner = json!({
        "name": "b", "namespace": "T",
        "parameters": { "o": { "parameterName": "o", "schema": { "type": "OBJECT" } } },
        "steps": { "ge": generate(result("r", "Arguments.o")) }
    });
    let outer = def(
        json!({
            "call": step("call", "T", "b", json!({ "o": value(json!({ "x": 1 })) })),
            "ge": generate(result("s", "Steps.call.output.r"))
        }),
        json!({}),
    );
    assert_eq!(
        run(outer.clone(), json!({}), vec![inner.clone()]).await,
        Err("Cannot invoke \"String.equals(Object)\" because the return value of \"com.fincity.nocode.kirun.engine.json.schema.Schema.getName()\" is null".to_string())
    );
    let mut named = inner;
    named["parameters"]["o"]["schema"]["name"] = json!("o");
    assert_eq!(run(outer, json!({}), vec![named]).await, output(json!({ "s": { "x": 1 } })));

    // A step that leaves out a parameter whose schema has no type and no default.
    let typeless = json!({
        "name": "b", "namespace": "T",
        "parameters": { "o": { "parameterName": "o", "schema": {} } },
        "steps": { "ge": generate(result("r", "1")) }
    });
    let calls = def(json!({ "call": step("call", "T", "b", json!({})) }), json!({}));
    assert_eq!(
        run(calls, json!({}), vec![typeless]).await,
        Err("Cannot invoke \"com.fincity.nocode.kirun.engine.json.schema.type.Type.getAllowedSchemaTypes()\" because the return value of \"com.fincity.nocode.kirun.engine.json.schema.Schema.getType()\" is null".to_string())
    );
}

#[tokio::test]
async fn definition_errors_print_as_kirun_javas_list() {
    let d = def(json!({ "abs": step("abs", "System.Math", "Absolute", json!({})), "ge": generate(result("s", "1")) }), json!({}));
    assert_eq!(
        run(d, json!({}), vec![]).await,
        Err("Please fix the errors in the function definition before execution : \n[StatementMessage(messageType=ERROR, message=Parameter \"value\" needs a value)]".to_string())
    );
}

#[tokio::test]
async fn events_without_output_and_none_raised() {
    let abs = json!({ "abs": step("abs", "System.Math", "Absolute", json!({ "value": value(json!(-3)) })) });
    let mut no_output = def(abs.clone(), json!({}));
    no_output["events"] = json!({ "result": { "name": "result", "parameters": { "x": { "type": "STRING" } } } });
    assert_eq!(
        run(no_output, json!({}), vec![]).await,
        Err("Cannot invoke \"com.fincity.nocode.kirun.engine.model.Event.getParameters()\" because the return value of \"java.util.Map.get(Object)\" is null".to_string())
    );
    let mut with_output = def(abs, json!({}));
    with_output["events"] = json!({ "output": { "name": "output", "parameters": { "x": { "type": "STRING" } } } });
    assert_eq!(run(with_output, json!({}), vec![]).await, Err("No events raised".to_string()));
}

#[tokio::test]
async fn a_functions_own_error_is_not_wrapped() {
    // kirun-java names the function only for an argument that fails validation.
    let d = def(
        json!({ "oc": step("oc", "System.Object", "ObjectConvert", json!({
            "source": value(json!({ "n": "abc" })),
            "schema": value(json!({ "type": "OBJECT", "name": "obj", "properties": { "n": { "type": "INTEGER" } } })),
            "conversionMode": value(json!("STRICT"))
        })) }),
        json!({}),
    );
    assert_eq!(run(d, json!({}), vec![]).await, Err("obj - obj - \"abc\" is not a Integer".to_string()));
}
