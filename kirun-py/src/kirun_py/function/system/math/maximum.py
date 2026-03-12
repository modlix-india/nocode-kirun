from __future__ import annotations

from typing import List, TYPE_CHECKING

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

VALUE = 'value'


class Maximum(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('Maximum')
            .set_namespace(Namespaces.MATH)
            .set_parameters({
                VALUE: Parameter(VALUE, Schema.of_number(VALUE))
                .set_variable_argument(True),
            })
            .set_events(
                dict([Event.output_event_map_entry({VALUE: Schema.of_number(VALUE)})])
            )
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        nums: List[float] = context.get_arguments().get(VALUE)
        result = max(nums)
        return FunctionOutput([
            EventResult.output_of({VALUE: result})
        ])
