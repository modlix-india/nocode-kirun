//! Port of kirun-js `runtime/debug/DebugCollector.ts`: a process-wide singleton that records
//! step logs per execution id. All methods take `&self` (interior mutability). Listeners are
//! called after the internal lock is released, so a listener may query the collector.

use std::sync::atomic::{AtomicBool, AtomicU64, Ordering};
use std::sync::{Arc, LazyLock};

use parking_lot::Mutex;
use serde_json::json;

use crate::runtime::debug::types::{DebugEvent, DebugEventListener, DebugEventType, ExecutionLog, LogEntry};
use crate::{Map, Value};

const MAX_EXECUTIONS: usize = 10;

#[derive(Default)]
struct State {
    executions: Map<ExecutionLog>,
    execution_order: Vec<String>,
    step_stacks: Map<Vec<LogEntry>>,
    pending_logs: Map<LogEntry>,
}

pub struct DebugCollector {
    state: Mutex<State>,
    listeners: Mutex<Vec<(u64, DebugEventListener)>>,
    enabled: AtomicBool,
    next_listener_id: AtomicU64,
}

/// Returned by `add_event_listener`; JS returns an unsubscribe closure.
pub struct Unsubscribe {
    pub id: u64,
}

impl Unsubscribe {
    pub fn call(self) {
        DebugCollector::get_instance().listeners.lock().retain(|(id, _)| *id != self.id);
    }
}

static INSTANCE: LazyLock<DebugCollector> = LazyLock::new(|| DebugCollector {
    state: Mutex::new(State::default()),
    listeners: Mutex::new(Vec::new()),
    enabled: AtomicBool::new(false),
    next_listener_id: AtomicU64::new(1),
});

fn now_millis() -> i64 {
    chrono::Utc::now().timestamp_millis()
}

