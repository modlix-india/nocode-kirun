from __future__ import annotations

from typing import Optional, TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

COUNT = 'count'
VALUE = 'value'
INDEX = 'index'


class _CountLoopGenerator:

    def __init__(self, count: int, statement_name: Optional[str],
                 context: FunctionExecutionParameters) -> None:
        self._count = count
        self._current = 0
        self._statement_name = statement_name
        self._context = context

    def next(self) -> Optional[EventResult]:
        if (self._current >= self._count or
                (self._statement_name and
                 self._context.get_execution_context().get(self._statement_name))):
            # check for break
            if self._statement_name:
                exec_ctx = self._context.get_execution_context()
                if self._statement_name in exec_ctx:
                    del exec_ctx[self._statement_name]
            return EventResult.output_of({VALUE: self._current})

        eve = EventResult.of(Event.ITERATION, {INDEX: self._current})
        self._current += 1
        return eve


class CountLoop(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('CountLoop')
            .set_namespace(Namespaces.SYSTEM_LOOP)
            .set_parameters(dict([
                Parameter.of_entry(COUNT, Schema.of(COUNT, SchemaType.INTEGER)),
            ]))
            .set_events(dict([
                Event.event_map_entry(
                    Event.ITERATION,
                    {INDEX: Schema.of(INDEX, SchemaType.INTEGER)},
                ),
                Event.output_event_map_entry(
                    {VALUE: Schema.of(VALUE, SchemaType.INTEGER)},
                ),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        count: int = args.get(COUNT, 0) if args else 0

        se = context.get_statement_execution()
        statement_name: Optional[str] = None
        if se is not None:
            stmt = se.get_statement()
            if stmt is not None:
                statement_name = stmt.get_statement_name()

        return FunctionOutput(_CountLoopGenerator(count, statement_name, context))
