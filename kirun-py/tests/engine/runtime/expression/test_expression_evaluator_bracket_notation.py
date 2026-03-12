from __future__ import annotations

from typing import Any, List

import pytest

from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor


class ContextExtractor(TokenValueExtractor):
    """Test extractor for bracket notation tests (Context. prefix)."""

    def __init__(self, data: Any):
        super().__init__()
        self._data = data

    def get_value_internal(self, token: str) -> Any:
        prefix = self.get_prefix()
        path = token[len(prefix):]
        parts = TokenValueExtractor.split_path(path)
        return self.retrieve_element_from(token, parts, 0, self._data)

    def get_prefix(self) -> str:
        return 'Context.'

    def get_store(self) -> Any:
        return self._data


class TestBracketNotationBasic:
    """Ported from ExpressionEvaluatorBracketNotationTest.ts"""

    @pytest.fixture
    def setup(self):
        test_data = {
            'obj': {
                'mail.props.port': 587,
                'mail.props.host': 'smtp.example.com',
                'api.key.secret': 'secret123',
                'simple': 'value',
                'count': 100,
            },
            'arr': [10, 20, 30, 40, 50],
            'nested': {
                'field.with.dots': 'nestedValue',
                'regular': 'regularValue',
            },
        }
        extractor = ContextExtractor(test_data)
        evaluator_map = {'Context.': extractor}
        return test_data, extractor, evaluator_map

    def test_double_quote_access(self, setup):
        _, _, evaluator_map = setup
        result = ExpressionEvaluator('Context.obj["mail.props.port"]').evaluate(evaluator_map)
        assert result == 587

    def test_single_quote_access(self, setup):
        _, _, evaluator_map = setup
        result = ExpressionEvaluator("Context.obj['mail.props.host']").evaluate(evaluator_map)
        assert result == 'smtp.example.com'

    def test_nested_dotted_key(self, setup):
        _, _, evaluator_map = setup
        result = ExpressionEvaluator("Context.nested['field.with.dots']").evaluate(evaluator_map)
        assert result == 'nestedValue'


class TestBracketNotationComparison:
    """Comparison operators with bracket notation."""

    @pytest.fixture
    def evaluator_map(self):
        test_data = {
            'obj': {
                'mail.props.port': 587,
                'mail.props.host': 'smtp.example.com',
                'api.key.secret': 'secret123',
                'simple': 'value',
                'count': 100,
            },
            'arr': [10, 20, 30, 40, 50],
            'nested': {
                'field.with.dots': 'nestedValue',
                'regular': 'regularValue',
            },
        }
        extractor = ContextExtractor(test_data)
        return {'Context.': extractor}

    def test_equality(self, evaluator_map):
        result = ExpressionEvaluator('Context.obj["mail.props.port"] = 587').evaluate(evaluator_map)
        assert result is True

    def test_not_equal(self, evaluator_map):
        result = ExpressionEvaluator('Context.obj["mail.props.port"] != 500').evaluate(evaluator_map)
        assert result is True

    def test_greater_than(self, evaluator_map):
        result = ExpressionEvaluator('Context.obj["mail.props.port"] > 500').evaluate(evaluator_map)
        assert result is True

    def test_greater_than_equal(self, evaluator_map):
        result = ExpressionEvaluator('Context.obj["mail.props.port"] >= 587').evaluate(evaluator_map)
        assert result is True

    def test_less_than(self, evaluator_map):
        result = ExpressionEvaluator('Context.obj["mail.props.port"] < 600').evaluate(evaluator_map)
        assert result is True

    def test_less_than_equal(self, evaluator_map):
        result = ExpressionEvaluator('Context.obj["mail.props.port"] <= 587').evaluate(evaluator_map)
        assert result is True


class TestBracketNotationArithmetic:
    """Arithmetic operators with bracket notation."""

    @pytest.fixture
    def evaluator_map(self):
        test_data = {
            'obj': {
                'mail.props.port': 587,
                'count': 100,
            },
            'arr': [10, 20, 30, 40, 50],
        }
        extractor = ContextExtractor(test_data)
        return {'Context.': extractor}

    def test_addition(self, evaluator_map):
        result = ExpressionEvaluator('Context.obj["mail.props.port"] + 13').evaluate(evaluator_map)
        assert result == 600

    def test_subtraction(self, evaluator_map):
        result = ExpressionEvaluator('Context.obj["mail.props.port"] - 87').evaluate(evaluator_map)
        assert result == 500

    def test_multiplication(self, evaluator_map):
        result = ExpressionEvaluator('Context.obj["count"] * 2').evaluate(evaluator_map)
        assert result == 200

    def test_division(self, evaluator_map):
        result = ExpressionEvaluator('Context.obj["count"] / 4').evaluate(evaluator_map)
        assert result == 25


class TestBracketNotationTernary:
    """Ternary operator with bracket notation."""

    @pytest.fixture
    def evaluator_map(self):
        test_data = {
            'obj': {
                'mail.props.port': 587,
                'count': 100,
            },
        }
        extractor = ContextExtractor(test_data)
        return {'Context.': extractor}

    def test_ternary_true_branch(self, evaluator_map):
        result = ExpressionEvaluator(
            'Context.obj["mail.props.port"] > 500 ? "high" : "low"'
        ).evaluate(evaluator_map)
        assert result == 'high'

    def test_ternary_false_branch(self, evaluator_map):
        result = ExpressionEvaluator(
            'Context.obj["mail.props.port"] < 500 ? "high" : "low"'
        ).evaluate(evaluator_map)
        assert result == 'low'


