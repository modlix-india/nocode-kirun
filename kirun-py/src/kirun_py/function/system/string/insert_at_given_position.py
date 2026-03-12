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


class InsertAtGivenPosition(AbstractFunction):

    PARAMETER_STRING_NAME: str = 'string'
    PARAMETER_AT_POSITION_NAME: str = 'position'
    PARAMETER_INSERT_STRING_NAME: str = 'insertString'
    EVENT_RESULT_NAME: str = 'result'

    def __init__(self) -> None:
        super().__init__()

        param_string = Parameter(
            self.PARAMETER_STRING_NAME,
            Schema.of_string(self.PARAMETER_STRING_NAME),
        )
        param_position = Parameter(
            self.PARAMETER_AT_POSITION_NAME,
            Schema.of_integer(self.PARAMETER_AT_POSITION_NAME),
        )
        param_insert = Parameter(
            self.PARAMETER_INSERT_STRING_NAME,
            Schema.of_string(self.PARAMETER_INSERT_STRING_NAME),
        )
        event_string = Event(
            Event.OUTPUT,
            {self.EVENT_RESULT_NAME: Schema.of_string(self.EVENT_RESULT_NAME)},
        )

        self._signature = (
            FunctionSignature('InsertAtGivenPosition')
            .set_namespace(Namespaces.STRING)
            .set_parameters({
                param_string.get_parameter_name(): param_string,
                param_position.get_parameter_name(): param_position,
                param_insert.get_parameter_name(): param_insert,
            })
            .set_events(dict([
                Event.output_event_map_entry(
                    {self.EVENT_RESULT_NAME: Schema.of_string(self.EVENT_RESULT_NAME)}
                )
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        input_string: str = context.get_arguments().get(self.PARAMETER_STRING_NAME)
        at: int = context.get_arguments().get(self.PARAMETER_AT_POSITION_NAME)
        insert_string: str = context.get_arguments().get(self.PARAMETER_INSERT_STRING_NAME)

        output_string = input_string[:at] + insert_string + input_string[at:]

        return FunctionOutput([
            EventResult.output_of({self.EVENT_RESULT_NAME: output_string})
        ])
