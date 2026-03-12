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


class DeleteForGivenLength(AbstractFunction):

    PARAMETER_STRING_NAME: str = 'string'
    PARAMETER_AT_START_NAME: str = 'startPosition'
    PARAMETER_AT_END_NAME: str = 'endPosition'
    EVENT_RESULT_NAME: str = 'result'

    def __init__(self) -> None:
        super().__init__()

        param_string = Parameter(
            self.PARAMETER_STRING_NAME,
            Schema.of_string(self.PARAMETER_STRING_NAME),
        )
        param_start = Parameter(
            self.PARAMETER_AT_START_NAME,
            Schema.of_integer(self.PARAMETER_AT_START_NAME),
        )
        param_end = Parameter(
            self.PARAMETER_AT_END_NAME,
            Schema.of_integer(self.PARAMETER_AT_END_NAME),
        )
        event_string = Event(
            Event.OUTPUT,
            {self.EVENT_RESULT_NAME: Schema.of_string(self.EVENT_RESULT_NAME)},
        )

        self._signature = (
            FunctionSignature('DeleteForGivenLength')
            .set_namespace(Namespaces.STRING)
            .set_parameters({
                param_string.get_parameter_name(): param_string,
                param_start.get_parameter_name(): param_start,
                param_end.get_parameter_name(): param_end,
            })
            .set_events({event_string.get_name(): event_string})
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        input_string: str = context.get_arguments().get(self.PARAMETER_STRING_NAME)
        start_position: int = context.get_arguments().get(self.PARAMETER_AT_START_NAME)
        end_position: int = context.get_arguments().get(self.PARAMETER_AT_END_NAME)

        if end_position >= start_position:
            output_string = input_string[:start_position] + input_string[end_position:]
            return FunctionOutput([
                EventResult.output_of({self.EVENT_RESULT_NAME: output_string})
            ])

        return FunctionOutput([
            EventResult.output_of({self.EVENT_RESULT_NAME: input_string})
        ])
