//! Port of kirun-js `runtime/debug/DebugFormatter.ts`.

use crate::runtime::debug::types::{ExecutionLog, LogEntry};

#[derive(Debug, Clone, PartialEq)]
pub struct PerformanceSummary {
    pub total_duration: i64,
    pub step_count: usize,
    pub slowest_steps: Vec<LogEntry>,
    pub average_duration: f64,
}

pub struct DebugFormatter;

impl DebugFormatter {
    pub fn format_as_text(execution: &ExecutionLog) -> String {
        let mut lines = Vec::new();
        let status = if execution.errored { "❌" } else { "✓" };
        lines.push(format!("{status} Execution: {}", execution.execution_id));
        lines.push(format!("Duration: {}ms", Self::total_duration(execution)));
        lines.push(format!("Steps: {}", Self::flatten(&execution.logs).len()));
        lines.push(String::new());
        Self::format_logs(&execution.logs, &mut lines, 0);
        lines.join("\n")
    }

    pub fn get_timeline(execution: &ExecutionLog) -> Vec<LogEntry> {
        let mut logs = Self::flatten(&execution.logs);
        logs.sort_by_key(|l| l.timestamp);
        logs
    }

    pub fn get_performance_summary(execution: &ExecutionLog) -> PerformanceSummary {
        let logs = Self::flatten(&execution.logs);
        let total_duration = Self::total_duration(execution);
        let mut slowest: Vec<LogEntry> = logs.iter().filter(|l| l.duration.is_some()).cloned().collect();
        slowest.sort_by(|a, b| b.duration.unwrap_or(0).cmp(&a.duration.unwrap_or(0)));
        slowest.truncate(10);
        PerformanceSummary {
            total_duration,
            step_count: logs.len(),
            average_duration: if logs.is_empty() { 0.0 } else { total_duration as f64 / logs.len() as f64 },
            slowest_steps: slowest,
        }
    }

    fn total_duration(execution: &ExecutionLog) -> i64 {
        execution.end_time.unwrap_or_else(|| chrono::Utc::now().timestamp_millis()) - execution.start_time
    }

    fn format_logs(logs: &[LogEntry], lines: &mut Vec<String>, depth: usize) {
        let indent = "  ".repeat(depth);
        for log in logs {
            let status = if log.error.is_some() { "❌" } else { "✓" };
            let name = log.statement_name.as_deref().filter(|n| !n.is_empty()).unwrap_or("(anonymous)");
            lines.push(format!("{indent}{status} {name} => {} ({}ms)", log.function_name, log.duration.unwrap_or(0)));
            if let Some(err) = &log.error {
                lines.push(format!("{indent}  Error: {err}"));
            }
            if !log.children.is_empty() {
                Self::format_logs(&log.children, lines, depth + 1);
            }
        }
    }

    fn flatten(logs: &[LogEntry]) -> Vec<LogEntry> {
        let mut out = Vec::new();
        for log in logs {
            out.push(log.clone());
            out.extend(Self::flatten(&log.children));
        }
        out
    }
}
