//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeUndefinedParamTest.ts`.
//! JS `value: undefined` keys are dropped by JSON; both are `Value::Null` in Rust anyway.

use std::sync::Arc;

use async_trait::async_trait;
use kirun::function::{AbstractFunction, Function};
use kirun::json::schema::Schema;
use kirun::model::{EventResult, FunctionOutput, FunctionSignature, Parameter};
use kirun::namespaces::Namespaces;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use kirun::util::map_util::MapUtil;
use kirun::{Map, Value};

use crate::common::schema_repo;
use crate::{fd, hybrid_with, shared_lock, AlwaysRepository};

struct Print {
    signature: FunctionSignature,
}

impl Print {
    const VALUES: &'static str = "values";
    const VALUE: &'static str = "value";

    fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Print").set_namespace(Namespaces::SYSTEM).set_parameters(
                MapUtil::of_entries_array(vec![
                    Parameter::of_entry(Print::VALUES, Schema::of_any(Print::VALUES), true),
                    Parameter::of_entry(Print::VALUE, Schema::of_any(Print::VALUE), false),
                ]),
            ),
        }
    }
}

#[async_trait]
impl AbstractFunction for Print {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> kirun::Result<FunctionOutput> {
        let values = context.get_arguments().and_then(|a| a.get(Print::VALUES)).cloned().unwrap_or(Value::Null);
        let value = context.get_arguments().and_then(|a| a.get(Print::VALUE)).cloned().unwrap_or(Value::Null);

        println!("Values {values}");
        println!("Value {value}");

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}

#[tokio::test]
async fn ki_runtime_undefined_and_null_param_type_with_varargs() {
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
                        "values": {
                            "one": {
                                "key": "one",
                                "type": "VALUE",
                                "value": null
                            },
                            "two": {
                                "key": "two",
                                "type": "VALUE"
                            },
                            "three": {
                                "key": "three",
                                "type": "EXPRESSION",
                                "expression": "null"
                            },
                            "four": {
                                "key": "four",
                                "type": "VALUE",
                                "value": 12
                            }
                        },
                        "value": {}
                    }
                }
            }
        }
    "#;

    let f = fd(def);

    let tst_print: Arc<dyn Function> = Arc::new(Print::new());

    let fo = KIRuntime::new(f, false)
        .unwrap()
        .execute(
            &mut FunctionExecutionParameters::new(hybrid_with(Arc::new(AlwaysRepository(tst_print))), schema_repo())
                .set_arguments(Map::new()),
        )
        .await
        .unwrap();
    assert_eq!(fo.all_results()[0].get_result().len(), 0);
}

#[tokio::test]
async fn ki_runtime_undefined_and_null_param_type_without_varargs() {
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
                        "value": {
                            "two": {
                                "key": "two",
                                "type": "VALUE"
                            }
                        }
                    }
                }
            }
        }
    "#;

    let f = fd(def);

    let tst_print: Arc<dyn Function> = Arc::new(Print::new());

    assert_eq!(
        KIRuntime::new(f, false)
            .unwrap()
            .execute(
                &mut FunctionExecutionParameters::new(hybrid_with(Arc::new(AlwaysRepository(tst_print))), schema_repo())
                    .set_arguments(Map::new()),
            )
            .await
            .unwrap()
            .all_results()[0]
            .get_result()
            .len(),
        0
    );
}
