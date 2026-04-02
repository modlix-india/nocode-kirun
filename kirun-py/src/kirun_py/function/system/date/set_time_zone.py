from __future__ import annotations

from typing import TYPE_CHECKING

try:
    from zoneinfo import ZoneInfo
except ImportError:
    from backports.zoneinfo import ZoneInfo  # type: ignore[no-redef]

from kirun_py.json.schema.schema import Schema
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.parameter import Parameter
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.common import get_datetime, datetime_to_iso

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class SetTimeZone(AbstractDateFunction):

    PARAMETER_TIMEZONE_NAME: str = 'timezone'

    def __init__(self) -> None:
        super().__init__(
            'SetTimeZone',
            AbstractDateFunction.EVENT_TIMESTAMP,
            AbstractDateFunction.PARAMETER_TIMESTAMP,
            Parameter.of(
                SetTimeZone.PARAMETER_TIMEZONE_NAME,
                Schema.of_string(SetTimeZone.PARAMETER_TIMEZONE_NAME),
            ),
        )

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}

        timestamp = args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
        dt = get_datetime(timestamp)

        tz_name = args.get(SetTimeZone.PARAMETER_TIMEZONE_NAME)
        new_dt = dt.astimezone(ZoneInfo(tz_name))

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_TIMESTAMP_NAME: datetime_to_iso(new_dt)}
            ),
        ])
