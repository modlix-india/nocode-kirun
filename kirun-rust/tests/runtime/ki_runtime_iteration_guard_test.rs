//! Port of kirun-js `__tests__/engine/runtime/KIRuntimeIterationGuardTest.ts`.
//!
//! Regression test for the runaway-execution guard.
//!
//! The counter used to be incremented only when a queue size changed, which disabled the guard
//! in exactly the case it exists for: a graph making no progress leaves the queue sizes
//! untouched, so the counter never advanced and executeGraph could spin forever. The check was
//! also `==` rather than `>=`, so a skipped boundary value would never trip it.

use std::sync::atomic::Ordering;

use kirun::function::Function;
use kirun::model::FunctionOutput;
use kirun::runtime::ki_runtime::MAX_EXECUTION_ITERATIONS;
use kirun::runtime::KIRuntime;
use kirun::Map;

use crate::common::{assert_err_contains, fep};
use crate::{exclusive_lock, fd};

// A CountLoop with a step in its body, so execution takes many statement iterations.
const LOOPING_DEFINITION: &str = r#"
    {
        "name": "loops",
        "namespace": "TestUI",
        "steps": {
            "loop": {
                "statementName": "loop",
                "name": "CountLoop",
                "namespace": "System.Loop",
                "parameterMap": {
                    "count": {
                        "one": {
                            "key": "one",
                            "type": "VALUE",
                            "value": 40
                        }
                    }
                }
            },
            "printer": {
                "statementName": "printer",
                "name": "Print",
                "namespace": "System",
                "dependentStatements": {
                    "Steps.loop.iteration": true
                },
                "parameterMap": {
                    "values": {
                        "one": {
                            "key": "one",
                            "type": "EXPRESSION",
                            "expression": "Steps.loop.iteration.index"
                        }
                    }
                }
            }
        }
    }
"#;

async fn run() -> kirun::Result<FunctionOutput> {
    let runtime = KIRuntime::new(fd(LOOPING_DEFINITION), false).unwrap();

    runtime.execute(&mut fep().set_arguments(Map::new())).await
}

/// JS `afterEach(() => KIRuntime.MAX_EXECUTION_ITERATIONS = originalMax)`.
struct RestoreMax(usize);

impl Drop for RestoreMax {
    fn drop(&mut self) {
        MAX_EXECUTION_ITERATIONS.store(self.0, Ordering::SeqCst);
    }
}

/// The JS `describe` captures `originalMax` once, before any test changes it.
const ORIGINAL_MAX: usize = 1_000_000;

mod ki_runtime_execution_iteration_guard {
    use super::*;

    #[test]
    fn default_cap_is_1_000_000_and_is_tunable() {
        let _lock = exclusive_lock();
        let _restore = RestoreMax(ORIGINAL_MAX);
        let original_max = MAX_EXECUTION_ITERATIONS.load(Ordering::SeqCst);
        assert_eq!(original_max, 1000000);
    }

    #[tokio::test]
    async fn a_loop_that_exceeds_the_cap_is_stopped() {
        let _lock = exclusive_lock();
        let _restore = RestoreMax(ORIGINAL_MAX);
        // Low enough that a 40-pass loop is guaranteed to cross it. If the counter were still
        // gated on a queue-size change this would run to completion instead of throwing.
        MAX_EXECUTION_ITERATIONS.store(5, Ordering::SeqCst);

        assert_err_contains(run().await, "Execution locked in an infinite loop");
    }

    #[tokio::test]
    async fn the_same_loop_completes_when_the_cap_is_not_exceeded() {
        let _lock = exclusive_lock();
        let _restore = RestoreMax(ORIGINAL_MAX);
        MAX_EXECUTION_ITERATIONS.store(ORIGINAL_MAX, Ordering::SeqCst);

        assert!(run().await.is_ok());
    }

    // A linear chain pops one vertex and pushes its successor, so the queue size is unchanged
    // across passes even though real work happened. Under the old "only count when a queue size
    // changed" rule the counter stayed at zero here, which is precisely why a stuck graph could
    // never trip the guard. Counting must now advance.
    #[tokio::test]
    async fn counts_passes_even_when_the_queue_size_never_changes() {
        let _lock = exclusive_lock();
        let _restore = RestoreMax(ORIGINAL_MAX);
        let linear_chain = r#"
            {
                "name": "linear",
                "namespace": "TestUI",
                "steps": {
                    "first": {
                        "statementName": "first",
                        "name": "Print",
                        "namespace": "System",
                        "parameterMap": {
                            "values": {
                                "one": {
                                    "key": "one",
                                    "type": "VALUE",
                                    "value": "a"
                                }
                            }
                        }
                    },
                    "second": {
                        "statementName": "second",
                        "name": "Print",
                        "namespace": "System",
                        "dependentStatements": {
                            "Steps.first.output": true
                        },
                        "parameterMap": {
                            "values": {
                                "one": {
                                    "key": "one",
                                    "type": "VALUE",
                                    "value": "b"
                                }
                            }
                        }
                    },
                    "third": {
                        "statementName": "third",
                        "name": "Print",
                        "namespace": "System",
                        "dependentStatements": {
                            "Steps.second.output": true
                        },
                        "parameterMap": {
                            "values": {
                                "one": {
                                    "key": "one",
                                    "type": "VALUE",
                                    "value": "c"
                                }
                            }
                        }
                    }
                }
            }
        "#;

        let mut params = fep().set_arguments(Map::new());

        KIRuntime::new(fd(linear_chain), false).unwrap().execute(&mut params).await.unwrap();

        assert!(params.get_count() >= 3);
    }
}
