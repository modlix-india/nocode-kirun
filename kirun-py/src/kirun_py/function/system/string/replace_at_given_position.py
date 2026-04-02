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


class ReplaceAtGivenPosition(AbstractFunction):

    PARAMETER_STRING_NAME: str = 'string'
    PARAMETER_AT_START_NAME: str = 'startPosition'
    PARAMETER_AT_LENGTH_NAME: str = 'lengthPosition'
    PARAMETER_REPLACE_STRING_NAME: str = 'replaceString'
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
        param_length = Parameter(
            self.PARAMETER_AT_LENGTH_NAME,
            Schema.of_integer(self.PARAMETER_AT_LENGTH_NAME),
        )
        param_replace = Parameter(
            self.PARAMETER_REPLACE_STRING_NAME,
            Schema.of_string(self.PARAMETER_REPLACE_STRING_NAME),
        )
        event_string = Event(
            Event.OUTPUT,
            {self.EVENT_RESULT_NAME: Schema.of_string(self.EVENT_RESULT_NAME)},
        )

        self._signature = (
            FunctionSignature('ReplaceAtGivenPosition')
            .set_namespace(Namespaces.STRING)
            .set_parameters({
                param_string.get_parameter_name(): param_string,
                param_start.get_parameter_name(): param_start,
                param_length.get_parameter_name(): param_length,
                param_replace.get_parameter_name(): param_replace,
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
        length: int = context.get_arguments().get(self.PARAMETER_AT_LENGTH_NAME)
        replace_string: str = context.get_arguments().get(self.PARAMETER_REPLACE_STRING_NAME)

        if start_position < length:
            output_string = (
                input_string[:start_position]
                + replace_string
                + input_string[start_position + length:]
            )
            return FunctionOutput([
                EventResult.output_of({self.EVENT_RESULT_NAME: output_string})
            ])

        return FunctionOutput([
            EventResult.output_of({self.EVENT_RESULT_NAME: input_string})
        ])
