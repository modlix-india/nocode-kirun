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
