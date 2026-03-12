from __future__ import annotations

import pytest

from kirun_py.runtime.expression.operators.unary.logical_not_operator import LogicalNotOperator
from kirun_py.runtime.expression.operators.binary.logical_greater_than_operator import LogicalGreaterThanOperator
from kirun_py.runtime.expression.operators.binary.logical_less_than_operator import LogicalLessThanOperator
from kirun_py.runtime.expression.operators.binary.logical_greater_than_equal_operator import LogicalGreaterThanEqualOperator
from kirun_py.runtime.expression.operators.binary.logical_less_than_equal_operator import LogicalLessThanEqualOperator


class TestLogicalNotOperator:
    """Ported from LogicalNotOperatorTest.ts"""

    def test_not_none(self):
        assert LogicalNotOperator().apply(None)

    def test_not_false(self):
        assert LogicalNotOperator().apply(False)

    def test_not_zero(self):
        assert LogicalNotOperator().apply(0)

    def test_not_true(self):
        assert not LogicalNotOperator().apply(True)

    def test_not_one(self):
        assert not LogicalNotOperator().apply(1)

    def test_not_object(self):
        assert not LogicalNotOperator().apply({'name': 'Kiran'})

    def test_not_empty_list(self):
        assert not LogicalNotOperator().apply([])

    def test_not_empty_string(self):
        assert not LogicalNotOperator().apply('')

    def test_not_truthy_string(self):
        assert not LogicalNotOperator().apply('TRUE')


class TestLogicalComparisonOperators:
    """Ported from LogicalNotOperatorTest.ts - comparison tests."""

    def test_greater_than_false(self):
        assert not LogicalGreaterThanOperator().apply(0, 4)

    def test_less_than_true(self):
        assert LogicalLessThanOperator().apply(0, 4)

    def test_greater_than_equal_same(self):
        assert LogicalGreaterThanEqualOperator().apply(0, 0)

    def test_less_than_equal_same(self):
        assert LogicalLessThanEqualOperator().apply(0, 0)
