from __future__ import annotations

import random
from typing import Any, List, TYPE_CHECKING

from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Shuffle(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Shuffle',
            [AbstractArrayFunction.PARAMETER_ARRAY_SOURCE],
            AbstractArrayFunction.EVENT_RESULT_ARRAY,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )

        if len(source) <= 1:
            return FunctionOutput([
                EventResult.output_of(
                    {AbstractArrayFunction.EVENT_RESULT_NAME: source}
                )
            ])

        source = list(source)
        size: int = len(source)
        x: int = 0

        for i in range(size):
            y: int = random.randint(0, size - 1)
            source[x], source[y] = source[y], source[x]
            x = y

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: source}
            )
        ])
