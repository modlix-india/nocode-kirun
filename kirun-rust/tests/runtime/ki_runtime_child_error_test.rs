//! `catchErrors` (kirun-rust only): a marked step whose function fails hands the failure to the
//! steps on its `error` event. An unmarked step, or a marked one nothing waits on, ends the run
//! as kirun 2.1.0, kirun-js and kirun-java do.

use std::sync::atomic::Ordering;
use std::sync::Arc;

use async_trait::async_trait;
use kirun::function::{AbstractFunction, Function};
use kirun::model::{Event, FunctionOutput, FunctionSignature, Statement};
use kirun::runtime::catch_errors::{self, Caught};
use kirun::runtime::ki_runtime::MAX_EXECUTION_ITERATIONS;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use kirun::util::map_util::MapUtil;
use kirun::{KIRunError, Map};
use serde_json::{json, Value};

use crate::common::schema_repo;
use crate::{exclusive_lock, fd, hybrid_with, shared_lock, ListRepository};

/// `Internal.Fails`: always fails with "child went wrong".
struct Fails(FunctionSignature);

impl Fails {
    fn new() -> Self {
        Self(
            FunctionSignature::new("Fails")
                .set_namespace("Internal")
                .set_events(MapUtil::of_entries_array(vec![Event::output_event_map_entry(Map::new())])),
        )
    }
}

#[async_trait]
impl AbstractFunction for Fails {
    fn get_signature(&self) -> &FunctionSignature {
        &self.0
    }

    async fn internal_execute(&self, _context: &mut FunctionExecutionParameters) -> kirun::Result<FunctionOutput> {
        Err(KIRunError::runtime("child went wrong"))
    }
}

/// A GenerateEvent step raising `event` with `name` = `expression`.
fn generate(statement: &str, event: &str, name: &str, expression: &str, depends_on: &[&str]) -> Value {
    let deps: serde_json::Map<String, Value> = depends_on.iter().map(|d| (d.to_string(), json!(true))).collect();
    json!({
        "statementName": statement, "namespace": "System", "name": "GenerateEvent",
        "dependentStatements": deps,
        "parameterMap": {
            "eventName": {"one": {"key": "one", "type": "VALUE", "value": event}},
            "results": {"one": {"key": "one", "type": "VALUE",
                "value": {"name": name, "value": {"isExpression": true, "value": expression}}}}
        }
    })
}

/// `Test.Parent`: calls `callee` as step `call` (marked `catchErrors` when `catching`), plus
/// `steps`.
fn parent(callee: (&str, &str), catching: bool, steps: Vec<Value>) -> KIRuntime {
    let mut all = serde_json::Map::new();
    let mut call = json!({"statementName": "call", "namespace": callee.0, "name": callee.1});
    if catching {
        call["catchErrors"] = json!(true);
    }
    all.insert("call".into(), call);
    for s in steps {
        all.insert(s["statementName"].as_str().unwrap().to_string(), s);
    }
    let def = json!({"name": "Parent", "namespace": "Test", "steps": all});
    KIRuntime::new(fd(&def.to_string()), false).unwrap()
}

/// `Test.Child`: a definition whose one step fails.
fn child() -> Arc<dyn Function> {
    let def = json!({
        "name": "Child", "namespace": "Test",
        "events": {"output": {"name": "output", "parameters": {}}},
        "steps": {"inner": {"statementName": "inner", "namespace": "Internal", "name": "Fails"}}
    });
    Arc::new(KIRuntime::new(fd(&def.to_string()), false).unwrap())
}

/// `Test.Spin`: a definition that loops until the runtime's iteration guard stops it.
fn spin() -> Arc<dyn Function> {
    let def = json!({
        "name": "Spin", "namespace": "Test",
        "steps": {
            "loop": {"statementName": "loop", "namespace": "System.Loop", "name": "RangeLoop",
                "parameterMap": {"to": {"one": {"key": "one", "type": "VALUE", "value": 1_000_000_000_i64}}}},
            "add": {"statementName": "add", "namespace": "System.Math", "name": "Add",
                "dependentStatements": {"Steps.loop.iteration": true},
                "parameterMap": {"value": {
                    "one": {"key": "one", "type": "VALUE", "value": 1, "order": 1},
                    "two": {"key": "two", "type": "VALUE", "value": 2, "order": 2}}}}
        }
    });
    Arc::new(KIRuntime::new(fd(&def.to_string()), false).unwrap())
}

