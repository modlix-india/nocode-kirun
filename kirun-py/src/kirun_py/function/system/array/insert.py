from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Insert(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Insert',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_INT_OFFSET,
                AbstractArrayFunction.PARAMETER_ANY,
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
        offset: int = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_INT_OFFSET.get_parameter_name()
        )
        element = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ANY.get_parameter_name()
        )

        if is_null_value(element) or is_null_value(offset) or offset > len(source):
            raise KIRuntimeException(
                'Please provide valid resources to insert at the correct location'
            )

        source.insert(offset, element)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: source}
            )
        ])
