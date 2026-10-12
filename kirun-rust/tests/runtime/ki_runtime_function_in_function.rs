//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeFunctionInFunction.ts`.

use std::sync::Arc;

use kirun::function::Function;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use serde_json::json;

use crate::common::{assert_json_eq, schema_repo};
use crate::{exclusive_lock, fd, hybrid_with, ListRepository};

#[tokio::test]
async fn ki_runtime_function_in_function() {
    // debug mode enables the global DebugCollector
    let _lock = exclusive_lock();

    let first = KIRuntime::new(
        fd(r#"
            {
                "name": "First",
                "namespace": "Internal",
                "events": {
                    "output": {
                        "name": "output",
                        "parameters": {
                            "aresult": {
                                "name": "aresult",
                                "type": "INTEGER"
                            }
                        }
                    }
                },
                "steps": {
                    "exSecond": {
                        "statementName": "exSecond",
                        "name": "Second",
                        "namespace": "Internal",
                        "parameterMap": {
                            "value": {
                                "one": {
                                    "key": "one",
                                    "type": "VALUE",
                                    "value": 2
                                }
                            }
                        }
                    },
                    "exThird": {
                        "statementName": "exThird",
                        "name": "Third",
                        "namespace": "Internal",
                        "parameterMap": {
                            "value": {
                                "one": {
                                    "key": "one",
                                    "type": "VALUE",
                                    "value": 3
                                }
                            }
                        }
                    },
                    "genOutput": {
                        "statementName": "genOutput",
                        "namespace": "System",
                        "name": "GenerateEvent",
                        "dependentStatements": {
                            "Steps.exSecond.output": true,
                            "Steps.exThird.output": true
                        },
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
                                        "name": "aresult",
                                        "value": {
                                            "isExpression": true,
                                            "value": "Steps.exThird.output.result"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        "#),
        true,
    )
    .unwrap();

    let second: Arc<dyn Function> = Arc::new(
        KIRuntime::new(
            fd(r#"
                {
                    "name": "Second",
                    "namespace": "Internal",
                    "parameters": {
                        "value": {
                            "parameterName": "value",
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
                                "result": {
                                    "name": "result",
                                    "type": "INTEGER"
                                }
                            }
                        }
                    },
                    "steps": {
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
                                            "name": "result",
                                            "value": {
                                                "isExpression": true,
                                                "value": "Arguments.value * 2"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            "#),
            true,
        )
        .unwrap(),
    );

    let third: Arc<dyn Function> = Arc::new(
        KIRuntime::new(
            fd(r#"
                {
                    "name": "Third",
                    "namespace": "Internal",
                    "parameters": {
                        "value": {
                            "parameterName": "value",
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
                                "result": {
                                    "name": "result",
                                    "type": "INTEGER"
                                }
                            }
                        }
                    },
                    "steps": {
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
                                            "name": "result",
                                            "value": {
                                                "isExpression": true,
                                                "value": "Arguments.value * 3"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            "#),
            true,
        )
        .unwrap(),
    );

    let repo = hybrid_with(Arc::new(ListRepository(vec![third, second])));

    let mut fep = FunctionExecutionParameters::with_execution_id(repo, schema_repo(), "Testing");
    let results = first.execute(&mut fep).await.unwrap().next();

    assert_json_eq!(results.unwrap().get_result().get("aresult").cloned().unwrap_or_default(), json!(9));
}
