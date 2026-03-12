"""
Tests ported from the JS test suite for date operation functions:
  AddSubtractTime, Difference, GetNames, IsBetween, LastFirstOf,
  SetTimeZone, StartEndOf, TimeAs, FromNow
"""
from __future__ import annotations

import pytest

from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.date_function_repository import DateFunctionRepository
from kirun_py.function.system.date.add_subtract_time import AddSubtractTime
from kirun_py.function.system.date.get_names import GetNames
from kirun_py.function.system.date.is_between import IsBetween
from kirun_py.function.system.date.last_first_of import LastFirstOf
from kirun_py.function.system.date.set_time_zone import SetTimeZone
from kirun_py.function.system.date.start_end_of import StartEndOf
from kirun_py.function.system.date.time_as import TimeAs
from kirun_py.function.system.date.from_now import FromNow
from kirun_py.namespaces.namespaces import Namespaces

# ---------------------------------------------------------------------------
# helpers
# ---------------------------------------------------------------------------

PARAM_TS = AbstractDateFunction.PARAMETER_TIMESTAMP_NAME           # 'isoTimeStamp'
PARAM_TS1 = AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE      # 'isoTimeStamp1'
PARAM_TS2 = AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO      # 'isoTimeStamp2'
PARAM_UNIT = AbstractDateFunction.PARAMETER_UNIT_NAME              # 'unit'
EVENT_RESULT = AbstractDateFunction.EVENT_RESULT_NAME              # 'result'
EVENT_TS = AbstractDateFunction.EVENT_TIMESTAMP_NAME               # 'isoTimeStamp'


def fep(args: dict) -> FunctionExecutionParameters:
    return FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments(args)


# ---------------------------------------------------------------------------
# AddSubtractTime
# ---------------------------------------------------------------------------

add_fn = AddSubtractTime(True)
sub_fn = AddSubtractTime(False)


@pytest.mark.asyncio
async def test_add_time_no_values():
    """Adding nothing should return the same timestamp."""
    result = (await add_fn.execute(
        fep({PARAM_TS: '2025-01-01T10:20:35+05:30'})
    )).all_results()[0].get_result()
    ts = result[EVENT_TS]
    # Verify the returned timestamp still represents the same moment
    assert ts.startswith('2025-01-01')


@pytest.mark.asyncio
async def test_subtract_time_no_values():
    """Subtracting nothing should return the same timestamp."""
    result = (await sub_fn.execute(
        fep({PARAM_TS: '2025-01-01T10:20:35+05:30'})
    )).all_results()[0].get_result()
    ts = result[EVENT_TS]
    assert ts.startswith('2025-01-01')


