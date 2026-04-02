from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.function.system.object.abstract_object_function import AbstractObjectFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.duplicate import duplicate
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

VALUE = 'value'


class ObjectKeys(AbstractObjectFunction):

    def __init__(self) -> None:
        super().__init__('ObjectKeys', Schema.of_array(VALUE, Schema.of_string(VALUE)))

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        source = context.get_arguments().get('source') if context.get_arguments() else None

        if is_null_value(source) or isinstance(source, (int, float, bool)):
            return FunctionOutput([EventResult.output_of({VALUE: []})])

        if isinstance(source, str):
            keys = sorted([str(i) for i in range(len(source))])
            return FunctionOutput([EventResult.output_of({VALUE: keys})])

        if isinstance(source, list):
            keys = sorted([str(i) for i in range(len(source))])
            return FunctionOutput([EventResult.output_of({VALUE: keys})])

        if isinstance(source, dict):
            keys = sorted(source.keys())
            return FunctionOutput([EventResult.output_of({VALUE: keys})])

        return FunctionOutput([EventResult.output_of({VALUE: []})])
