//! Steps marked `catchErrors` (a kirun-rust extension; kirun-js and kirun-java have no such
//! property): when the function such a step calls fails and steps of the definition wait on the
//! step's `error` event, they get the failure as that event, and the run goes on.
//!
//! The event has the shape functions raise `error` with on purpose, so a handler reads it the
//! same way whichever way the function failed:
//!
//! ```json
//! {"status": 500, "reason": "CHILD_FAILED", "message": "<the error's message>",
//!  "details": {"step": "<step>", "function": "<namespace>.<name>"}}
//! ```
//!
//! `reason` is the error's own code when the embedding knows one ([`set_policy`]). A failure that
//! must end the run whatever the step says (a run stopped at its limits) is never caught: the
//! runtime's own iteration guard, and whatever the embedding's policy says to rethrow.

use std::sync::Arc;

use parking_lot::RwLock;

use crate::model::{Event, EventResult};
use crate::{KIRunError, Map, Value};

/// The `reason` of a caught failure that has no code of its own.
pub const CHILD_FAILED: &str = "CHILD_FAILED";

/// The `status` of a caught failure.
pub const STATUS: i64 = 500;

/// What a step marked `catchErrors` does with its function's failure.
#[derive(Debug, Clone, PartialEq, Eq)]
pub enum Caught {
    /// Raise it as the step's `error` event, with this `reason` (`None`: [`CHILD_FAILED`]).
    Event { reason: Option<String> },
    /// End the run with it, as an unmarked step does.
    Rethrow,
}

type Policy = Arc<dyn Fn(&KIRunError) -> Caught + Send + Sync>;

static POLICY: RwLock<Option<Policy>> = RwLock::new(None);

/// Sets how failures are caught for this process (`None`: every failure is caught as
/// [`CHILD_FAILED`], except the runtime's iteration guard). The embedding uses it to keep its own
/// limits from being caught and to name an error's code. It runs on the task of the failing
/// step, so task-local state of the run is visible to it.
pub fn set_policy(policy: Option<Policy>) {
    *POLICY.write() = policy;
}

/// Prefix of the iteration guard's error ([`crate::runtime::ki_runtime::MAX_EXECUTION_ITERATIONS`]).
pub(crate) const ITERATION_GUARD: &str = "Execution locked in an infinite loop";

/// The `error` event a marked step raises for `err`, or `None` when `err` must end the run.
pub(crate) fn caught(err: &KIRunError, step: &str, function: &str) -> Option<EventResult> {
    if err.to_string().starts_with(ITERATION_GUARD) {
        return None;
    }
    let policy = POLICY.read().clone();
    let reason = match policy.map(|p| p(err)) {
        Some(Caught::Rethrow) => return None,
        Some(Caught::Event { reason }) => reason,
        None => None,
    };
    let details = Value::Object(
        [("step".to_string(), Value::from(step)), ("function".to_string(), Value::from(function))].into_iter().collect(),
    );
    let result: Map<Value> = Map::from([
        ("status".to_string(), Value::from(STATUS)),
        ("reason".to_string(), Value::from(reason.unwrap_or_else(|| CHILD_FAILED.to_string()))),
        ("message".to_string(), Value::from(err.to_string())),
        ("details".to_string(), details),
    ]);
    Some(EventResult::of(Event::ERROR, result))
}
