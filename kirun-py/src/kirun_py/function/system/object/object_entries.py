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


class ObjectEntries(AbstractObjectFunction):

    def __init__(self) -> None:
        super().__init__(
            'ObjectEntries',
            Schema.of_array(
                VALUE,
                Schema.of_array('tuple', Schema.of_string('key'), Schema.of_any('value')),
            ),
        )

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        source = context.get_arguments().get('source') if context.get_arguments() else None

        if is_null_value(source):
            return FunctionOutput([EventResult.output_of({VALUE: []})])

        entries = sorted(duplicate(source).items(), key=lambda x: x[0])
        entries_list = [[k, v] for k, v in entries]

        return FunctionOutput([EventResult.output_of({VALUE: entries_list})])
