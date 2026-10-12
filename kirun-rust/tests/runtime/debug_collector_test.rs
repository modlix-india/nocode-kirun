//! Port of kirun-js `__tests__/engine/runtime/debug/DebugCollector.test.ts`.

use std::sync::Arc;
use std::time::Duration;

use kirun::runtime::debug::{DebugCollector, DebugEvent, DebugEventType};
use parking_lot::{Mutex, RwLockWriteGuard};
use serde_json::json;

use crate::exclusive_lock;

/// JS beforeEach (`clear()` + `enable()`) and, on drop, afterEach (`disable()` + `clear()`).
/// Holds the exclusive lock: the collector is a process-wide singleton.
pub struct Setup {
    pub collector: &'static DebugCollector,
    _lock: RwLockWriteGuard<'static, ()>,
}

impl Setup {
    pub fn new() -> Self {
        let lock = exclusive_lock();
        let collector = DebugCollector::get_instance();
        collector.clear();
        collector.enable();
        Setup { collector, _lock: lock }
    }
}

impl Drop for Setup {
    fn drop(&mut self) {
        // Do not panic again while unwinding from a failed (or todo!()) test.
        if std::thread::panicking() {
            return;
        }
        self.collector.disable();
        self.collector.clear();
    }
}

async fn sleep_ms(ms: u64) {
    tokio::time::sleep(Duration::from_millis(ms)).await;
}

mod debug_collector {
    use super::*;

    #[test]
    fn should_initialize_execution_correctly() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let debug_info = collector.get_execution("test-eid");

