from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.primitive.primitive_util import PrimitiveUtil

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class LastIndexOf(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'LastIndexOf',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT,
                AbstractArrayFunction.PARAMETER_INT_FIND_FROM,
            ],
            AbstractArrayFunction.EVENT_RESULT_INTEGER,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        find = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name()
        )
        from_idx: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name()
        )

        if len(source) == 0:
            return FunctionOutput([
                EventResult.output_of(
                    {AbstractArrayFunction.EVENT_RESULT_NAME: -1}
                )
            ])

        if from_idx < 0 or from_idx > len(source):
            raise KIRuntimeException(
                "The value of length shouldn't the exceed the size of the array "
                "or shouldn't be in terms"
            )

        index: int = -1
        for i in range(len(source) - 1, from_idx - 1, -1):
            if PrimitiveUtil.compare(source[i], find) == 0:
                index = i
                break

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: index}
            )
        ])