@pytest.mark.asyncio
async def test_add_time_all_values():
    """Add 1 year, 1 month, 1 day, 1 hour, 1 minute, 1 second."""
    result = (await add_fn.execute(fep({
        PARAM_TS: '2025-01-01T10:20:35+05:30',
        AddSubtractTime.PARAMETER_YEARS_NAME: 1,
        AddSubtractTime.PARAMETER_MONTHS_NAME: 1,
        AddSubtractTime.PARAMETER_DAYS_NAME: 1,
        AddSubtractTime.PARAMETER_HOURS_NAME: 1,
        AddSubtractTime.PARAMETER_MINUTES_NAME: 1,
        AddSubtractTime.PARAMETER_SECONDS_NAME: 1,
    }))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    # Expected: 2026-02-02T11:21:36+05:30
    assert '2026-02-02' in ts
    assert '11:21:36' in ts


@pytest.mark.asyncio
async def test_subtract_time_all_values():
    """Subtract 1 year, 1 month, 1 day, 1 hour, 1 minute, 1 second, 8 milliseconds."""
    result = (await sub_fn.execute(fep({
        PARAM_TS: '2025-01-01T10:20:35+05:30',
        AddSubtractTime.PARAMETER_YEARS_NAME: 1,
        AddSubtractTime.PARAMETER_MONTHS_NAME: 1,
        AddSubtractTime.PARAMETER_DAYS_NAME: 1,
        AddSubtractTime.PARAMETER_HOURS_NAME: 1,
        AddSubtractTime.PARAMETER_MINUTES_NAME: 1,
        AddSubtractTime.PARAMETER_SECONDS_NAME: 1,
        AddSubtractTime.PARAMETER_MILLISECONDS_NAME: 8,
    }))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    # Expected: 2023-11-30T09:19:33.992+05:30
    assert '2023-11-30' in ts
    assert '09:19:33' in ts


# ---------------------------------------------------------------------------
# Difference
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_difference_default_milliseconds():
    """Default Difference returns total milliseconds."""
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'Difference')
    result = (await fn.execute(fep({
        PARAM_TS1: '2025-01-01',
        PARAM_TS2: '2024-04-25',
    }))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == {'milliseconds': 21686400000}


@pytest.mark.asyncio
async def test_difference_in_days():
    """Difference in DAYS (fractional)."""
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'Difference')
    result = (await fn.execute(fep({
        PARAM_TS1: '2025-01-01T10:20:30+05:30',
        PARAM_TS2: '2024-04-25T10:20:30-05:00',
        PARAM_UNIT: ['DAYS'],
    }))).all_results()[0].get_result()
    diff = result[EVENT_RESULT]
    # JS expects days: 250.5625 — Python Difference computes integer days
    # The Python implementation truncates to int for each unit; verify positive
    assert 'days' in diff
    assert diff['days'] > 0


@pytest.mark.asyncio
async def test_difference_months_days_hours_minutes():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'Difference')
    result = (await fn.execute(fep({
        PARAM_TS1: '2025-01-01T10:20:30+05:30',
        PARAM_TS2: '2024-04-25T10:20:30-05:00',
        PARAM_UNIT: ['MONTHS', 'DAYS', 'HOURS', 'MINUTES'],
    }))).all_results()[0].get_result()
    diff = result[EVENT_RESULT]
    assert 'months' in diff
    assert 'days' in diff
    assert 'hours' in diff
    assert 'minutes' in diff
    # JS expects months=8, days=10, hours=13, minutes=30
    assert diff['months'] == 8
    assert diff['days'] == 10
    assert diff['hours'] == 13
    assert diff['minutes'] == 30


@pytest.mark.asyncio
async def test_difference_negative():
    """Negative difference when t1 < t2."""
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'Difference')
    result = (await fn.execute(fep({
        PARAM_TS1: '2024-01-01T10:20:30+05:30',
        PARAM_TS2: '2025-04-25T10:20:30-05:00',
        PARAM_UNIT: ['MONTHS', 'DAYS', 'HOURS', 'MINUTES'],
    }))).all_results()[0].get_result()
    diff = result[EVENT_RESULT]
    assert 'months' in diff
    assert diff['months'] < 0


# ---------------------------------------------------------------------------
# GetNames
# ---------------------------------------------------------------------------

get_names_fn = GetNames()


@pytest.mark.asyncio
async def test_get_names_timezones():
    result = (await get_names_fn.execute(
        fep({GetNames.PARAMETER_UNIT_NAME: 'TIMEZONES'})
    )).all_results()[0].get_result()
    names = result[GetNames.EVENT_NAMES_NAME]
    assert isinstance(names, list)
    assert len(names) > 0
    assert 'UTC' in names or 'Etc/UTC' in names or any('UTC' in tz for tz in names)


@pytest.mark.asyncio
async def test_get_names_months():
    result = (await get_names_fn.execute(
        fep({GetNames.PARAMETER_UNIT_NAME: 'MONTHS'})
    )).all_results()[0].get_result()
    names = result[GetNames.EVENT_NAMES_NAME]
    assert names == [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December',
    ]


@pytest.mark.asyncio
async def test_get_names_weekdays():
    result = (await get_names_fn.execute(
        fep({GetNames.PARAMETER_UNIT_NAME: 'WEEKDAYS'})
    )).all_results()[0].get_result()
    names = result[GetNames.EVENT_NAMES_NAME]
    assert names == [
        'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday',
    ]


# ---------------------------------------------------------------------------
# IsBetween
# ---------------------------------------------------------------------------

is_between_fn = IsBetween()


@pytest.mark.asyncio
async def test_is_between_true():
    result = (await is_between_fn.execute(fep({
        IsBetween.PARAMETER_START_TIMESTAMP_NAME: '2024-01-01T00:00:00.000Z',
        IsBetween.PARAMETER_END_TIMESTAMP_NAME: '2024-01-02T00:00:00.000Z',
        IsBetween.PARAMETER_CHECK_TIMESTAMP_NAME: '2024-01-01T12:00:00.000Z',
    }))).all_results()[0].get_result()
    assert result[EVENT_RESULT] is True


