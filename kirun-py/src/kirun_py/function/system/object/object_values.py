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


class ObjectValues(AbstractObjectFunction):

    def __init__(self) -> None:
        super().__init__('ObjectValues', Schema.of_array(VALUE, Schema.of_any(VALUE)))

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        source = context.get_arguments().get('source') if context.get_arguments() else None

        if is_null_value(source) or isinstance(source, (int, float, bool)):
            return FunctionOutput([EventResult.output_of({VALUE: []})])

        if isinstance(source, str):
            values = list(source)
            return FunctionOutput([EventResult.output_of({VALUE: values})])

        if isinstance(source, list):
            return FunctionOutput([EventResult.output_of({VALUE: list(source)})])

        if isinstance(source, dict):
            object_values = [v for _, v in sorted(source.items())]
            return FunctionOutput([EventResult.output_of({VALUE: object_values})])

        return FunctionOutput([EventResult.output_of({VALUE: []})])
