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


class TestRootLevelBracketAccess:
    """A token whose FIRST separator is a bracket used to match no extractor.

    The prefix is the text up to the first dot, so ``Context[0]`` produced an
    empty prefix and fell through to the literal extractor, which raises. In the
    UI that raise reached the root error boundary and replaced the whole page.
    ``Parent[0]`` was the case that surfaced it, from a repeater bound to
    ObjectEntries output, but it was never Parent-specific.
    """

    def test_numeric_index_on_the_extractor_root(self):
        evaluator_map = {'Context.': ContextExtractor(['zero', 'one', 'two'])}
        assert ExpressionEvaluator('Context[0]').evaluate(evaluator_map) == 'zero'
        assert ExpressionEvaluator('Context[2]').evaluate(evaluator_map) == 'two'

    def test_the_dotted_form_keeps_working(self):
        evaluator_map = {'Context.': ContextExtractor(['zero', 'one', 'two'])}
        assert ExpressionEvaluator('Context.0').evaluate(evaluator_map) == 'zero'

    def test_quoted_key_on_the_extractor_root(self):
        evaluator_map = {'Context.': ContextExtractor({'a.b': 'dotted', 'plain': 'p'})}
        assert ExpressionEvaluator('Context["a.b"]').evaluate(evaluator_map) == 'dotted'
        assert ExpressionEvaluator('Context["plain"]').evaluate(evaluator_map) == 'p'
        assert ExpressionEvaluator("Context['plain']").evaluate(evaluator_map) == 'p'

    def test_a_bracket_after_a_dotted_path_is_untouched(self):
        evaluator_map = {'Context.': ContextExtractor({'arr': [1, 2, 3]})}
        assert ExpressionEvaluator('Context.arr[1]').evaluate(evaluator_map) == 2


class TestBracketNotationOperatorCharacterKeys:
    """Keys whose characters collide with operators.

    `http-equiv` is the real case: it is one of the attributes a
    `properties.metas.<uid>` entry can carry in an app definition, and a hyphen
    is also subtraction. Mirrors the same block in kirun-js and kirun-java, so
    the three runtimes are held to one contract.
    """

    @pytest.fixture
    def evaluator_map(self):
        test_data = {
            'obj': {
                'key-with-hyphen': 'hyphenValue',
                'key with space': 'spaceValue',
                'key+plus': 'plusValue',
                'key:colon': 'colonValue',
                'key/slash': 'slashValue',
                'mail.props.port': 587,
                'a-b': 'literalAMinusB',
                'a': 10,
                'b': 3,
            },
            'metas': {'m1': {'http-equiv': 'X-UA-Compatible', 'content': 'IE=edge'}},
            'arr': ['first', 'second', 'third'],
            'i': 0,
            'k': 'm1',
        }
        extractor = ContextExtractor(test_data)
        return {'Context.': extractor}

    def test_hyphen_in_double_quoted_key(self, evaluator_map):
        assert ExpressionEvaluator(
            'Context.obj["key-with-hyphen"]'
        ).evaluate(evaluator_map) == 'hyphenValue'

    def test_hyphen_in_single_quoted_key(self, evaluator_map):
        assert ExpressionEvaluator(
            "Context.obj['key-with-hyphen']"
        ).evaluate(evaluator_map) == 'hyphenValue'

    def test_http_equiv_the_app_definition_meta_attribute(self, evaluator_map):
        assert ExpressionEvaluator(
            'Context.metas.m1["http-equiv"]'
        ).evaluate(evaluator_map) == 'X-UA-Compatible'

    def test_space_plus_colon_and_slash_in_key(self, evaluator_map):
        assert ExpressionEvaluator(
            'Context.obj["key with space"]'
        ).evaluate(evaluator_map) == 'spaceValue'
        assert ExpressionEvaluator('Context.obj["key+plus"]').evaluate(evaluator_map) == 'plusValue'
        assert ExpressionEvaluator('Context.obj["key:colon"]').evaluate(evaluator_map) == 'colonValue'
        assert ExpressionEvaluator('Context.obj["key/slash"]').evaluate(evaluator_map) == 'slashValue'

    def test_quoted_key_wins_over_the_arithmetic_it_looks_like(self, evaluator_map):
        assert ExpressionEvaluator(
            'Context.obj["a-b"]'
        ).evaluate(evaluator_map) == 'literalAMinusB'

    def test_the_same_key_unquoted_is_arithmetic_and_throws(self, evaluator_map):
        with pytest.raises(Exception):
            ExpressionEvaluator('Context.obj.key-with-hyphen').evaluate(evaluator_map)
        with pytest.raises(Exception):
            ExpressionEvaluator('Context.metas.m1.http-equiv').evaluate(evaluator_map)

    def test_chained_brackets_both_keys_hyphenated_or_dotted(self, evaluator_map):
        assert ExpressionEvaluator(
            "Context.metas['m1']['http-equiv']"
        ).evaluate(evaluator_map) == 'X-UA-Compatible'

    def test_bare_token_path_inside_brackets_resolves(self, evaluator_map):
        """A bare token path inside brackets, with no operator forcing evaluation.

        This is the form that used to resolve here and in kirun-js but silently
        miss in kirun-java, so the same expression behaved differently in a UI
        function and a server function.
        """
        assert ExpressionEvaluator(
            'Context.metas[Context.k].content'
        ).evaluate(evaluator_map) == 'IE=edge'
        assert ExpressionEvaluator(
            "Context.metas[Context.k]['http-equiv']"
        ).evaluate(evaluator_map) == 'X-UA-Compatible'
        assert ExpressionEvaluator('Context.arr[Context.i]').evaluate(evaluator_map) == 'first'

    def test_token_path_inside_brackets_with_arithmetic(self, evaluator_map):
        assert ExpressionEvaluator('Context.arr[Context.i + 1]').evaluate(evaluator_map) == 'second'

    def test_bare_token_path_that_resolves_to_nothing_misses_quietly(self, evaluator_map):
        assert ExpressionEvaluator(
            'Context.metas[Context.nosuch].content'
        ).evaluate(evaluator_map) is None

    def test_an_expression_inside_brackets_is_not_evaluated(self, evaluator_map):
        """LIMITATION, asserted so a fix surfaces as a failing test rather than
        going unnoticed: a bracket segment is a literal or a token path, never an
        expression. A concatenation inside brackets is taken as the key text
        itself and silently misses. All three runtimes behave this way.
        """
        assert ExpressionEvaluator(
            "Context.metas['m' + '1'].content"
        ).evaluate(evaluator_map) is None
