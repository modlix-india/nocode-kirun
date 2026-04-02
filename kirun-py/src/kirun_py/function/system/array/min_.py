from __future__ import annotations

from typing import Any, List, Optional, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.null_check import is_null_value
from kirun_py.util.primitive.primitive_util import PrimitiveUtil

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Min(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Min',
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

        min_val: Optional[Any] = None
        found = False
        for i in range(len(source)):
            if is_null_value(source[i]):
                continue
            if not found or PrimitiveUtil.compare_primitive(source[i], min_val) < 0:
                min_val = source[i]
                found = True

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: min_val}
            )
        ])
