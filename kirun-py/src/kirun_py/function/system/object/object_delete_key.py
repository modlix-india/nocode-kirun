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
from kirun_py.util.duplicate import duplicate
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

VALUE = 'value'
SOURCE = 'source'
KEY = 'key'


class ObjectDeleteKey(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('ObjectDeleteKey')
            .set_namespace(Namespaces.SYSTEM_OBJECT)
            .set_parameters(
                dict([
                    Parameter.of_entry(SOURCE, Schema.of_any(SOURCE)),
                    Parameter.of_entry(KEY, Schema.of_string(KEY)),
                ])
            )
            .set_events(
                dict([Event.output_event_map_entry({VALUE: Schema.of_any(VALUE)})])
            )
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        source = context.get_arguments().get(SOURCE) if context.get_arguments() else None
        key = context.get_arguments().get(KEY) if context.get_arguments() else None

        if is_null_value(source):
            return FunctionOutput([EventResult.output_of({VALUE: None})])

        source = duplicate(source)
        if isinstance(source, dict) and key in source:
            del source[key]

        return FunctionOutput([EventResult.output_of({VALUE: source})])
