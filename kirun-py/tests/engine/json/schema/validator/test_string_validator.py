from __future__ import annotations

import pytest

from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.json.schema.string.string_format import StringFormat
from kirun_py.json.schema.validator.string_validator import StringValidator
from kirun_py.json.schema.validator.schema_validator import SchemaValidator


def test_string_valid_case():
    value = 'surendhar'
    schema = Schema().set_type(TypeUtil.of(SchemaType.STRING))

    assert StringValidator.validate([], schema, value) == value


def test_string_invalid_case():
    schema = Schema().set_type(TypeUtil.of(SchemaType.STRING))

    with pytest.raises(Exception):
        StringValidator.validate([], schema, 123)


def test_string_min_length_invalid():
    value = 'abcd'
    schema = Schema().set_type(TypeUtil.of(SchemaType.STRING)).set_min_length(5)

    with pytest.raises(Exception):
        StringValidator.validate([], schema, value)


def test_string_max_length_invalid():
    value = 'surendhar'
    schema = Schema().set_type(TypeUtil.of(SchemaType.STRING)).set_max_length(8)

    with pytest.raises(Exception):
        StringValidator.validate([], schema, value)


def test_string_min_length_valid():
    value = 'abcdefg'
    schema = Schema().set_type(TypeUtil.of(SchemaType.STRING)).set_min_length(5)

    assert StringValidator.validate([], schema, value) == value


def test_string_max_length_valid():
    value = 'surendhar'
    schema = Schema().set_type(TypeUtil.of(SchemaType.STRING)).set_max_length(12323)

    assert StringValidator.validate([], schema, value) == value


def test_string_date_invalid_case():
    value = '1234-12-1245'
    schema = Schema().set_format(StringFormat.DATE)

    with pytest.raises(Exception):
        StringValidator.validate([], schema, value)


def test_string_date_valid_case():
    value = '2023-01-26'
    schema = Schema().set_format(StringFormat.DATE)

    assert StringValidator.validate([], schema, value) == value


def test_string_time_invalid_case():
    value = '231:45:56'
    schema = Schema().set_format(StringFormat.TIME)

    with pytest.raises(Exception):
        StringValidator.validate([], schema, value)


def test_string_time_valid_case():
    value = '22:32:45'
    schema = Schema().set_format(StringFormat.TIME)

    assert StringValidator.validate([], schema, value) == value


def test_string_date_time_invalid_case():
    value = '26-jan-2023 231:45:56'
    schema = Schema().set_format(StringFormat.DATETIME)

    with pytest.raises(Exception):
        StringValidator.validate([], schema, value)


def test_string_date_time_valid_case():
    value = '2032-02-12T02:54:23'
    schema = Schema().set_format(StringFormat.DATETIME)

    assert StringValidator.validate([], schema, value) == value


@pytest.mark.parametrize('value', ['2026-01-10', '2026-01-20', '2026-01-19', '2026-01-29'])
def test_string_date_valid_on_tenth_and_twentieth(value):
    schema = Schema().set_format(StringFormat.DATE)

    assert StringValidator.validate([], schema, value) == value


@pytest.mark.parametrize('value', ['2026-01-10T10:00:00', '2026-01-20T10:00:00', '2026-01-20T10:00:00+05:30'])
def test_string_date_time_valid_on_tenth_and_twentieth(value):
    schema = Schema().set_format(StringFormat.DATETIME)

    assert StringValidator.validate([], schema, value) == value


