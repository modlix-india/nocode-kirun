from __future__ import annotations

import asyncio
import re
from collections import deque
from typing import Any, Dict, List, Optional, Set, Tuple, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.json_expression import JsonExpression
from kirun_py.json.schema.array.array_schema_type import ArraySchemaType
from kirun_py.json.schema.schema_util import SchemaUtil
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_definition import FunctionDefinition
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.model.parameter_reference import ParameterReference
from kirun_py.model.parameter_reference_type import ParameterReferenceType
from kirun_py.runtime.context_element import ContextElement
from kirun_py.runtime.debug.debug_collector import DebugCollector
from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.runtime.graph.execution_graph import ExecutionGraph
from kirun_py.runtime.graph.graph_vertex import GraphVertex
from kirun_py.runtime.statement_execution import StatementExecution
from kirun_py.runtime.statement_message_type import StatementMessageType
from kirun_py.runtime.tokenextractor.arguments_token_value_extractor import ArgumentsTokenValueExtractor
from kirun_py.runtime.tokenextractor.context_token_value_extractor import ContextTokenValueExtractor
from kirun_py.runtime.tokenextractor.output_map_token_value_extractor import OutputMapTokenValueExtractor
from kirun_py.util.null_check import is_null_value
from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.util.string.string_util import StringUtil
from kirun_py.util.tuples import Tuple2, Tuple4

if TYPE_CHECKING:
    from kirun_py.function.function import Function
    from kirun_py.json.schema.schema import Schema
    from kirun_py.repository import Repository


