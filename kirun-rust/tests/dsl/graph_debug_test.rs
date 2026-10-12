//! Port of kirun-js `__tests__/engine/dsl/GraphDebugTest.ts`.

use indexmap::IndexSet;
use kirun::dsl::JSONToTextTransformer;
use kirun::model::FunctionDefinition;
use kirun::runtime::KIRuntime;
use kirun::Value;

use crate::common::{function_repo, schema_repo};

// Fibonacci function from test-data
fn fibonacci_json() -> Value {
    serde_json::from_str(
        r#"
{
    "name": "fibonaccii",
    "namespace": "TestUI",
    "parameters": {
        "n": {
            "parameterName": "n",
            "schema": {
                "type": [
                    "INTEGER"
                ],
                "version": 1
            }
        }
    },
    "events": {
        "output": {
            "name": "output",
            "parameters": {
                "result": {
                    "type": [
                        "ARRAY"
                    ],
                    "version": 1,
                    "items": {
                        "type": [
                            "INTEGER"
                        ]
                    }
                }
            }
        }
    },
    "steps": {
        "create": {
            "statementName": "create",
            "name": "Create",
            "namespace": "System.Context",
            "parameterMap": {
                "name": {
                    "2JUURU6NHj9voaArLv0pS8": {
                        "key": "2JUURU6NHj9voaArLv0pS8",
                        "type": "VALUE",
                        "order": 1,
                        "value": "a"
                    }
                },
                "schema": {
                    "2vxDXyOakSmhiUmH4CuDED": {
                        "key": "2vxDXyOakSmhiUmH4CuDED",
                        "type": "VALUE",
                        "order": 1,
                        "value": {
                            "type": "ARRAY",
                            "items": {
                                "type": "INTEGER"
                            }
                        }
                    }
                }
            }
        },
        "rangeLoop": {
            "statementName": "rangeLoop",
            "name": "RangeLoop",
            "namespace": "System.Loop",
            "parameterMap": {
                "to": {
                    "6RAIEdM1OAek2AKf64ucqM": {
                        "key": "6RAIEdM1OAek2AKf64ucqM",
                        "type": "EXPRESSION",
                        "expression": "Arguments.n",
                        "value": 1
                    }
                }
            },
            "dependentStatements": {
                "Steps.create.output": false,
                "Steps.set2.output": true
            }
        },
        "if": {
            "statementName": "if",
            "name": "If",
            "namespace": "System",
            "parameterMap": {
                "condition": {
                    "C2FB54BobtFXTmLKv4v6s": {
                        "key": "C2FB54BobtFXTmLKv4v6s",
                        "type": "EXPRESSION",
                        "expression": "Steps.rangeLoop.iteration.index < 2",
                        "order": 1
                    }
                }
            }
        },
        "trueInsert": {
            "statementName": "trueInsert",
            "name": "InsertLast",
            "namespace": "System.Array",
            "parameterMap": {
                "source": {
                    "4DzS3icjwGl7nUPvk7QUST": {
                        "key": "4DzS3icjwGl7nUPvk7QUST",
                        "type": "EXPRESSION",
                        "expression": "Context.a",
                        "order": 1
                    }
                },
                "element": {
                    "1sWH8NKqkdKevU7vQJk9jn": {
                        "key": "1sWH8NKqkdKevU7vQJk9jn",
                        "type": "EXPRESSION",
                        "expression": "Steps.rangeLoop.iteration.index",
                        "order": 1
                    }
                }
            },
            "dependentStatements": {
                "Steps.if.true": true
            }
        },
        "set": {
            "statementName": "set",
            "name": "Set",
            "namespace": "System.Context",
            "parameterMap": {
                "name": {
                    "UPMhlPSuFHCygh0DYa7Zc": {
                        "key": "UPMhlPSuFHCygh0DYa7Zc",
                        "type": "VALUE",
                        "order": 1,
                        "value": "Context.a"
                    }
                },
                "value": {
                    "1B8Bfavmym6BmbdsDesmNX": {
                        "key": "1B8Bfavmym6BmbdsDesmNX",
                        "type": "EXPRESSION",
                        "expression": "Steps.trueInsert.output.result",
                        "order": 1
                    }
                }
            },
            "dependentStatements": {
                "Steps.if.true": false
            }
        },
        "set1": {
            "statementName": "set1",
            "name": "Set",
            "namespace": "System.Context",
            "parameterMap": {
                "name": {
                    "6dYVAyspJbF82O8qJm3f2O": {
                        "key": "6dYVAyspJbF82O8qJm3f2O",
                        "type": "VALUE",
                        "order": 1,
                        "value": "Context.a"
                    }
                },
                "value": {
                    "fVB75EKlR6It0i7iho3oQ": {
                        "key": "fVB75EKlR6It0i7iho3oQ",
                        "type": "EXPRESSION",
                        "expression": "Steps.falseInsert.output.result",
                        "order": 1
                    }
                }
            },
            "dependentStatements": {
                "Steps.if.false": false
            }
        },
        "falseInsert": {
            "statementName": "falseInsert",
            "name": "InsertLast",
            "namespace": "System.Array",
            "parameterMap": {
                "source": {
                    "1GsJu3FbuponBIcaqI48rF": {
                        "key": "1GsJu3FbuponBIcaqI48rF",
                        "type": "EXPRESSION",
                        "expression": "Context.a",
                        "order": 1
                    }
                },
                "element": {
                    "53vsp50RjvhS94xdCtWWx0": {
                        "key": "53vsp50RjvhS94xdCtWWx0",
                        "type": "EXPRESSION",
                        "expression": "Context.a[Steps.rangeLoop.iteration.index - 1] + Context.a[Steps.rangeLoop.iteration.index - 2]",
                        "order": 1
                    }
                }
            },
            "dependentStatements": {
                "Steps.if.false": true
            }
        },
        "set2": {
            "statementName": "set2",
            "name": "Set",
            "namespace": "System.Context",
            "parameterMap": {
                "name": {
                    "2XVsWDK0VUQhZJ0VnJllrQ": {
                        "key": "2XVsWDK0VUQhZJ0VnJllrQ",
                        "type": "VALUE",
                        "order": 1,
                        "value": "Context.a"
                    }
                },
                "value": {
                    "1ksRfalqqgGfh4uZcLXDy9": {
                        "key": "1ksRfalqqgGfh4uZcLXDy9",
                        "type": "VALUE",
                        "order": 1,
                        "value": []
                    }
                }
            },
            "dependentStatements": {
                "Steps.create.output": true
            }
        },
        "generateEvent": {
            "statementName": "generateEvent",
            "name": "GenerateEvent",
            "namespace": "System",
            "parameterMap": {
                "results": {
                    "8brcu20HciA9rYDPOFeef": {
                        "key": "8brcu20HciA9rYDPOFeef",
                        "type": "VALUE",
                        "order": 1,
                        "value": {
                            "name": "result",
                            "value": {
                                "isExpression": true,
                                "value": "Context.a"
                            }
                        }
                    }
                }
            },
            "dependentStatements": {
                "Steps.rangeLoop.output": true
            }
        }
    }
}
"#,
    )
    .unwrap()
}

