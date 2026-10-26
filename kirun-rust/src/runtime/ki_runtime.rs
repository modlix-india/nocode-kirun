//! Port of kirun-js `runtime/KIRuntime.ts`: executes a `FunctionDefinition` graph.
//!
//! Steps whose dependencies are met run concurrently (JS `Promise.all`). A step that raises a
//! non-`output` event (a loop's `iteration`, an If's `true`) queues a *branch*: the sub-graph
//! hanging off that event, re-run for every further event the step's output generator yields.

use std::any::Any;
use std::collections::{HashMap, VecDeque};
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::{Arc, LazyLock};

use async_trait::async_trait;
use futures::future::{join_all, BoxFuture};
use parking_lot::Mutex;
use regex::Regex;

use crate::function::system::validate_schema::ValidateSchema;
use crate::function::{AbstractFunction, Function};
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::schema_util::SchemaUtil;
use crate::json::schema::Schema;
use crate::model::{
    Event, EventResult, FunctionDefinition, FunctionOutput, FunctionSignature, Parameter, ParameterReference,
    ParameterReferenceType, Statement,
};
use crate::repository::Repository;
use crate::runtime::debug::debug_collector::DebugCollector;
use crate::runtime::expression::ExpressionEvaluator;
use crate::runtime::function_execution_parameters::{FunctionExecutionParameters, StepsMap};
use crate::runtime::graph::{ExecutionGraph, GraphVertex, GraphVertexType};
use crate::runtime::statement_execution::StatementExecution;
use crate::runtime::statement_message::StatementMessageType;
use crate::runtime::tokenextractor::{ArgumentsTokenValueExtractor, ContextTokenValueExtractor, OutputMapTokenValueExtractor};
use crate::util::string::string_util::StringUtil;
use crate::{KIRunError, Map, Result, Value};

/// JS `KIRuntime.MAX_EXECUTION_ITERATIONS` (public and mutable in JS). Counts statement
/// iterations, not loop passes: a ForEachLoop over N rows with S statements in its body
/// consumes roughly N * (S + 1).
pub static MAX_EXECUTION_ITERATIONS: AtomicUsize = AtomicUsize::new(1_000_000);

const PARAMETER_NEEDS_A_VALUE: &str = "Parameter \"$\" needs a value";

/// JS `STEP_REGEX_PATTERN`: `Steps.<step>.<event>` references inside an expression.
static STEP_REGEX: LazyLock<Regex> =
    LazyLock::new(|| Regex::new(r"Steps\.([a-zA-Z0-9\\-]+)\.([a-zA-Z0-9\\-]+)").unwrap());

type Graph = ExecutionGraph<Arc<StatementExecution>>;

/// JS `Tuple4<subGraph, unresolvedDependencies, functionOutput, vertex>`.
struct Branch {
    graph: Arc<Graph>,
    unresolved: Vec<(String, String)>,
    output: FunctionOutput,
    vertex_key: String,
}

/// What executing one vertex adds to the queues.
enum VertexOutcome {
    Skipped,
    Next(Vec<String>),
    Branch(Branch),
}

pub struct KIRuntime {
    fd: FunctionDefinition,
    debug_mode: bool,
    function_cache: Mutex<HashMap<String, Arc<dyn Function>>>,
    /// JS caches sub-graphs per vertex; a vertex's sub-graph of a given event type is the same
    /// whichever graph the vertex sits in, so (statement, event) is the key here.
    sub_graph_cache: Mutex<HashMap<(String, String), Arc<Graph>>>,
}

impl KIRuntime {
    pub const VERSION: i64 = 1;

    /// Fails when the definition's version is newer than the runtime's.
    pub fn new(fd: FunctionDefinition, debug_mode: bool) -> Result<Self> {
        if debug_mode {
            DebugCollector::get_instance().enable();
        }
        if fd.get_version() > Self::VERSION {
            return Err(KIRunError::runtime(format!(
                "Runtime is at a lower version {} and trying to run code from version {}.",
                Self::VERSION,
                fd.get_version()
            )));
        }
        Ok(Self {
            fd,
            debug_mode,
            function_cache: Mutex::new(HashMap::new()),
            sub_graph_cache: Mutex::new(HashMap::new()),
        })
    }

    pub fn get_function_definition(&self) -> &FunctionDefinition {
        &self.fd
    }

    fn function_name(&self) -> String {
        if self.fd.get_namespace().is_empty() {
            self.fd.get_name().to_string()
        } else {
            format!("{}.{}", self.fd.get_namespace(), self.fd.get_name())
        }
    }

