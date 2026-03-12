from __future__ import annotations

from datetime import datetime, timezone
from typing import TYPE_CHECKING

from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class GetCurrentTimestamp(AbstractDateFunction):

    def __init__(self) -> None:
        super().__init__('GetCurrentTimestamp', AbstractDateFunction.EVENT_TIMESTAMP)

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_TIMESTAMP_NAME: datetime.now(timezone.utc).isoformat()}
            ),
        ])
