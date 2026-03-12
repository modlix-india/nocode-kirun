from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.duplicate import duplicate

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Fill(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Fill',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
                AbstractArrayFunction.PARAMETER_ANY,
            ],
            AbstractArrayFunction.EVENT_RESULT_ARRAY,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source = list(
            context.get_arguments().get(
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
            )
        )
        srcfrom = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name()
        )
        length = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )
        element = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ANY.get_parameter_name()
        )

        if srcfrom < 0:
            raise KIRuntimeException(
                f'Arrays out of bound trying to access {srcfrom} index'
            )

        if length == -1:
            length = len(source) - srcfrom

        add = srcfrom + length - len(source)
        if add > 0:
            source.extend([None] * add)

        for i in range(srcfrom, srcfrom + length):
            source[i] = element if element is None else duplicate(element)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: source}
            )
        ])