    fn debug(&self) -> Option<&'static DebugCollector> {
        self.debug_mode.then(DebugCollector::get_instance)
    }

    async fn get_cached_function(
        &self,
        f_repo: &dyn Repository<dyn Function>,
        namespace: &str,
        name: &str,
    ) -> Option<Arc<dyn Function>> {
        let key = format!("{namespace}.{name}");
        if let Some(f) = self.function_cache.lock().get(&key) {
            return Some(f.clone());
        }
        let fun = f_repo.find(namespace, name).await?;
        self.function_cache.lock().insert(key, fun.clone());
        Some(fun)
    }

    /// JS `getExecutionPlan(functionRepository, schemaRepository)`: the statement graph with
    /// each vertex's validation messages (missing functions, unknown steps, ...).
    pub async fn get_execution_plan(
        &self,
        function_repository: &Arc<dyn Repository<dyn Function>>,
        schema_repository: &Arc<dyn Repository<Schema>>,
    ) -> Result<ExecutionGraph<StatementExecution>> {
        let mut g: ExecutionGraph<StatementExecution> = ExecutionGraph::new(false);

        let prepared = join_all(self.fd.get_steps().values().map(|s| {
            self.prepare_statement_execution(s, function_repository.as_ref(), schema_repository.as_ref())
        }))
        .await;
        for se in prepared {
            g.add_vertex(se?);
        }

        let (_, unresolved_messages) = make_edges(&mut g);
        for (statement, message) in unresolved_messages {
            if let Some(v) = g.get_vertex_mut(&statement) {
                v.get_data_mut().add_message(StatementMessageType::Error, message);
            }
        }
        Ok(g)
    }

    async fn prepare_statement_execution(
        &self,
        s: &Statement,
        f_repo: &dyn Repository<dyn Function>,
        s_repo: &dyn Repository<Schema>,
    ) -> Result<StatementExecution> {
        let mut se = StatementExecution::new(s.clone());

        let Some(fun) = self.get_cached_function(f_repo, s.get_namespace(), s.get_name()).await else {
            se.add_message(
                StatementMessageType::Error,
                format!("{}.{} is not available", s.get_namespace(), s.get_name()),
            );
            return Ok(se);
        };

        let mut param_set: Map<Parameter> = fun.get_signature().get_parameters().clone();

        for (param_name, refs) in s.get_parameter_map() {
            // kirun-java reads `p.isVariableArgument()` on a parameter the function does not
            // have and fails the whole run; kirun-js skips the reference.
            let Some(p) = param_set.get(param_name).cloned() else {
                return Err(KIRunError::uncaught(
                    "Cannot invoke \"com.fincity.nocode.kirun.engine.model.Parameter.isVariableArgument()\" because \"p\" is null",
                ));
            };
            let mut ref_list: Vec<&ParameterReference> = refs.values().collect();

            if ref_list.is_empty() && !p.is_variable_argument() {
                if !SchemaUtil::has_default_value_or_null_schema_type(Some(p.get_schema()), Some(s_repo)).await? {
                    se.add_message(StatementMessageType::Error, needs_a_value(p.get_parameter_name()));
                }
                param_set.shift_remove(p.get_parameter_name());
                continue;
            }

            if p.is_variable_argument() {
                ref_list.sort_by_key(|r| r.get_order().unwrap_or(0));
                for r in ref_list {
                    self.parameter_reference_validation(&mut se, &p, r, s_repo).await?;
                }
            } else if let Some(r) = ref_list.first() {
                self.parameter_reference_validation(&mut se, &p, r, s_repo).await?;
            }

            param_set.shift_remove(p.get_parameter_name());
        }

        let dependent: Vec<String> =
            se.get_statement().get_dependent_statements().iter().filter(|(_, v)| **v).map(|(k, _)| k.clone()).collect();
        for d in dependent {
            se.add_dependency(d);
        }

        let execute_iftrue: Vec<String> =
            se.get_statement().get_execute_iftrue().iter().filter(|(_, v)| **v).map(|(k, _)| k.clone()).collect();
        for e in execute_iftrue {
            add_dependencies(&mut se, Some(&e));
        }

        for param in param_set.values() {
            if param.is_variable_argument() {
                continue;
            }
            if !SchemaUtil::has_default_value_or_null_schema_type(Some(param.get_schema()), Some(s_repo)).await? {
                se.add_message(StatementMessageType::Error, needs_a_value(param.get_parameter_name()));
            }
        }

        Ok(se)
    }

    async fn parameter_reference_validation(
        &self,
        se: &mut StatementExecution,
        p: &Parameter,
        r: &ParameterReference,
        s_repo: &dyn Repository<Schema>,
    ) -> Result<()> {
        match r.get_type() {
            ParameterReferenceType::Value => {
                if r.get_value().is_null() {
                    if !SchemaUtil::has_default_value_or_null_schema_type(Some(p.get_schema()), Some(s_repo)).await? {
                        se.add_message(StatementMessageType::Error, needs_a_value(p.get_parameter_name()));
                    }
                    return Ok(());
                }

                // kirun-java walks the value alongside its schema to find `{isExpression, value}`
                // objects, whose expressions make this step depend on others. It reads them
                // without null checks, so a malformed one fails the run here, before any step
                // runs: see `expression_dependency`.
                let mut param_elements: Vec<(Schema, Value)> = vec![(p.get_schema().clone(), r.get_value().clone())];
                while let Some((schema, value)) = param_elements.pop() {
                    let Some(t) = schema.get_type() else { continue };

                    if t.contains(SchemaType::Array) && value.is_array() {
                        let Value::Array(arr) = value else { continue };
                        let Some(ast) = schema.get_items() else { continue };
                        if let Some(single) = ast.get_single_schema() {
                            for je in arr {
                                param_elements.push((single.clone(), je));
                            }
                        } else {
                            let tuple = ast.get_tuple_schema().map(Vec::as_slice).unwrap_or(&[]);
                            for (i, je) in arr.into_iter().enumerate() {
                                // Java `List.get(i)` past the tuple's end.
                                let Some(ts) = tuple.get(i) else {
                                    return Err(KIRunError::uncaught(format!(
                                        "Index {i} out of bounds for length {}",
                                        tuple.len()
                                    )));
                                };
                                param_elements.push((ts.clone(), je));
                            }
                        }
                    } else if t.contains(SchemaType::Object) && value.is_object() {
                        let expr_schema = Parameter::expression();
                        let Some(name) = schema.get_name() else {
                            return Err(KIRunError::uncaught(
                                "Cannot invoke \"String.equals(Object)\" because the return value of \"com.fincity.nocode.kirun.engine.json.schema.Schema.getName()\" is null",
                            ));
                        };
                        let Value::Object(obj) = value else { continue };
                        if Some(name) == expr_schema.get_name() && schema.get_namespace() == expr_schema.get_namespace() {
                            if let Some(expression) = expression_dependency(&obj)? {
                                add_dependencies(se, Some(&expression));
                            }
                        } else if let Some(props) = schema.get_properties() {
                            for (k, v) in obj {
                                if let Some(ps) = props.get(&k) {
                                    param_elements.push((ps.clone(), v));
                                }
                            }
                        }
                    }
                }
            }
            ParameterReferenceType::Expression => {
                // JS checks `isNullValue(SchemaUtil.getDefaultValue(...))` without awaiting the
                // promise, which is never null, so a blank expression never adds a message.
                if !StringUtil::is_null_or_blank(r.get_expression()) {
                    add_dependencies(se, r.get_expression());
                }
            }
        }
        Ok(())
    }

    fn execute_graph<'a>(
        &'a self,
        e_graph: &'a Graph,
        in_context: &'a mut FunctionExecutionParameters,
    ) -> BoxFuture<'a, Result<FunctionOutput>> {
        Box::pin(async move {
            let mut execution_que: VecDeque<String> =
                e_graph.get_vertices_with_no_incoming_edges().iter().map(|v| v.get_key().to_string()).collect();
            let mut branch_que: VecDeque<Branch> = VecDeque::new();

            while (!execution_que.is_empty() || !branch_que.is_empty()) && !has_event(in_context, Event::OUTPUT) {
                self.process_branch_que(e_graph, in_context, &mut execution_que, &mut branch_que).await?;
                self.process_execution_que(e_graph, in_context, &mut execution_que, &mut branch_que).await?;

                // Count every pass, unconditionally: a graph that makes no progress must still
                // trip the guard.
                let count = in_context.get_count() + 1;
                in_context.set_count_in_place(count);

                let max = MAX_EXECUTION_ITERATIONS.load(Ordering::Relaxed);
                if count >= max {
                    return Err(KIRunError::runtime(format!(
                        "Execution locked in an infinite loop : {}.{} exceeded {} statement iterations",
                        self.fd.get_namespace(),
                        self.fd.get_name(),
                        max
                    )));
                }
            }

            let events = in_context.get_events().map(|e| e.read().clone()).unwrap_or_default();

            if !e_graph.is_sub_graph() && events.is_empty() {
                let event_map = self.fd.get_events();
                if !event_map.is_empty() {
                    // kirun-java reads `eventMap.get(OUTPUT).getParameters()` without a null
                    // check: events declared without `output` fail a run that raised none.
                    let Some(output) = event_map.get(Event::OUTPUT) else {
                        return Err(KIRunError::uncaught(
                            "Cannot invoke \"com.fincity.nocode.kirun.engine.model.Event.getParameters()\" because the return value of \"java.util.Map.get(Object)\" is null",
                        ));
                    };
                    if !output.get_parameters().is_empty() {
                        return Err(KIRunError::runtime("No events raised"));
                    }
                }
            }

            let er: Vec<EventResult> = events
                .into_iter()
                .flat_map(|(name, list)| list.into_iter().map(move |v| EventResult::of(&name, v)))
                .collect();

            let function_output = if !er.is_empty() || e_graph.is_sub_graph() {
                FunctionOutput::new(er)
            } else {
                FunctionOutput::new(vec![EventResult::of(Event::OUTPUT, Map::new())])
            };

            if let Some(dc) = self.debug() {
                dc.end_execution(in_context.get_execution_id());
            }

            Ok(function_output)
        })
    }

    async fn process_execution_que(
        &self,
        e_graph: &Graph,
        in_context: &FunctionExecutionParameters,
        execution_que: &mut VecDeque<String>,
        branch_que: &mut VecDeque<Branch>,
    ) -> Result<()> {
        if execution_que.is_empty() {
            return Ok(());
        }

        let mut ready = Vec::new();
        with_steps(in_context, |steps| {
            for key in execution_que.drain(..).collect::<Vec<_>>() {
                match e_graph.get_vertex(&key) {
                    Some(v) if all_dependencies_resolved_vertex(v, steps) => ready.push(key),
                    Some(_) => execution_que.push_back(key),
                    None => {}
                }
            }
        });

        if ready.is_empty() {
            return Ok(());
        }

        // JS runs the ready vertices with Promise.all; the first failure wins.
        let outcomes = join_all(ready.iter().map(|key| self.execute_vertex(e_graph, key, in_context))).await;
        for outcome in outcomes {
            match outcome? {
                VertexOutcome::Skipped => {}
                VertexOutcome::Next(keys) => execution_que.extend(keys),
                // JS LinkedList.push adds at the head.
                VertexOutcome::Branch(b) => branch_que.push_front(b),
            }
        }
        Ok(())
    }

    async fn process_branch_que(
        &self,
        e_graph: &Graph,
        in_context: &mut FunctionExecutionParameters,
        execution_que: &mut VecDeque<String>,
        branch_que: &mut VecDeque<Branch>,
    ) -> Result<()> {
        if branch_que.is_empty() {
            return Ok(());
        }

        let mut ready = Vec::new();
        with_steps(in_context, |steps| {
            for branch in branch_que.drain(..).collect::<Vec<_>>() {
                if all_dependencies_resolved_tuples(&branch.unresolved, steps) {
                    ready.push(branch);
                } else {
                    branch_que.push_back(branch);
                }
            }
        });

        // Sequential: branches share state.
        for branch in ready {
            self.execute_branch(e_graph, in_context, execution_que, branch).await?;
        }
        Ok(())
    }

    async fn execute_branch(
        &self,
        e_graph: &Graph,
        in_context: &mut FunctionExecutionParameters,
        execution_que: &mut VecDeque<String>,
        mut branch: Branch,
    ) -> Result<()> {
        let statements_to_delete: Vec<String> =
            branch.graph.get_vertices_data().iter().map(|e| e.get_statement().get_statement_name().to_string()).collect();

        let mut next_output: Option<EventResult>;
        loop {
            // Clear the previous iteration's step outputs.
            if let Some(steps) = in_context.get_steps() {
                let mut steps = steps.write();
                for name in &statements_to_delete {
                    steps.shift_remove(name);
                }
            }

            self.execute_graph(&branch.graph, in_context).await?;
            next_output = branch.output.next();

            if let Some(next) = &next_output {
                if let Some(steps) = in_context.get_steps() {
                    steps
                        .write()
                        .entry(branch.vertex_key.clone())
                        .or_default()
                        .insert(next.get_name().to_string(), next.get_result().clone());
                }
            }

            match &next_output {
                Some(n) if n.get_name() != Event::OUTPUT => continue,
                _ => break,
            }
        }

        if next_output.is_some_and(|n| n.get_name() == Event::OUTPUT) {
            if let Some(vertex) = e_graph.get_vertex(&branch.vertex_key) {
                if let Some(outs) = vertex.get_out_vertices().get(Event::OUTPUT) {
                    with_steps(in_context, |steps| {
                        for key in outs {
                            if e_graph.get_vertex(key).is_some_and(|v| all_dependencies_resolved_vertex(v, steps)) {
                                execution_que.push_back(key.clone());
                            }
                        }
                    });
                }
            }
        }
        Ok(())
    }

    async fn execute_vertex(
        &self,
        e_graph: &Graph,
        key: &str,
        in_context: &FunctionExecutionParameters,
    ) -> Result<VertexOutcome> {
        let Some(vertex) = e_graph.get_vertex(key) else {
            return Ok(VertexOutcome::Skipped);
        };
        let se = vertex.get_data().clone();
        let s = se.get_statement();

        if !s.get_execute_iftrue().is_empty() {
            for (expression, enabled) in s.get_execute_iftrue() {
                if !*enabled {
                    continue;
                }
                let v = ExpressionEvaluator::new(expression).evaluate(in_context.get_values_map())?;
                if v.is_null() || v == Value::Bool(false) {
                    return Ok(VertexOutcome::Skipped);
                }
            }
        }

        let f_repo = in_context.get_function_repository().clone();
        let Some(fun) = self.get_cached_function(f_repo.as_ref(), s.get_namespace(), s.get_name()).await else {
            return Err(KIRunError::runtime(format!("{}.{} function is not found.", s.get_namespace(), s.get_name())));
        };

        let args = get_arguments_from_parameters_map(in_context, s, fun.get_signature().get_parameters())?;
        let validate_schema_args = is_validate_schema(s).then(|| args.clone());

        let debug = self.debug().filter(|d| d.is_enabled());
        let step_id = debug.and_then(|dc| {
            dc.start_step(
                in_context.get_execution_id(),
                s.get_statement_name(),
                &format!("{}.{}", s.get_namespace(), s.get_name()),
                Some(map_to_value(&args)),
                Some(&self.function_name()),
            )
        });

        let is_ki_runtime = fun.as_any().is_some_and(|a| a.is::<KIRuntime>());

        let mut fep = if is_ki_runtime {
            let values_map = in_context
                .get_values_map()
                .iter()
                .filter(|(_, e)| {
                    let p = e.get_prefix();
                    p != ArgumentsTokenValueExtractor::PREFIX
                        && p != OutputMapTokenValueExtractor::PREFIX
                        && p != ContextTokenValueExtractor::PREFIX
                })
                .map(|(k, e)| (k.clone(), e.clone()))
                .collect();
            FunctionExecutionParameters::with_execution_id(
                in_context.get_function_repository().clone(),
                in_context.get_schema_repository().clone(),
                in_context.get_execution_id(),
            )
            .set_arguments(args)
            .set_values_map(values_map)
        } else {
            FunctionExecutionParameters::for_step(in_context, args, se.clone())
        };

        let executed: Result<(FunctionOutput, EventResult)> = async {
            let mut result = fun.execute(&mut fep).await?;
            let er = result
                .next()
                .ok_or_else(|| KIRunError::runtime(format!("Executing {} returned no events", s.get_statement_name())))?;
            if let Some(steps) = in_context.get_steps() {
                steps
                    .write()
                    .entry(s.get_statement_name().to_string())
                    .or_default()
                    .insert(er.get_name().to_string(), er.get_result().clone());
            }
            Ok((result, er))
        }
        .await;

        let (result, er) = match executed {
            Ok(x) => x,
            Err(err) => {
                if let (Some(dc), Some(id)) = (debug, &step_id) {
                    dc.end_step(in_context.get_execution_id(), id, "error", None, Some(&err.to_string()));
                }
                return Err(err);
            }
        };

        if let (Some(dc), Some(id)) = (debug, &step_id) {
            dc.end_step(in_context.get_execution_id(), id, er.get_name(), Some(map_to_value(er.get_result())), None);
        }

        if let Some(vs_args) = validate_schema_args {
            if er.get_result().get("isValid") == Some(&Value::Bool(true)) {
                write_back_validated_source(in_context, s, &vs_args).await;
            }
        }

        if er.get_name() != Event::OUTPUT {
            let (graph, unresolved) = self.sub_graph_of_type(e_graph, key, er.get_name());
            return Ok(VertexOutcome::Branch(Branch {
                graph,
                unresolved,
                output: result,
                vertex_key: key.to_string(),
            }));
        }

        let mut next = Vec::new();
        if let Some(outs) = vertex.get_out_vertices().get(Event::OUTPUT) {
            with_steps(in_context, |steps| {
                for k in outs {
                    if e_graph.get_vertex(k).is_some_and(|v| all_dependencies_resolved_vertex(v, steps)) {
                        next.push(k.clone());
                    }
                }
            });
        }
        Ok(VertexOutcome::Next(next))
    }

    /// JS `vertex.getSubGraphOfType(type)` plus `makeEdges` on first build. Like JS, the
    /// unresolved dependencies come back only when the sub-graph is first built.
    fn sub_graph_of_type(&self, e_graph: &Graph, key: &str, event: &str) -> (Arc<Graph>, Vec<(String, String)>) {
        let cache_key = (key.to_string(), event.to_string());
        if let Some(g) = self.sub_graph_cache.lock().get(&cache_key) {
            return (g.clone(), Vec::new());
        }
        let mut sub = e_graph.get_sub_graph_of_type(key, event);
        let (unresolved, _) = make_edges(&mut sub);
        sub.set_edges_built(true);
        let sub = Arc::new(sub);
        self.sub_graph_cache.lock().insert(cache_key, sub.clone());
        (sub, unresolved)
    }
}

