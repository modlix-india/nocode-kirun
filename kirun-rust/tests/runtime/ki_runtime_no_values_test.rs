//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeNoValuesTest.ts`.

use std::sync::Arc;

use kirun::function::system::print::Print;
use kirun::function::Function;
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};
use kirun::Map;

use crate::common::schema_repo;
use crate::{fd, hybrid_with, shared_lock, AlwaysRepository};

#[tokio::test]
async fn ki_runtime_with_definition_without_values_object_inside_parameter_map() {
    let _lock = shared_lock();
    let def = r#"
        {
            "name": "Make an error",
            "namespace": "UIApp",
            "steps": {
                "print": {
                    "statementName": "print",
                    "namespace": "function",
                    "name": "test",
                    "parameterMap": {}
                }
            }
        }
    "#;

    let f = fd(def);

    let tst_print: Arc<dyn Function> = Arc::new(Print::new());

    let result = KIRuntime::new(f, false)
        .unwrap()
        .execute(
            &mut FunctionExecutionParameters::new(hybrid_with(Arc::new(AlwaysRepository(tst_print))), schema_repo())
                .set_arguments(Map::new()),
        )
        .await;
    // JS wraps this in try/catch and only logs an error; kirun-js runs it without error
    // (checked), so the port asserts success.
    if let Err(e) = &result {
        eprintln!("{e}");
    }
    assert!(result.is_ok());
}
