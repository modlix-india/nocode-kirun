from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.primitive.primitive_util import PrimitiveUtil

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Frequency(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Frequency',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_ANY,
                AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
            ],
            AbstractArrayFunction.EVENT_RESULT_INTEGER,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        find: Any = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ANY.get_parameter_name()
        )
        start: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name()
        )
        length: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )

        if len(source) == 0:
            return FunctionOutput([
                EventResult.output_of(
                    {AbstractArrayFunction.EVENT_RESULT_NAME: 0}
                )
            ])

        if start > len(source):
            raise KIRuntimeException(
                'Given start point is more than the size of source'
            )

        end: int = start + length
        if length == -1:
            end = len(source) - start

        if end > len(source):
            raise KIRuntimeException(
                'Given length is more than the size of source'
            )

        freq: int = 0
        for i in range(start, min(end, len(source))):
            if PrimitiveUtil.compare(source[i], find) == 0:
                freq += 1

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: freq}
            )
        ])