#[async_trait]
impl AbstractFunction for KIRuntime {
    fn get_signature(&self) -> &FunctionSignature {
        &self.fd.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        if context.get_context().is_none() {
            *context = context.clone().set_context(Map::new());
        }
        if context.get_events().is_none() {
            *context = context.clone().set_events(Map::new());
        }
        if context.get_steps().is_none() {
            *context = context.clone().set_steps(Map::new());
        }
        if let Some(args) = context.get_arguments().cloned() {
            context.add_token_value_extractor_in_place(vec![Arc::new(ArgumentsTokenValueExtractor::new(args))]);
        }

        if let Some(dc) = self.debug() {
            dc.start_execution(context.get_execution_id(), &self.function_name(), Some(self.fd.to_json()));
        }

        let plan = self
            .get_execution_plan(context.get_function_repository(), context.get_schema_repository())
            .await?;

        // kirun-java prints the messages as its `List<StatementMessage>`: Lombok's
        // `StatementMessage(messageType=ERROR, message=...)` for each, in a `[a, b]` list. Its
        // vertices sit in a `ConcurrentHashMap`, so with messages on several steps the order
        // can differ; kirun-rust lists the steps in definition order.
        let messages: Vec<String> = plan
            .get_vertices_data()
            .iter()
            .flat_map(|e| e.get_messages().iter())
            .map(|m| {
                format!(
                    "StatementMessage(messageType={}, message={})",
                    m.get_message_type().map(|t| t.name()).unwrap_or("null"),
                    m.get_message().unwrap_or("null")
                )
            })
            .collect();

        if !messages.is_empty() {
            if let Some(dc) = self.debug() {
                dc.mark_errored(context.get_execution_id());
            }
            return Err(KIRunError::runtime(format!(
                "Please fix the errors in the function definition before execution : \n[{}]",
                messages.join(", ")
            )));
        }

        let graph: Graph = plan.map_data(|se| Arc::new(se.clone()));
        let result = self.execute_graph(&graph, context).await;
        if result.is_err() {
            if let Some(dc) = self.debug() {
                dc.mark_errored(context.get_execution_id());
            }
        }
        result
    }

