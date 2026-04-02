from __future__ import annotations

from typing import Any, Dict, List, Optional, TYPE_CHECKING

from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.parameter import Parameter
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

KEY_PATH = 'keyPath'
VALUE_PATH = 'valuePath'
IGNORE_NULL_VALUES = 'ignoreNullValues'
IGNORE_NULL_KEYS = 'ignoreNullKeys'
IGNORE_DUPLICATE_KEYS = 'ignoreDuplicateKeys'


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


class ArrayToObject(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'ArrayToObjects',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                Parameter.of(KEY_PATH, Schema.of_string(KEY_PATH)),
                Parameter.of(
                    VALUE_PATH,
                    Schema.of(VALUE_PATH, SchemaType.STRING, SchemaType.NULL),
                ),
                Parameter.of(
                    IGNORE_NULL_VALUES,
                    Schema.of_boolean(IGNORE_NULL_VALUES).set_default_value(False),
                ),
                Parameter.of(
                    IGNORE_NULL_KEYS,
                    Schema.of_boolean(IGNORE_NULL_KEYS).set_default_value(True),
                ),
                Parameter.of(
                    IGNORE_DUPLICATE_KEYS,
                    Schema.of_boolean(IGNORE_DUPLICATE_KEYS).set_default_value(False),
                ),
            ],
            AbstractArrayFunction.EVENT_RESULT_ANY,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        key_path: str = context.get_arguments().get(KEY_PATH)
        value_path: Optional[str] = context.get_arguments().get(VALUE_PATH) or ''

        ignore_null_values: bool = context.get_arguments().get(IGNORE_NULL_VALUES)
        ignore_null_keys: bool = context.get_arguments().get(IGNORE_NULL_KEYS)
        ignore_duplicate_keys: bool = context.get_arguments().get(IGNORE_DUPLICATE_KEYS)

        result: Dict[str, Any] = {}

        for item in source:
            if is_null_value(item):
                continue

            key = _get_nested_value(item, key_path)
            if ignore_null_keys and is_null_value(key):
                continue

            value = _get_nested_value(item, value_path) if value_path else item
            if ignore_null_values and is_null_value(value):
                continue

            str_key = str(key) if key is not None else 'None'
            if ignore_duplicate_keys and str_key in result:
                continue

            result[str_key] = value

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: result}
            )
        ])
