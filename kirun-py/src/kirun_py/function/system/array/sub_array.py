from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class SubArray(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'SubArray',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_FIND_FROM,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
            ],
            AbstractArrayFunction.EVENT_RESULT_ARRAY,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        start: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name()
        )
        length: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )

        if length == -1:
            length = len(source) - start

        if length <= 0:
            return FunctionOutput([EventResult.output_of({})])

        if not (0 <= start < len(source)) or start + length > len(source):
            raise KIRuntimeException(
                'Given find from point is more than the source size array or '
                'the Requested length for the subarray was more than the source size'
            )

        sliced = source[start:start + length]

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: sliced}
            )
        ])