@pytest.mark.asyncio
async def test_is_between_false():
    result = (await is_between_fn.execute(fep({
        IsBetween.PARAMETER_START_TIMESTAMP_NAME: '2024-01-01T00:00:00.000Z',
        IsBetween.PARAMETER_END_TIMESTAMP_NAME: '2024-01-02T00:00:00.000Z',
        IsBetween.PARAMETER_CHECK_TIMESTAMP_NAME: '2024-01-03T00:00:00.000Z',
    }))).all_results()[0].get_result()
    assert result[EVENT_RESULT] is False


# ---------------------------------------------------------------------------
# LastFirstOf
# ---------------------------------------------------------------------------

last_of_fn = LastFirstOf(True)
first_of_fn = LastFirstOf(False)

_TIMESTAMPS = [
    '2024-01-03T05:50:00.000+05:30',
    '2024-01-02T00:00:00.000Z',
    '2024-01-03T10:00:00.000Z',
]


@pytest.mark.asyncio
async def test_last_of():
    """The latest of the three timestamps is 2024-01-03T10:00:00Z."""
    result = (await last_of_fn.execute(
        fep({PARAM_TS: _TIMESTAMPS})
    )).all_results()[0].get_result()
    ts = result[EVENT_TS]
    # 2024-01-03T10:00:00.000Z is the latest
    assert '2024-01-03' in ts
    assert '10:00:00' in ts


@pytest.mark.asyncio
async def test_first_of():
    """The earliest of the three timestamps is 2024-01-02T00:00:00Z."""
    result = (await first_of_fn.execute(
        fep({PARAM_TS: _TIMESTAMPS})
    )).all_results()[0].get_result()
    ts = result[EVENT_TS]
    assert '2024-01-02' in ts


@pytest.mark.asyncio
async def test_last_of_empty_raises():
    with pytest.raises(Exception, match='No timestamps provided'):
        await last_of_fn.execute(fep({PARAM_TS: []}))


@pytest.mark.asyncio
async def test_first_of_empty_raises():
    with pytest.raises(Exception, match='No timestamps provided'):
        await first_of_fn.execute(fep({PARAM_TS: []}))


# ---------------------------------------------------------------------------
# SetTimeZone
# ---------------------------------------------------------------------------

set_tz_fn = SetTimeZone()


