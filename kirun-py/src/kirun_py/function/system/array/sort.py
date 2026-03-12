from __future__ import annotations

import functools
from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


def _get_nested_value(obj: Any, path: str) -> Any:
    """Navigate a nested dict/list using a dot-separated path."""
    if not path:
        return obj
    parts = path.split('.')
    current = obj
    for part in parts:
        if current is None:
            return None
        if isinstance(current, dict):
            current = current.get(part)
        elif isinstance(current, list):
            try:
                current = current[int(part)]
            except (ValueError, IndexError):
                return None
        else:
            return None
    return current


def _compare_function(a: Any, b: Any, ascending: bool) -> int:
    if a == b:
        return 0
    if a is None:
        return 1
    if b is None:
        return -1
    if not ascending:
        return 1 if a < b else -1
    return -1 if a < b else 1


class Sort(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Sort',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_FIND_FROM,
                AbstractArrayFunction.PARAMETER_INT_LENGTH,
                AbstractArrayFunction.PARAMETER_BOOLEAN_ASCENDING,
                AbstractArrayFunction.PARAMETER_KEY_PATH,
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
            AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name()
        )
        length: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
        )
        ascending: bool = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_BOOLEAN_ASCENDING.get_parameter_name()
        )
        key_path: str = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_KEY_PATH.get_parameter_name()
        )

        if len(source) == 0:
            return FunctionOutput([
                EventResult.output_of(
                    {AbstractArrayFunction.EVENT_RESULT_NAME: source}
                )
            ])

        if length == -1:
            length = len(source) - start

        if start < 0 or start >= len(source) or start + length > len(source):
            raise KIRuntimeException(
                'Given start point is more than the size of the array '
                'or not available at that point'
            )

        sliced = source[start:start + length + 1]

        def cmp(a: Any, b: Any) -> int:
            if isinstance(a, dict) and isinstance(b, dict) and key_path:
                a_val = _get_nested_value(a, key_path)
                b_val = _get_nested_value(b, key_path)
                return _compare_function(a_val, b_val, ascending)
            return _compare_function(a, b, ascending)

        sliced.sort(key=functools.cmp_to_key(cmp))
        source[start:start + length] = sliced

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: source}
            )
        ])