    fn as_any(&self) -> Option<&dyn Any> {
        Some(self)
    }
}

fn needs_a_value(parameter_name: &str) -> String {
    PARAMETER_NEEDS_A_VALUE.replace('$', parameter_name)
}

fn has_event(ctx: &FunctionExecutionParameters, name: &str) -> bool {
    ctx.get_events().is_some_and(|e| e.read().contains_key(name))
}

/// Runs `f` on the step outputs under a read lock (JS reads the live map; cloning it for every
/// readiness check was a full copy per step). Callers never await inside `f`.
fn with_steps<R>(ctx: &FunctionExecutionParameters, f: impl FnOnce(&StepsMap) -> R) -> R {
    match ctx.get_steps() {
        Some(steps) => f(&steps.read_recursive()),
        None => f(&StepsMap::new()),
    }
}

fn map_to_value(m: &Map<Value>) -> Value {
    Value::Object(m.iter().map(|(k, v)| (k.clone(), v.clone())).collect())
}

/// JS `addDependencies`: every `Steps.<step>.<event>` in the expression.
fn add_dependencies(se: &mut StatementExecution, expression: Option<&str>) {
    let Some(expression) = expression else { return };
    for m in STEP_REGEX.find_iter(expression) {
        se.add_dependency(m.as_str());
    }
}