mod execution_graph_debug {
    use super::*;

    #[test]
    fn should_transform_fibonacci_to_dsl_with_nested_blocks() {
        let transformer = JSONToTextTransformer::new();
        let dsl_text = transformer.transform(&fibonacci_json()).unwrap();
        println!("\n=== GENERATED DSL ===");
        println!("{dsl_text}");

        // Verify nesting
        assert!(dsl_text.contains("rangeLoop:"));
        assert!(dsl_text.contains("iteration"));
        assert!(dsl_text.contains("if:"));
        assert!(dsl_text.contains("true"));
        assert!(dsl_text.contains("false"));
        assert!(dsl_text.contains("trueInsert:"));
        assert!(dsl_text.contains("falseInsert:"));
    }

    #[tokio::test]
    async fn should_show_graph_edges_for_fibonacci() {
        let func_def = FunctionDefinition::from_value(&fibonacci_json()).unwrap();
        let runtime = KIRuntime::new(func_def, false).unwrap();
        let function_repo = function_repo();
        let schema_repo = schema_repo();

        let graph = runtime.get_execution_plan(&function_repo, &schema_repo).await.unwrap();
        println!("Graph created successfully");
        println!("Nodes: {:?}", graph.get_node_map().keys().collect::<Vec<_>>());

        println!("\n=== EXPECTED NESTING ===");
        println!("if -> nested under rangeLoop.iteration (uses Steps.rangeLoop.iteration.index)");
        println!("trueInsert -> nested under if.true (dependentStatements[\"Steps.if.true\"])");
        println!("set -> nested under if.true (has dep on trueInsert.output which is under if.true)");
        println!("falseInsert -> nested under if.false (dependentStatements[\"Steps.if.false\"])");
        println!("set1 -> nested under if.false (has dep on falseInsert.output which is under if.false)");
        println!("generateEvent -> nested under rangeLoop.output (dependentStatements[\"Steps.rangeLoop.output\"])");
        println!("set2 -> nested under create.output (dependentStatements[\"Steps.create.output\"])");

        println!("\n=== ACTUAL GRAPH EDGES ===");
        let valid_block_names: IndexSet<&str> = ["true", "false", "iteration", "output", "error"].into_iter().collect();

        for (step_name, vertex) in graph.get_node_map() {
            println!("\nStep: {step_name}");
            let in_vertices = vertex.get_in_vertices();
            println!("  InVertices count: {}", in_vertices.len());

            let mut block_deps: Vec<(String, String)> = Vec::new();
            for (parent_key, edge_type) in in_vertices {
                println!("  - Parent: {parent_key}, EdgeType: \"{edge_type}\"");
                if valid_block_names.contains(edge_type.as_str()) {
                    block_deps.push((parent_key.clone(), edge_type.clone()));
                }
            }
            if !block_deps.is_empty() {
                println!(
                    "  -> Should be nested under: {}",
                    block_deps.iter().map(|(p, b)| format!("{p}.{b}")).collect::<Vec<_>>().join(", ")
                );
            }
        }

        assert!(!graph.get_node_map().is_empty());
    }
}
