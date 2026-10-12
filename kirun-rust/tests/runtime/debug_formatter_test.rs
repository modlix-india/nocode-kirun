//! Port of kirun-js `__tests__/engine/runtime/debug/DebugFormatter.test.ts`.

use std::time::Duration;

use kirun::runtime::debug::DebugFormatter;
use serde_json::json;

use crate::debug_collector_test::Setup;

async fn sleep_ms(ms: u64) {
    tokio::time::sleep(Duration::from_millis(ms)).await;
}

mod debug_formatter {
    use super::*;

    #[test]
    fn should_format_execution_as_text() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let step1 = collector.start_step("test-eid", "step1", "System.Math.Add", None, None);
        collector.end_step("test-eid", &step1.unwrap(), "output", Some(json!({ "result": 3 })), None);

        let step2 = collector.start_step("test-eid", "step2", "System.Math.Multiply", None, None);
        collector.end_step("test-eid", &step2.unwrap(), "output", Some(json!({ "result": 6 })), None);

        let execution = collector.get_execution("test-eid");
        let text = DebugFormatter::format_as_text(&execution.unwrap());

        assert!(text.contains("test-eid"));
        assert!(text.contains("step1"));
        assert!(text.contains("System.Math.Add"));
        assert!(text.contains("step2"));
        assert!(text.contains("System.Math.Multiply"));
        assert!(regex::Regex::new(r"Duration: \d+ms").unwrap().is_match(&text));
    }

    #[test]
    fn should_format_execution_with_error_status() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let step_id = collector.start_step("test-eid", "failingStep", "System.Test.Failing", None, None);
        collector.end_step("test-eid", &step_id.unwrap(), "error", None, Some("Something went wrong"));

        let execution = collector.get_execution("test-eid");
        let text = DebugFormatter::format_as_text(&execution.unwrap());

        assert!(text.contains('❌'));
        assert!(text.contains("Error: Something went wrong"));
    }

    #[test]
    fn should_get_execution_timeline_in_chronological_order() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let step1 = collector.start_step("test-eid", "step1", "Function1", None, None);
        collector.end_step("test-eid", &step1.unwrap(), "output", None, None);

        let step2 = collector.start_step("test-eid", "step2", "Function2", None, None);
        collector.end_step("test-eid", &step2.unwrap(), "output", None, None);

        let step3 = collector.start_step("test-eid", "step3", "Function3", None, None);
        collector.end_step("test-eid", &step3.unwrap(), "output", None, None);

        let execution = collector.get_execution("test-eid");
        let timeline = DebugFormatter::get_timeline(&execution.unwrap());

        assert_eq!(timeline.len(), 3);
        assert_eq!(timeline[0].statement_name.as_deref(), Some("step1"));
        assert_eq!(timeline[1].statement_name.as_deref(), Some("step2"));
        assert_eq!(timeline[2].statement_name.as_deref(), Some("step3"));

        // Verify chronological order
        for i in 1..timeline.len() {
            assert!(timeline[i].timestamp >= timeline[i - 1].timestamp);
        }
    }

    #[test]
    fn should_get_timeline_including_nested_calls() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let step1 = collector.start_step("test-eid", "step1", "Function1", None, None);

        // Add nested call in same execution
        let step2 = collector.start_step("test-eid", "nestedStep", "Nested.Internal", None, None);
        collector.end_step("test-eid", &step2.unwrap(), "output", None, None);

        collector.end_step("test-eid", &step1.unwrap(), "output", None, None);

        let execution = collector.get_execution("test-eid");
        let timeline = DebugFormatter::get_timeline(&execution.unwrap());

        // Should include both parent and nested steps
        assert_eq!(timeline.len(), 2);
        assert!(timeline.iter().any(|s| s.statement_name.as_deref() == Some("step1")));
        assert!(timeline.iter().any(|s| s.statement_name.as_deref() == Some("nestedStep")));
    }

    #[tokio::test]
    async fn should_get_performance_summary() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let step1 = collector.start_step("test-eid", "fastStep", "Function1", None, None);
        sleep_ms(5).await;
        collector.end_step("test-eid", &step1.unwrap(), "output", None, None);

        let step2 = collector.start_step("test-eid", "slowStep", "Function2", None, None);
        sleep_ms(50).await;
        collector.end_step("test-eid", &step2.unwrap(), "output", None, None);

        let step3 = collector.start_step("test-eid", "mediumStep", "Function3", None, None);
        sleep_ms(20).await;
        collector.end_step("test-eid", &step3.unwrap(), "output", None, None);

        let execution = collector.get_execution("test-eid");
        let summary = DebugFormatter::get_performance_summary(&execution.unwrap());

        assert_eq!(summary.step_count, 3);
        assert!(summary.total_duration > 0);
        assert!(summary.average_duration > 0.0);
        assert_eq!(summary.slowest_steps.len(), 3);

        // Slowest step should be first
        assert_eq!(summary.slowest_steps[0].statement_name.as_deref(), Some("slowStep"));
        assert!(summary.slowest_steps[0].duration.unwrap() > summary.slowest_steps[1].duration.unwrap());
    }

    #[test]
    fn should_format_hierarchical_logs_correctly() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "Parent.Function", None);

        let parent_step = collector.start_step("test-eid", "parentStep", "Parent.Function", None, None);
        let child_step = collector.start_step("test-eid", "childStep", "Child.Function", None, None);
        let grandchild_step = collector.start_step("test-eid", "grandchildStep", "Grandchild.Function", None, None);

        collector.end_step("test-eid", &grandchild_step.unwrap(), "output", None, None);
        collector.end_step("test-eid", &child_step.unwrap(), "output", None, None);
        collector.end_step("test-eid", &parent_step.unwrap(), "output", None, None);

        let execution = collector.get_execution("test-eid").unwrap();
        let text = DebugFormatter::format_as_text(&execution);

        // Should contain all step names with proper indentation
        assert!(text.contains("parentStep"));
        assert!(text.contains("childStep"));
        assert!(text.contains("grandchildStep"));

        // Timeline should flatten all logs
        let timeline = DebugFormatter::get_timeline(&execution);
        assert_eq!(timeline.len(), 3);
    }

    #[test]
    fn should_handle_empty_execution() {
        let s = Setup::new();
        let collector = s.collector;
        collector.start_execution("test-eid", "System.Test.Function", None);

        let execution = collector.get_execution("test-eid").unwrap();
        let text = DebugFormatter::format_as_text(&execution);
        let timeline = DebugFormatter::get_timeline(&execution);
        let summary = DebugFormatter::get_performance_summary(&execution);

        assert!(text.contains("test-eid"));
        assert!(text.contains("Steps: 0"));
        assert_eq!(timeline.len(), 0);
        assert_eq!(summary.step_count, 0);
        assert_eq!(summary.average_duration, 0.0);
    }
}
