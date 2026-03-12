from __future__ import annotations

from typing import Any, Dict, List, TYPE_CHECKING

from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.parameter import Parameter

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

KEY_NAME = 'keyName'


class ArrayToArrayOfObjects(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'ArrayToArrayOfObjects',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                Parameter.of(KEY_NAME, Schema.of_string(KEY_NAME), True),
            ],
            AbstractArrayFunction.EVENT_RESULT_ARRAY,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        keys: List[str] = context.get_arguments().get(KEY_NAME)

        if not source:
            return FunctionOutput([
                EventResult.output_of(
                    {AbstractArrayFunction.EVENT_RESULT_NAME: []}
                )
            ])

        result: List[Dict[str, Any]] = []
        for e in source:
            obj: Dict[str, Any] = {}
            if isinstance(e, list):
                if keys:
                    for idx, key in enumerate(keys):
                        obj[key] = e[idx] if idx < len(e) else None
                else:
                    for i in range(len(e)):
                        obj[f'value{i + 1}'] = e[i]
            else:
                obj[keys[0] if keys else 'value'] = e
            result.append(obj)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: result}
            )
        ])