@pytest.mark.parametrize('fmt,value', [
    (StringFormat.DATETIME, '2026-01-15T10:00:00Z'),
    (StringFormat.DATETIME, '2026-01-15T10:00:00.123Z'),
    (StringFormat.DATETIME, '2026-01-15T10:00:00.123456789+05:30'),
    (StringFormat.TIME, '10:00:00Z'),
    (StringFormat.TIME, '10:00:00.123'),
    (StringFormat.TIME, '10:00:00.123+05:30'),
    (StringFormat.DECIMAL, '0'),
    (StringFormat.DECIMAL, '100'),
    (StringFormat.DECIMAL, '-12.50'),
    (StringFormat.DECIMAL, '+3.14159'),
    (StringFormat.ID, '65f1c2a9e4b0a1b2c3d4e5f6'),
    (StringFormat.ID, '01J9ZQ3V8K2M4N6P8R0S2T4V6W'),
    (StringFormat.ID, '01j9zq3v8k2m4n6p8r0s2t4v6w'),
])
def test_string_format_valid(fmt, value):
    schema = Schema().set_format(fmt)

    assert StringValidator.validate([], schema, value) == value


@pytest.mark.parametrize('fmt,value,pattern_name', [
    (StringFormat.DATETIME, '2026-01-15T10:00.123', 'date time pattern'),
    (StringFormat.DATETIME, '2026-01-15T10:00:00.1234567890Z', 'date time pattern'),
    (StringFormat.DATETIME, '2026-01-15T10:00:00.Z', 'date time pattern'),
    (StringFormat.TIME, '10:00.5', 'time pattern'),
    (StringFormat.TIME, '10:00:00.', 'time pattern'),
    (StringFormat.DECIMAL, '1e5', 'decimal pattern'),
    (StringFormat.DECIMAL, '12.', 'decimal pattern'),
    (StringFormat.DECIMAL, '.5', 'decimal pattern'),
    (StringFormat.DECIMAL, '1,000', 'decimal pattern'),
    (StringFormat.DECIMAL, 'abc', 'decimal pattern'),
    (StringFormat.DECIMAL, '', 'decimal pattern'),
    (StringFormat.ID, '65f1c2a9e4b0a1b2c3d4e5fg', 'id pattern'),
    (StringFormat.ID, '01J9ZQ3V8K2M4N6P8R0S2T4V6U', 'id pattern'),
    (StringFormat.ID, '01J9ZQ3V8K2M4N6P8R0S2T4V6', 'id pattern'),
    (StringFormat.ID, 'abc', 'id pattern'),
])
def test_string_format_invalid(fmt, value, pattern_name):
    schema = Schema().set_format(fmt)

    with pytest.raises(Exception, match=' is not matched with the ' + pattern_name):
        StringValidator.validate([], schema, value)


def test_string_email_invalid_case():
    value = 'testemail fai%6&8ls@gmail.com'
    schema = Schema().set_format(StringFormat.EMAIL)

    with pytest.raises(Exception):
        StringValidator.validate([], schema, value)


def test_string_email_valid_case():
    value = 'testemaifai%6&8lworkings@magil.com'
    schema = Schema().set_format(StringFormat.EMAIL)

    assert StringValidator.validate([], schema, value) == value


@pytest.mark.asyncio
async def test_string_custom_message():
    schema = Schema.from_value({
        'type': 'STRING',
        'minLength': 10,
        'details': {
            'validationMessages': {
                'minLength': 'You must enter something with minimum of ten characters'
            }
        }
    })

    with pytest.raises(Exception) as exc_info:
        await SchemaValidator.validate([], schema, None, 'asdf')

    assert 'You must enter something with minimum of ten characters' in str(exc_info.value)


def test_date_rejects_month_zero():
    # The month alternation was ([0][0-9]|[1][0-2]), so 00 matched the first
    # branch. 2026-00-15 validated on all three runtimes and then failed wherever
    # something tried to make a real date out of it.
    date = Schema().set_format(StringFormat.DATE)

    for bad in ['2026-00-15', '2026-00-01']:
        with pytest.raises(Exception):
            StringValidator.validate([], date, bad)

    for good in ['2026-01-15', '2026-09-30', '2026-10-01', '2026-12-31']:
        assert StringValidator.validate([], date, good) == good

    date_time = Schema().set_format(StringFormat.DATETIME)
    with pytest.raises(Exception):
        StringValidator.validate([], date_time, '2026-00-15T10:00:00Z')
