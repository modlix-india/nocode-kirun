from __future__ import annotations

import math
from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.primitive.primitive_util import PrimitiveUtil

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class BinarySearch(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'BinarySearch',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE,
                AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM,
                AbstractArrayFunction.PARAMETER_FIND_PRIMITIVE,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
            ],
            AbstractArrayFunction.EVENT_INDEX,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        start: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name()
        )
        find: Any = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_FIND_PRIMITIVE.get_parameter_name()
        )
        end: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )

        if len(source) == 0 or start < 0 or start > len(source):
            raise KIRuntimeException('Search source array cannot be empty')

        if end == -1:
            end = len(source) - start

        end = start + end

        if end > len(source):
            raise KIRuntimeException(
                'End point for array cannot be more than the size of the source array'
            )

        index: int = -1

        while start <= end:
            mid: int = (start + end) // 2
            cmp = PrimitiveUtil.compare(source[mid], find)
            if cmp == 0:
                index = mid
                break
            elif cmp > 0:
                end = mid - 1
            else:
                start = mid + 1

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_INDEX_NAME: index}
            )
        ])