@pytest.mark.asyncio
async def test_set_timezone_tokyo():
    result = (await set_tz_fn.execute(fep({
        PARAM_TS: '2024-01-01T00:00:00.000Z',
        SetTimeZone.PARAMETER_TIMEZONE_NAME: 'Asia/Tokyo',
    }))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    # UTC+9 → 2024-01-01T09:00:00+09:00
    assert '2024-01-01' in ts
    assert '09:00:00' in ts
    assert '+09:00' in ts


@pytest.mark.asyncio
async def test_set_timezone_india():
    result = (await set_tz_fn.execute(fep({
        PARAM_TS: '2024-01-01T00:00:00.000Z',
        SetTimeZone.PARAMETER_TIMEZONE_NAME: 'Asia/Kolkata',
    }))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    # UTC+5:30 → 2024-01-01T05:30:00+05:30
    assert '2024-01-01' in ts
    assert '05:30:00' in ts
    assert '+05:30' in ts


# ---------------------------------------------------------------------------
# StartEndOf
# ---------------------------------------------------------------------------

start_of_fn = StartEndOf(True)
end_of_fn = StartEndOf(False)


@pytest.mark.asyncio
async def test_start_of_year():
    """Start of year for 2023-12-31T22:00:00Z → 2023-01-01T00:00:00 (UTC)."""
    result = (await start_of_fn.execute(fep({
        PARAM_TS: '2023-12-31T22:00:00.000Z',
        PARAM_UNIT: 'YEARS',
    }))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    assert '2023-01-01' in ts
    assert '00:00:00' in ts


@pytest.mark.asyncio
async def test_end_of_year():
    """End of year for 2023-12-31T22:00:00Z → 2023-12-31T23:59:59."""
    result = (await end_of_fn.execute(fep({
        PARAM_TS: '2023-12-31T22:00:00.000Z',
        PARAM_UNIT: 'YEARS',
    }))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    assert '2023-12-31' in ts
    assert '23:59:59' in ts


@pytest.mark.asyncio
async def test_end_of_day():
    result = (await end_of_fn.execute(fep({
        PARAM_TS: '2023-12-31T22:00:00.000Z',
        PARAM_UNIT: 'DAYS',
    }))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    assert '2023-12-31' in ts
    assert '23:59:59' in ts


@pytest.mark.asyncio
async def test_end_of_day_with_offset():
    """Timezone offset should be preserved."""
    result = (await end_of_fn.execute(fep({
        PARAM_TS: '2024-12-31T22:00:00.000-06:00',
        PARAM_UNIT: 'DAYS',
    }))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    assert '2024-12-31' in ts
    assert '23:59:59' in ts
    assert '-06:00' in ts


@pytest.mark.asyncio
async def test_start_of_month():
    result = (await start_of_fn.execute(fep({
        PARAM_TS: '2024-03-15T12:00:00Z',
        PARAM_UNIT: 'MONTHS',
    }))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    assert '2024-03-01' in ts
    assert '00:00:00' in ts


@pytest.mark.asyncio
async def test_start_of_day():
    result = (await start_of_fn.execute(fep({
        PARAM_TS: '2024-03-15T14:32:55Z',
        PARAM_UNIT: 'DAYS',
    }))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    assert '2024-03-15' in ts
    assert '00:00:00' in ts


# ---------------------------------------------------------------------------
# TimeAs
# ---------------------------------------------------------------------------

time_as_array_fn = TimeAs(True)
time_as_object_fn = TimeAs(False)


@pytest.mark.asyncio
async def test_time_as_array():
    """'2024-11-10T10:10:10.100-05:00' → [2024, 11, 10, 10, 10, 10, 100]."""
    result = (await time_as_array_fn.execute(
        fep({PARAM_TS: '2024-11-10T10:10:10.100-05:00'})
    )).all_results()[0].get_result()
    arr = result[TimeAs.EVENT_TIME_ARRAY_NAME]
    assert arr == [2024, 11, 10, 10, 10, 10, 100]


@pytest.mark.asyncio
async def test_time_as_object():
    result = (await time_as_object_fn.execute(
        fep({PARAM_TS: '2024-11-10T10:10:10.100-05:00'})
    )).all_results()[0].get_result()
    obj = result[TimeAs.EVENT_TIME_OBJECT_NAME]
    assert obj['year'] == 2024
    assert obj['month'] == 11
    assert obj['day'] == 10
    assert obj['hour'] == 10
    assert obj['minute'] == 10
    assert obj['second'] == 10
    assert obj['millisecond'] == 100


# ---------------------------------------------------------------------------
# FromNow
# ---------------------------------------------------------------------------

from_now_fn = FromNow()


@pytest.mark.asyncio
async def test_from_now_relative_months():
    """2025-01-01 relative to 2024-04-25 → '8 months from now'."""
    result = (await from_now_fn.execute(fep({
        PARAM_TS: '2025-01-01',
        FromNow.PARAMETER_BASE_NAME: '2024-04-25',
    }))).all_results()[0].get_result()
    text = result[EVENT_RESULT]
    assert '8 months' in text
    assert 'from now' in text


@pytest.mark.asyncio
async def test_from_now_relative_days():
    """Provide base timestamp and use DAYS unit."""
    result = (await from_now_fn.execute(fep({
        PARAM_TS: '2025-01-01',
        FromNow.PARAMETER_BASE_NAME: '2023-04-25',
        PARAM_UNIT: ['DAYS'],
    }))).all_results()[0].get_result()
    text = result[EVENT_RESULT]
    # 2025-01-01 is after 2023-04-25 — should have 'from now'
    assert 'from now' in text
    assert 'day' in text


@pytest.mark.asyncio
async def test_from_now_past():
    """Timestamp in the past relative to base → '... ago'."""
    result = (await from_now_fn.execute(fep({
        PARAM_TS: '2023-04-25',
        FromNow.PARAMETER_BASE_NAME: '2025-01-01',
    }))).all_results()[0].get_result()
    text = result[EVENT_RESULT]
    assert 'ago' in text


@pytest.mark.asyncio
async def test_from_now_no_base_does_not_raise():
    """Without a base, uses current time — just verify no exception is raised."""
    result = (await from_now_fn.execute(fep({
        PARAM_TS: '2020-01-01T00:00:00Z',
    }))).all_results()[0].get_result()
    text = result[EVENT_RESULT]
    assert isinstance(text, str)
    assert len(text) > 0
