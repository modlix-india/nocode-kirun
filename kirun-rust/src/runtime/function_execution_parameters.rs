//! Port of kirun-js `runtime/FunctionExecutionParameters.ts`.
//!
//! `context`, `events`, `steps` and `execution_context` are `Shared` because JS passes
//! the same Map objects to every step of an execution (and loop generators read
//! `execution_context` lazily to see `Break`).

use std::sync::Arc;

use crate::function::Function;
use crate::json::schema::Schema;
use crate::repository::Repository;
use crate::runtime::context_element::ContextElement;
use crate::runtime::expression::tokenextractor::token_value_extractor::{TokenValueExtractor, ValuesMap};
use crate::runtime::statement_execution::StatementExecution;
use crate::runtime::tokenextractor::context_token_value_extractor::ContextTokenValueExtractor;
use crate::runtime::tokenextractor::output_map_token_value_extractor::OutputMapTokenValueExtractor;
use crate::{shared, Map, Shared, Value};

/// `Steps.<statementName>.<eventName>.<resultName>`.
pub type StepsMap = Map<Map<Map<Value>>>;
/// Raised events: event name → each raised result map, in order.
pub type EventsMap = Map<Vec<Map<Value>>>;
pub type ContextMap = Map<ContextElement>;

#[derive(Clone)]
pub struct FunctionExecutionParameters {
    context: Option<Shared<ContextMap>>,
    args: Option<Map<Value>>,
    events: Option<Shared<EventsMap>>,
    statement_execution: Option<Arc<StatementExecution>>,
    steps: Option<Shared<StepsMap>>,
    count: usize,
    function_repository: Arc<dyn Repository<dyn Function>>,
    schema_repository: Arc<dyn Repository<Schema>>,
    execution_id: String,
    execution_context: Shared<Map<Value>>,
    value_extractors: ValuesMap,
    /// The extractors `set_context_shared` / `set_steps_shared` registered, so a step's
    /// parameters can reuse them instead of building identical ones (see `for_step`).
    context_extractor: Option<Arc<dyn TokenValueExtractor>>,
    steps_extractor: Option<Arc<dyn TokenValueExtractor>>,
}

impl FunctionExecutionParameters {
    pub fn new(
        function_repository: Arc<dyn Repository<dyn Function>>,
        schema_repository: Arc<dyn Repository<Schema>>,
    ) -> Self {
        Self::with_execution_id(function_repository, schema_repository, crate::util::uuid::uuid())
    }

    pub fn with_execution_id(
        function_repository: Arc<dyn Repository<dyn Function>>,
        schema_repository: Arc<dyn Repository<Schema>>,
        execution_id: impl Into<String>,
    ) -> Self {
        Self {
            context: None,
            args: None,
            events: None,
            statement_execution: None,
            steps: None,
            count: 0,
            function_repository,
            schema_repository,
            execution_id: execution_id.into(),
            execution_context: shared(Map::new()),
            value_extractors: Map::new(),
            context_extractor: None,
            steps_extractor: None,
        }
    }

    /// The parameters KIRuntime gives a step: the parent's repositories, execution id, values
    /// map, context, events, steps and execution context, plus the step's arguments. Same as
    /// JS `new FunctionExecutionParameters(..).setValuesMap(parent).setContext(..).setSteps(..)…`,
    /// but the Context/Steps extractors are reused when the parent's map still holds the ones
    /// built for those very stores (building identical ones per step was most of its cost).
    pub(crate) fn for_step(
        parent: &FunctionExecutionParameters,
        args: Map<Value>,
        statement_execution: Arc<StatementExecution>,
    ) -> Self {
        let mut fep = Self {
            context: None,
            args: Some(args),
            events: parent.events.clone(),
            statement_execution: Some(statement_execution),
            steps: None,
            count: parent.count,
            function_repository: parent.function_repository.clone(),
            schema_repository: parent.schema_repository.clone(),
            execution_id: parent.execution_id.clone(),
            execution_context: parent.execution_context.clone(),
            value_extractors: parent.value_extractors.clone(),
            context_extractor: None,
            steps_extractor: None,
        };
        let registered = |prefix: &str, own: &Option<Arc<dyn TokenValueExtractor>>| {
            match (parent.value_extractors.get(prefix), own) {
                (Some(current), Some(own)) => Arc::ptr_eq(current, own),
                _ => false,
            }
        };
        if let Some(c) = &parent.context {
            if registered(ContextTokenValueExtractor::PREFIX, &parent.context_extractor) {
                fep.context = Some(c.clone());
                fep.context_extractor = parent.context_extractor.clone();
            } else {
                fep = fep.set_context_shared(c.clone());
            }
        }
        if let Some(st) = &parent.steps {
            if registered(OutputMapTokenValueExtractor::PREFIX, &parent.steps_extractor) {
                fep.steps = Some(st.clone());
                fep.steps_extractor = parent.steps_extractor.clone();
            } else {
                fep = fep.set_steps_shared(st.clone());
            }
        }
        fep
    }

