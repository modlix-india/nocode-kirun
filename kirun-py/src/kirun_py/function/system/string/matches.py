from __future__ import annotations

import re
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


class Matches(AbstractFunction):

    PARAMETER_REGEX_NAME: str = 'regex'
    PARAMETER_STRING_NAME: str = 'string'
    EVENT_RESULT_NAME: str = 'result'

    def __init__(self) -> None:
        super().__init__()

        self._signature = (
            FunctionSignature('Matches')
            .set_namespace(Namespaces.STRING)
            .set_parameters(dict([
                Parameter.of_entry(
                    self.PARAMETER_REGEX_NAME,
                    Schema.of_string(self.PARAMETER_REGEX_NAME),
                ),
                Parameter.of_entry(
                    self.PARAMETER_STRING_NAME,
                    Schema.of_string(self.PARAMETER_STRING_NAME),
                ),
            ]))
            .set_events(dict([
                Event.output_event_map_entry(
                    {self.EVENT_RESULT_NAME: Schema.of_boolean(self.EVENT_RESULT_NAME)}
                )
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        regex_pat: str = context.get_arguments().get(self.PARAMETER_REGEX_NAME)
        input_string: str = context.get_arguments().get(self.PARAMETER_STRING_NAME)

        result = bool(re.search(regex_pat, input_string))

        return FunctionOutput([
            EventResult.output_of({self.EVENT_RESULT_NAME: result})
        ])