/// kirun-java's read of a `{isExpression, value}` object in a VALUE parameter: the expression
/// when `isExpression` is true. Gson's errors for a missing or null `isExpression` (or `value`)
/// leave the run as kirun-java's do.
fn expression_dependency(obj: &serde_json::Map<String, Value>) -> Result<Option<String>> {
    const GET: &str = "the return value of \"com.google.gson.JsonObject.get(String)\"";
    let npe = |method: &str| {
        KIRunError::uncaught(format!("Cannot invoke \"com.google.gson.JsonElement.{method}\" because {GET} is null"))
    };
    let is_expression = match obj.get("isExpression") {
        None => return Err(npe("getAsBoolean()")),
        Some(Value::Null) => return Err(KIRunError::uncaught("JsonNull")),
        Some(Value::Bool(b)) => *b,
        Some(Value::String(s)) => s.eq_ignore_ascii_case("true"),
        Some(Value::Number(_)) => false,
        Some(Value::Object(_)) => return Err(KIRunError::uncaught("JsonObject")),
        Some(Value::Array(a)) => return Err(KIRunError::uncaught(format!("Array must have size 1, but has size {}", a.len()))),
    };
    if !is_expression {
        return Ok(None);
    }
    match obj.get("value") {
        None => Err(npe("getAsString()")),
        Some(Value::Null) => Err(KIRunError::uncaught("JsonNull")),
        Some(Value::String(s)) => Ok(Some(s.clone())),
        Some(Value::Number(n)) => Ok(Some(n.to_string())),
        Some(Value::Bool(b)) => Ok(Some(b.to_string())),
        Some(Value::Object(_)) => Err(KIRunError::uncaught("JsonObject")),
        Some(Value::Array(a)) => Err(KIRunError::uncaught(format!("Array must have size 1, but has size {}", a.len()))),
    }
}