    pub fn get_execution_id(&self) -> &str {
        &self.execution_id
    }

    pub fn get_context(&self) -> Option<&Shared<ContextMap>> {
        self.context.as_ref()
    }
    /// Wraps a fresh map. Use `set_context_shared` to share an existing one.
    pub fn set_context(self, context: ContextMap) -> Self {
        self.set_context_shared(shared(context))
    }
    pub fn set_context_shared(mut self, context: Shared<ContextMap>) -> Self {
        let x: Arc<dyn TokenValueExtractor> = Arc::new(ContextTokenValueExtractor::new(context.clone()));
        self.value_extractors.insert(x.get_prefix().to_string(), x.clone());
        self.context_extractor = Some(x);
        self.context = Some(context);
        self
    }

    pub fn get_arguments(&self) -> Option<&Map<Value>> {
        self.args.as_ref()
    }
    pub fn set_arguments(mut self, args: Map<Value>) -> Self {
        self.args = Some(args);
        self
    }
    pub fn set_arguments_in_place(&mut self, args: Map<Value>) {
        self.args = Some(args);
    }

    pub fn get_events(&self) -> Option<&Shared<EventsMap>> {
        self.events.as_ref()
    }
    pub fn set_events(self, events: EventsMap) -> Self {
        self.set_events_shared(shared(events))
    }
    pub fn set_events_shared(mut self, events: Shared<EventsMap>) -> Self {
        self.events = Some(events);
        self
    }

    pub fn get_statement_execution(&self) -> Option<&Arc<StatementExecution>> {
        self.statement_execution.as_ref()
    }
    pub fn set_statement_execution(mut self, se: Arc<StatementExecution>) -> Self {
        self.statement_execution = Some(se);
        self
    }

    pub fn get_steps(&self) -> Option<&Shared<StepsMap>> {
        self.steps.as_ref()
    }
    pub fn set_steps(self, steps: StepsMap) -> Self {
        self.set_steps_shared(shared(steps))
    }
    pub fn set_steps_shared(mut self, steps: Shared<StepsMap>) -> Self {
        let x: Arc<dyn TokenValueExtractor> = Arc::new(OutputMapTokenValueExtractor::new(steps.clone()));
        self.value_extractors.insert(x.get_prefix().to_string(), x.clone());
        self.steps_extractor = Some(x);
        self.steps = Some(steps);
        self
    }

    pub fn get_count(&self) -> usize {
        self.count
    }
    pub fn set_count(mut self, count: usize) -> Self {
        self.count = count;
        self
    }
    pub fn set_count_in_place(&mut self, count: usize) {
        self.count = count;
    }

    pub fn get_values_map(&self) -> &ValuesMap {
        &self.value_extractors
    }
    pub fn set_values_map(mut self, values_map: ValuesMap) -> Self {
        for (k, v) in values_map {
            self.value_extractors.insert(k, v);
        }
        self
    }
    pub fn add_token_value_extractor(mut self, extractors: Vec<Arc<dyn TokenValueExtractor>>) -> Self {
        self.add_token_value_extractor_in_place(extractors);
        self
    }
    pub fn add_token_value_extractor_in_place(&mut self, extractors: Vec<Arc<dyn TokenValueExtractor>>) {
        for tve in extractors {
            self.value_extractors.insert(tve.get_prefix().to_string(), tve);
        }
    }

    pub fn get_function_repository(&self) -> &Arc<dyn Repository<dyn Function>> {
        &self.function_repository
    }
    pub fn set_function_repository(mut self, repo: Arc<dyn Repository<dyn Function>>) -> Self {
        self.function_repository = repo;
        self
    }
    pub fn get_schema_repository(&self) -> &Arc<dyn Repository<Schema>> {
        &self.schema_repository
    }
    pub fn set_schema_repository(mut self, repo: Arc<dyn Repository<Schema>>) -> Self {
        self.schema_repository = repo;
        self
    }

    pub fn get_execution_context(&self) -> &Shared<Map<Value>> {
        &self.execution_context
    }
    pub fn set_execution_context(mut self, execution_context: Shared<Map<Value>>) -> Self {
        self.execution_context = execution_context;
        self
    }
}
