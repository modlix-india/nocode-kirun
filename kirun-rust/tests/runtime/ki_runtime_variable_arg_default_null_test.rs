//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeVariableArgDefaultNullTest.ts`.
//! JS mocks `console.log`; here `System.Print` is a recording stand-in.

use std::sync::Arc;

use async_trait::async_trait;
use kirun::function::{AbstractFunction, Function};
use kirun::json::schema::r#type::SchemaType;
use kirun::json::schema::Schema;
use kirun::model::{EventResult, FunctionOutput, FunctionSignature, Parameter};
use kirun::namespaces::Namespaces;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use kirun::util::map_util::MapUtil;
use kirun::{Map, Value};
use serde_json::json;

use crate::common::{fep, schema_repo};
use crate::{fd, repo_with_recording_print, shared_lock, ListRepository, RecordingPrint};

#[tokio::test]
async fn ki_runtime_print_function_with_no_arguments() {
    let _lock = shared_lock();
    let def = r#"
        {
            "name": "varArgWithNothing",
            "namespace": "Test",
            "steps": {
                "testFunction": {
                    "statementName": "testFunction",
                    "namespace": "LocalFunction",
                    "name": "Other",
                    "parameterMap": {
                        "storageName": {
                            "one": {
                                "type": "VALUE",
                                "value": "Test",
                                "key": "one",
                                "order": 1
                            }
                        }
                    }
                }
            }
        }
    "#;

    let other_function: Arc<dyn Function> = Arc::new(
        KIRuntime::new(
            fd(r#"
                {
                    "namespace": "LocalFunction",
                    "name": "Other",
                    "steps": {
                        "print": {
                            "statementName": "print",
                            "namespace": "System",
                            "name": "Print",
                            "parameterMap": {
                                "values": {
                                    "one": {
                                        "type": "EXPRESSION",
                                        "expression": "\"Storage : \" + Arguments.storageName + \"\n\"",
                                        "key": "one",
                                        "order": 1
                                    },
                                    "two": {
                                        "type": "EXPRESSION",
                                        "expression": "\"Page : \" + Arguments.page + \"\n\"",
                                        "key": "two",
                                        "order": 2
                                    },
                                    "three": {
                                        "type": "EXPRESSION",
                                        "expression": "\"Size : \" + Arguments.size + \"\n\"",
                                        "key": "three",
                                        "order": 3
                                    },
                                    "five": {
                                        "type": "EXPRESSION",
                                        "expression": "\"Count : \" + Arguments.count + \"\n\"",
                                        "key": "five",
                                        "order": 5
                                    },
                                    "six": {
                                        "type": "EXPRESSION",
                                        "expression": "\"Client Code : \" + Arguments.clientCode + \"\n\"",
                                        "key": "six",
                                        "order": 6
                                    },
                                    "eight": {
                                        "type": "EXPRESSION",
                                        "expression": "\"Eager : \" + Arguments.eager + \"\n\"",
                                        "key": "eight",
                                        "order": 8
                                    }
                                }
                            }
                        }
                    },
                    "parameters": {
                        "appCode": {
                            "schema": {
                                "namespace": "_",
                                "name": "appCode",
                                "version": 1,
                                "type": {
                                    "type": "STRING"
                                },
                                "defaultValue": ""
                            },
                            "parameterName": "appCode",
                            "variableArgument": false,
                            "type": "EXPRESSION"
                        },
                        "page": {
                            "schema": {
                                "namespace": "_",
                                "name": "page",
                                "version": 1,
                                "type": {
                                    "type": "INTEGER"
                                },
                                "defaultValue": 0
                            },
                            "parameterName": "page",
                            "variableArgument": false,
                            "type": "EXPRESSION"
                        },
                        "storageName": {
                            "schema": {
                                "namespace": "_",
                                "name": "storageName",
                                "version": 1,
                                "type": {
                                    "type": "STRING"
                                }
                            },
                            "parameterName": "storageName",
                            "variableArgument": false,
                            "type": "EXPRESSION"
                        },
                        "size": {
                            "schema": {
                                "namespace": "_",
                                "name": "size",
                                "version": 1,
                                "type": {
                                    "type": "INTEGER"
                                },
                                "defaultValue": 20
                            },
                            "parameterName": "size",
                            "variableArgument": false,
                            "type": "EXPRESSION"
                        },
                        "count": {
                            "schema": {
                                "namespace": "_",
                                "name": "count",
                                "version": 1,
                                "type": {
                                    "type": "BOOLEAN"
                                },
                                "defaultValue": true
                            },
                            "parameterName": "count",
                            "variableArgument": false,
                            "type": "EXPRESSION"
                        },
                        "clientCode": {
                            "schema": {
                                "namespace": "_",
                                "name": "clientCode",
                                "version": 1,
                                "type": {
                                    "type": "STRING"
                                },
                                "defaultValue": ""
                            },
                            "parameterName": "clientCode",
                            "variableArgument": false,
                            "type": "EXPRESSION"
                        },
                        "eagerFields": {
                            "schema": {
                                "namespace": "_",
                                "name": "eagerFields",
                                "version": 1,
                                "type": {
                                    "type": "STRING"
                                }
                            },
                            "parameterName": "eagerFields",
                            "variableArgument": true,
                            "type": "EXPRESSION"
                        },
                        "filter": {
                            "schema": {
                                "namespace": "_",
                                "name": "filter",
                                "version": 1,
                                "type": {
                                    "type": "OBJECT"
                                },
                                "defaultValue": {}
                            },
                            "parameterName": "filter",
                            "variableArgument": false,
                            "type": "EXPRESSION"
                        },
                        "eager": {
                            "schema": {
                                "namespace": "_",
                                "name": "eager",
                                "version": 1,
                                "type": {
                                    "type": "BOOLEAN"
                                },
                                "defaultValue": false
                            },
                            "parameterName": "eager",
                            "variableArgument": false,
                            "type": "EXPRESSION"
                        }
                    }
                }
            "#),
            false,
        )
        .unwrap(),
    );
    let f = fd(def);

    let print = Arc::new(RecordingPrint::new());

    let _result = KIRuntime::new(f, false)
        .unwrap()
        .execute(&mut FunctionExecutionParameters::new(
            repo_with_recording_print(print.clone(), vec![Arc::new(ListRepository(vec![other_function]))]),
            schema_repo(),
        ))
        .await
        .unwrap();

    assert_eq!(
        print.calls()[0],
        vec![
            json!("Storage : Test\n"),
            json!("Page : 0\n"),
            json!("Size : 20\n"),
            json!("Count : true\n"),
            json!("Client Code : \n"),
            json!("Eager : false\n"),
        ]
    );
}

struct TestPrint {
    signature: FunctionSignature,
}

impl TestPrint {
    const VALUES: &'static str = "values";

    fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Print").set_namespace(Namespaces::SYSTEM).set_parameters(
                MapUtil::of_entries_array(vec![Parameter::of_entry(
                    TestPrint::VALUES,
                    Schema::of(TestPrint::VALUES, &[SchemaType::String, SchemaType::Null]).set_default_value(Value::Null),
                    false,
                )]),
            ),
        }
    }
}

#[async_trait]
impl AbstractFunction for TestPrint {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> kirun::Result<FunctionOutput> {
        let values = context.get_arguments().and_then(|a| a.get(TestPrint::VALUES)).cloned().unwrap_or(Value::Null);

        println!("{values}");

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}

#[tokio::test]
async fn ki_runtime_print_function_with_null_argument() {
    let fun = TestPrint::new();
    // JS calls execute without awaiting it and asserts nothing; the outcome is ignored here too.
    let _ = fun.execute(&mut fep()).await;
}
