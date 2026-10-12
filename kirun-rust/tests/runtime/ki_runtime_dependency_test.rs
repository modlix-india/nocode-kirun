//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeDependencyTest.ts`.
//! JS spies on `console.log` (called once per `System.Print`); here `System.Print` is a
//! recording stand-in and the spy count is its call count (cumulative, as in JS).

use std::sync::Arc;

use kirun::function::Function;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use serde_json::json;

use crate::common::{args, schema_repo};
use crate::{fd, repo_with_recording_print, shared_lock, RecordingPrint};

#[tokio::test]
async fn ki_runtime_with_execute_if_true_1() {
    let _lock = shared_lock();
    let print = Arc::new(RecordingPrint::new());

    let def = r#"
        {
            "name": "getAppData",
            "namespace": "UIApp",
            "parameters": {
                "a": {
                    "parameterName": "a",
                    "schema": {
                        "name": "INTEGER",
                        "type": [
                            "ARRAY"
                        ],
                        "items": {
                            "type": [
                                "OBJECT",
                                "NULL"
                            ]
                        }
                    }
                }
            },
            "steps": {
                "forEach": {
                    "statementName": "forEach",
                    "namespace": "System.Loop",
                    "name": "ForEachLoop",
                    "parameterMap": {
                        "source": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Arguments.a"
                            }
                        }
                    }
                },
                "print": {
                    "statementName": "print",
                    "namespace": "System",
                    "name": "Print",
                    "parameterMap": {
                        "values": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Steps.forEach.iteration.each.name"
                            }
                        }
                    },
                    "executeIftrue": {
                        "Steps.forEach.iteration.each.name": true
                    }
                }
            }
        }
    "#;

    let f = fd(def);

    KIRuntime::new(f, false)
        .unwrap()
        .execute(
            &mut FunctionExecutionParameters::new(repo_with_recording_print(print.clone(), vec![]), schema_repo())
                .set_arguments(args! { "a" => json!([{ "name": "Kiran", "age": 40 }, null, { "name": "Kumar", "age": 39 }]) }),
        )
        .await
        .unwrap();

    assert_eq!(print.calls().len(), 2);

    let new_def = r#"
        {
            "name": "getAppData",
            "namespace": "UIApp",
            "parameters": {
                "a": {
                    "parameterName": "a",
                    "schema": {
                        "name": "INTEGER",
                        "type": [
                            "ARRAY"
                        ],
                        "items": {
                            "type": [
                                "OBJECT",
                                "NULL"
                            ]
                        }
                    }
                }
            },
            "steps": {
                "forEach": {
                    "statementName": "forEach",
                    "namespace": "System.Loop",
                    "name": "ForEachLoop",
                    "parameterMap": {
                        "source": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Arguments.a"
                            }
                        }
                    }
                },
                "print": {
                    "statementName": "print",
                    "namespace": "System",
                    "name": "Print",
                    "parameterMap": {
                        "values": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Steps.forEach.iteration.each.name"
                            }
                        }
                    },
                    "executeIftrue": {
                        "(Steps.forEach.iteration.each.age ?? 1) % 2 = 0": true
                    }
                }
            }
        }
    "#;

    let f = fd(new_def);

    let _result = KIRuntime::new(f, false)
        .unwrap()
        .execute(
            &mut FunctionExecutionParameters::new(repo_with_recording_print(print.clone(), vec![]), schema_repo())
                .set_arguments(args! { "a" => json!([{ "name": "Kiran", "age": 40 }, null, { "name": "Kumar", "age": 39 }]) }),
        )
        .await
        .unwrap();

    assert_eq!(print.calls().len(), 3);
}
