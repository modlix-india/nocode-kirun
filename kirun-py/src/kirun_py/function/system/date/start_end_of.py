from __future__ import annotations

from datetime import datetime, timedelta
from typing import TYPE_CHECKING

from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.common import get_datetime, datetime_to_iso

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


def _start_of(dt: datetime, unit: str) -> datetime:
    """Return the start of the given unit for *dt*, preserving timezone."""
    tz = dt.tzinfo
    if unit == 'year':
        return datetime(dt.year, 1, 1, tzinfo=tz)
    if unit == 'month':
        return datetime(dt.year, dt.month, 1, tzinfo=tz)
    if unit == 'week':
        # ISO week starts on Monday
        day_of_week = dt.isoweekday()  # Monday=1
        start = dt - timedelta(days=day_of_week - 1)
        return datetime(start.year, start.month, start.day, tzinfo=tz)
    if unit == 'day':
        return datetime(dt.year, dt.month, dt.day, tzinfo=tz)
    if unit == 'hour':
        return dt.replace(minute=0, second=0, microsecond=0)
    if unit == 'minute':
        return dt.replace(second=0, microsecond=0)
    if unit == 'second':
        return dt.replace(microsecond=0)
    raise ValueError('Unknown unit: {}'.format(unit))


def _end_of(dt: datetime, unit: str) -> datetime:
    """Return the end of the given unit for *dt*, preserving timezone."""
    import calendar
    tz = dt.tzinfo
    if unit == 'year':
        return datetime(dt.year, 12, 31, 23, 59, 59, 999000, tzinfo=tz)
    if unit == 'month':
        last_day = calendar.monthrange(dt.year, dt.month)[1]
        return datetime(dt.year, dt.month, last_day, 23, 59, 59, 999000, tzinfo=tz)
    if unit == 'week':
        day_of_week = dt.isoweekday()  # Monday=1
        end = dt + timedelta(days=7 - day_of_week)
        return datetime(end.year, end.month, end.day, 23, 59, 59, 999000, tzinfo=tz)
    if unit == 'day':
        return datetime(dt.year, dt.month, dt.day, 23, 59, 59, 999000, tzinfo=tz)
    if unit == 'hour':
        return dt.replace(minute=59, second=59, microsecond=999000)
    if unit == 'minute':
        return dt.replace(second=59, microsecond=999000)
    if unit == 'second':
        return dt.replace(microsecond=999000)
    raise ValueError('Unknown unit: {}'.format(unit))


class StartEndOf(AbstractDateFunction):

    def __init__(self, is_start: bool) -> None:
        super().__init__(
            'StartOf' if is_start else 'EndOf',
            AbstractDateFunction.EVENT_TIMESTAMP,
            AbstractDateFunction.PARAMETER_TIMESTAMP,
            AbstractDateFunction.PARAMETER_UNIT,
        )
        self._is_start = is_start

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}

        timestamp = args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
        dt = get_datetime(timestamp)

        unit = args.get(AbstractDateFunction.PARAMETER_UNIT_NAME, '').lower()
        # Strip trailing 's' to normalise plural form (e.g. "years" -> "year")
        if unit.endswith('s'):
            unit = unit[:-1]

        new_dt = _start_of(dt, unit) if self._is_start else _end_of(dt, unit)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_TIMESTAMP_NAME: datetime_to_iso(new_dt)}
            ),
        ])
