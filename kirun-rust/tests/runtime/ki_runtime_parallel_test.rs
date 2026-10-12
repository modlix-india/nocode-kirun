//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeParallelTest.ts`.

use std::sync::Arc;
use std::time::Instant;

use kirun::function::Function;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use serde_json::json;

use crate::common::schema_repo;
use crate::{fd, repo_with_recording_print, shared_lock, ListRepository, RecordingPrint};

#[tokio::test]
async fn ki_runtime_parallel_execution_waits_for_all_paths_to_complete() {
    let _lock = shared_lock();
    // 3 separate functions, one per parallel path:
    //   Path1: Wait 2000ms -> Print "2000 Finished"
    //   Path2: Wait 3000ms -> Print "3000 Finished"
    //   Path3: Print "Without Time Finished" (instant)
    //
    // Main function calls all 3 in parallel and should wait for all to complete.

    let path1: Arc<dyn Function> = Arc::new(
        KIRuntime::new(
            fd(r#"
                {
                    "name": "Path1",
                    "namespace": "Internal",
                    "events": {
                        "output": {
                            "name": "output",
                            "parameters": {}
                        }
                    },
                    "steps": {
                        "wait": {
                            "statementName": "wait",
                            "namespace": "System",
                            "name": "Wait",
                            "parameterMap": {
                                "millis": {
                                    "one": {
                                        "key": "one",
                                        "type": "VALUE",
                                        "value": 2000
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
                                        "type": "VALUE",
                                        "value": "2000 Finished"
                                    }
                                }
                            },
                            "dependentStatements": {
                                "Steps.wait.output": true
                            }
                        },
                        "genOutput": {
                            "statementName": "genOutput",
                            "namespace": "System",
                            "name": "GenerateEvent",
                            "dependentStatements": {
                                "Steps.print.output": true
                            },
                            "parameterMap": {
                                "eventName": {
                                    "one": {
                                        "key": "one",
                                        "type": "VALUE",
                                        "value": "output"
                                    }
                                }
                            }
                        }
                    }
                }
            "#),
            false,
        )
        .unwrap(),
    );

    let path2: Arc<dyn Function> = Arc::new(
        KIRuntime::new(
            fd(r#"
                {
                    "name": "Path2",
                    "namespace": "Internal",
                    "events": {
                        "output": {
                            "name": "output",
                            "parameters": {}
                        }
                    },
                    "steps": {
                        "wait": {
                            "statementName": "wait",
                            "namespace": "System",
                            "name": "Wait",
                            "parameterMap": {
                                "millis": {
                                    "one": {
                                        "key": "one",
                                        "type": "VALUE",
                                        "value": 3000
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
                                        "type": "VALUE",
                                        "value": "3000 Finished"
                                    }
                                }
                            },
                            "dependentStatements": {
                                "Steps.wait.output": true
                            }
                        },
                        "genOutput": {
                            "statementName": "genOutput",
                            "namespace": "System",
                            "name": "GenerateEvent",
                            "dependentStatements": {
                                "Steps.print.output": true
                            },
                            "parameterMap": {
                                "eventName": {
                                    "one": {
                                        "key": "one",
                                        "type": "VALUE",
                                        "value": "output"
                                    }
                                }
                            }
                        }
                    }
                }
            "#),
            false,
        )
        .unwrap(),
    );

    let path3: Arc<dyn Function> = Arc::new(
        KIRuntime::new(
            fd(r#"
                {
                    "name": "Path3",
                    "namespace": "Internal",
                    "events": {
                        "output": {
                            "name": "output",
                            "parameters": {}
                        }
                    },
                    "steps": {
                        "print": {
                            "statementName": "print",
                            "namespace": "System",
                            "name": "Print",
                            "parameterMap": {
                                "values": {
                                    "one": {
                                        "key": "one",
                                        "type": "VALUE",
                                        "value": "Without Time Finished"
                                    }
                                }
                            }
                        },
                        "genOutput": {
                            "statementName": "genOutput",
                            "namespace": "System",
                            "name": "GenerateEvent",
                            "dependentStatements": {
                                "Steps.print.output": true
                            },
                            "parameterMap": {
                                "eventName": {
                                    "one": {
                                        "key": "one",
                                        "type": "VALUE",
                                        "value": "output"
                                    }
                                }
                            }
                        }
                    }
                }
            "#),
            false,
        )
        .unwrap(),
    );

    // Main function calls all 3 paths in parallel
    let main_def = fd(r#"
        {
            "name": "TestParallel",
            "namespace": "UIApp",
            "steps": {
                "exPath1": {
                    "statementName": "exPath1",
                    "name": "Path1",
                    "namespace": "Internal",
                    "parameterMap": {}
                },
                "exPath2": {
                    "statementName": "exPath2",
                    "name": "Path2",
                    "namespace": "Internal",
                    "parameterMap": {}
                },
                "exPath3": {
                    "statementName": "exPath3",
                    "name": "Path3",
                    "namespace": "Internal",
                    "parameterMap": {}
                }
            }
        }
    "#);

    // JS spies on console.log; System.Print is replaced by a recording stand-in.
    let print = Arc::new(RecordingPrint::new());
    let repo = repo_with_recording_print(print.clone(), vec![Arc::new(ListRepository(vec![path1, path2, path3]))]);

    let start_time = Instant::now();

    KIRuntime::new(main_def, false)
        .unwrap()
        .execute(&mut FunctionExecutionParameters::new(repo, schema_repo()))
        .await
        .unwrap();

    let elapsed = start_time.elapsed().as_millis();

    // All 3 print statements should have been called
    let calls = print.calls();
    assert_eq!(calls.len(), 3);
    assert!(calls.contains(&vec![json!("Without Time Finished")]));
    assert!(calls.contains(&vec![json!("2000 Finished")]));
    assert!(calls.contains(&vec![json!("3000 Finished")]));

    // Execution should have waited for the longest path (3000ms).
    // If parallel execution finishes early (before all paths), elapsed would be < 2000ms.
    assert!(elapsed >= 2000);
}
