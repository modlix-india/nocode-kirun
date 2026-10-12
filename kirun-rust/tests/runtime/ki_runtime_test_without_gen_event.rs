//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeTestWithoutGenEvent.ts`.
//! JS mocks `console.log`; here `System.Print` is a recording stand-in.

use std::sync::Arc;

use kirun::function::Function;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use serde_json::json;

use crate::common::{args, assert_json_eq, fep, schema_repo};
use crate::{fd, repo_with_recording_print, shared_lock, RecordingPrint};

#[tokio::test]
async fn ki_runtime_without_gen_event_definition_1() {
    let _lock = shared_lock();
    let def = r#"
        {
            "name": "getAppData",
            "namespace": "UIApp",
            "parameters": {
                "a": {
                    "parameterName": "a",
                    "schema": {
                        "name": "INTEGER",
                        "type": "INTEGER"
                    }
                },
                "b": {
                    "parameterName": "b",
                    "schema": {
                        "name": "INTEGER",
                        "type": "INTEGER"
                    }
                },
                "c": {
                    "parameterName": "c",
                    "schema": {
                        "name": "INTEGER",
                        "type": "INTEGER"
                    }
                }
            },
            "steps": {
                "add": {
                    "statementName": "add",
                    "namespace": "System.Math",
                    "name": "Add",
                    "parameterMap": {
                        "value": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Arguments.a"
                            },
                            "two": {
                                "key": "two",
                                "type": "EXPRESSION",
                                "expression": "10 + 1"
                            },
                            "three": {
                                "key": "three",
                                "type": "EXPRESSION",
                                "expression": "Arguments.c"
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
                                "expression": "Steps.add.output.value",
                                "order": 2
                            },
                            "abc": {
                                "key": "abc",
                                "type": "VALUE",
                                "value": "Nothing muchh....",
                                "order": 1
                            }
                        }
                    }
                }
            }
        }
    "#;

    let f = fd(def);

    let print = Arc::new(RecordingPrint::new());

    KIRuntime::new(f, false)
        .unwrap()
        .execute(
            &mut FunctionExecutionParameters::new(repo_with_recording_print(print.clone(), vec![]), schema_repo())
                .set_arguments(args! { "a" => 7, "b" => 11, "c" => 13 }),
        )
        .await
        .unwrap();
    let calls = print.calls();
    assert_eq!(calls[0][0], json!("Nothing muchh...."));
    assert_json_eq!(calls[0][1].clone(), json!(31));
}

#[tokio::test]
async fn ki_runtime_without_gen_event_definition_2() {
    let _lock = shared_lock();
    let def = r#"
        {
            "name": "getAppData",
            "namespace": "UIApp",
            "parameters": {
                "a": {
                    "parameterName": "a",
                    "schema": {
                        "name": "INTEGER",
                        "type": "INTEGER"
                    }
                },
                "b": {
                    "parameterName": "b",
                    "schema": {
                        "name": "INTEGER",
                        "type": "INTEGER"
                    }
                },
                "c": {
                    "parameterName": "c",
                    "schema": {
                        "name": "INTEGER",
                        "type": "INTEGER"
                    }
                }
            },
            "events": {
                "output": {
                    "name": "output",
                    "parameters": {}
                }
            },
            "steps": {
                "add": {
                    "statementName": "add",
                    "namespace": "System.Math",
                    "name": "Add",
                    "parameterMap": {
                        "value": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Arguments.a"
                            },
                            "two": {
                                "key": "two",
                                "type": "EXPRESSION",
                                "expression": "10 + 1"
                            },
                            "three": {
                                "key": "three",
                                "type": "EXPRESSION",
                                "expression": "Arguments.c"
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
                                "expression": "Steps.add.output.value",
                                "order": 2
                            },
                            "abc": {
                                "key": "abc",
                                "type": "VALUE",
                                "value": "Something muchh....",
                                "order": 1
                            }
                        }
                    }
                }
            }
        }
    "#;

    let f = fd(def);

    let print = Arc::new(RecordingPrint::new());

    KIRuntime::new(f, false)
        .unwrap()
        .execute(
            &mut FunctionExecutionParameters::new(repo_with_recording_print(print.clone(), vec![]), schema_repo())
                .set_arguments(args! { "a" => 7, "b" => 11, "c" => 13 }),
        )
        .await
        .unwrap();
    let calls = print.calls();
    assert_eq!(calls[0][0], json!("Something muchh...."));
    assert_json_eq!(calls[0][1].clone(), json!(31));
}

#[tokio::test]
async fn ki_runtime_with_definition_1() {
    let _lock = shared_lock();
    let def = r#"
        {
            "name": "getAppData",
            "namespace": "UIApp",
            "parameters": {
                "a": {
                    "parameterName": "a",
                    "schema": {
                        "name": "INTEGER",
                        "type": "INTEGER"
                    }
                },
                "b": {
                    "parameterName": "b",
                    "schema": {
                        "name": "INTEGER",
                        "type": "INTEGER"
                    }
                },
                "c": {
                    "parameterName": "c",
                    "schema": {
                        "name": "INTEGER",
                        "type": "INTEGER"
                    }
                }
            },
            "events": {
                "output": {
                    "name": "output",
                    "parameters": {
                        "additionResult": {
                            "name": "additionResult",
                            "type": "INTEGER"
                        }
                    }
                }
            },
            "steps": {
                "add": {
                    "statementName": "add",
                    "namespace": "System.Math",
                    "name": "Add",
                    "parameterMap": {
                        "value": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Arguments.a"
                            },
                            "two": {
                                "key": "two",
                                "type": "EXPRESSION",
                                "expression": "10 + 1"
                            },
                            "three": {
                                "key": "three",
                                "type": "EXPRESSION",
                                "expression": "Arguments.c"
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
                                "expression": "Steps.add.output.value",
                                "order": 2
                            },
                            "abc": {
                                "key": "abc",
                                "type": "VALUE",
                                "value": "Something muchh....",
                                "order": 1
                            }
                        }
                    }
                }
            }
        }
    "#;

    let f = fd(def);

    let mut fep = fep().set_arguments(args! { "a" => 7, "b" => 11, "c" => 13 });

    let runtime = KIRuntime::new(f, false).unwrap();

    assert!(runtime.execute(&mut fep).await.is_err());
}
