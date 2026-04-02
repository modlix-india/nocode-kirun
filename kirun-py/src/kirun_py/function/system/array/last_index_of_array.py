from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.primitive.primitive_util import PrimitiveUtil

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class LastIndexOfArray(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'LastIndexOfArray',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_FIND_FROM,
            ],
            AbstractArrayFunction.EVENT_RESULT_INTEGER,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        second_source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name()
        )
        from_idx: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name()
        )

        if len(source) == 0:
            return FunctionOutput([
                EventResult.output_of(
                    {AbstractArrayFunction.EVENT_RESULT_NAME: -1}
                )
            ])

        if from_idx < 0 or from_idx > len(source) or len(second_source) > len(source):
            raise KIRuntimeException(
                'Given from index is more than the size of the source array'
            )

        second_size: int = len(second_source)
        index: int = -1

        for i in range(from_idx, len(source)):
            j: int = 0
            if PrimitiveUtil.compare(source[i], second_source[j]) == 0:
                while j < second_size:
                    if i + j >= len(source):
                        break
                    if PrimitiveUtil.compare(source[i + j], second_source[j]) != 0:
                        break
                    j += 1
                if j == second_size:
                    index = i

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: index}
            )
        ])
