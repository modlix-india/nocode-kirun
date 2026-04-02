from __future__ import annotations

import json
from typing import Any, TYPE_CHECKING

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


class ToString(AbstractFunction):

    PARAMETER_INPUT_ANYTYPE_NAME: str = 'anytype'
    EVENT_RESULT_NAME: str = 'result'

    def __init__(self) -> None:
        super().__init__()

        param_input = Parameter(
            self.PARAMETER_INPUT_ANYTYPE_NAME,
            Schema.of_any(self.PARAMETER_INPUT_ANYTYPE_NAME),
        )
        event_string = Event(
            Event.OUTPUT,
            {self.EVENT_RESULT_NAME: Schema.of_string(self.EVENT_RESULT_NAME)},
        )

        self._signature = (
            FunctionSignature('ToString')
            .set_namespace(Namespaces.STRING)
            .set_parameters({
                param_input.get_parameter_name(): param_input,
            })
            .set_events({event_string.get_name(): event_string})
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        input_val: Any = context.get_arguments().get(self.PARAMETER_INPUT_ANYTYPE_NAME)

        if isinstance(input_val, (dict, list)):
            output = json.dumps(input_val, indent=2)
        elif input_val is None:
            output = 'null'
        else:
            output = str(input_val)

        return FunctionOutput([
            EventResult.output_of({self.EVENT_RESULT_NAME: output})
        ])
