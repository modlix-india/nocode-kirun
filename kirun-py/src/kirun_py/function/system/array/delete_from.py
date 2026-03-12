from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class DeleteFrom(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'DeleteFrom',
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
        start: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name()
        )
        length: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )

        if len(source) == 0:
            raise KIRuntimeException('There are no elements to be deleted')

        if start >= len(source) or start < 0:
            raise KIRuntimeException(
                'The int source for the array should be in between 0 and length of the array '
            )

        if length == -1:
            length = len(source) - start

        if start + length > len(source):
            raise KIRuntimeException(
                'Requested length to be deleted is more than the size of array '
            )

        del source[start:start + length]

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: source}
            )
        ])
