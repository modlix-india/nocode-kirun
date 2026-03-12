from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class If(AbstractFunction):

    CONDITION: str = 'condition'

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('If')
            .set_namespace(Namespaces.SYSTEM)
            .set_parameters(dict([
                Parameter.of_entry(If.CONDITION, Schema.of_any(If.CONDITION)),
            ]))
            .set_events(dict([
                Event.event_map_entry(Event.TRUE, {}),
                Event.event_map_entry(Event.FALSE, {}),
                Event.output_event_map_entry({}),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        condition = args.get(If.CONDITION) if args else None

        # JS-style: None, False, 0 are falsy; '', {}, [] are truthy
        condition_value = not (
            condition is None
            or condition is False
            or (isinstance(condition, (int, float)) and not isinstance(condition, bool) and condition == 0)
        )

        return FunctionOutput([
            EventResult.of(Event.TRUE if condition_value else Event.FALSE, {}),
            EventResult.output_of({}),
        ])
