from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.function.system.array.compare import Compare
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Equals(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Equals',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM,
                AbstractArrayFunction.PARAMETER_ARRAY_FIND,
                AbstractArrayFunction.PARAMETER_INT_FIND_FROM,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
            ],
            AbstractArrayFunction.EVENT_RESULT_BOOLEAN,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        compare = Compare()
        fo: FunctionOutput = await compare.execute(context)

        result_map = fo.all_results()[0].get_result()
        v: int = result_map.get(AbstractArrayFunction.EVENT_RESULT_NAME)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: v == 0}
            )
        ])
