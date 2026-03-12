from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.duplicate import duplicate
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Copy(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Copy',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
                AbstractArrayFunction.PARAMETER_BOOLEAN_DEEP_COPY,
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

        deep = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_BOOLEAN_DEEP_COPY.get_parameter_name()
        )

        ja: List[Any] = [None] * length

        for i in range(srcfrom, srcfrom + length):
            if not is_null_value(source[i]):
                ja[i - srcfrom] = duplicate(source[i]) if deep else source[i]

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: ja}
            )
        ])
