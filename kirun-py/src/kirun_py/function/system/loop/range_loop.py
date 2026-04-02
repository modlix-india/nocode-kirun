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

FROM = 'from'
TO = 'to'
STEP = 'step'
VALUE = 'value'
INDEX = 'index'


class _RangeLoopGenerator:

    def __init__(self, from_val: float, to_val: float, step_val: float,
                 statement_name: Optional[str],
                 context: FunctionExecutionParameters) -> None:
        self._current = from_val
        self._to = to_val
        self._step = step_val
        self._forward = step_val > 0
        self._done = False
        self._statement_name = statement_name
        self._context = context

    def next(self) -> Optional[EventResult]:
        if self._done:
            return None

        if ((self._forward and self._current >= self._to) or
                (not self._forward and self._current <= self._to) or
                (self._statement_name and
                 self._context.get_execution_context().get(self._statement_name))):
            self._done = True
            if self._statement_name:
                exec_ctx = self._context.get_execution_context()
                if self._statement_name in exec_ctx:
                    del exec_ctx[self._statement_name]
            return EventResult.output_of({VALUE: self._current})

        eve = EventResult.of(Event.ITERATION, {INDEX: self._current})
        self._current += self._step
        return eve


class RangeLoop(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('RangeLoop')
            .set_namespace(Namespaces.SYSTEM_LOOP)
            .set_parameters(dict([
                Parameter.of_entry(
                    FROM,
                    Schema.of(
                        FROM,
                        SchemaType.INTEGER, SchemaType.LONG,
                        SchemaType.FLOAT, SchemaType.DOUBLE,
                    ).set_default_value(0),
                ),
                Parameter.of_entry(
                    TO,
                    Schema.of(
                        TO,
                        SchemaType.INTEGER, SchemaType.LONG,
                        SchemaType.FLOAT, SchemaType.DOUBLE,
                    ).set_default_value(1),
                ),
                Parameter.of_entry(
                    STEP,
                    Schema.of(
                        STEP,
                        SchemaType.INTEGER, SchemaType.LONG,
                        SchemaType.FLOAT, SchemaType.DOUBLE,
                    )
                    .set_default_value(1)
                    .set_not(Schema().set_constant(0)),
                ),
            ]))
            .set_events(dict([
                Event.event_map_entry(
                    Event.ITERATION,
                    {
                        INDEX: Schema.of(
                            INDEX,
                            SchemaType.INTEGER, SchemaType.LONG,
                            SchemaType.FLOAT, SchemaType.DOUBLE,
                        ),
                    },
                ),
                Event.output_event_map_entry({
                    VALUE: Schema.of(
                        VALUE,
                        SchemaType.INTEGER, SchemaType.LONG,
                        SchemaType.FLOAT, SchemaType.DOUBLE,
                    ),
                }),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        from_val: float = args.get(FROM, 0) if args else 0
        to_val: float = args.get(TO, 1) if args else 1
        step_val: float = args.get(STEP, 1) if args else 1

        se = context.get_statement_execution()
        statement_name: Optional[str] = None
        if se is not None:
            stmt = se.get_statement()
            if stmt is not None:
                statement_name = stmt.get_statement_name()

        return FunctionOutput(
            _RangeLoopGenerator(from_val, to_val, step_val, statement_name, context)
        )