/// JS `allDependenciesResolvedVertex`: every parent has raised the event this vertex waits on.
fn all_dependencies_resolved_vertex<T: GraphVertexType>(vertex: &GraphVertex<T>, output: &StepsMap) -> bool {
    vertex
        .get_in_vertices()
        .iter()
        .all(|(step, event)| output.get(step).is_some_and(|m| m.contains_key(event)))
}

/// JS `allDependenciesResolvedTuples`.
fn all_dependencies_resolved_tuples(unresolved: &[(String, String)], output: &StepsMap) -> bool {
    unresolved.iter().all(|(step, event)| output.get(step).is_some_and(|m| m.contains_key(event)))
}

/// JS `makeEdges`: links each vertex to the steps its dependencies name. Returns the
/// dependencies on steps outside this graph, and an error message per statement for them.
pub fn make_edges<T: GraphVertexType + Clone>(graph: &mut ExecutionGraph<T>) -> (Vec<(String, String)>, Map<String>) {
    let mut ret_value = Vec::new();
    let mut ret_map = Map::new();

    let deps: Vec<(String, Vec<String>)> = graph
        .get_node_map()
        .values()
        .map(|v| (v.get_key().to_string(), v.get_data().get_dependencies().iter().cloned().collect()))
        .collect();

    for (key, dependencies) in deps {
        for d in dependencies {
            let (step, event) = split_dependency(&d);
            if graph.get_vertex(&step).is_none() {
                ret_value.push((step.clone(), event));
                ret_map.insert(key.clone(), format!("Unable to find the step with name {step}"));
            } else {
                graph.add_edge(&step, &event, &key);
            }
        }
    }
    (ret_value, ret_map)
}

