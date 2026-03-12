from __future__ import annotations

from typing import TYPE_CHECKING

from dateutil.relativedelta import relativedelta

from kirun_py.json.schema.schema import Schema
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.parameter import Parameter
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.common import get_datetime, datetime_to_iso

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class AddSubtractTime(AbstractDateFunction):

    PARAMETER_YEARS_NAME: str = 'years'
    PARAMETER_MONTHS_NAME: str = 'months'
    PARAMETER_DAYS_NAME: str = 'days'
    PARAMETER_HOURS_NAME: str = 'hours'
    PARAMETER_MINUTES_NAME: str = 'minutes'
    PARAMETER_SECONDS_NAME: str = 'seconds'
    PARAMETER_MILLISECONDS_NAME: str = 'milliseconds'

    def __init__(self, is_add: bool) -> None:
        super().__init__(
            'AddTime' if is_add else 'SubtractTime',
            AbstractDateFunction.EVENT_TIMESTAMP,
            AbstractDateFunction.PARAMETER_TIMESTAMP,
            Parameter.of(
                AddSubtractTime.PARAMETER_YEARS_NAME,
                Schema.of_number(AddSubtractTime.PARAMETER_YEARS_NAME).set_default_value(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_MONTHS_NAME,
                Schema.of_number(AddSubtractTime.PARAMETER_MONTHS_NAME).set_default_value(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_DAYS_NAME,
                Schema.of_number(AddSubtractTime.PARAMETER_DAYS_NAME).set_default_value(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_HOURS_NAME,
                Schema.of_number(AddSubtractTime.PARAMETER_HOURS_NAME).set_default_value(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_MINUTES_NAME,
                Schema.of_number(AddSubtractTime.PARAMETER_MINUTES_NAME).set_default_value(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_SECONDS_NAME,
                Schema.of_number(AddSubtractTime.PARAMETER_SECONDS_NAME).set_default_value(0),
            ),
            Parameter.of(
                AddSubtractTime.PARAMETER_MILLISECONDS_NAME,
                Schema.of_number(AddSubtractTime.PARAMETER_MILLISECONDS_NAME).set_default_value(0),
            ),
        )
        self._is_add = is_add

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}

        timestamp = args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
        dt = get_datetime(timestamp)

        years = int(args.get(AddSubtractTime.PARAMETER_YEARS_NAME, 0) or 0)
        months = int(args.get(AddSubtractTime.PARAMETER_MONTHS_NAME, 0) or 0)
        days = int(args.get(AddSubtractTime.PARAMETER_DAYS_NAME, 0) or 0)
        hours = int(args.get(AddSubtractTime.PARAMETER_HOURS_NAME, 0) or 0)
        minutes = int(args.get(AddSubtractTime.PARAMETER_MINUTES_NAME, 0) or 0)
        seconds = int(args.get(AddSubtractTime.PARAMETER_SECONDS_NAME, 0) or 0)
        milliseconds = int(args.get(AddSubtractTime.PARAMETER_MILLISECONDS_NAME, 0) or 0)

        delta = relativedelta(
            years=years,
            months=months,
            days=days,
            hours=hours,
            minutes=minutes,
            seconds=seconds,
            microseconds=milliseconds * 1000,
        )

        if self._is_add:
            new_dt = dt + delta
        else:
            new_dt = dt - delta

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_TIMESTAMP_NAME: datetime_to_iso(new_dt)}
            ),
        ])