async fn run(parent: &KIRuntime) -> kirun::Result<Vec<(String, Value)>> {
    let repo = hybrid_with(Arc::new(ListRepository(vec![Arc::new(Fails::new()), child(), spin()])));
    let mut fep = FunctionExecutionParameters::with_execution_id(repo, schema_repo(), "child-error");
    let mut out = parent.execute(&mut fep).await?;
    let mut events = Vec::new();
    while let Some(e) = out.next() {
        events.push((e.get_name().to_string(), Value::Object(e.get_result().clone().into_iter().collect())));
    }
    Ok(events)
}

/// The error event of a caught failure, as the handler reads it.
fn caught_event(reason: &str, function: &str) -> Value {
    json!({"status": 500, "reason": reason, "message": "child went wrong", "details": {"step": "call", "function": function}})
}

/// An error handler as the apps write them: the event's fields, read one by one.
fn handler() -> Value {
    json!({"statementName": "handled", "namespace": "System", "name": "GenerateEvent",
        "dependentStatements": {"Steps.call.error": true},
        "parameterMap": {
            "eventName": {"one": {"key": "one", "type": "VALUE", "value": "output"}},
            "results": {
                "s": {"key": "s", "type": "VALUE", "order": 1, "value": {"name": "status", "value": {"isExpression": true, "value": "Steps.call.error.status"}}},
                "r": {"key": "r", "type": "VALUE", "order": 2, "value": {"name": "reason", "value": {"isExpression": true, "value": "Steps.call.error.reason"}}},
                "m": {"key": "m", "type": "VALUE", "order": 3, "value": {"name": "message", "value": {"isExpression": true, "value": "Steps.call.error.message"}}},
                "d": {"key": "d", "type": "VALUE", "order": 4, "value": {"name": "details", "value": {"isExpression": true, "value": "Steps.call.error.details"}}}}}})
}

// ------------------------------------------------------------------ unmarked steps: as 2.1.0

#[tokio::test]
async fn an_unmarked_step_with_an_error_branch_still_fails_the_run() {
    let _lock = shared_lock();
    for callee in [("Internal", "Fails"), ("Test", "Child")] {
        let p = parent(callee, false, vec![handler(), generate("after", "output", "x", "'not reached'", &["Steps.call.output"])]);
        let err = run(&p).await.unwrap_err();
        assert_eq!(err, KIRunError::runtime("child went wrong"), "{callee:?}: the run fails as in kirun 2.1.0");
    }
}

#[tokio::test]
async fn a_marked_step_nothing_waits_on_still_fails_the_run() {
    let _lock = shared_lock();
    let p = parent(("Test", "Child"), true, vec![generate("after", "output", "x", "'not reached'", &["Steps.call.output"])]);
    assert_eq!(run(&p).await.unwrap_err().to_string(), "child went wrong");

    // Nor does a step on another step's error event catch it.
    let p = parent(
        ("Internal", "Fails"),
        true,
        vec![
            json!({"statementName": "other", "namespace": "System.Math", "name": "Add",
                "parameterMap": {"value": {
                    "one": {"key": "one", "type": "VALUE", "value": 1, "order": 1},
                    "two": {"key": "two", "type": "VALUE", "value": 2, "order": 2}}}}),
            generate("onOther", "output", "x", "'other failed'", &["Steps.other.error"]),
        ],
    );
    assert_eq!(run(&p).await.unwrap_err().to_string(), "child went wrong");
}

// ------------------------------------------------------------------ marked steps

#[tokio::test]
async fn a_marked_step_hands_the_failure_to_its_error_branch_in_the_apps_shape() {
    let _lock = shared_lock();
    let p = parent(("Internal", "Fails"), true, vec![handler(), generate("after", "output", "x", "'not reached'", &["Steps.call.output"])]);
    let events = run(&p).await.unwrap();
    assert_eq!(events, vec![("output".to_string(), caught_event("CHILD_FAILED", "Internal.Fails"))]);

    // A nested definition that fails, read through an expression only (which alone puts the
    // step on the `error` event).
    let p = parent(("Test", "Child"), true, vec![generate("handled", "output", "message", "Steps.call.error.message", &[])]);
    assert_eq!(run(&p).await.unwrap(), vec![("output".to_string(), json!({"message": "child went wrong"}))]);
}