/// `Steps.<step>.<event>[.rest]` → (step, event).
fn split_dependency(d: &str) -> (String, String) {
    let rest = d.get(6..).unwrap_or("");
    let mut parts = rest.splitn(3, '.');
    let step = parts.next().unwrap_or("").to_string();
    let event = parts.next().unwrap_or("").to_string();
    (step, event)
}

fn is_validate_schema(s: &Statement) -> bool {
    s.get_namespace() == "System" && s.get_name() == "ValidateSchema"
}

/// kirun-java's `System.ValidateSchema` converts the source object in place (its validators
/// put each converted property back into the same `JsonObject`), and the source is the very
/// object the step read, so `Arguments.query.size` sent as 3 reads "3" after a STRING check.
/// Stored functions rely on it (`leadPrepQuery` trims `Arguments.query.size` after the check).
/// kirun-rust copies values, so the converted object is written back to the path the source
/// came from. Only a source that is one expression naming a plain path (optionally followed by
/// `?? default`) to an object or array is written back: a primitive is never changed in place
/// in Java either. Java's sharing across function calls (a caller's argument object changing
/// when the callee converts it) is not reproduced.
async fn write_back_validated_source(in_context: &FunctionExecutionParameters, s: &Statement, args: &Map<Value>) {
    let Some(original) = args.get("source").filter(|v| v.is_object() || v.is_array()) else { return };
    let Some(path) = validated_source_path(s) else { return };
    let Some(converted) = ValidateSchema::converted_source(args, in_context.get_schema_repository().as_ref()).await
    else {
        return;
    };
    if &converted == original {
        return;
    }
    let prefix = &path[..path.find('.').map_or(path.len(), |i| i + 1)];
    if let Some(extractor) = in_context.get_values_map().get(prefix) {
        extractor.write_back(&path, converted);
    }
}

