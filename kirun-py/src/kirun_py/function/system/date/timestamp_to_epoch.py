from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.common import get_datetime

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class TimestampToEpoch(AbstractFunction):

    def __init__(self, name: str, is_seconds: bool) -> None:
        super().__init__()
        self._is_seconds = is_seconds

        self._signature = (
            FunctionSignature(name)
            .set_namespace(Namespaces.DATE)
            .set_parameters({
                AbstractDateFunction.PARAMETER_TIMESTAMP_NAME: AbstractDateFunction.PARAMETER_TIMESTAMP,
            })
            .set_events({
                AbstractDateFunction.EVENT_TIMESTAMP.get_name(): AbstractDateFunction.EVENT_LONG,
            })
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}
        timestamp = args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
        dt = get_datetime(timestamp)

        if self._is_seconds:
            epoch = int(dt.timestamp())
        else:
            epoch = int(dt.timestamp() * 1000)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_RESULT_NAME: epoch}
            ),
        ])
