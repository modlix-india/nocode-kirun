//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeValuesEmptyTest.ts`.
//!
//! JS spies on `console.log` once for the whole file, so its expected counts are cumulative
//! (4, 8, 12): each run calls the test Print once, which logs 4 lines. Here every test has its
//! own Print and asserts its own 4 lines. JS also wraps each run in try/catch (which would
//! swallow even a failed expect); kirun-js runs all three without error (checked), so the port
//! asserts success.

use std::sync::Arc;

use async_trait::async_trait;
use kirun::function::{AbstractFunction, Function};
use kirun::json::schema::Schema;
use kirun::model::{EventResult, FunctionOutput, FunctionSignature, Parameter};
use kirun::namespaces::Namespaces;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use kirun::util::map_util::MapUtil;
use kirun::{Map, Value};
use parking_lot::Mutex;
use serde_json::json;

use crate::common::schema_repo;
use crate::{fd, hybrid_with, shared_lock, AlwaysRepository};

struct Print {
    signature: FunctionSignature,
    /// The `console.log` lines this Print would write.
    logs: Arc<Mutex<Vec<Vec<Value>>>>,
}

impl Print {
    const VALUES: &'static str = "values";
    const VALUE: &'static str = "value";

    fn new() -> Self {
        let pick1 = format!("{}Pick1", Print::VALUE);
        let pick2 = format!("{}Pick2", Print::VALUE);
        Self {
            signature: FunctionSignature::new("Print").set_namespace(Namespaces::SYSTEM).set_parameters(
                MapUtil::of_entries_array(vec![
                    Parameter::of_entry(Print::VALUES, Schema::of_any(Print::VALUES), true),
                    Parameter::of_entry(Print::VALUE, Schema::of_any(Print::VALUE), false),
                    Parameter::of_entry(&pick1, Schema::of_boolean(&pick1).set_default_value(json!(false)), false),
                    Parameter::of_entry(&pick2, Schema::of_string(&pick2).set_default_value(json!("")), false),
                ]),
            ),
            logs: Arc::new(Mutex::new(Vec::new())),
        }
    }
}

#[async_trait]
impl AbstractFunction for Print {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> kirun::Result<FunctionOutput> {
        let get = |k: &str| context.get_arguments().and_then(|a| a.get(k)).cloned().unwrap_or(Value::Null);
        let values = get(Print::VALUES);
        let value = get(Print::VALUE);
        let value_pick1 = get(&format!("{}Pick1", Print::VALUE));
        let value_pick2 = get(&format!("{}Pick2", Print::VALUE));

        let mut logs = self.logs.lock();
        let mut first = vec![json!("Values")];
        first.extend(values.as_array().cloned().unwrap_or_default());
        logs.push(first);
        logs.push(vec![json!("Value"), value]);
        logs.push(vec![json!("ValuePick1"), value_pick1]);
        logs.push(vec![json!("ValuePick2"), value_pick2]);

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}

async fn run(def: &str) -> usize {
    let f = fd(def);

    let tst_print = Arc::new(Print::new());
    let logs = tst_print.logs.clone();
    let tst_print: Arc<dyn Function> = tst_print;

    let fo = KIRuntime::new(f, false)
        .unwrap()
        .execute(
            &mut FunctionExecutionParameters::new(hybrid_with(Arc::new(AlwaysRepository(tst_print))), schema_repo())
                .set_arguments(Map::new()),
        )
        .await;
    if let Err(e) = &fo {
        eprintln!("{e}");
    }
    assert!(fo.is_ok());
    let n = logs.lock().len();
    n
}

#[tokio::test]
async fn ki_runtime_with_definition_no_values_passed() {
    let _lock = shared_lock();
    let def = r#"
        {
            "name": "Make an error",
            "namespace": "UIApp",
            "steps": {
                "print": {
                    "statementName": "print",
                    "namespace": "function",
                    "name": "test",
                    "parameterMap": {
                        "values": {}
                    }
                }
            }
        }
    "#;

    assert_eq!(run(def).await, 4);
}

#[tokio::test]
async fn ki_runtime_with_definition_with_no_value_passed() {
    let _lock = shared_lock();
    let def = r#"
        {
            "name": "Make an error",
            "namespace": "UIApp",
            "steps": {
                "print": {
                    "statementName": "print",
                    "namespace": "function",
                    "name": "test",
                    "parameterMap": {
                        "value": {}
                    }
                }
            }
        }
    "#;

    assert_eq!(run(def).await, 4); // JS: 8, cumulative over the file
}

#[tokio::test]
async fn ki_runtime_with_definition_with_no_value_and_values_passed() {
    let _lock = shared_lock();
    let def = r#"
        {
            "name": "Make an error",
            "namespace": "UIApp",
            "steps": {
                "print": {
                    "statementName": "print",
                    "namespace": "function",
                    "name": "test",
                    "parameterMap": {
                        "value": {},
                        "values": {}
                    }
                }
            }
        }
    "#;

    assert_eq!(run(def).await, 4); // JS: 12, cumulative over the file
}
