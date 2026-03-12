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


class PostPad(AbstractFunction):

    PARAMETER_STRING_NAME: str = 'string'
    PARAMETER_POSTPAD_STRING_NAME: str = 'postpadString'
    PARAMETER_LENGTH_NAME: str = 'length'
    EVENT_RESULT_NAME: str = 'result'

    def __init__(self) -> None:
        super().__init__()

        param_string = Parameter(
            self.PARAMETER_STRING_NAME,
            Schema.of_string(self.PARAMETER_STRING_NAME),
        )
        param_postpad = Parameter(
            self.PARAMETER_POSTPAD_STRING_NAME,
            Schema.of_string(self.PARAMETER_POSTPAD_STRING_NAME),
        )
        param_length = Parameter(
            self.PARAMETER_LENGTH_NAME,
            Schema.of_integer(self.PARAMETER_LENGTH_NAME),
        )
        event_string = Event(
            Event.OUTPUT,
            {self.EVENT_RESULT_NAME: Schema.of_string(self.EVENT_RESULT_NAME)},
        )

        self._signature = (
            FunctionSignature('PostPad')
            .set_namespace(Namespaces.STRING)
            .set_parameters({
                param_string.get_parameter_name(): param_string,
                param_postpad.get_parameter_name(): param_postpad,
                param_length.get_parameter_name(): param_length,
            })
            .set_events({event_string.get_name(): event_string})
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        input_string: str = context.get_arguments().get(self.PARAMETER_STRING_NAME)
        postpad_string: str = context.get_arguments().get(self.PARAMETER_POSTPAD_STRING_NAME)
        length: int = context.get_arguments().get(self.PARAMETER_LENGTH_NAME)

        output_string = input_string
        pad_len = len(postpad_string)

        while len(output_string) - len(input_string) + pad_len <= length:
            output_string += postpad_string
            if len(output_string) - len(input_string) >= length:
                break

        current_pad = len(output_string) - len(input_string)
        if current_pad < length:
            output_string += postpad_string[:length - current_pad]

        return FunctionOutput([
            EventResult.output_of({self.EVENT_RESULT_NAME: output_string})
        ])
