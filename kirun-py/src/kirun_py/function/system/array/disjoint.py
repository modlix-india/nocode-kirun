from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Disjoint(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Disjoint',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM,
                AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
            ],
            AbstractArrayFunction.EVENT_RESULT_ARRAY,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        first_source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        first: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name()
        )
        second_source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name()
        )
        second: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM.get_parameter_name()
        )
        length: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )

        if length == -1:
            length = (
                len(first_source) - first
                if len(first_source) <= len(second_source)
                else len(second_source) - second
            )

        if (
            length > len(first_source)
            or length > len(second_source)
            or first + length > len(first_source)
            or second + length > len(second_source)
        ):
            raise KIRuntimeException(
                'The length which was being requested is more than than the size '
                'either source array or second source array'
            )

        # Build lists for the relevant slices (use list to preserve duplicates in order)
        slice1 = [first_source[i + first] for i in range(length)]
        slice2 = [second_source[i + second] for i in range(length)]

        # Symmetric difference: elements in one but not both
        # Use a simple approach that mirrors the JS Set-based logic
        result_list: List[Any] = []
        seen_in_2 = list(slice2)

        for elem in slice1:
            if elem in seen_in_2:
                seen_in_2.remove(elem)
            else:
                if elem not in result_list:
                    result_list.append(elem)

        for elem in seen_in_2:
            if elem not in result_list:
                result_list.append(elem)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: result_list}
            )
        ])
