"""
Tests ported from the JS test suite for basic date functions:
  DateFunctionRepository, GetCurrentTimestamp, IsValidISODate,
  EpochToTimestamp, TimestampToEpoch, ToDateString, FromDateString
"""
from __future__ import annotations

import pytest

from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.date_function_repository import DateFunctionRepository
from kirun_py.function.system.date.is_valid_iso_date import IsValidISODate
from kirun_py.function.system.date.epoch_to_timestamp import EpochToTimestamp
from kirun_py.function.system.date.timestamp_to_epoch import TimestampToEpoch
from kirun_py.function.system.date.to_date_string import ToDateString
from kirun_py.function.system.date.from_date_string import FromDateString
from kirun_py.namespaces.namespaces import Namespaces

# ---------------------------------------------------------------------------
# helpers
# ---------------------------------------------------------------------------

PARAM_TS = AbstractDateFunction.PARAMETER_TIMESTAMP_NAME          # 'isoTimeStamp'
PARAM_NUM = AbstractDateFunction.PARAMETER_NUMBER_NAME             # 'number'
EVENT_RESULT = AbstractDateFunction.EVENT_RESULT_NAME              # 'result'
EVENT_TS = AbstractDateFunction.EVENT_TIMESTAMP_NAME               # 'isoTimeStamp'


def fep(args: dict) -> FunctionExecutionParameters:
    return FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments(args)