class KIRuntime(AbstractFunction):
    """Main execution engine for KIRun function definitions."""

    PARAMETER_NEEDS_A_VALUE: str = 'Parameter "$" needs a value'

    _STEP_REGEX_PATTERN: re.Pattern = re.compile(
        r'Steps\.([a-zA-Z0-9\\-]+)\.([a-zA-Z0-9\\-]+)'
    )

    VERSION: int = 1

    MAX_EXECUTION_ITERATIONS: int = 10_000_000

    def __init__(self, fd: FunctionDefinition, debug_mode: bool = False) -> None:
        super().__init__()
        self._debug_mode: bool = debug_mode
        self._fd: FunctionDefinition = fd
        self._function_cache: Dict[str, Any] = {}

        if self._debug_mode:
            DebugCollector.get_instance().enable()

        if self._fd.get_version() > KIRuntime.VERSION:
            raise KIRuntimeException(
                'Runtime is at a lower version '
                + str(KIRuntime.VERSION)
                + ' and trying to run code from version '
                + str(self._fd.get_version())
                + '.'
            )

    def get_signature(self) -> FunctionSignature:
        return self._fd

    # ------------------------------------------------------------------
    # Function cache
    # ------------------------------------------------------------------

    async def _get_cached_function(
        self,
        f_repo: Repository,
        namespace: str,
        name: str,
    ) -> Optional[Any]:
        key = f'{namespace}.{name}'
        if key in self._function_cache:
            return self._function_cache[key]
        fun = await f_repo.find(namespace, name)
        if fun is not None:
            self._function_cache[key] = fun
        return fun

    # ------------------------------------------------------------------
    # Execution plan
    # ------------------------------------------------------------------

    async def get_execution_plan(
        self,
        f_repo: Repository,
        s_repo: Repository,
    ) -> ExecutionGraph:
        g: ExecutionGraph = ExecutionGraph()

        # Parallelize statement preparation
        statements = list(self._fd.get_steps().values())
        statement_executions = await asyncio.gather(
            *(self._prepare_statement_execution(s, f_repo, s_repo) for s in statements)
        )

        for se in statement_executions:
            g.add_vertex(se)

        unresolved = self.make_edges(g)

        for step_name, msg in unresolved.get_t2().items():
            ex = g.get_node_map().get(step_name)
            if ex is None:
                continue
            ex.get_data().add_message(StatementMessageType.ERROR, msg)

        return g

    # ------------------------------------------------------------------
    # Internal execute (called by AbstractFunction.execute)
    # ------------------------------------------------------------------

    async def internal_execute(
        self,
        in_context: FunctionExecutionParameters,
    ) -> FunctionOutput:
        if in_context.get_context() is None:
            in_context.set_context({})

        if in_context.get_events() is None:
            in_context.set_events({})

        if in_context.get_steps() is None:
            in_context.set_steps({})

        if in_context.get_arguments() is not None:
            in_context.add_token_value_extractor(
                ArgumentsTokenValueExtractor(in_context.get_arguments())
            )

        if self._debug_mode:
            function_name = (
                f'{self._fd.get_namespace()}.{self._fd.get_name()}'
                if self._fd.get_namespace()
                else self._fd.get_name()
            )
            DebugCollector.get_instance().start_execution(
                in_context.get_execution_id(),
                function_name,
                self._fd.to_json(),
            )

        e_graph: ExecutionGraph = await self.get_execution_plan(
            in_context.get_function_repository(),
            in_context.get_schema_repository(),
        )

        messages: List[str] = [
            e.get_statement().get_statement_name()
            + ': \n'
            + ','.join(str(m) for m in e.get_messages())
            for e in e_graph.get_vertices_data()
            if e.get_messages()
        ]

        if messages:
            if self._debug_mode:
                DebugCollector.get_instance().mark_errored(in_context.get_execution_id())
            raise KIRuntimeException(
                'Please fix the errors in the function definition before execution : \n'
                + ',\n'.join(messages)
            )

        try:
            return await self._execute_graph(e_graph, in_context)
        except Exception:
            if self._debug_mode:
                DebugCollector.get_instance().mark_errored(in_context.get_execution_id())
            raise

    # ------------------------------------------------------------------
    # Graph execution
    # ------------------------------------------------------------------

    async def _execute_graph(
        self,
        e_graph: ExecutionGraph,
        in_context: FunctionExecutionParameters,
    ) -> FunctionOutput:
        execution_que: deque = deque(e_graph.get_vertices_with_no_incoming_edges())

        branch_que: deque = deque()

        while (execution_que or branch_que) and Event.OUTPUT not in (
            in_context.get_events() or {}
        ):
            prev_exec_que_size = len(execution_que)
            prev_branch_que_size = len(branch_que)

            await self._process_branch_que(in_context, execution_que, branch_que)
            await self._process_execution_que(in_context, execution_que, branch_que)

            if prev_exec_que_size != len(execution_que) or prev_branch_que_size != len(branch_que):
                in_context.set_count(in_context.get_count() + 1)

                if in_context.get_count() == KIRuntime.MAX_EXECUTION_ITERATIONS:
                    raise KIRuntimeException('Execution locked in an infinite loop')

        events = in_context.get_events()
        if not e_graph.is_sub_graph() and not events:
            event_map = self.get_signature().get_events()
            if event_map and Event.OUTPUT in event_map:
                output_event = event_map[Event.OUTPUT]
                if output_event.get_parameters():
                    raise KIRuntimeException('No events raised')

        er_list: List[EventResult] = []
        for event_name, results in (events or {}).items():
            for v in results:
                er_list.append(EventResult.of(event_name, v))

        if er_list or e_graph.is_sub_graph():
            function_output = FunctionOutput(er_list)
        else:
            function_output = FunctionOutput([EventResult.of(Event.OUTPUT, {})])

        if self._debug_mode:
            DebugCollector.get_instance().end_execution(in_context.get_execution_id())

        return function_output

    # ------------------------------------------------------------------
    # Queue processing
    # ------------------------------------------------------------------

    async def _process_execution_que(
        self,
        in_context: FunctionExecutionParameters,
        execution_que: deque,
        branch_que: deque,
    ) -> None:
        if not execution_que:
            return

        # Collect all vertices from the queue
        all_vertices: List[GraphVertex] = []
        while execution_que:
            all_vertices.append(execution_que.popleft())

        # Separate ready and not-ready vertices
        ready_vertices: List[GraphVertex] = []
        not_ready_vertices: List[GraphVertex] = []
        steps = in_context.get_steps()

        for vertex in all_vertices:
            if self._all_dependencies_resolved_vertex(vertex, steps):
                ready_vertices.append(vertex)
            else:
                not_ready_vertices.append(vertex)

        # Add not-ready vertices back to the queue
        for vertex in not_ready_vertices:
            execution_que.append(vertex)

        # Execute all ready vertices in parallel
        if ready_vertices:
            await asyncio.gather(
                *(
                    self._execute_vertex(
                        vertex,
                        in_context,
                        branch_que,
                        execution_que,
                        in_context.get_function_repository(),
                    )
                    for vertex in ready_vertices
                )
            )

    async def _process_branch_que(
        self,
        in_context: FunctionExecutionParameters,
        execution_que: deque,
        branch_que: deque,
    ) -> None:
        if not branch_que:
            return

        # Collect all branches from the queue
        all_branches: List[Tuple4] = []
        while branch_que:
            all_branches.append(branch_que.popleft())

        # Separate ready and not-ready branches
        ready_branches: List[Tuple4] = []
        not_ready_branches: List[Tuple4] = []
        steps = in_context.get_steps()

        for branch in all_branches:
            if self._all_dependencies_resolved_tuples(branch.get_t2(), steps):
                ready_branches.append(branch)
            else:
                not_ready_branches.append(branch)

        # Add not-ready branches back to the queue
        for branch in not_ready_branches:
            branch_que.append(branch)

        # Execute all ready branches sequentially (shared state)
        for branch in ready_branches:
            await self._execute_branch(in_context, execution_que, branch)

    # ------------------------------------------------------------------
    # Branch execution
    # ------------------------------------------------------------------

    async def _execute_branch(
        self,
        in_context: FunctionExecutionParameters,
        execution_que: deque,
        branch: Tuple4,
    ) -> None:
        vertex: GraphVertex = branch.get_t4()
        next_output: Optional[EventResult] = None

        # Pre-compute statement names to delete
        statements_to_delete = [
            e.get_statement().get_statement_name()
            for e in branch.get_t1().get_vertices_data()
        ]

        while True:
            # Clear previous iteration's step outputs
            steps = in_context.get_steps()
            if steps is not None:
                for statement_name in statements_to_delete:
                    steps.pop(statement_name, None)

            await self._execute_graph(branch.get_t1(), in_context)
            next_output = branch.get_t3().next()

            if next_output is not None:
                step_name = vertex.get_data().get_statement().get_statement_name()
                if step_name not in (in_context.get_steps() or {}):
                    in_context.get_steps()[step_name] = {}

                in_context.get_steps()[step_name][next_output.get_name()] = (
                    self._resolve_internal_expressions(next_output.get_result(), in_context)
                )

            if next_output is None or next_output.get_name() == Event.OUTPUT:
                break

        if (
            next_output is not None
            and next_output.get_name() == Event.OUTPUT
            and Event.OUTPUT in vertex.get_out_vertices()
        ):
            out_vertices = list(vertex.get_out_vertices().get(Event.OUTPUT, set()))
            for e in out_vertices:
                if self._all_dependencies_resolved_vertex(e, in_context.get_steps()):
                    execution_que.append(e)

    # ------------------------------------------------------------------
    # Vertex execution
    # ------------------------------------------------------------------

    async def _execute_vertex(
        self,
        vertex: GraphVertex,
        in_context: FunctionExecutionParameters,
        branch_que: deque,
        execution_que: deque,
        f_repo: Repository,
    ) -> None:
        from kirun_py.model.statement import Statement

        s: Statement = vertex.get_data().get_statement()

        # Check execute-if-true conditions
        execute_if_true = s.get_execute_if_true()
        if execute_if_true:
            all_true = all(
                (lambda v: not is_null_value(v) and v is not False)(
                    ExpressionEvaluator(e).evaluate(in_context.get_values_map())
                )
                for e, flag in execute_if_true.items()
                if flag
            )
            if not all_true:
                return

        fun = await self._get_cached_function(f_repo, s.get_namespace(), s.get_name())

        if fun is None:
            raise KIRuntimeException(
                StringFormatter.format(
                    '$.$ function is not found.', s.get_namespace(), s.get_name()
                )
            )

        param_set: Dict[str, Parameter] = fun.get_signature().get_parameters() or {}

        args: Dict[str, Any] = self._get_arguments_from_parameters_map(
            in_context, s, param_set
        )

        # Record step start for debug
        step_id: Optional[str] = None
        debug_collector: Optional[DebugCollector] = (
            DebugCollector.get_instance() if self._debug_mode else None
        )
        if debug_collector is not None and debug_collector.is_enabled():
            kirun_function_name = (
                f'{self._fd.get_namespace()}.{self._fd.get_name()}'
                if self._fd.get_namespace()
                else self._fd.get_name()
            )
            step_id = debug_collector.start_step(
                in_context.get_execution_id(),
                s.get_statement_name(),
                f'{s.get_namespace()}.{s.get_name()}',
                args,
                kirun_function_name,
            )

        context = in_context.get_context()
        if context is None:
            context = {}

        if isinstance(fun, KIRuntime):
            fep = FunctionExecutionParameters(
                in_context.get_function_repository(),
                in_context.get_schema_repository(),
                in_context.get_execution_id(),
            ).set_arguments(args).set_values_map(
                {
                    k: v
                    for k, v in in_context.get_values_map().items()
                    if v.get_prefix() != ArgumentsTokenValueExtractor.PREFIX
                    and v.get_prefix() != OutputMapTokenValueExtractor.PREFIX
                    and v.get_prefix() != ContextTokenValueExtractor.PREFIX
                }
            )
        else:
            fep = (
                FunctionExecutionParameters(
                    in_context.get_function_repository(),
                    in_context.get_schema_repository(),
                    in_context.get_execution_id(),
                )
                .set_values_map(in_context.get_values_map())
                .set_context(context)
                .set_arguments(args)
                .set_events(in_context.get_events())
                .set_steps(in_context.get_steps())
                .set_statement_execution(vertex.get_data())
                .set_count(in_context.get_count())
                .set_execution_context(in_context.get_execution_context())
            )

        result: Optional[FunctionOutput] = None
        er: Optional[EventResult] = None

        try:
            result = await fun.execute(fep)
            er = result.next()

            if er is None:
                raise KIRuntimeException(
                    StringFormatter.format(
                        'Executing $ returned no events', s.get_statement_name()
                    )
                )

            steps = in_context.get_steps()
            if s.get_statement_name() not in steps:
                steps[s.get_statement_name()] = {}

            steps[s.get_statement_name()][er.get_name()] = (
                self._resolve_internal_expressions(er.get_result(), in_context)
            )
        except Exception as error:
            execution_error = str(error)

            if debug_collector is not None and step_id is not None:
                debug_collector.end_step(
                    in_context.get_execution_id(),
                    step_id,
                    'error',
                    None,
                    execution_error,
                )

            raise

        # Record step end for successful execution
        if debug_collector is not None and step_id is not None:
            debug_collector.end_step(
                in_context.get_execution_id(),
                step_id,
                er.get_name(),
                in_context.get_steps().get(s.get_statement_name(), {}).get(er.get_name()),
            )

        is_output: bool = er.get_name() == Event.OUTPUT

        if not is_output:
            sub_graph = vertex.get_sub_graph_of_type(er.get_name())
            unresolved_dependencies: List[Tuple2] = []
            if not sub_graph.are_edges_built():
                unresolved_dependencies = self.make_edges(sub_graph).get_t1()
                sub_graph.set_edges_built(True)
            branch_que.append(Tuple4(sub_graph, unresolved_dependencies, result, vertex))
        else:
            out: Optional[set] = vertex.get_out_vertices().get(Event.OUTPUT)
            if out is not None:
                for e in list(out):
                    if self._all_dependencies_resolved_vertex(e, in_context.get_steps()):
                        execution_que.append(e)

    # ------------------------------------------------------------------
    # Expression resolution
    # ------------------------------------------------------------------

    def _resolve_internal_expressions(
        self,
        result: Optional[Dict[str, Any]],
        in_context: FunctionExecutionParameters,
    ) -> Dict[str, Any]:
        if result is None:
            return result

        resolved: Dict[str, Any] = {}
        for key, value in result.items():
            resolved[key] = self._resolve_internal_expression(value, in_context)
        return resolved

    def _resolve_internal_expression(
        self, value: Any, in_context: FunctionExecutionParameters
    ) -> Any:
        if is_null_value(value) or not isinstance(value, (dict, list, JsonExpression)):
            if isinstance(value, (int, float, str, bool)):
                return value
            if is_null_value(value):
                return value
            # Fall through for other object types

        if isinstance(value, JsonExpression):
            return ExpressionEvaluator(value.get_expression()).evaluate(
                in_context.get_values_map()
            )

        if isinstance(value, list):
            return [self._resolve_internal_expression(obj, in_context) for obj in value]

        if isinstance(value, dict):
            return {
                k: self._resolve_internal_expression(v, in_context)
                for k, v in value.items()
            }

        return None

    # ------------------------------------------------------------------
    # Dependency checking
    # ------------------------------------------------------------------

    def _all_dependencies_resolved_tuples(
        self,
        unresolved_dependencies: List[Tuple2],
        output: Optional[Dict[str, Dict[str, Dict[str, Any]]]],
    ) -> bool:
        if output is None:
            output = {}
        for tup in unresolved_dependencies:
            if tup.get_t1() not in output:
                return False
            if tup.get_t2() not in (output.get(tup.get_t1()) or {}):
                return False
        return True

    def _all_dependencies_resolved_vertex(
        self,
        vertex: GraphVertex,
        output: Optional[Dict[str, Dict[str, Dict[str, Any]]]],
    ) -> bool:
        if output is None:
            output = {}
        in_vertices = vertex.get_in_vertices()
        if not in_vertices:
            return True

        for e in in_vertices:
            step_name = e.get_t1().get_data().get_statement().get_statement_name()
            type_ = e.get_t2()
            if step_name not in output or type_ not in (output.get(step_name) or {}):
                return False
        return True

    # ------------------------------------------------------------------
    # Argument resolution
    # ------------------------------------------------------------------

    def _get_arguments_from_parameters_map(
        self,
        in_context: FunctionExecutionParameters,
        s: Any,
        param_set: Dict[str, Parameter],
    ) -> Dict[str, Any]:
        args: Dict[str, Any] = {}

        for param_name, param_ref_map in s.get_parameter_map().items():
            pr_list: List[ParameterReference] = list(
                (param_ref_map or {}).values()
            )

            if not pr_list:
                continue

            p_def: Optional[Parameter] = param_set.get(param_name)
            if p_def is None:
                continue

            ret: Any
            if p_def.is_variable_argument():
                sorted_refs = sorted(
                    [r for r in pr_list if not is_null_value(r)],
                    key=lambda a: a.get_order() or 0,
                )
                ret_list: List[Any] = []
                for r in sorted_refs:
                    evaluated = self._parameter_reference_evaluation(in_context, r)
                    if isinstance(evaluated, list):
                        ret_list.extend(evaluated)
                    else:
                        ret_list.append(evaluated)
                ret = ret_list
            else:
                ret = self._parameter_reference_evaluation(in_context, pr_list[0])

            if not is_null_value(ret):
                args[param_name] = ret

        return args

    def _parameter_reference_evaluation(
        self,
        in_context: FunctionExecutionParameters,
        ref: ParameterReference,
    ) -> Any:
        ret: Any = None

        if ref.get_type() == ParameterReferenceType.VALUE:
            ret = self._resolve_internal_expression(ref.get_value(), in_context)
        elif (
            ref.get_type() == ParameterReferenceType.EXPRESSION
            and not StringUtil.is_null_or_blank(ref.get_expression())
        ):
            ret = ExpressionEvaluator(ref.get_expression() or '').evaluate(
                in_context.get_values_map()
            )
        return ret

    # ------------------------------------------------------------------
    # Statement preparation / validation
    # ------------------------------------------------------------------

    async def _prepare_statement_execution(
        self,
        s: Any,
        f_repo: Repository,
        s_repo: Repository,
    ) -> StatementExecution:
        se = StatementExecution(s)

        fun = await self._get_cached_function(f_repo, s.get_namespace(), s.get_name())

        if fun is None:
            se.add_message(
                StatementMessageType.ERROR,
                StringFormatter.format(
                    '$.$ is not available', s.get_namespace(), s.get_name()
                ),
            )
            return se

        param_set: Dict[str, Parameter] = dict(fun.get_signature().get_parameters() or {})
        if s.get_parameter_map() is None:
            return se

        for param_name, param_ref_map in s.get_parameter_map().items():
            p: Optional[Parameter] = param_set.get(param_name)
            if p is None:
                continue

            ref_list: List[ParameterReference] = list((param_ref_map or {}).values())

            if not ref_list and not p.is_variable_argument():
                if not SchemaUtil.has_default_value_or_null_schema_type(p.get_schema()):
                    se.add_message(
                        StatementMessageType.ERROR,
                        StringFormatter.format(
                            KIRuntime.PARAMETER_NEEDS_A_VALUE,
                            p.get_parameter_name(),
                        ),
                    )
                param_set.pop(p.get_parameter_name(), None)
                continue

            if p.is_variable_argument():
                ref_list.sort(key=lambda a: a.get_order() or 0)
                for ref in ref_list:
                    await self._parameter_reference_validation(se, p, ref, s_repo)
            elif ref_list:
                ref = ref_list[0]
                await self._parameter_reference_validation(se, p, ref, s_repo)

            param_set.pop(p.get_parameter_name(), None)

        # Process dependent statements
        dependent_stmts = se.get_statement().get_dependent_statements()
        if not is_null_value(dependent_stmts):
            for stmt_name, flag in dependent_stmts.items():
                if flag:
                    se.add_dependency(stmt_name)

        # Process execute-if-true
        execute_if_true = se.get_statement().get_execute_if_true()
        if not is_null_value(execute_if_true):
            for expr, flag in execute_if_true.items():
                if flag:
                    self._add_dependencies(se, expr)

        # Check remaining required parameters
        if param_set:
            for param in param_set.values():
                if param.is_variable_argument():
                    continue
                if not SchemaUtil.has_default_value_or_null_schema_type(param.get_schema()):
                    se.add_message(
                        StatementMessageType.ERROR,
                        StringFormatter.format(
                            KIRuntime.PARAMETER_NEEDS_A_VALUE,
                            param.get_parameter_name(),
                        ),
                    )

        return se

    async def _parameter_reference_validation(
        self,
        se: StatementExecution,
        p: Parameter,
        ref: Optional[ParameterReference],
        s_repo: Repository,
    ) -> None:
        if ref is None:
            default_val = SchemaUtil.get_default_value(p.get_schema(), s_repo)
            if is_null_value(default_val):
                se.add_message(
                    StatementMessageType.ERROR,
                    StringFormatter.format(
                        KIRuntime.PARAMETER_NEEDS_A_VALUE, p.get_parameter_name()
                    ),
                )
        elif ref.get_type() == ParameterReferenceType.VALUE:
            if is_null_value(ref.get_value()):
                if not SchemaUtil.has_default_value_or_null_schema_type(p.get_schema()):
                    se.add_message(
                        StatementMessageType.ERROR,
                        StringFormatter.format(
                            KIRuntime.PARAMETER_NEEDS_A_VALUE,
                            p.get_parameter_name(),
                        ),
                    )
                return

            # Walk the parameter value tree looking for JsonExpressions and dependencies
            param_elements: deque = deque()
            param_elements.append((p.get_schema(), ref.get_value()))

            while param_elements:
                schema_elem, val_elem = param_elements.popleft()

                if isinstance(val_elem, JsonExpression):
                    self._add_dependencies(se, val_elem.get_expression())
                else:
                    if is_null_value(schema_elem) or is_null_value(schema_elem.get_type()):
                        continue

                    if (
                        schema_elem.get_type().contains(SchemaType.ARRAY)
                        and isinstance(val_elem, list)
                    ):
                        ast: Optional[ArraySchemaType] = schema_elem.get_items()
                        if ast is None:
                            continue
                        if ast.is_single_type():
                            for je in val_elem:
                                param_elements.append((ast.get_single_schema(), je))
                        else:
                            tuple_schema = ast.get_tuple_schema()
                            if tuple_schema is not None:
                                for i in range(len(val_elem)):
                                    if i < len(tuple_schema):
                                        param_elements.append((tuple_schema[i], val_elem[i]))

                    elif (
                        schema_elem.get_type().contains(SchemaType.OBJECT)
                        and isinstance(val_elem, dict)
                    ):
                        sch = schema_elem

                        if (
                            sch.get_name() == Parameter.EXPRESSION.get_name()
                            and sch.get_namespace() == Parameter.EXPRESSION.get_namespace()
                        ):
                            is_expression = val_elem.get('isExpression', False)
                            if is_expression:
                                self._add_dependencies(se, val_elem.get('value'))
                        else:
                            if sch.get_properties() is not None:
                                for entry_key, entry_val in val_elem.items():
                                    prop_schema = sch.get_properties().get(entry_key)
                                    if prop_schema is not None:
                                        param_elements.append((prop_schema, entry_val))

        elif ref.get_type() == ParameterReferenceType.EXPRESSION:
            if StringUtil.is_null_or_blank(ref.get_expression()):
                # JS bug: getDefaultValue is async but called without await, so it
                # returns a Promise (never null), meaning variadic blank expressions
                # are silently skipped in JS. Match that behavior here.
                if not p.is_variable_argument():
                    default_val = SchemaUtil.get_default_value(p.get_schema(), s_repo)
                    if is_null_value(default_val):
                        se.add_message(
                            StatementMessageType.ERROR,
                            StringFormatter.format(
                                KIRuntime.PARAMETER_NEEDS_A_VALUE,
                                p.get_parameter_name(),
                            ),
                        )
            else:
                try:
                    self._add_dependencies(se, ref.get_expression())
                except Exception as err:
                    se.add_message(
                        StatementMessageType.ERROR,
                        StringFormatter.format(
                            'Error evaluating $ : $', ref.get_expression(), err
                        ),
                    )

    # ------------------------------------------------------------------
    # Dependency extraction
    # ------------------------------------------------------------------

    def _add_dependencies(
        self, se: StatementExecution, expression: Optional[str]
    ) -> None:
        if expression is None:
            return

        for match in KIRuntime._STEP_REGEX_PATTERN.finditer(expression):
            se.add_dependency(match.group(0))

    # ------------------------------------------------------------------
    # Edge building
    # ------------------------------------------------------------------

    def make_edges(
        self,
        graph: ExecutionGraph,
    ) -> Tuple2:
        """Build dependency edges in the execution graph.

        Returns a Tuple2 containing:
          - t1: list of Tuple2(step_name, event_name) for unresolved external deps
          - t2: dict mapping statement_name -> error message
        """
        values = graph.get_node_map().values()

        ret_value: List[Tuple2] = []
        ret_map: Dict[str, str] = {}

        for e in values:
            for d in e.get_data().get_dependencies():
                second_dot: int = d.index('.', 6)
                step: str = d[6:second_dot]
                event_dot: int = d.find('.', second_dot + 1)
                event: str = (
                    d[second_dot + 1:]
                    if event_dot == -1
                    else d[second_dot + 1:event_dot]
                )

                if step not in graph.get_node_map():
                    ret_value.append(Tuple2(step, event))
                    ret_map[e.get_data().get_statement().get_statement_name()] = (
                        StringFormatter.format(
                            'Unable to find the step with name $', step
                        )
                    )
                else:
                    e.add_in_edge_to(graph.get_node_map()[step], event)

        return Tuple2(ret_value, ret_map)