/// The plain path a `ValidateSchema` step's source reads, when the source is a single
/// expression such as `Arguments.query` or `Context.q ?? {}`.
fn validated_source_path(s: &Statement) -> Option<String> {
    let refs = s.get_parameter_map().get("source")?;
    let [r] = refs.values().collect::<Vec<_>>()[..] else { return None };
    if r.get_type() != ParameterReferenceType::Expression {
        return None;
    }
    let expression = r.get_expression()?;
    let path = expression.split("??").next()?.trim();
    let rooted = ["Arguments.", "Context.", "Steps."].iter().any(|p| path.starts_with(p));
    let plain = path.chars().all(|c| c.is_alphanumeric() || matches!(c, '_' | '$' | '.' | '[' | ']'));
    (rooted && plain).then(|| path.to_string())
}

/// kirun-java `ReactiveKIRuntime.getArgumentsFromParametersMap`. A variable argument drops the
/// references that come to `null` (an empty expression, a path to nothing) before it spreads
/// arrays, as kirun-java does; kirun-js keeps them and the argument then fails validation.
/// Nulls inside an array a reference gives are kept (kirun-java filters before it flattens).
fn get_arguments_from_parameters_map(
    in_context: &FunctionExecutionParameters,
    s: &Statement,
    param_set: &Map<Parameter>,
) -> Result<Map<Value>> {
    let mut args = Map::new();
    for (param_name, refs) in s.get_parameter_map() {
        if refs.is_empty() {
            continue;
        }
        let Some(p_def) = param_set.get(param_name) else { continue };

        let ret = if p_def.is_variable_argument() {
            let mut list: Vec<&ParameterReference> = refs.values().collect();
            list.sort_by_key(|r| r.get_order().unwrap_or(0));
            let mut values = Vec::new();
            for r in list {
                match parameter_reference_evaluation(in_context, r)? {
                    Value::Null => {}
                    Value::Array(a) => values.extend(a),
                    v => values.push(v),
                }
            }
            Value::Array(values)
        } else {
            parameter_reference_evaluation(in_context, refs.values().next().unwrap())?
        };

        if !ret.is_null() {
            args.insert(param_name.clone(), ret);
        }
    }
    Ok(args)
}

/// JS `parameterReferenceEvaluation`. JS also resolves `JsonExpression` objects inside VALUE
/// references; those only exist when built in code, never in JSON definitions.
fn parameter_reference_evaluation(in_context: &FunctionExecutionParameters, r: &ParameterReference) -> Result<Value> {
    match r.get_type() {
        ParameterReferenceType::Value => Ok(r.get_value().clone()),
        ParameterReferenceType::Expression => match r.get_expression() {
            Some(e) if !StringUtil::is_null_or_blank(Some(e)) => {
                ExpressionEvaluator::new(e).evaluate(in_context.get_values_map())
            }
            _ => Ok(Value::Null),
        },
    }
}
