"""
Ported from:
  - BooleanConvertorTest.ts
  - NullConvertorTest.ts
  - NumberConvertorTest.ts
  - StringConvertorTest.ts

Note: The Python convertor API is simplified compared to TypeScript.
The Python convertors do not accept `mode`, `schema`, or `parents` parameters.
Tests are adapted to the actual Python API.
"""
from __future__ import annotations

import pytest

from kirun_py.json.schema.convertor.boolean_convertor import BooleanConvertor
from kirun_py.json.schema.convertor.null_convertor import NullConvertor
from kirun_py.json.schema.convertor.number_convertor import NumberConvertor
from kirun_py.json.schema.convertor.string_convertor import StringConvertor


# ---------------------------------------------------------------------------
# BooleanConvertor tests (ported from BooleanConvertorTest.ts)
# ---------------------------------------------------------------------------

class TestBooleanConvertor:

    def test_convert_null_returns_none(self):
        """null input -> None (Python: None, not exception like TS strict mode)"""
        assert BooleanConvertor.convert(None) is None

    def test_convert_true_string(self):
        assert BooleanConvertor.convert('true') is True

    def test_convert_false_string(self):
        assert BooleanConvertor.convert('false') is False

    def test_convert_one_as_number(self):
        assert BooleanConvertor.convert(1) is True

    def test_convert_zero_as_number(self):
        assert BooleanConvertor.convert(0) is False

    def test_convert_invalid_string_returns_none(self):
        """invalid strings return None (lenient behaviour in Python)"""
        assert BooleanConvertor.convert('invalid') is None

    def test_convert_true_bool(self):
        assert BooleanConvertor.convert(True) is True

    def test_convert_false_bool(self):
        assert BooleanConvertor.convert(False) is False

    def test_convert_yes_string(self):
        assert BooleanConvertor.convert('yes') is True

    def test_convert_no_string(self):
        assert BooleanConvertor.convert('no') is False

    def test_convert_y_string(self):
        assert BooleanConvertor.convert('y') is True

    def test_convert_n_string(self):
        assert BooleanConvertor.convert('n') is False

    def test_convert_t_string(self):
        assert BooleanConvertor.convert('t') is True

    def test_convert_f_string(self):
        assert BooleanConvertor.convert('f') is False

    def test_convert_string_one(self):
        assert BooleanConvertor.convert('1') is True

    def test_convert_string_zero(self):
        assert BooleanConvertor.convert('0') is False

    def test_convert_non_bool_number_returns_none(self):
        """Numbers other than 0 and 1 should return None"""
        assert BooleanConvertor.convert(2) is None
        assert BooleanConvertor.convert(-1) is None

    def test_convert_dict_returns_none(self):
        assert BooleanConvertor.convert({'key': 'value'}) is None

    def test_convert_list_returns_none(self):
        assert BooleanConvertor.convert([True, False]) is None


# ---------------------------------------------------------------------------
# NullConvertor tests (ported from NullConvertorTest.ts)
# ---------------------------------------------------------------------------

class TestNullConvertor:

    def test_convert_none_strict(self):
        assert NullConvertor.convert(None) is None

    def test_convert_none_json_element(self):
        """null JSON element -> None"""
        assert NullConvertor.convert(None) is None

    def test_convert_string_null(self):
        assert NullConvertor.convert('null') is None

    def test_convert_invalid_string_raises(self):
        """invalid non-null string raises ValueError"""
        with pytest.raises(Exception):
            NullConvertor.convert('invalid')

    def test_convert_null_string_case_insensitive(self):
        assert NullConvertor.convert('NULL') is None
        assert NullConvertor.convert('Null') is None

    def test_convert_integer_raises(self):
        with pytest.raises(Exception):
            NullConvertor.convert(42)

    def test_convert_empty_string_raises(self):
        with pytest.raises(Exception):
            NullConvertor.convert('')

    def test_convert_dict_raises(self):
        with pytest.raises(Exception):
            NullConvertor.convert({'key': 'value'})


# ---------------------------------------------------------------------------
# NumberConvertor tests (ported from NumberConvertorTest.ts)
# ---------------------------------------------------------------------------

class TestNumberConvertor:

    def test_convert_null_returns_none(self):
        assert NumberConvertor.convert(None) is None

    def test_convert_non_number_string_returns_none(self):
        assert NumberConvertor.convert('not a number') is None

    def test_convert_valid_integer(self):
        assert NumberConvertor.convert(42) == 42

    def test_convert_valid_float(self):
        assert NumberConvertor.convert(42.0) == 42.0

    def test_convert_integer_string(self):
        assert NumberConvertor.convert('42') == 42

    def test_convert_float_string(self):
        assert NumberConvertor.convert('42.0') == 42.0

    def test_convert_bool_returns_none(self):
        """bool values are rejected in Python convertor"""
        assert NumberConvertor.convert(True) is None
        assert NumberConvertor.convert(False) is None

    def test_convert_dict_returns_none(self):
        assert NumberConvertor.convert({'key': 'value'}) is None

    def test_convert_list_returns_none(self):
        assert NumberConvertor.convert([1, 2, 3]) is None

    def test_convert_negative_integer(self):
        assert NumberConvertor.convert(-10) == -10

    def test_convert_negative_float_string(self):
        result = NumberConvertor.convert('-3.14')
        assert result == pytest.approx(-3.14)

    def test_convert_large_number(self):
        assert NumberConvertor.convert(11192371231) == 11192371231

    def test_convert_large_number_string(self):
        assert NumberConvertor.convert('11192371231') == 11192371231

    def test_convert_float_string_precision(self):
        result = NumberConvertor.convert('1123.123')
        assert result == pytest.approx(1123.123)

    def test_convert_zero(self):
        assert NumberConvertor.convert(0) == 0

    def test_convert_zero_string(self):
        assert NumberConvertor.convert('0') == 0


# ---------------------------------------------------------------------------
# StringConvertor tests (ported from StringConvertorTest.ts)
# ---------------------------------------------------------------------------

class TestStringConvertor:

    def test_convert_valid_string(self):
        assert StringConvertor.convert('test string') == 'test string'

    def test_convert_null_returns_none(self):
        """null input returns None in Python (lenient, not exception like TS strict)"""
        assert StringConvertor.convert(None) is None

    def test_convert_empty_string(self):
        assert StringConvertor.convert('') == ''

    def test_convert_integer_to_string(self):
        assert StringConvertor.convert(123) == '123'

    def test_convert_float_to_string(self):
        assert StringConvertor.convert(3.14) == '3.14'

    def test_convert_bool_to_string(self):
        assert StringConvertor.convert(True) == 'True'
        assert StringConvertor.convert(False) == 'False'

    def test_convert_dict_returns_none(self):
        """dicts are not convertible to string, returns None"""
        assert StringConvertor.convert({'key': 'val'}) is None

    def test_convert_list_returns_none(self):
        """lists are not convertible to string, returns None"""
        assert StringConvertor.convert([1, 2, 3]) is None

    def test_convert_zero_to_string(self):
        assert StringConvertor.convert(0) == '0'
