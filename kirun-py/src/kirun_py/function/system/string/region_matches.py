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


class RegionMatches(AbstractFunction):

    PARAMETER_STRING_NAME: str = 'string'
    PARAMETER_BOOLEAN_NAME: str = 'boolean'
    PARAMETER_FIRST_OFFSET_NAME: str = 'firstOffset'
    PARAMETER_OTHER_STRING_NAME: str = 'otherString'
    PARAMETER_SECOND_OFFSET_NAME: str = 'secondOffset'
    PARAMETER_INTEGER_NAME: str = 'length'
    EVENT_RESULT_NAME: str = 'result'

    def __init__(self) -> None:
        super().__init__()

        param_string = Parameter(
            self.PARAMETER_STRING_NAME,
            Schema.of_string(self.PARAMETER_STRING_NAME),
        )
        param_other = Parameter(
            self.PARAMETER_OTHER_STRING_NAME,
            Schema.of_string(self.PARAMETER_OTHER_STRING_NAME),
        )
        param_first_offset = Parameter(
            self.PARAMETER_FIRST_OFFSET_NAME,
            Schema.of_integer(self.PARAMETER_FIRST_OFFSET_NAME),
        )
        param_second_offset = Parameter(
            self.PARAMETER_SECOND_OFFSET_NAME,
            Schema.of_integer(self.PARAMETER_SECOND_OFFSET_NAME),
        )
        param_length = Parameter(
            self.PARAMETER_INTEGER_NAME,
            Schema.of_integer(self.PARAMETER_INTEGER_NAME),
        )
        param_boolean = Parameter(
            self.PARAMETER_BOOLEAN_NAME,
            Schema.of_boolean(self.PARAMETER_BOOLEAN_NAME),
        )
        event_boolean = Event(
            Event.OUTPUT,
            {self.EVENT_RESULT_NAME: Schema.of_boolean(self.EVENT_RESULT_NAME)},
        )

        self._signature = (
            FunctionSignature('RegionMatches')
            .set_namespace(Namespaces.STRING)
            .set_parameters({
                param_string.get_parameter_name(): param_string,
                param_boolean.get_parameter_name(): param_boolean,
                param_first_offset.get_parameter_name(): param_first_offset,
                param_other.get_parameter_name(): param_other,
                param_second_offset.get_parameter_name(): param_second_offset,
                param_length.get_parameter_name(): param_length,
            })
            .set_events({event_boolean.get_name(): event_boolean})
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        input_string: str = context.get_arguments().get(self.PARAMETER_STRING_NAME)
        ignore_case: bool = context.get_arguments().get(self.PARAMETER_BOOLEAN_NAME)
        t_offset: int = context.get_arguments().get(self.PARAMETER_FIRST_OFFSET_NAME)
        other_string: str = context.get_arguments().get(self.PARAMETER_OTHER_STRING_NAME)
        o_offset: int = context.get_arguments().get(self.PARAMETER_SECOND_OFFSET_NAME)
        length: int = context.get_arguments().get(self.PARAMETER_INTEGER_NAME)

        matches = False

        if (
            t_offset < 0
            or o_offset < 0
            or t_offset + length > len(input_string)
            or o_offset + length > len(other_string)
        ):
            matches = False
        elif ignore_case:
            s1 = input_string[t_offset:t_offset + length].upper()
            s2 = other_string[o_offset:o_offset + length].upper()
            matches = s1 == s2
        else:
            s1 = input_string[t_offset:t_offset + length]
            s2 = other_string[o_offset:o_offset + length]
            matches = s1 == s2

        return FunctionOutput([
            EventResult.output_of({self.EVENT_RESULT_NAME: matches})
        ])
