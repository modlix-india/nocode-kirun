//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeVarArgsTest.ts`.
//! JS mocks `console.log`; here `System.Print` is a recording stand-in.

use std::sync::Arc;

use kirun::function::Function;
use kirun::runtime::expression::tokenextractor::object_value_setter_extractor::ObjectValueSetterExtractor;
use kirun::runtime::expression::tokenextractor::token_value_extractor::TokenValueExtractor;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use kirun::util::map_util::MapUtil;
use serde_json::json;

use crate::common::schema_repo;
use crate::{fd, repo_with_recording_print, shared_lock, ListRepository, RecordingPrint};

#[tokio::test]
async fn ki_runtime_print_function_with_variable_arguments() {
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
                        "eagerFields": {
                            "one": {
                                "type": "EXPRESSION",
                                "expression": "Store.names",
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
                                        "expression": "Arguments.eagerFields",
                                        "value": "Test",
                                        "key": "one",
                                        "order": 1
                                    }
                                }
                            }
                        }
                    },
                    "parameters": {
                        "eagerFields": {
                            "schema": {
                                "namespace": "_",
                                "name": "eagerFields",
                                "version": 1,
                                "type": "STRING"
                            },
                            "parameterName": "eagerFields",
                            "variableArgument": true,
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

    let store: Arc<dyn TokenValueExtractor> =
        Arc::new(ObjectValueSetterExtractor::new(json!({ "names": ["kiran", "kumar", "grandhi"] }), "Store."));
    let prefix = store.get_prefix().to_string();

    let print = Arc::new(RecordingPrint::new());

    let _result = KIRuntime::new(f, false)
        .unwrap()
        .execute(
            &mut FunctionExecutionParameters::new(
                repo_with_recording_print(print.clone(), vec![Arc::new(ListRepository(vec![other_function]))]),
                schema_repo(),
            )
            .set_values_map(MapUtil::of(vec![(prefix.as_str(), store)])),
        )
        .await
        .unwrap();

    assert_eq!(print.calls()[0], vec![json!("kiran"), json!("kumar"), json!("grandhi")]);
}
