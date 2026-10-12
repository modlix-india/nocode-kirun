//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeWithDefinitionTest.ts`.

use kirun::function::Function;
use kirun::runtime::KIRuntime;
use kirun::Map;
use serde_json::json;

use crate::common::{args, assert_json_eq, fep};
use crate::{exclusive_lock, fd, shared_lock};

#[tokio::test]
async fn ki_runtime_with_definition_1() {
    // debug mode enables the global DebugCollector
    let _lock = exclusive_lock();
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
                "genOutput": {
                    "statementName": "genOutput",
                    "namespace": "System",
                    "name": "GenerateEvent",
                    "parameterMap": {
                        "eventName": {
                            "one": {
                                "key": "one",
                                "type": "VALUE",
                                "value": "output"
                            }
                        },
                        "results": {
                            "one": {
                                "key": "one",
                                "type": "VALUE",
                                "value": {
                                    "name": "additionResult",
                                    "value": {
                                        "isExpression": true,
                                        "value": "Steps.add.output.value"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    "#;

    let f = fd(def);

    let result = KIRuntime::new(f, true)
        .unwrap()
        .execute(&mut fep().set_arguments(args! { "a" => 7, "b" => 11, "c" => 13 }))
        .await
        .unwrap();
    assert_json_eq!(result.all_results()[0].get_result().get("additionResult").cloned().unwrap_or_default(), json!(31));
}

#[tokio::test]
async fn ki_runtime_with_definition_2() {
    let _lock = shared_lock();
    let def = r#"
        {
            "name": "checkWithNoParamsOrEvents",
            "namespace": "UIApp",
            "steps": {
                "add": {
                    "statementName": "add",
                    "namespace": "System.Math",
                    "name": "Add",
                    "parameterMap": {
                        "value": {
                            "one": {
                                "key": "one",
                                "type": "VALUE",
                                "value": 2
                            },
                            "two": {
                                "key": "two",
                                "type": "VALUE",
                                "value": 5
                            }
                        }
                    }
                },
                "genOutput": {
                    "statementName": "genOutput",
                    "namespace": "System",
                    "name": "GenerateEvent",
                    "dependentStatements": {
                        "Steps.add.output": true
                    }
                }
            }
        }
    "#;

    let f = fd(def);

    let result = KIRuntime::new(f, false).unwrap().execute(&mut fep().set_arguments(Map::new())).await.unwrap();

    // JS `toMatchObject({})` holds for any result map; what it really checks is that a first
    // result exists.
    let _ = result.all_results()[0].get_result();
}

#[tokio::test]
async fn ki_runtime_with_definition_3() {
    let _lock = shared_lock();
    let def = r#"
        {
            "name": "Make an error",
            "namespace": "UIApp",
            "steps": {
                "add": {
                    "statementName": "add",
                    "namespace": "System.Math",
                    "name": "Add",
                    "parameterMap": {
                        "value": {
                            "one": {
                                "key": "one",
                                "type": "VALUE",
                                "value": "X"
                            },
                            "two": {
                                "key": "two",
                                "type": "VALUE",
                                "value": 5
                            }
                        }
                    }
                },
                "genOutput": {
                    "statementName": "genOutput",
                    "namespace": "System",
                    "name": "GenerateEvent",
                    "dependentStatements": {
                        "Steps.add.output": true
                    }
                }
            }
        }
    "#;

    let f = fd(def);

    // JS: try { await execute } catch (e) { expect(e.message).toMatch(/error/i) }
    if let Err(e) = KIRuntime::new(f, false).unwrap().execute(&mut fep().set_arguments(Map::new())).await {
        assert!(regex::Regex::new("(?i)error").unwrap().is_match(&e.to_string()), "{e}");
    }
}
