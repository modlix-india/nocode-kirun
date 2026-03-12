from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Rotate(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Rotate',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_ROTATE_LENGTH,
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
        rot_len: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ROTATE_LENGTH.get_parameter_name()
        )

        if len(source) == 0:
            return FunctionOutput([
                EventResult.output_of(
                    {AbstractArrayFunction.EVENT_RESULT_NAME: source}
                )
            ])

        size: int = len(source)
        rot_len = rot_len % size

        self._rotate(source, 0, rot_len - 1)
        self._rotate(source, rot_len, size - 1)
        self._rotate(source, 0, size - 1)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: source}
            )
        ])

    @staticmethod
    def _rotate(elements: List[Any], start: int, end: int) -> None:
        while start < end:
            elements[start], elements[end] = elements[end], elements[start]
            start += 1
            end -= 1
