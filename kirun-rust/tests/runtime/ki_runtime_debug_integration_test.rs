//! Port of kirun-js `__tests__/engine/runtime/debug/KIRuntimeDebugIntegration.test.ts`.

use std::sync::Arc;

use kirun::function::Function;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};

use crate::common::{assert_json_eq, schema_repo};
use crate::debug_collector_test::Setup;
use crate::{fd, hybrid_with, ListRepository};

mod ki_runtime_debug_integration {
    use super::*;
    use serde_json::json;

    #[tokio::test]
    async fn should_track_nested_ki_runtime_execution_hierarchy() {
        let s = Setup::new();
        let collector = s.collector;

        // First function calls Second and Third
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
                            "dependentStatements": {
                                "Steps.exSecond.output": true
                            },
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
            true, // debugMode enabled
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
                true, // debugMode enabled
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
                true, // debugMode enabled
            )
            .unwrap(),
        );

        let repo = hybrid_with(Arc::new(ListRepository(vec![third, second])));

        let mut fep = FunctionExecutionParameters::with_execution_id(repo, schema_repo(), "test-nested");
        let results = first.execute(&mut fep).await.unwrap().next();

        // Verify result
        assert_json_eq!(results.unwrap().get_result().get("aresult").cloned().unwrap_or_default(), json!(9));

        // Verify debug info
        let execution = collector.get_execution("test-nested");
        assert!(execution.is_some());
        let execution = execution.unwrap();

        // All 3 function definitions should be stored
        assert_eq!(execution.definitions.len(), 3);
        assert!(execution.definitions.contains_key("Internal.First"));
        assert!(execution.definitions.contains_key("Internal.Second"));
        assert!(execution.definitions.contains_key("Internal.Third"));

        // Check hierarchy - First's steps should be at root level
        // exSecond should have Second's genOutput as child
        // exThird should have Third's genOutput as child
        let flat_logs = collector.get_flat_logs("test-nested");
        assert!(!flat_logs.is_empty());

        // Root logs should be First's steps
        assert_eq!(execution.logs.len(), 3); // exSecond, exThird, genOutput

        // Find exSecond step - it should have nested children from Second
        let ex_second_log = execution.logs.iter().find(|l| l.statement_name.as_deref() == Some("exSecond"));
        assert!(ex_second_log.is_some());
        let ex_second_log = ex_second_log.unwrap();
        assert_eq!(ex_second_log.children.len(), 1); // Second's genOutput
        assert_eq!(ex_second_log.children[0].statement_name.as_deref(), Some("genOutput"));

        // Find exThird step - it should have nested children from Third
        let ex_third_log = execution.logs.iter().find(|l| l.statement_name.as_deref() == Some("exThird"));
        assert!(ex_third_log.is_some());
        let ex_third_log = ex_third_log.unwrap();
        assert_eq!(ex_third_log.children.len(), 1); // Third's genOutput
        assert_eq!(ex_third_log.children[0].statement_name.as_deref(), Some("genOutput"));

        println!("Definitions: {:?}", execution.definitions.keys().collect::<Vec<_>>());
        println!("Root logs: {:?}", execution.logs.iter().map(|l| l.statement_name.clone()).collect::<Vec<_>>());
        println!("exSecond children: {:?}", ex_second_log.children.iter().map(|l| l.statement_name.clone()).collect::<Vec<_>>());
        println!("exThird children: {:?}", ex_third_log.children.iter().map(|l| l.statement_name.clone()).collect::<Vec<_>>());
    }
}
