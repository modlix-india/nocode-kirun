from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Compare(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Compare',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM,
                AbstractArrayFunction.PARAMETER_ARRAY_FIND,
                AbstractArrayFunction.PARAMETER_INT_FIND_FROM,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
            ],
            AbstractArrayFunction.EVENT_RESULT_INTEGER,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        srcfrom = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name()
        )
        find = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_FIND.get_parameter_name()
        )
        findfrom = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name()
        )
        length = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )

        if len(source) == 0:
            raise KIRuntimeException('Compare source array cannot be empty')

        if len(find) == 0:
            raise KIRuntimeException('Compare find array cannot be empty')

        if length == -1:
            length = len(source) - srcfrom

        if srcfrom + length > len(source):
            raise KIRuntimeException(
                f'Source array size {len(source)} is less than comparing size {srcfrom + length}'
            )

        if findfrom + length > len(find):
            raise KIRuntimeException(
                f'Find array size {len(find)} is less than comparing size {findfrom + length}'
            )

        result = self._compare(
            source, srcfrom, srcfrom + length,
            find, findfrom, findfrom + length,
        )

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: result}
            )
        ])

    def _compare(
        self,
        source: List[Any],
        srcfrom: int,
        srcto: int,
        find: List[Any],
        findfrom: int,
        findto: int,
    ) -> int:
        if srcto < srcfrom:
            srcfrom, srcto = srcto, srcfrom

        if findto < findfrom:
            findfrom, findto = findto, findfrom

        if srcto - srcfrom != findto - findfrom:
            raise KIRuntimeException(
                f'Cannot compare uneven arrays from {srcto} to {srcfrom} '
                f'in source array with {findto} to {findfrom} in find array'
            )

        i = srcfrom
        j = findfrom
        while i < srcto:
            x: int = 1

            if is_null_value(source[i]) or is_null_value(find[j]):
                s = is_null_value(source[i])
                f = is_null_value(find[j])
                if s == f:
                    x = 0
                elif s:
                    x = -1
            else:
                typs = type(source[i])
                typf = type(find[j])

                if isinstance(source[i], dict) or isinstance(find[j], dict):
                    x = 1
                elif isinstance(source[i], str) or isinstance(find[j], str):
                    sv = str(source[i])
                    fv = str(find[j])
                    if sv == fv:
                        x = 0
                    elif sv < fv:
                        x = -1
                elif isinstance(source[i], bool) or isinstance(find[j], bool):
                    x = 0 if typs == typf and source[i] == find[j] else 1
                elif isinstance(source[i], (int, float)) and isinstance(find[j], (int, float)):
                    diff = source[i] - find[j]
                    x = 0 if diff == 0 else (1 if diff > 0 else -1)

            if x != 0:
                return x

            i += 1
            j += 1

        return 0
