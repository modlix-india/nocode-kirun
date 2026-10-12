//! Port of kirun-js `runtime/debug/*`.

pub mod debug_collector;
pub mod debug_formatter;
pub mod types;

pub use debug_collector::{DebugCollector, Unsubscribe};
pub use debug_formatter::{DebugFormatter, PerformanceSummary};
pub use types::{DebugEvent, DebugEventListener, DebugEventType, ExecutionLog, LogEntry};
