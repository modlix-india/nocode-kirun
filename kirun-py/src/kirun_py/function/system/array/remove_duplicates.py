from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.deep_equal import deep_equal

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class RemoveDuplicates(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'RemoveDuplicates',
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
        source = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        srcfrom = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name()
        )
        length = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )

        if length == -1:
            length = len(source) - srcfrom

        if srcfrom + length > len(source):
            raise KIRuntimeException(
                f'Array has no elements from {srcfrom} to {srcfrom + length} '
                f'as the array size is {len(source)}'
            )

        ja: List[Any] = list(source)
        to = srcfrom + length

        i = to - 1
        while i >= srcfrom:
            should_remove = False
            for j in range(i - 1, srcfrom - 1, -1):
                if deep_equal(ja[i], ja[j]):
                    should_remove = True
                    break
            if should_remove:
                ja.pop(i)
            i -= 1

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: ja}
            )
        ])
