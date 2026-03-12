from __future__ import annotations

from typing import Callable, TYPE_CHECKING

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

MIN_VALUE = 'minValue'
MAX_VALUE = 'maxValue'
VALUE = 'value'


class RandomAny(AbstractFunction):

    def __init__(
        self,
        name: str,
        min_parameter: Parameter,
        max_parameter: Parameter,
        output_schema: Schema,
        random_function: Callable[[float, float], float],
    ) -> None:
        super().__init__()
        self._random_function = random_function
        self._signature = (
            FunctionSignature(name)
            .set_namespace(Namespaces.MATH)
            .set_parameters({
                MIN_VALUE: min_parameter,
                MAX_VALUE: max_parameter,
            })
            .set_events(
                dict([Event.output_event_map_entry({VALUE: output_schema})])
            )
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        args = context.get_arguments()
        min_val: float = args.get(MIN_VALUE)
        max_val: float = args.get(MAX_VALUE)
        num = self._random_function(min_val, max_val)
        return FunctionOutput([
            EventResult.output_of({VALUE: num})
        ])
