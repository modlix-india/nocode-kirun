//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeExtended.ts`.

use kirun::function::Function;
use kirun::runtime::KIRuntime;
use serde_json::json;

use crate::common::{args, assert_json_eq, fep};
use crate::{exclusive_lock, fd};

#[tokio::test]
async fn ki_runtime_extended_definition() {
    // debug mode enables the global DebugCollector
    let _lock = exclusive_lock();

    let def = r#"
        {
            "name": "checkExtendedFunction",
            "namespace": "Test",
            "parameters": {
                "a": {
                    "parameterName": "a",
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
                "add1": {
                    "statementName": "add1",
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
                                "type": "VALUE",
                                "value": 1
                            }
                        }
                    }
                },
                "if1": {
                    "statementName": "if1",
                    "namespace": "System",
                    "name": "If",
                    "parameterMap": {
                        "condition": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Steps.add1.output.value % 2 = 0"
                            }
                        }
                    }
                },
                "add2": {
                    "statementName": "add2",
                    "namespace": "System.Math",
                    "name": "Add",
                    "parameterMap": {
                        "value": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Steps.add1.output.value"
                            },
                            "two": {
                                "key": "two",
                                "type": "VALUE",
                                "value": 2
                            }
                        }
                    },
                    "dependentStatements": {
                        "Steps.if1.true": true
                    }
                },
                "add3": {
                    "statementName": "add3",
                    "namespace": "System.Math",
                    "name": "Add",
                    "parameterMap": {
                        "value": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Steps.add2.output.value"
                            },
                            "two": {
                                "key": "two",
                                "type": "VALUE",
                                "value": 2
                            }
                        }
                    }
                },
                "add4": {
                    "statementName": "add4",
                    "namespace": "System.Math",
                    "name": "Add",
                    "parameterMap": {
                        "value": {
                            "one": {
                                "key": "one",
                                "type": "EXPRESSION",
                                "expression": "Steps.add1.output.value"
                            },
                            "two": {
                                "key": "two",
                                "type": "VALUE",
                                "value": 1
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
                                        "value": "Steps.add4.output.value"
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
        .execute(&mut fep().set_arguments(args! { "a" => 7 }))
        .await
        .unwrap();

    assert_json_eq!(result.all_results()[0].get_result().get("additionResult").cloned().unwrap_or_default(), json!(9));
}
