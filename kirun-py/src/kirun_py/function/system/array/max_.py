from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.primitive.primitive_util import PrimitiveUtil

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Max(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Max',
            [AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE],
            AbstractArrayFunction.EVENT_RESULT_ANY,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name()
        )

        if len(source) == 0:
            raise KIRuntimeException('Search source array cannot be empty')

        max_val: Any = source[0]
        for i in range(1, len(source)):
            y = source[i]
            if PrimitiveUtil.compare_primitive(max_val, y) < 0:
                max_val = y

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: max_val}
            )
        ])