#[tokio::test]
async fn the_rest_of_the_definition_goes_on_and_a_later_failure_still_ends_it() {
    let _lock = shared_lock();
    let p = parent(
        ("Internal", "Fails"),
        true,
        vec![
            json!({"statementName": "sum", "namespace": "System.Math", "name": "Add",
                "parameterMap": {"value": {
                    "one": {"key": "one", "type": "VALUE", "value": 1, "order": 1},
                    "two": {"key": "two", "type": "VALUE", "value": 2, "order": 2}}}}),
            json!({"statementName": "note", "namespace": "System.Context", "name": "Create",
                "dependentStatements": {"Steps.call.error": true},
                "parameterMap": {
                    "name": {"one": {"key": "one", "type": "VALUE", "value": "failure"}},
                    "schema": {"one": {"key": "one", "type": "VALUE", "value": {"type": "STRING"}}}}}),
            json!({"statementName": "keep", "namespace": "System.Context", "name": "Set",
                "dependentStatements": {"Steps.note.output": true},
                "parameterMap": {
                    "name": {"one": {"key": "one", "type": "VALUE", "value": "Context.failure"}},
                    "value": {"one": {"key": "one", "type": "EXPRESSION", "expression": "Steps.call.error.message"}}}}),
            generate("out", "output", "summary", "Context.failure + ' / ' + Steps.sum.output.value", &["Steps.keep.output", "Steps.sum.output"]),
        ],
    );
    assert_eq!(run(&p).await.unwrap(), vec![("output".to_string(), json!({"summary": "child went wrong / 3"}))]);

    // A step of the error branch that fails itself (unmarked) ends the run.
    let p = parent(
        ("Internal", "Fails"),
        true,
        vec![json!({"statementName": "again", "namespace": "Internal", "name": "Fails", "dependentStatements": {"Steps.call.error": true}})],
    );
    assert_eq!(run(&p).await.unwrap_err().to_string(), "child went wrong");
}

#[tokio::test]
async fn each_iteration_of_a_loop_handles_its_own_failure() {
    let _lock = shared_lock();
    let def = json!({
        "name": "Looping", "namespace": "Test",
        "steps": {
            "create": {"statementName": "create", "namespace": "System.Context", "name": "Create",
                "parameterMap": {
                    "name": {"one": {"key": "one", "type": "VALUE", "value": "failed"}},
                    "schema": {"one": {"key": "one", "type": "VALUE", "value": {"type": "ARRAY", "items": {"type": "INTEGER"}}}}}},
            "init": {"statementName": "init", "namespace": "System.Context", "name": "Set",
                "dependentStatements": {"Steps.create.output": true},
                "parameterMap": {
                    "name": {"one": {"key": "one", "type": "VALUE", "value": "Context.failed"}},
                    "value": {"one": {"key": "one", "type": "VALUE", "value": []}}}},
            "loop": {"statementName": "loop", "namespace": "System.Loop", "name": "ForEachLoop",
                "dependentStatements": {"Steps.init.output": true},
                "parameterMap": {"source": {"one": {"key": "one", "type": "VALUE", "value": [10, 20, 30]}}}},
            "call": {"statementName": "call", "namespace": "Internal", "name": "Fails", "catchErrors": true,
                "dependentStatements": {"Steps.loop.iteration": true}},
            "insert": {"statementName": "insert", "namespace": "System.Array", "name": "InsertLast",
                "dependentStatements": {"Steps.call.error": true},
                "parameterMap": {
                    "source": {"one": {"key": "one", "type": "EXPRESSION", "expression": "Context.failed"}},
                    "element": {"one": {"key": "one", "type": "EXPRESSION", "expression": "Steps.loop.iteration.each"}}}},
            "set": {"statementName": "set", "namespace": "System.Context", "name": "Set",
                "parameterMap": {
                    "name": {"one": {"key": "one", "type": "VALUE", "value": "Context.failed"}},
                    "value": {"one": {"key": "one", "type": "EXPRESSION", "expression": "Steps.insert.output.result"}}}},
            "out": generate("out", "output", "failed", "Context.failed", &["Steps.loop.output"])
        }
    });
    let p = KIRuntime::new(fd(&def.to_string()), false).unwrap();
    assert_eq!(run(&p).await.unwrap(), vec![("output".to_string(), json!({"failed": [10, 20, 30]}))]);
}