# ---------------------------------------------------------------------------
# DateFunctionRepository — simple getter functions
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_repo_get_day():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetDay')
    result = (await fn.execute(fep({PARAM_TS: '2024-01-01'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 1


@pytest.mark.asyncio
async def test_repo_get_days_in_month():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetDaysInMonth')
    result = (await fn.execute(fep({PARAM_TS: '2024-01-01'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 31


@pytest.mark.asyncio
async def test_repo_get_days_in_leap_year():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetDaysInYear')
    result = (await fn.execute(fep({PARAM_TS: '2024-01-01'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 366


@pytest.mark.asyncio
async def test_repo_get_days_in_non_leap_year():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetDaysInYear')
    result = (await fn.execute(fep({PARAM_TS: '2023-01-01'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 365


@pytest.mark.asyncio
async def test_repo_get_day_of_week():
    """2024-01-01 is a Monday — isoweekday() == 1."""
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetDayOfWeek')
    result = (await fn.execute(fep({PARAM_TS: '2024-01-01'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 1


@pytest.mark.asyncio
async def test_repo_set_day():
    """SetDay replaces day=2 on 2024-01-01."""
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'SetDay')
    result = (
        await fn.execute(fep({PARAM_TS: '2024-01-01', PARAM_NUM: 2}))
    ).all_results()[0].get_result()
    # Python isoformat for a naive UTC date has no offset; the JS test expects a specific offset.
    # We just check the date portion is correct.
    ts = result[EVENT_RESULT]
    assert ts.startswith('2024-01-02')


@pytest.mark.asyncio
async def test_repo_get_month():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetMonth')
    result = (await fn.execute(fep({PARAM_TS: '2024-01-15'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 1


@pytest.mark.asyncio
async def test_repo_get_year():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetYear')
    result = (await fn.execute(fep({PARAM_TS: '2024-01-15'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 2024


@pytest.mark.asyncio
async def test_repo_get_hours():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetHours')
    result = (await fn.execute(fep({PARAM_TS: '2024-01-15T10:30:00Z'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 10


@pytest.mark.asyncio
async def test_repo_get_minutes():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetMinutes')
    result = (await fn.execute(fep({PARAM_TS: '2024-01-15T10:30:00Z'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 30


@pytest.mark.asyncio
async def test_repo_get_seconds():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetSeconds')
    result = (await fn.execute(fep({PARAM_TS: '2024-01-15T10:30:45Z'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 45


@pytest.mark.asyncio
async def test_repo_get_milliseconds():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetMilliseconds')
    result = (await fn.execute(fep({PARAM_TS: '2024-01-15T10:30:45.250Z'}))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 250


# ---------------------------------------------------------------------------
# GetCurrentTimestamp
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_get_current_timestamp_is_defined():
    """Just verify the function returns a non-empty timestamp string."""
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'GetCurrentTimestamp')
    result = (await fn.execute(fep({}))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    assert ts is not None
    assert isinstance(ts, str)
    assert len(ts) > 0


# ---------------------------------------------------------------------------
# IsValidISODate
# ---------------------------------------------------------------------------

is_valid_fn = IsValidISODate()


@pytest.mark.asyncio
async def test_is_valid_iso_date_valid():
    result = (await is_valid_fn.execute(
        fep({PARAM_TS: '2024-01-01T00:00:00.000Z'})
    )).all_results()[0].get_result()
    assert result[EVENT_RESULT] is True


@pytest.mark.asyncio
async def test_is_valid_iso_date_invalid_string():
    result = (await is_valid_fn.execute(
        fep({PARAM_TS: 'invalid'})
    )).all_results()[0].get_result()
    assert result[EVENT_RESULT] is False


@pytest.mark.asyncio
async def test_is_valid_iso_date_invalid_feb31():
    """2024-02-31 does not exist."""
    result = (await is_valid_fn.execute(
        fep({PARAM_TS: '2024-02-31'})
    )).all_results()[0].get_result()
    assert result[EVENT_RESULT] is False


@pytest.mark.asyncio
async def test_is_valid_iso_date_invalid_non_leap_year_feb29():
    """2022 is not a leap year, so Feb 29 is invalid."""
    result = (await is_valid_fn.execute(
        fep({PARAM_TS: '2022-02-29T00:00:00.000Z'})
    )).all_results()[0].get_result()
    assert result[EVENT_RESULT] is False


# ---------------------------------------------------------------------------
# EpochToTimestamp
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_epoch_seconds_to_timestamp():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'EpochSecondsToTimestamp')
    result = (await fn.execute(fep({'epochSeconds': 1694072117}))).all_results()[0].get_result()
    assert result[EVENT_TS] == '2023-09-07T07:35:17+00:00'


@pytest.mark.asyncio
async def test_epoch_milliseconds_to_timestamp():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'EpochMillisecondsToTimestamp')
    result = (await fn.execute(fep({'epochMilliseconds': 1694072117000}))).all_results()[0].get_result()
    assert result[EVENT_TS] == '2023-09-07T07:35:17+00:00'


@pytest.mark.asyncio
async def test_epoch_seconds_large():
    """Large epoch value — year 7338."""
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'EpochSecondsToTimestamp')
    result = (await fn.execute(fep({'epochSeconds': 169407211700}))).all_results()[0].get_result()
    ts = result[EVENT_TS]
    assert ts.startswith('7338-04-20')


@pytest.mark.asyncio
async def test_epoch_seconds_from_string():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'EpochSecondsToTimestamp')
    result = (await fn.execute(fep({'epochSeconds': '1696489387'}))).all_results()[0].get_result()
    assert result[EVENT_TS] == '2023-10-05T07:03:07+00:00'


@pytest.mark.asyncio
async def test_epoch_milliseconds_from_small_string():
    """Small integer treated as epoch milliseconds."""
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'EpochMillisecondsToTimestamp')
    result = (await fn.execute(fep({'epochMilliseconds': '1696489386'}))).all_results()[0].get_result()
    assert result[EVENT_TS] == '1970-01-20T15:14:49.386000+00:00'


@pytest.mark.asyncio
async def test_epoch_seconds_invalid_string_raises():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'EpochSecondsToTimestamp')
    with pytest.raises(Exception, match='Please provide a valid value for epochSeconds'):
        await fn.execute(fep({'epochSeconds': 'abcdef'}))


@pytest.mark.asyncio
async def test_epoch_seconds_string_2():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'EpochSecondsToTimestamp')
    result = (await fn.execute(fep({'epochSeconds': '1696494131'}))).all_results()[0].get_result()
    assert result[EVENT_TS] == '2023-10-05T08:22:11+00:00'


@pytest.mark.asyncio
async def test_epoch_seconds_small_string():
    repo = DateFunctionRepository()
    fn = await repo.find(Namespaces.DATE, 'EpochSecondsToTimestamp')
    result = (await fn.execute(fep({'epochSeconds': '169640'}))).all_results()[0].get_result()
    assert result[EVENT_TS] == '1970-01-02T23:07:20+00:00'


# ---------------------------------------------------------------------------
# TimestampToEpoch
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_timestamp_to_epoch_seconds():
    fn = TimestampToEpoch('TimestampToEpochSeconds', True)
    result = (await fn.execute(
        fep({PARAM_TS: '2024-01-01T00:00:00.000+05:30'})
    )).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 1704047400


@pytest.mark.asyncio
async def test_timestamp_to_epoch_milliseconds():
    fn = TimestampToEpoch('TimestampToEpochMilliseconds', False)
    result = (await fn.execute(
        fep({PARAM_TS: '2024-01-01T00:00:00.000+05:30'})
    )).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 1704047400000


# ---------------------------------------------------------------------------
# ToDateString
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_to_date_string_basic_format():
    fn = ToDateString()
    result = (await fn.execute(fep({
        PARAM_TS: '2024-01-01T00:00:00.000+05:30',
        ToDateString.PARAMETER_FORMAT_NAME: 'yyyy-MM-dd',
    }))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == '2024-01-01'


@pytest.mark.asyncio
async def test_to_date_string_year_only():
    fn = ToDateString()
    result = (await fn.execute(fep({
        PARAM_TS: '2024-03-15T12:00:00Z',
        ToDateString.PARAMETER_FORMAT_NAME: 'yyyy',
    }))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == '2024'


@pytest.mark.asyncio
async def test_to_date_string_month_name():
    fn = ToDateString()
    result = (await fn.execute(fep({
        PARAM_TS: '2024-03-15T12:00:00Z',
        ToDateString.PARAMETER_FORMAT_NAME: 'MMMM',
    }))).all_results()[0].get_result()
    assert result[EVENT_RESULT] == 'March'


# ---------------------------------------------------------------------------
# FromDateString
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_from_date_string_basic():
    fn = FromDateString()
    result = (await fn.execute(fep({
        FromDateString.PARAMETER_TIMESTAMP_STRING_NAME: '2024-01-01',
        FromDateString.PARAMETER_FORMAT_NAME: 'yyyy-MM-dd',
    }))).all_results()[0].get_result()
    # Python returns naive datetime isoformat without timezone
    ts = result[EVENT_RESULT]
    assert ts.startswith('2024-01-01')


@pytest.mark.asyncio
async def test_from_date_string_year_month():
    fn = FromDateString()
    result = (await fn.execute(fep({
        FromDateString.PARAMETER_TIMESTAMP_STRING_NAME: '2024-05',
        FromDateString.PARAMETER_FORMAT_NAME: 'yyyy-MM',
    }))).all_results()[0].get_result()
    ts = result[EVENT_RESULT]
    assert ts.startswith('2024-05-01')


@pytest.mark.asyncio
async def test_from_date_string_partial_fields():
    """day=03, seconds=12, milliseconds=123 — just verify it parses without error."""
    fn = FromDateString()
    result = (await fn.execute(fep({
        FromDateString.PARAMETER_TIMESTAMP_STRING_NAME: '03 12 123',
        FromDateString.PARAMETER_FORMAT_NAME: 'dd ss SSS',
    }))).all_results()[0].get_result()
    ts = result[EVENT_RESULT]
    assert ts is not None
    # day=3 should appear in the result
    from datetime import datetime
    dt = datetime.fromisoformat(ts)
    assert dt.day == 3
    assert dt.second == 12
