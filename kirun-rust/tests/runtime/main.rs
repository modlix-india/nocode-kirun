#[path = "../common/mod.rs"]
mod common;

mod debug_collector_test;
mod debug_formatter_test;
mod ki_run_function_repository_test;
mod ki_runtime_debug_integration_test;
mod ki_runtime_dependency_test;
mod ki_runtime_extended;
mod ki_runtime_fib_test;
mod ki_runtime_function_in_function;
mod ki_runtime_iteration_guard_test;
mod ki_runtime_loop_bug_branch_test;
mod ki_runtime_messages_test;
mod ki_runtime_no_param_map_test;
mod ki_runtime_no_values_test;
mod ki_runtime_parallel_test;
mod ki_runtime_test;
mod ki_runtime_test_without_gen_event;
mod ki_runtime_undefined_param_test;
mod ki_runtime_values_empty_test;
mod ki_runtime_var_args_test;
mod ki_runtime_variable_arg_default_null_test;
mod ki_runtime_with_definition_test;
mod repository_filter_test;

// ---------------------------------------------------------------------------------------------
// Helpers shared by the runtime tests (kept here, not in tests/common, because they are
// specific to this binary).
// ---------------------------------------------------------------------------------------------

use std::sync::Arc;

use async_trait::async_trait;
use kirun::function::{AbstractFunction, Function};
use kirun::json::schema::Schema;
use kirun::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter, ParameterReference};
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use kirun::runtime::FunctionExecutionParameters;
use kirun::util::map_util::MapUtil;
use kirun::{Map, Value};
use parking_lot::{Mutex, RwLock, RwLockReadGuard, RwLockWriteGuard};
use serde_json::json;

/// Jest isolates each test file (its own module registry, so its own `DebugCollector`
/// singleton and `KIRuntime.MAX_EXECUTION_ITERATIONS`). Here every test shares one process and
/// runs in parallel, so tests that touch that global state take the exclusive lock and every
/// other KIRuntime test takes the shared one.
static GLOBAL_STATE: RwLock<()> = RwLock::new(());

/// For tests that only read global state (run KIRuntime without debug mode).
pub fn shared_lock() -> RwLockReadGuard<'static, ()> {
    GLOBAL_STATE.read()
}

/// For tests that change `MAX_EXECUTION_ITERATIONS`, use the `DebugCollector`, or create a
/// KIRuntime in debug mode (which enables the global collector).
pub fn exclusive_lock() -> RwLockWriteGuard<'static, ()> {
    GLOBAL_STATE.write()
}

/// JS `MapUtil.ofEntriesArray(ParameterReference.ofValue(v))`.
pub fn value_ref(v: Value) -> Map<ParameterReference> {
    MapUtil::of_entries_array(vec![ParameterReference::of_value(v)])
}

/// JS `MapUtil.ofEntriesArray(ParameterReference.ofExpression(e))`.
pub fn expr_ref(e: &str) -> Map<ParameterReference> {
    MapUtil::of_entries_array(vec![ParameterReference::of_expression(e)])
}

/// Stand-in for `System.Print` whose `console.log` calls the JS tests spy on. Each call
/// records the arguments JS would pass to `console.log(...values)`.
pub struct RecordingPrint {
    signature: FunctionSignature,
    calls: Arc<Mutex<Vec<Vec<Value>>>>,
}

impl RecordingPrint {
    pub fn new() -> Self {
        // Same signature as kirun-js `function/system/Print.ts`.
        let signature = FunctionSignature::new("Print")
            .set_namespace(Namespaces::SYSTEM)
            .set_parameters(MapUtil::of_entries_array(vec![
                Parameter::of_entry("values", Schema::of_any("values"), true),
                Parameter::of_entry(
                    "stream",
                    Schema::of_string("stream")
                        .set_enums(vec![json!("LOG"), json!("ERROR")])
                        .set_default_value(json!("LOG")),
                    false,
                ),
            ]))
            .set_events(MapUtil::of_entries_array(vec![Event::output_event_map_entry(Map::new())]));
        Self { signature, calls: Arc::new(Mutex::new(Vec::new())) }
    }

    /// Every recorded `console.log` call (JS `mock.calls`).
    pub fn calls(&self) -> Vec<Vec<Value>> {
        self.calls.lock().clone()
    }
}

#[async_trait]
impl AbstractFunction for RecordingPrint {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> kirun::Result<FunctionOutput> {
        let values = match context.get_arguments().and_then(|a| a.get("values")) {
            Some(Value::Array(a)) => a.clone(),
            _ => Vec::new(),
        };
        self.calls.lock().push(values);
        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}

/// Serves a `RecordingPrint` for `System.Print`; put it before the KIRun repository in a
/// `HybridRepository` so it wins.
pub struct RecordingPrintRepository(pub Arc<RecordingPrint>);

#[async_trait]
impl Repository<dyn Function> for RecordingPrintRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<dyn Function>> {
        if namespace == Namespaces::SYSTEM && name == "Print" {
            let f: Arc<dyn Function> = self.0.clone();
            return Some(f);
        }
        None
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        ["System.Print"]
            .into_iter()
            .filter(|e| e.to_lowercase().contains(&name.to_lowercase()))
            .map(str::to_string)
            .collect()
    }
}

/// `HybridRepository(RecordingPrintRepository, KIRunFunctionRepository, ...extra)`.
pub fn repo_with_recording_print(
    print: Arc<RecordingPrint>,
    extra: Vec<Arc<dyn Repository<dyn Function>>>,
) -> Arc<dyn Repository<dyn Function>> {
    let mut repos: Vec<Arc<dyn Repository<dyn Function>>> =
        vec![Arc::new(RecordingPrintRepository(print)), common::function_repo()];
    repos.extend(extra);
    Arc::new(kirun::hybrid_repository::HybridRepository::new(repos))
}

/// A repository that always returns the same function (the JS `TestRepository` classes);
/// `filter` throws "Method not implemented." in JS, so it panics here.
pub struct AlwaysRepository(pub Arc<dyn Function>);

#[async_trait]
impl Repository<dyn Function> for AlwaysRepository {
    async fn find(&self, _namespace: &str, _name: &str) -> Option<Arc<dyn Function>> {
        Some(self.0.clone())
    }

    async fn filter(&self, _name: &str) -> Vec<String> {
        panic!("Method not implemented.")
    }
}

/// A repository over a fixed list of functions, found by namespace and name; `filter` matches
/// full names case-insensitively (the JS `InternalRepository` classes).
pub struct ListRepository(pub Vec<Arc<dyn Function>>);

#[async_trait]
impl Repository<dyn Function> for ListRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<dyn Function>> {
        self.0
            .iter()
            .find(|f| f.get_signature().get_namespace() == namespace && f.get_signature().get_name() == name)
            .cloned()
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        self.0
            .iter()
            .map(|f| f.get_signature().get_full_name())
            .filter(|e| e.to_lowercase().contains(&name.to_lowercase()))
            .collect()
    }
}

/// `HybridRepository(KIRunFunctionRepository, repo)`.
pub fn hybrid_with(repo: Arc<dyn Repository<dyn Function>>) -> Arc<dyn Repository<dyn Function>> {
    Arc::new(kirun::hybrid_repository::HybridRepository::new(vec![common::function_repo(), repo]))
}

/// JS `FunctionDefinition.from(<json>)`.
pub fn fd(json: &str) -> kirun::model::FunctionDefinition {
    kirun::model::FunctionDefinition::from_value(&serde_json::from_str::<Value>(json).unwrap()).unwrap()
}
