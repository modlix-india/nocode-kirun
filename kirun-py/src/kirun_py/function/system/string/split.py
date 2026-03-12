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


class Split(AbstractFunction):

    PARAMETER_STRING_NAME: str = 'string'
    PARAMETER_SPLIT_STRING_NAME: str = 'searchString'
    EVENT_RESULT_NAME: str = 'result'

    def __init__(self) -> None:
        super().__init__()

        param_string = Parameter(
            self.PARAMETER_STRING_NAME,
            Schema.of_string(self.PARAMETER_STRING_NAME),
        )
        param_split = Parameter(
            self.PARAMETER_SPLIT_STRING_NAME,
            Schema.of_string(self.PARAMETER_SPLIT_STRING_NAME),
        )

        self._signature = (
            FunctionSignature('Split')
            .set_namespace(Namespaces.STRING)
            .set_parameters({
                self.PARAMETER_STRING_NAME: param_string,
                self.PARAMETER_SPLIT_STRING_NAME: param_split,
            })
            .set_events(dict([
                Event.output_event_map_entry(
                    {self.EVENT_RESULT_NAME: Schema.of_array(self.EVENT_RESULT_NAME)}
                )
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        s1: str = context.get_arguments().get(self.PARAMETER_STRING_NAME)
        s2: str = context.get_arguments().get(self.PARAMETER_SPLIT_STRING_NAME)

        return FunctionOutput([
            EventResult.output_of({self.EVENT_RESULT_NAME: s1.split(s2)})
        ])
