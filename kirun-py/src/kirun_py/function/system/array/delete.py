from __future__ import annotations

from typing import Any, List, Set, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.util.primitive.primitive_util import PrimitiveUtil

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Delete(AbstractArrayFunction):

    def __init__(self) -> None:
        super().__init__(
            'Delete',
            [
                AbstractArrayFunction.PARAMETER_ARRAY_SOURCE,
                AbstractArrayFunction.PARAMETER_ANY_VAR_ARGS,
            ],
            AbstractArrayFunction.EVENT_RESULT_ARRAY,
        )

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
        )
        received_args: List[Any] = context.get_arguments().get(
            AbstractArrayFunction.PARAMETER_ANY_VAR_ARGS.get_parameter_name()
        )

        if received_args is None:
            raise KIRuntimeException(
                'The deletable var args are empty. So cannot be proceeded further.'
            )

        if len(source) == 0 or len(received_args) == 0:
            raise KIRuntimeException(
                'Expected a source or deletable for an array but not found any'
            )

        indexes: Set[int] = set()

        for i in range(len(source) - 1, -1, -1):
            for arg in received_args:
                if i not in indexes and PrimitiveUtil.compare(source[i], arg) == 0:
                    indexes.add(i)

        result = [v for i, v in enumerate(source) if i not in indexes]

        return FunctionOutput([
            EventResult.output_of(
                {AbstractArrayFunction.EVENT_RESULT_NAME: result}
            )
        ])
