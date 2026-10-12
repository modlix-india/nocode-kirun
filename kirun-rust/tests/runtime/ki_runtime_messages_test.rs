//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeMessagesTest.ts`.

use kirun::function::Function;
use kirun::runtime::KIRuntime;

use crate::common::{args, assert_err_contains, fep, function_repo, schema_repo};
use crate::{fd, shared_lock};

#[tokio::test]
async fn messages_test_1() {
    let _lock = shared_lock();
    let func = r#"
        {
            "name": "loginFunction",
            "steps": {
                "messageStep": {
                    "statementName": "messageStep",
                    "namespace": "UIEngine",
                    "name": "Message",
                    "parameterMap": {
                        "msg": {
                            "value1": {
                                "key": "value1",
                                "type": "EXPRESSION",
                                "expression": "Steps.loginStep.error.data"
                            }
                        }
                    },
                    "position": {
                        "left": 198,
                        "top": 245
                    }
                },
                "genOutput": {
                    "statementName": "genOutput",
                    "namespace": "System",
                    "name": "GenerateEvent",
                    "dependentStatements": {
                        "Steps.loginStep.output": true
                    },
                    "position": {
                        "left": 482,
                        "top": 172
                    }
                },
                "loginStep1": {
                    "name": "Login",
                    "namespace": "UIEngine",
                    "statementName": "loginStep1",
                    "parameterMap": {
                        "userName": {
                            "value1": {
                                "key": "value1",
                                "type": "EXPRESSION",
                                "expression": "Page.user.userName"
                            }
                        },
                        "password": {
                            "value1": {
                                "key": "value1",
                                "type": "EXPRESSION",
                                "expression": "Page.user.password"
                            }
                        },
                        "rememberMe": {
                            "value1": {
                                "key": "value1",
                                "type": "EXPRESSION",
                                "expression": "Page.user.rememberMe"
                            }
                        }
                    },
                    "position": {
                        "left": 472,
                        "top": 333
                    }
                }
            }
        }
    "#;

    let f = fd(func);

    let graph = KIRuntime::new(f, false)
        .unwrap()
        .get_execution_plan(&function_repo(), &schema_repo())
        .await
        .unwrap();

    let messages: Vec<String> = graph
        .get_node_map()
        .values()
        .flat_map(|node| {
            node.get_data()
                .get_messages()
                .iter()
                .map(|e| e.get_message().unwrap_or_default().to_string())
                .collect::<Vec<_>>()
        })
        .collect();

    assert_eq!(
        messages,
        vec![
            "UIEngine.Message is not available",
            "Unable to find the step with name loginStep",
            "UIEngine.Login is not available",
        ]
    );
}

#[tokio::test]
async fn messages_test_2() {
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

    let graph = KIRuntime::new(f.clone(), false)
        .unwrap()
        .get_execution_plan(&function_repo(), &schema_repo())
        .await
        .unwrap();

    let messages: Vec<String> = graph
        .get_node_map()
        .values()
        .flat_map(|node| {
            node.get_data()
                .get_messages()
                .iter()
                .map(|e| e.get_message().unwrap_or_default().to_string())
                .collect::<Vec<_>>()
        })
        .collect();

    assert_eq!(messages, vec!["Unable to find the step with name add"]);

    assert_err_contains(
        KIRuntime::new(f, false)
            .unwrap()
            .execute(&mut fep().set_arguments(args! { "a" => 7, "b" => 11, "c" => 13 }))
            .await,
        "Unable to find the step with name add",
    );
}