impl DebugCollector {
    pub fn get_instance() -> &'static DebugCollector {
        &INSTANCE
    }

    pub fn enable(&self) {
        self.enabled.store(true, Ordering::SeqCst);
    }

    pub fn disable(&self) {
        self.enabled.store(false, Ordering::SeqCst);
    }

    pub fn is_enabled(&self) -> bool {
        self.enabled.load(Ordering::SeqCst)
    }

    /// Starts an execution, or registers another definition on a nested call with the same id.
    pub fn start_execution(&self, execution_id: &str, function_name: &str, definition: Option<Value>) {
        if !self.is_enabled() {
            return;
        }
        let mut emit_start = false;
        {
            let mut st = self.state.lock();
            if !st.executions.contains_key(execution_id) {
                st.executions.insert(
                    execution_id.to_string(),
                    ExecutionLog {
                        execution_id: execution_id.to_string(),
                        start_time: now_millis(),
                        end_time: None,
                        errored: false,
                        logs: Vec::new(),
                        definitions: Map::new(),
                    },
                );
                st.execution_order.push(execution_id.to_string());
                st.step_stacks.insert(execution_id.to_string(), Vec::new());
                while st.execution_order.len() > MAX_EXECUTIONS {
                    let old = st.execution_order.remove(0);
                    st.executions.shift_remove(&old);
                    st.step_stacks.shift_remove(&old);
                }
                emit_start = true;
            }
            if let (Some(def), Some(exec)) = (definition, st.executions.get_mut(execution_id)) {
                if !exec.definitions.contains_key(function_name) {
                    exec.definitions.insert(function_name.to_string(), def);
                }
            }
        }
        if emit_start {
            self.emit(DebugEventType::ExecutionStart, execution_id, json!({ "functionName": function_name }));
        }
    }

    pub fn end_execution(&self, execution_id: &str) {
        if !self.is_enabled() {
            return;
        }
        let data = {
            let mut st = self.state.lock();
            let Some(exec) = st.executions.get_mut(execution_id) else {
                return;
            };
            let end = now_millis();
            exec.end_time = Some(end);
            json!({ "duration": end - exec.start_time, "errored": exec.errored })
        };
        self.emit(DebugEventType::ExecutionEnd, execution_id, data);
    }

    pub fn start_step(
        &self,
        execution_id: &str,
        statement_name: &str,
        function_name: &str,
        args: Option<Value>,
        kirun_function_name: Option<&str>,
    ) -> Option<String> {
        if !self.is_enabled() {
            return None;
        }
        let step_id = {
            let mut st = self.state.lock();
            if !st.executions.contains_key(execution_id) {
                return None;
            }
            let step_id = format!("{}_{}", now_millis(), &crate::util::uuid::uuid().replace('-', "")[..12]);
            let log = LogEntry {
                step_id: step_id.clone(),
                timestamp: now_millis(),
                function_name: function_name.to_string(),
                statement_name: Some(statement_name.to_string()),
                kirun_function_name: kirun_function_name.map(str::to_string),
                duration: None,
                arguments: args.unwrap_or(Value::Null),
                result: Value::Null,
                event_name: None,
                error: None,
                children: Vec::new(),
            };
            st.pending_logs.insert(step_id.clone(), log.clone());
            st.step_stacks.entry(execution_id.to_string()).or_default().push(log);
            step_id
        };
        self.emit(
            DebugEventType::StepStart,
            execution_id,
            json!({ "stepId": step_id, "statementName": statement_name, "functionName": function_name }),
        );
        Some(step_id)
    }

    pub fn end_step(
        &self,
        execution_id: &str,
        step_id: &str,
        event_name: &str,
        result: Option<Value>,
        error: Option<&str>,
    ) {
        if !self.is_enabled() {
            return;
        }
        let log = {
            let mut st = self.state.lock();
            let Some(mut log) = st.pending_logs.get(step_id).cloned() else {
                return;
            };
            if !st.executions.contains_key(execution_id) {
                return;
            }
            log.duration = Some(now_millis() - log.timestamp);
            log.result = result.unwrap_or(Value::Null);
            log.event_name = Some(event_name.to_string());
            log.error = error.map(str::to_string);

            // Pending logs are copies on the stack; children are attached when a step ends, so
            // the stack copy of the parent is the one that collects them.
            let stack = st.step_stacks.entry(execution_id.to_string()).or_default();
            if let Some(idx) = stack.iter().position(|l| l.step_id == step_id) {
                let on_stack = stack.remove(idx);
                log.children = on_stack.children;
            }
            if let Some(parent) = stack.last_mut() {
                parent.children.push(log.clone());
            } else if let Some(exec) = st.executions.get_mut(execution_id) {
                exec.logs.push(log.clone());
            }
            st.pending_logs.shift_remove(step_id);
            if error.is_some() {
                if let Some(exec) = st.executions.get_mut(execution_id) {
                    exec.errored = true;
                }
            }
            log
        };
        if error.is_some() {
            self.emit(DebugEventType::ExecutionErrored, execution_id, Value::Null);
        }
        let log_json = serde_json::to_value(&log).unwrap_or(Value::Null);
        self.emit(DebugEventType::StepEnd, execution_id, json!({ "log": log_json }));
    }

    pub fn mark_errored(&self, execution_id: &str) {
        let found = {
            let mut st = self.state.lock();
            match st.executions.get_mut(execution_id) {
                Some(exec) => {
                    exec.errored = true;
                    true
                }
                None => false,
            }
        };
        if found {
            self.emit(DebugEventType::ExecutionErrored, execution_id, Value::Null);
        }
    }

    pub fn get_execution(&self, execution_id: &str) -> Option<ExecutionLog> {
        self.state.lock().executions.get(execution_id).cloned()
    }

    pub fn get_last_execution(&self) -> Option<ExecutionLog> {
        let st = self.state.lock();
        st.execution_order.last().and_then(|id| st.executions.get(id).cloned())
    }

    pub fn get_definition(&self, execution_id: &str, key: &str) -> Option<Value> {
        self.state.lock().executions.get(execution_id).and_then(|e| e.definitions.get(key).cloned())
    }

    pub fn get_flat_logs(&self, execution_id: &str) -> Vec<LogEntry> {
        fn flatten(logs: &[LogEntry], out: &mut Vec<LogEntry>) {
            for log in logs {
                out.push(log.clone());
                flatten(&log.children, out);
            }
        }
        let st = self.state.lock();
        let mut out = Vec::new();
        if let Some(exec) = st.executions.get(execution_id) {
            flatten(&exec.logs, &mut out);
        }
        out
    }

    pub fn get_all_execution_ids(&self) -> Vec<String> {
        self.state.lock().execution_order.clone()
    }

    pub fn clear(&self) {
        *self.state.lock() = State::default();
    }

    pub fn add_event_listener(&self, listener: impl Fn(&DebugEvent) + Send + Sync + 'static) -> Unsubscribe {
        let id = self.next_listener_id.fetch_add(1, Ordering::SeqCst);
        self.listeners.lock().push((id, Arc::new(listener)));
        Unsubscribe { id }
    }

    fn emit(&self, event_type: DebugEventType, execution_id: &str, data: Value) {
        let listeners: Vec<DebugEventListener> = self.listeners.lock().iter().map(|(_, l)| l.clone()).collect();
        let event = DebugEvent { event_type, execution_id: execution_id.to_string(), data };
        for l in listeners {
            // JS swallows listener exceptions; a panicking listener must not take the run down.
            let _ = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| l(&event)));
        }
    }
}
