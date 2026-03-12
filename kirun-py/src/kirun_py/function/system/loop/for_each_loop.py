from __future__ import annotations

from typing import Any, List, Optional, TYPE_CHECKING

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

SOURCE = 'source'
EACH = 'each'
INDEX = 'index'
VALUE = 'value'


class _ForEachLoopGenerator:

    def __init__(self, source: List[Any], statement_name: Optional[str],
                 context: FunctionExecutionParameters) -> None:
        self._source = source
        self._current = 0
        self._statement_name = statement_name
        self._context = context

    def next(self) -> Optional[EventResult]:
        if (self._current >= len(self._source) or
                (self._statement_name and
                 self._context.get_execution_context().get(self._statement_name))):
            # check for break
            if self._statement_name:
                exec_ctx = self._context.get_execution_context()
                if self._statement_name in exec_ctx:
                    del exec_ctx[self._statement_name]
            return EventResult.output_of({VALUE: self._current})

        eve = EventResult.of(
            Event.ITERATION,
            {INDEX: self._current, EACH: self._source[self._current]},
        )
        self._current += 1
        return eve


class ForEachLoop(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('ForEachLoop')
            .set_namespace(Namespaces.SYSTEM_LOOP)
            .set_parameters(dict([
                Parameter.of_entry(SOURCE, Schema.of_array(SOURCE, Schema.of_any(SOURCE))),
            ]))
            .set_events(dict([
                Event.event_map_entry(
                    Event.ITERATION,
                    {
                        INDEX: Schema.of(INDEX, SchemaType.INTEGER),
                        EACH: Schema.of_any(EACH),
                    },
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
        source: List[Any] = args.get(SOURCE, []) if args else []

        se = context.get_statement_execution()
        statement_name: Optional[str] = None
        if se is not None:
            stmt = se.get_statement()
            if stmt is not None:
                statement_name = stmt.get_statement_name()

        return FunctionOutput(_ForEachLoopGenerator(source, statement_name, context))