class TestBracketNotationLogical:
    """Logical operators with bracket notation."""

    @pytest.fixture
    def evaluator_map(self):
        test_data = {
            'obj': {
                'mail.props.port': 587,
                'count': 100,
            },
        }
        extractor = ContextExtractor(test_data)
        return {'Context.': extractor}

    def test_logical_and(self, evaluator_map):
        result = ExpressionEvaluator(
            'Context.obj["mail.props.port"] > 500 and Context.obj["count"] = 100'
        ).evaluate(evaluator_map)
        assert result is True

    def test_logical_or(self, evaluator_map):
        result = ExpressionEvaluator(
            'Context.obj["mail.props.port"] < 500 or Context.obj["count"] = 100'
        ).evaluate(evaluator_map)
        assert result is True

    def test_logical_not(self, evaluator_map):
        result = ExpressionEvaluator(
            'not Context.obj["mail.props.port"] < 500'
        ).evaluate(evaluator_map)
        assert result is True


class TestBracketNotationMixed:
    """Mixed bracket and dot notation."""

    def test_bracket_then_dot(self):
        test_data = {
            'obj': {
                'mail.props.port': {'subfield': 'subvalue'},
            },
        }
        extractor = ContextExtractor(test_data)
        evaluator_map = {'Context.': extractor}

        result = ExpressionEvaluator(
            'Context.obj["mail.props.port"].subfield'
        ).evaluate(evaluator_map)
        assert result == 'subvalue'

    def test_dot_then_bracket(self):
        test_data = {
            'nested': {
                'field.with.dots': 'nestedValue',
            },
        }
        extractor = ContextExtractor(test_data)
        evaluator_map = {'Context.': extractor}

        result = ExpressionEvaluator(
            "Context.nested['field.with.dots']"
        ).evaluate(evaluator_map)
        assert result == 'nestedValue'


class TestBracketNotationArray:
    """Array bracket notation (pre-existing functionality)."""

    @pytest.fixture
    def evaluator_map(self):
        test_data = {
            'arr': [10, 20, 30, 40, 50],
        }
        extractor = ContextExtractor(test_data)
        return {'Context.': extractor}

    def test_array_index(self, evaluator_map):
        result = ExpressionEvaluator('Context.arr[0]').evaluate(evaluator_map)
        assert result == 10

    def test_array_index_comparison(self, evaluator_map):
        result = ExpressionEvaluator('Context.arr[0] = 10').evaluate(evaluator_map)
        assert result is True

    def test_array_index_addition(self, evaluator_map):
        result = ExpressionEvaluator('Context.arr[1] + Context.arr[2]').evaluate(evaluator_map)
        assert result == 50


class TestBracketNotationComplex:
    """Complex expressions with bracket notation."""

    @pytest.fixture
    def evaluator_map(self):
        test_data = {
            'obj': {
                'mail.props.port': 587,
                'mail.props.host': 'smtp.example.com',
                'count': 100,
            },
        }
        extractor = ContextExtractor(test_data)
        return {'Context.': extractor}

    def test_multiple_bracket_notations(self, evaluator_map):
        result = ExpressionEvaluator(
            'Context.obj["mail.props.port"] + Context.obj["count"]'
        ).evaluate(evaluator_map)
        assert result == 687

    def test_bracket_in_nested_expression(self, evaluator_map):
        result = ExpressionEvaluator(
            '(Context.obj["mail.props.port"] > 500) and (Context.obj["count"] < 200)'
        ).evaluate(evaluator_map)
        assert result is True

    def test_string_concatenation(self, evaluator_map):
        result = ExpressionEvaluator(
            'Context.obj["mail.props.host"] + ":587"'
        ).evaluate(evaluator_map)
        assert result == 'smtp.example.com:587'


class TestBracketNotationEdgeCases:
    """Edge cases for bracket notation."""

    def test_multiple_dots_in_key(self):
        test_data = {'obj': {'a.b.c.d': 'deepValue'}}
        extractor = ContextExtractor(test_data)
        evaluator_map = {'Context.': extractor}

        result = ExpressionEvaluator('Context.obj["a.b.c.d"]').evaluate(evaluator_map)
        assert result == 'deepValue'

    def test_empty_string_key(self):
        test_data = {'obj': {'': 'emptyKey'}}
        extractor = ContextExtractor(test_data)
        evaluator_map = {'Context.': extractor}

        result = ExpressionEvaluator('Context.obj[""]').evaluate(evaluator_map)
        assert result == 'emptyKey'

    def test_special_characters_key(self):
        test_data = {'obj': {'key@#$%': 'specialValue'}}
        extractor = ContextExtractor(test_data)
        evaluator_map = {'Context.': extractor}

        result = ExpressionEvaluator('Context.obj["key@#$%"]').evaluate(evaluator_map)
        assert result == 'specialValue'
