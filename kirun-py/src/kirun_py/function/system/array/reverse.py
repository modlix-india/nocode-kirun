from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Reverse(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Reverse',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
            ],
            AbstractArrayFunction.EVENT_RESULT_ARRAY,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = list(
            context.get_arguments().get(
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
            )
        )
        st: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name()
        )
        length: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )

        if length == -1:
            length = len(source) - st

        if length >= len(source) or length < 0 or st < 0:
            raise KIRuntimeException(
                'Please provide start point between the start and end indexes '
                'or provide the length which was less than the source size '
            )

        endpoint: int = st + length - 1
        while st <= endpoint:
            source[st], source[endpoint] = source[endpoint], source[st]
            st += 1
            endpoint -= 1

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: source}
            )
        ])
