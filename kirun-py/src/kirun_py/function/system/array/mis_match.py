from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class MisMatch(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'MisMatch',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_FIND_FROM,
                AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
            ],
            AbstractArrayFunction.EVENT_RESULT_INTEGER,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        first_source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        first_find: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name()
        )
        second_source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name()
        )
        second_find: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM.get_parameter_name()
        )
        length: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )

        first = first_find if 0 < first_find < len(first_source) else 0
        second = second_find if 0 < second_find < len(second_source) else 0

        if first + length >= len(first_source) or second + length > len(second_source):
            raise KIRuntimeException(
                'The size of the array for first and second which was being requested '
                'is more than size of the given array'
            )

        index: int = -1
        for i in range(length):
            if first_source[first + i] != second_source[second + i]:
                index = i
                break

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: index}
            )
        ])