#[tokio::test]
async fn a_caught_failure_in_an_if_branch_lets_the_ifs_output_go_on() {
    let _lock = shared_lock();
    // AdZump's synOne: `r` starts ok; the job runs in an If's true branch; its error branch
    // marks `r` failed; the step after the If reads `r`.
    let def = json!({
        "name": "Job", "namespace": "Test",
        "steps": {
            "cr": {"statementName": "cr", "namespace": "System.Context", "name": "Create",
                "parameterMap": {
                    "name": {"one": {"key": "one", "type": "VALUE", "value": "r"}},
                    "schema": {"one": {"key": "one", "type": "VALUE", "value": {"type": "OBJECT"}}}}},
            "init": {"statementName": "init", "namespace": "System.Context", "name": "Set",
                "dependentStatements": {"Steps.cr.output": true},
                "parameterMap": {
                    "name": {"one": {"key": "one", "type": "VALUE", "value": "Context.r"}},
                    "value": {"one": {"key": "one", "type": "VALUE", "value": {"ok": true, "error": ""}}}}},
            "is": {"statementName": "is", "namespace": "System", "name": "If",
                "dependentStatements": {"Steps.init.output": true},
                "parameterMap": {"condition": {"one": {"key": "one", "type": "VALUE", "value": true}}}},
            "job": {"statementName": "job", "namespace": "Test", "name": "Child", "catchErrors": true,
                "dependentStatements": {"Steps.is.true": true}},
            "jobE": {"statementName": "jobE", "namespace": "System.Context", "name": "Set",
                "dependentStatements": {"Steps.job.error": true},
                "parameterMap": {
                    "name": {"one": {"key": "one", "type": "VALUE", "value": "Context.r.ok"}},
                    "value": {"one": {"key": "one", "type": "VALUE", "value": false}}}},
            "jobM": {"statementName": "jobM", "namespace": "System.Context", "name": "Set",
                "dependentStatements": {"Steps.jobE.output": true},
                "parameterMap": {
                    "name": {"one": {"key": "one", "type": "VALUE", "value": "Context.r.error"}},
                    "value": {"one": {"key": "one", "type": "EXPRESSION", "expression": "Steps.job.error.message"}}}},
            "out": generate("out", "output", "r", "Context.r", &["Steps.is.output"])
        }
    });
    let p = KIRuntime::new(fd(&def.to_string()), false).unwrap();
    assert_eq!(run(&p).await.unwrap(), vec![("output".to_string(), json!({"r": {"ok": false, "error": "child went wrong"}}))]);
}

// ------------------------------------------------------------------ limits win

#[tokio::test]
async fn the_embeddings_policy_names_the_code_or_rethrows() {
    let _lock = exclusive_lock();
    let p = parent(("Internal", "Fails"), true, vec![handler()]);

    catch_errors::set_policy(Some(Arc::new(|_: &KIRunError| Caught::Event { reason: Some("CORE.FORBIDDEN".into()) })));
    let named = run(&p).await;
    // A run stopped at the embedding's limits: the marked step does not catch it.
    catch_errors::set_policy(Some(Arc::new(|_: &KIRunError| Caught::Rethrow)));
    let stopped = run(&p).await;
    catch_errors::set_policy(None);

    assert_eq!(named.unwrap(), vec![("output".to_string(), caught_event("CORE.FORBIDDEN", "Internal.Fails"))]);
    assert_eq!(stopped.unwrap_err().to_string(), "child went wrong");
}

#[tokio::test]
async fn the_iteration_guard_is_never_caught() {
    let _lock = exclusive_lock();
    let before = MAX_EXECUTION_ITERATIONS.load(Ordering::Relaxed);
    MAX_EXECUTION_ITERATIONS.store(200, Ordering::Relaxed);
    let p = parent(("Test", "Spin"), true, vec![handler()]);
    let result = run(&p).await;
    MAX_EXECUTION_ITERATIONS.store(before, Ordering::Relaxed);
    let err = result.unwrap_err().to_string();
    assert!(err.starts_with("Execution locked in an infinite loop : Test.Spin exceeded 200"), "{err}");
}

// ------------------------------------------------------------------ the model

#[test]
fn the_property_round_trips_and_is_written_only_when_set() {
    let marked = json!({"statementName": "s", "namespace": "N", "name": "f", "catchErrors": true});
    let s = Statement::from_value(&marked).unwrap();
    assert!(s.is_catch_errors());
    assert_eq!(s.to_json()["catchErrors"], json!(true));
    assert!(Statement::from_value(&s.to_json()).unwrap().is_catch_errors());

    let plain = Statement::from_value(&json!({"statementName": "s", "namespace": "N", "name": "f"})).unwrap();
    assert!(!plain.is_catch_errors());
    assert!(plain.to_json().get("catchErrors").is_none(), "an unmarked step writes as before");
    let off = Statement::from_value(&json!({"statementName": "s", "namespace": "N", "name": "f", "catchErrors": false})).unwrap();
    assert!(off.to_json().get("catchErrors").is_none());
}
