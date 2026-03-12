from __future__ import annotations

import calendar
from typing import Any, Dict, List, Optional

from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.common import get_datetime, datetime_to_iso
from kirun_py.function.system.date.add_subtract_time import AddSubtractTime
from kirun_py.function.system.date.epoch_to_timestamp import EpochToTimestamp
from kirun_py.function.system.date.timestamp_to_epoch import TimestampToEpoch
from kirun_py.function.system.date.to_date_string import ToDateString
from kirun_py.function.system.date.from_date_string import FromDateString
from kirun_py.function.system.date.from_now import FromNow
from kirun_py.function.system.date.get_current import GetCurrentTimestamp
from kirun_py.function.system.date.get_names import GetNames
from kirun_py.function.system.date.is_between import IsBetween
from kirun_py.function.system.date.is_valid_iso_date import IsValidISODate
from kirun_py.function.system.date.last_first_of import LastFirstOf
from kirun_py.function.system.date.set_time_zone import SetTimeZone
from kirun_py.function.system.date.start_end_of import StartEndOf
from kirun_py.function.system.date.time_as import TimeAs


def _difference(ts1: str, ts2: str, extra_params: List[Any]) -> Dict[str, Any]:
    """Compute the difference between two ISO timestamps using Luxon-compatible
    flat conversion factors (1 month = 30 days, 1 year = 365 days, 1 week = 7 days)."""

    dt1 = get_datetime(ts1)
    dt2 = get_datetime(ts2)

    # Determine requested units
    units: Optional[List[str]] = None
    if extra_params and extra_params[0]:
        raw_units = extra_params[0]
        if isinstance(raw_units, list):
            units = [u.lower() for u in raw_units if u]

    # Luxon casual-matrix conversion factors (in seconds): 1 month = 30 days, 1 year = 365 days
    _FACTORS = {
        'years': 365 * 86400,
        'months': 30 * 86400,
        'weeks': 7 * 86400,
        'days': 86400,
        'hours': 3600,
        'minutes': 60,
        'seconds': 1,
    }
    _UNIT_ORDER = ['years', 'months', 'weeks', 'days', 'hours', 'minutes', 'seconds', 'milliseconds']

    total_seconds = (dt1 - dt2).total_seconds()

    if units:
        result: Dict[str, Any] = {}
        remainder = total_seconds
        for u in _UNIT_ORDER:
            if u not in units:
                continue
            if u == 'milliseconds':
                result['milliseconds'] = int(round(remainder * 1000))
                remainder = 0
            else:
                val = int(remainder // _FACTORS[u])
                result[u] = val
                remainder -= val * _FACTORS[u]
        return result

    # Default: return total milliseconds
    return {'milliseconds': int(round(total_seconds * 1000))}


class DateFunctionRepository:

    def __init__(self) -> None:
        repo_entries: List = [
            ('EpochSecondsToTimestamp', EpochToTimestamp('EpochSecondsToTimestamp', True)),
            ('EpochMillisecondsToTimestamp', EpochToTimestamp('EpochMillisecondsToTimestamp', False)),

            # Getters from timestamp
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetDay',
                lambda iso: get_datetime(iso).day,
            ),
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetDayOfWeek',
                lambda iso: get_datetime(iso).isoweekday(),
            ),
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetMonth',
                lambda iso: get_datetime(iso).month,
            ),
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetYear',
                lambda iso: get_datetime(iso).year,
            ),
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetHours',
                lambda iso: get_datetime(iso).hour,
            ),
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetMinutes',
                lambda iso: get_datetime(iso).minute,
            ),
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetSeconds',
                lambda iso: get_datetime(iso).second,
            ),
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetMilliseconds',
                lambda iso: get_datetime(iso).microsecond // 1000,
            ),
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetDaysInMonth',
                lambda iso: calendar.monthrange(get_datetime(iso).year, get_datetime(iso).month)[1],
            ),
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetDaysInYear',
                lambda iso: 366 if calendar.isleap(get_datetime(iso).year) else 365,
            ),

            ('TimestampToEpochSeconds', TimestampToEpoch('TimestampToEpochSeconds', True)),
            ('TimestampToEpochMilliseconds', TimestampToEpoch('TimestampToEpochMilliseconds', False)),

            # Timezone info
            AbstractDateFunction.of_entry_timestamp_and_string_output(
                'GetTimeZoneName',
                lambda iso: str(get_datetime(iso).tzname() or ''),
            ),
            AbstractDateFunction.of_entry_timestamp_and_string_output(
                'GetTimeZoneOffsetLong',
                lambda iso: str(get_datetime(iso).strftime('%Z') or ''),
            ),
            AbstractDateFunction.of_entry_timestamp_and_string_output(
                'GetTimeZoneOffsetShort',
                lambda iso: str(get_datetime(iso).strftime('%z') or ''),
            ),
            AbstractDateFunction.of_entry_timestamp_and_integer_output(
                'GetTimeZoneOffset',
                lambda iso: int(get_datetime(iso).utcoffset().total_seconds() // 60)
                if get_datetime(iso).utcoffset() is not None else 0,
            ),

            ('ToDateString', ToDateString()),
            ('AddTime', AddSubtractTime(True)),
            ('SubtractTime', AddSubtractTime(False)),
            ('GetCurrentTimestamp', GetCurrentTimestamp()),

            # Difference
            AbstractDateFunction.of_entry_timestamp_timestamp_and_t_output(
                'Difference',
                Event(
                    Event.OUTPUT,
                    {AbstractDateFunction.EVENT_RESULT_NAME: Schema.of_ref(Namespaces.DATE + '.Duration')},
                ),
                _difference,
                AbstractDateFunction.PARAMETER_VARIABLE_UNIT,
            ),

            # Setters
            AbstractDateFunction.of_entry_timestamp_integer_and_timestamp_output(
                'SetDay',
                lambda iso, day: datetime_to_iso(get_datetime(iso).replace(day=day)),
            ),
            AbstractDateFunction.of_entry_timestamp_integer_and_timestamp_output(
                'SetMonth',
                lambda iso, month: datetime_to_iso(get_datetime(iso).replace(month=month)),
            ),
            AbstractDateFunction.of_entry_timestamp_integer_and_timestamp_output(
                'SetYear',
                lambda iso, year: datetime_to_iso(get_datetime(iso).replace(year=year)),
            ),
            AbstractDateFunction.of_entry_timestamp_integer_and_timestamp_output(
                'SetHours',
                lambda iso, hour: datetime_to_iso(get_datetime(iso).replace(hour=hour)),
            ),
            AbstractDateFunction.of_entry_timestamp_integer_and_timestamp_output(
                'SetMinutes',
                lambda iso, minute: datetime_to_iso(get_datetime(iso).replace(minute=minute)),
            ),
            AbstractDateFunction.of_entry_timestamp_integer_and_timestamp_output(
                'SetSeconds',
                lambda iso, second: datetime_to_iso(get_datetime(iso).replace(second=second)),
            ),
            AbstractDateFunction.of_entry_timestamp_integer_and_timestamp_output(
                'SetMilliseconds',
                lambda iso, ms: datetime_to_iso(get_datetime(iso).replace(microsecond=ms * 1000)),
            ),

            ('SetTimeZone', SetTimeZone()),

            # Comparisons
            AbstractDateFunction.of_entry_timestamp_timestamp_and_t_output(
                'IsBefore',
                Event(
                    Event.OUTPUT,
                    {AbstractDateFunction.EVENT_RESULT_NAME: Schema.of_boolean(AbstractDateFunction.EVENT_RESULT_NAME)},
                ),
                lambda t1, t2, _: get_datetime(t1) < get_datetime(t2),
            ),
            AbstractDateFunction.of_entry_timestamp_timestamp_and_t_output(
                'IsAfter',
                Event(
                    Event.OUTPUT,
                    {AbstractDateFunction.EVENT_RESULT_NAME: Schema.of_boolean(AbstractDateFunction.EVENT_RESULT_NAME)},
                ),
                lambda t1, t2, _: get_datetime(t1) > get_datetime(t2),
            ),
            AbstractDateFunction.of_entry_timestamp_timestamp_and_t_output(
                'IsSame',
                Event(
                    Event.OUTPUT,
                    {AbstractDateFunction.EVENT_RESULT_NAME: Schema.of_boolean(AbstractDateFunction.EVENT_RESULT_NAME)},
                ),
                lambda t1, t2, _: get_datetime(t1) == get_datetime(t2),
            ),
            AbstractDateFunction.of_entry_timestamp_timestamp_and_t_output(
                'IsSameOrBefore',
                Event(
                    Event.OUTPUT,
                    {AbstractDateFunction.EVENT_RESULT_NAME: Schema.of_boolean(AbstractDateFunction.EVENT_RESULT_NAME)},
                ),
                lambda t1, t2, _: get_datetime(t1) <= get_datetime(t2),
            ),
            AbstractDateFunction.of_entry_timestamp_timestamp_and_t_output(
                'IsSameOrAfter',
                Event(
                    Event.OUTPUT,
                    {AbstractDateFunction.EVENT_RESULT_NAME: Schema.of_boolean(AbstractDateFunction.EVENT_RESULT_NAME)},
                ),
                lambda t1, t2, _: get_datetime(t1) >= get_datetime(t2),
            ),

            # Boolean checks
            AbstractDateFunction.of_entry_timestamp_and_boolean_output(
                'IsInLeapYear',
                lambda iso: calendar.isleap(get_datetime(iso).year),
            ),
            AbstractDateFunction.of_entry_timestamp_and_boolean_output(
                'IsInDST',
                lambda iso: bool(get_datetime(iso).dst()) if get_datetime(iso).dst() is not None else False,
            ),

            ('IsBetween', IsBetween()),
            ('LastOf', LastFirstOf(True)),
            ('FirstOf', LastFirstOf(False)),
            ('StartOf', StartEndOf(True)),
            ('EndOf', StartEndOf(False)),
            ('TimeAsObject', TimeAs(False)),
            ('TimeAsArray', TimeAs(True)),
            ('GetNames', GetNames()),
            ('IsValidISODate', IsValidISODate()),
            ('FromNow', FromNow()),
            ('FromDateString', FromDateString()),
        ]

        self._repo_map: Dict[str, Any] = {}
        for name, func in repo_entries:
            self._repo_map[name] = func

        self._filterable_names: List[str] = [
            func.get_signature().get_full_name()
            for func in self._repo_map.values()
        ]

    async def find(self, namespace: str, name: str) -> Optional[Any]:
        if namespace != Namespaces.DATE:
            return None
        return self._repo_map.get(name)

    async def filter(self, name: str) -> List[str]:
        lower = name.lower()
        return [n for n in self._filterable_names if lower in n.lower()]