        assert!(debug_info.is_some());
        let debug_info = debug_info.unwrap();
        assert_eq!(debug_info.execution_id, "test-eid");
        assert_eq!(debug_info.logs.len(), 0);
        assert!(debug_info.start_time > 0);
    }

    #[tokio::test]
    async fn should_track_step_start_and_end() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let step_id = collector.start_step("test-eid", "step1", "System.Math.Add", Some(json!({ "a": 1, "b": 2 })), None);

        sleep_ms(10).await;

        collector.end_step("test-eid", &step_id.unwrap(), "output", Some(json!({ "result": 3 })), None);

        let debug_info = collector.get_execution("test-eid").unwrap();

        assert_eq!(debug_info.logs.len(), 1);
        assert_eq!(debug_info.logs[0].statement_name.as_deref(), Some("step1"));
        assert_eq!(debug_info.logs[0].function_name, "System.Math.Add");
        assert_eq!(debug_info.logs[0].event_name.as_deref(), Some("output"));
        assert!(debug_info.logs[0].duration.unwrap() > 0);
        assert_eq!(debug_info.logs[0].arguments, json!({ "a": 1, "b": 2 }));
        assert_eq!(debug_info.logs[0].result, json!({ "result": 3 }));
    }

    #[test]
    fn should_track_multiple_steps_in_order() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let step1 = collector.start_step("test-eid", "step1", "Function1", None, None);
        collector.end_step("test-eid", &step1.unwrap(), "output", None, None);

        let step2 = collector.start_step("test-eid", "step2", "Function2", None, None);
        collector.end_step("test-eid", &step2.unwrap(), "output", None, None);

        let step3 = collector.start_step("test-eid", "step3", "Function3", None, None);
        collector.end_step("test-eid", &step3.unwrap(), "output", None, None);

        let debug_info = collector.get_execution("test-eid").unwrap();

        assert_eq!(debug_info.logs.len(), 3);
        assert_eq!(debug_info.logs[0].statement_name.as_deref(), Some("step1"));
        assert_eq!(debug_info.logs[1].statement_name.as_deref(), Some("step2"));
        assert_eq!(debug_info.logs[2].statement_name.as_deref(), Some("step3"));
    }

    #[test]
    fn should_record_error_in_step() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let step_id = collector.start_step("test-eid", "failingStep", "System.Test.Failing", None, None);
        collector.end_step("test-eid", &step_id.unwrap(), "error", None, Some("Test error message"));

        let debug_info = collector.get_execution("test-eid").unwrap();

        assert_eq!(debug_info.logs[0].error.as_deref(), Some("Test error message"));
        assert_eq!(debug_info.logs[0].event_name.as_deref(), Some("error"));
        assert!(debug_info.errored);
    }

    #[test]
    fn should_track_nested_function_calls_hierarchically() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "Parent.Function", None);

        let parent_step = collector.start_step("test-eid", "parentStep", "Parent.Function", None, None);
        let child_step = collector.start_step("test-eid", "childStep", "Child.Function", None, None);
        collector.end_step("test-eid", &child_step.unwrap(), "output", Some(json!({ "value": 42 })), None);
        collector.end_step("test-eid", &parent_step.unwrap(), "output", None, None);

        let debug_info = collector.get_execution("test-eid").unwrap();

        // Parent step should be at root level
        assert_eq!(debug_info.logs.len(), 1);
        assert_eq!(debug_info.logs[0].statement_name.as_deref(), Some("parentStep"));
        // Child step should be nested in parent's children
        assert_eq!(debug_info.logs[0].children.len(), 1);
        assert_eq!(debug_info.logs[0].children[0].statement_name.as_deref(), Some("childStep"));
    }

    #[test]
    fn should_flatten_nested_logs_correctly() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "Parent.Function", None);

        let parent_step = collector.start_step("test-eid", "parentStep", "Parent.Function", None, None);
        let child_step = collector.start_step("test-eid", "childStep", "Child.Function", None, None);
        collector.end_step("test-eid", &child_step.unwrap(), "output", None, None);
        collector.end_step("test-eid", &parent_step.unwrap(), "output", None, None);

        let flat_logs = collector.get_flat_logs("test-eid");

        assert_eq!(flat_logs.len(), 2);
        assert!(flat_logs.iter().any(|l| l.statement_name.as_deref() == Some("parentStep")));
        assert!(flat_logs.iter().any(|l| l.statement_name.as_deref() == Some("childStep")));
    }

    #[test]
    fn should_handle_missing_step_id_gracefully() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        // JS: expect(() => endStep(...)).not.toThrow()
        let r = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
            collector.end_step("test-eid", "non-existent-step", "output", None, None);
        }));
        assert!(r.is_ok());

        let debug_info = collector.get_execution("test-eid").unwrap();
        assert_eq!(debug_info.logs.len(), 0);
    }

    #[tokio::test]
    async fn should_calculate_correct_duration() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let step_id = collector.start_step("test-eid", "timedStep", "System.Test.Timed", None, None);

        sleep_ms(50).await;

        collector.end_step("test-eid", &step_id.unwrap(), "output", None, None);

        let debug_info = collector.get_execution("test-eid").unwrap();

        assert!(debug_info.logs[0].duration.unwrap() >= 45);
        assert!(debug_info.logs[0].duration.unwrap() < 150);
    }

    #[test]
    fn should_store_definitions_via_start_execution() {
        let s = Setup::new();
        let collector = s.collector;
        let definition = json!({ "name": "TestFunc", "steps": { "step1": {} } });

        // Start execution with definition
        collector.start_execution("test-eid", "MyNamespace.TestFunc", Some(definition.clone()));

        let step1 = collector.start_step("test-eid", "step1", "System.Math.Add", None, None);
        collector.end_step("test-eid", &step1.unwrap(), "output", None, None);

        let debug_info = collector.get_execution("test-eid").unwrap();

        // Definition should be stored
        assert_eq!(debug_info.definitions.len(), 1);
        assert_eq!(debug_info.definitions.get("MyNamespace.TestFunc"), Some(&definition));
    }

    #[test]
    fn should_store_multiple_definitions_for_nested_function_calls() {
        let s = Setup::new();
        let collector = s.collector;
        let parent_def = json!({ "name": "Parent", "steps": { "callChild": {} } });
        let child_def = json!({ "name": "Child", "steps": { "doWork": {} } });

        // Parent function starts
        collector.start_execution("test-eid", "App.Parent", Some(parent_def.clone()));

        // Parent starts a step that will call child
        let parent_step = collector.start_step("test-eid", "callChild", "App.Child", None, None);

        // Child function starts (nested call with same executionId)
        collector.start_execution("test-eid", "App.Child", Some(child_def.clone()));

        let child_step = collector.start_step("test-eid", "doWork", "System.Math.Add", None, None);
        collector.end_step("test-eid", &child_step.unwrap(), "output", None, None);

        collector.end_step("test-eid", &parent_step.unwrap(), "output", None, None);

        let debug_info = collector.get_execution("test-eid").unwrap();

        // Both definitions should be stored
        assert_eq!(debug_info.definitions.len(), 2);
        assert_eq!(debug_info.definitions.get("App.Parent"), Some(&parent_def));
        assert_eq!(debug_info.definitions.get("App.Child"), Some(&child_def));
    }

    mod event_listeners {
        use super::*;

        fn recorder(collector: &DebugCollector) -> (Arc<Mutex<Vec<DebugEvent>>>, kirun::runtime::debug::Unsubscribe) {
            let events: Arc<Mutex<Vec<DebugEvent>>> = Arc::new(Mutex::new(Vec::new()));
            let sink = events.clone();
            let unsubscribe = collector.add_event_listener(move |e| sink.lock().push(e.clone()));
            (events, unsubscribe)
        }

        #[test]
        fn should_emit_execution_start_event() {
            let s = Setup::new();
            let collector = s.collector;
            let (events, unsubscribe) = recorder(collector);

            collector.start_execution("test-eid", "Test.Function", None);

            let events_now = events.lock().clone();
            assert_eq!(events_now.len(), 1);
            assert_eq!(events_now[0].event_type, DebugEventType::ExecutionStart);
            assert_eq!(events_now[0].event_type.name(), "executionStart");
            assert_eq!(events_now[0].execution_id, "test-eid");
            assert_eq!(events_now[0].data["functionName"], json!("Test.Function"));

            unsubscribe.call();
        }

        #[test]
        fn should_emit_step_start_and_step_end_events() {
            let s = Setup::new();
            let collector = s.collector;
            // JS never unsubscribes here; the listener outlives the test there too.
            let (events, _unsubscribe) = recorder(collector);

            collector.start_execution("test-eid", "Test.Function", None);
            let step_id = collector.start_step("test-eid", "step1", "System.Math.Add", Some(json!({ "a": 1 })), None);
            collector.end_step("test-eid", &step_id.unwrap(), "output", Some(json!({ "result": 2 })), None);

            let events = events.lock().clone();
            // executionStart, stepStart, stepEnd
            assert_eq!(events.len(), 3);

            assert_eq!(events[1].event_type, DebugEventType::StepStart);
            assert_eq!(events[1].data["statementName"], json!("step1"));
            assert_eq!(events[1].data["functionName"], json!("System.Math.Add"));

            assert_eq!(events[2].event_type, DebugEventType::StepEnd);
            assert_eq!(events[2].data["log"]["statementName"], json!("step1"));
            assert_eq!(events[2].data["log"]["result"], json!({ "result": 2 }));
        }

        #[test]
        fn should_emit_execution_end_event() {
            let s = Setup::new();
            let collector = s.collector;
            let (events, _unsubscribe) = recorder(collector);

            collector.start_execution("test-eid", "Test.Function", None);
            collector.end_execution("test-eid");

            let events = events.lock().clone();
            let end_event = events.iter().find(|e| e.event_type == DebugEventType::ExecutionEnd);
            assert!(end_event.is_some());
            let end_event = end_event.unwrap();
            assert_eq!(end_event.execution_id, "test-eid");
            assert!(end_event.data["duration"].as_f64().unwrap() >= 0.0);
        }

        #[test]
        fn should_emit_execution_errored_event_on_error() {
            let s = Setup::new();
            let collector = s.collector;
            let (events, _unsubscribe) = recorder(collector);

            collector.start_execution("test-eid", "Test.Function", None);
            let step_id = collector.start_step("test-eid", "step1", "Failing.Function", None, None);
            collector.end_step("test-eid", &step_id.unwrap(), "error", None, Some("Something failed"));

            let events = events.lock().clone();
            let error_event = events.iter().find(|e| e.event_type == DebugEventType::ExecutionErrored);
            assert!(error_event.is_some());
            assert_eq!(error_event.unwrap().execution_id, "test-eid");
        }

        #[test]
        fn should_allow_unsubscribing_from_events() {
            let s = Setup::new();
            let collector = s.collector;
            let (events, unsubscribe) = recorder(collector);

            collector.start_execution("test-eid", "Test.Function", None);
            assert_eq!(events.lock().len(), 1);

            unsubscribe.call();

            collector.start_execution("test-eid2", "Test.Function2", None);
            assert_eq!(events.lock().len(), 1); // No new events after unsubscribe
        }

        #[test]
        fn should_emit_events_in_correct_order_for_nested_calls() {
            let s = Setup::new();
            let collector = s.collector;
            let (events, _unsubscribe) = recorder(collector);

            collector.start_execution("test-eid", "Parent.Function", None);
            let parent_step = collector.start_step("test-eid", "parentStep", "Child.Function", None, None);

            // Nested function
            collector.start_execution("test-eid", "Child.Function", None);
            let child_step = collector.start_step("test-eid", "childStep", "System.Math.Add", None, None);
            collector.end_step("test-eid", &child_step.unwrap(), "output", None, None);

            collector.end_step("test-eid", &parent_step.unwrap(), "output", None, None);
            collector.end_execution("test-eid");

            let event_types: Vec<&'static str> = events.lock().iter().map(|e| e.event_type.name()).collect();
            assert_eq!(
                event_types,
                vec![
                    "executionStart", // Parent starts
                    "stepStart",      // parentStep starts
                    "stepStart",      // childStep starts
                    "stepEnd",        // childStep ends
                    "stepEnd",        // parentStep ends
                    "executionEnd",   // Execution ends
                ]
            );
        }
    }
}
