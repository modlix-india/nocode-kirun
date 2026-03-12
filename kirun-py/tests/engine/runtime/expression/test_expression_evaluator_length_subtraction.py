from __future__ import annotations

from typing import Any

import pytest

from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor
from kirun_py.util.map_util import MapUtil


class PageExtractor(TokenValueExtractor):
    """Page-prefixed test extractor."""

    def __init__(self, store: Any):
        super().__init__()
        self._store = store

    def get_value_internal(self, token: str) -> Any:
        parts = token.split('.')
        return self.retrieve_element_from(token, parts, 1, self._store)

    def get_prefix(self) -> str:
        return 'Page.'

    def get_store(self) -> Any:
        return self._store


class TestLengthSubtractionBasic:
    """Ported from ExpressionEvaluatorLengthSubtractionTest.ts"""

    def test_array_length_alone(self):
        ttv = PageExtractor({'conditions': [1, 2, 3, 4, 5]})
        ev = ExpressionEvaluator('Page.conditions.length')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 5

    def test_array_length_minus_one_with_spaces(self):
        ttv = PageExtractor({'conditions': [1, 2, 3, 4, 5]})
        ev = ExpressionEvaluator('Page.conditions.length - 1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 4

    def test_array_length_minus_one_without_spaces(self):
        ttv = PageExtractor({'conditions': [1, 2, 3, 4, 5]})
        ev = ExpressionEvaluator('Page.conditions.length-1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 4

    def test_nested_array_length_minus_one_without_spaces(self):
        ttv = PageExtractor({
            'mainFilterCondition': {
                'condition': {
                    'conditions': [
                        {'conditions': [1, 2, 3, 4]},
                        {'conditions': [5, 6]},
                    ],
                },
            },
        })
        ev = ExpressionEvaluator(
            'Page.mainFilterCondition.condition.conditions[0].conditions.length-1'
        )
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 3

    def test_nested_array_length_minus_one_with_spaces(self):
        ttv = PageExtractor({
            'mainFilterCondition': {
                'condition': {
                    'conditions': [
                        {'conditions': [1, 2, 3, 4]},
                        {'conditions': [5, 6]},
                    ],
                },
            },
        })
        ev = ExpressionEvaluator(
            'Page.mainFilterCondition.condition.conditions[0].conditions.length - 1'
        )
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 3

    def test_array_length_with_addition(self):
        ttv = PageExtractor({'items': [1, 2, 3]})
        ev = ExpressionEvaluator('Page.items.length+2')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 5

    def test_array_length_with_multiplication(self):
        ttv = PageExtractor({'items': [1, 2, 3]})
        ev = ExpressionEvaluator('Page.items.length*2')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 6

    def test_array_length_complex_expression(self):
        ttv = PageExtractor({
            'arr1': [1, 2, 3],
            'arr2': [4, 5, 6, 7],
        })
        ev = ExpressionEvaluator('Page.arr1.length + Page.arr2.length - 1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 6

    def test_length_minus_one_as_array_index(self):
        ttv = PageExtractor({'items': [10, 20, 30, 40, 50]})
        ev = ExpressionEvaluator('Page.items[Page.items.length - 1]')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 50

    def test_length_minus_one_as_array_index_no_spaces(self):
        ttv = PageExtractor({'items': [10, 20, 30, 40, 50]})
        ev = ExpressionEvaluator('Page.items[Page.items.length-1]')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 50

    def test_nested_object_with_array_length(self):
        ttv = PageExtractor({
            'data': {'nested': {'list': ['a', 'b', 'c', 'd']}},
        })
        ev = ExpressionEvaluator('Page.data.nested.list.length-1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 3

    def test_string_length(self):
        ttv = PageExtractor({'text': 'hello'})
        ev = ExpressionEvaluator('Page.text.length-1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 4

    def test_object_keys_length(self):
        ttv = PageExtractor({'obj': {'a': 1, 'b': 2, 'c': 3, 'd': 4}})
        ev = ExpressionEvaluator('Page.obj.length-1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 3

    def test_empty_array_length_minus_one(self):
        ttv = PageExtractor({'empty': []})
        ev = ExpressionEvaluator('Page.empty.length-1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == -1

    def test_multiple_length_operations(self):
        ttv = PageExtractor({
            'list1': [1, 2, 3],
            'list2': [4, 5],
        })
        ev = ExpressionEvaluator('(Page.list1.length-1) * (Page.list2.length-1)')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 2

    def test_length_with_division(self):
        ttv = PageExtractor({'items': [1, 2, 3, 4, 5, 6]})
        ev = ExpressionEvaluator('Page.items.length/2')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 3

    def test_length_with_modulus(self):
        ttv = PageExtractor({'items': [1, 2, 3, 4, 5]})
        ev = ExpressionEvaluator('Page.items.length%3')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 2

    def test_length_greater_than_zero(self):
        ttv = PageExtractor({'items': [1, 2, 3]})
        ev = ExpressionEvaluator('Page.items.length > 0')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) is True


class TestLengthSubtractionBug:
    """Ported from ExpressionEvaluatorLengthSubtractionBugTest.ts"""

    def test_exact_bug_report_case(self):
        ttv = PageExtractor({
            'mainFilterCondition': {
                'condition': {
                    'conditions': [
                        {'conditions': [1, 2, 3, 4]},
                        {'conditions': [5, 6]},
                    ],
                },
            },
        })
        ev = ExpressionEvaluator(
            'Page.mainFilterCondition.condition.conditions[0].conditions.length-1'
        )
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 3

    def test_object_with_length_property_that_is_object(self):
        ttv = PageExtractor({
            'obj': {'length': {'nested': 'object'}},
        })
        ev = ExpressionEvaluator('Page.obj.length-1')
        # Should return Object.keys(obj).length - 1 = 1 - 1 = 0
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 0

    def test_object_with_length_property_that_is_number(self):
        ttv = PageExtractor({
            'obj': {'length': 5},
        })
        ev = ExpressionEvaluator('Page.obj.length-1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 4

    def test_deeply_nested_structure(self):
        ttv = PageExtractor({
            'level1': {
                'level2': {
                    'level3': {
                        'items': [
                            {'subItems': ['a', 'b', 'c', 'd', 'e']},
                            {'subItems': ['f', 'g']},
                        ],
                    },
                },
            },
        })
        ev = ExpressionEvaluator(
            'Page.level1.level2.level3.items[0].subItems.length-1'
        )
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 4

    def test_multiple_length_ops(self):
        ttv = PageExtractor({
            'arr1': [1, 2, 3],
            'arr2': [4, 5, 6, 7, 8],
        })
        ev = ExpressionEvaluator('Page.arr1.length-1 + Page.arr2.length-1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 6

    def test_length_minus_one_as_index(self):
        ttv = PageExtractor({'items': [10, 20, 30, 40, 50]})
        ev = ExpressionEvaluator('Page.items[Page.items.length-1]')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 50

    def test_string_length_subtraction(self):
        ttv = PageExtractor({'text': 'hello world'})
        ev = ExpressionEvaluator('Page.text.length-1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 10

    def test_empty_array_length_minus_one(self):
        ttv = PageExtractor({'empty': []})
        ev = ExpressionEvaluator('Page.empty.length-1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == -1

    def test_object_no_length_property(self):
        ttv = PageExtractor({'obj': {'a': 1, 'b': 2, 'c': 3}})
        ev = ExpressionEvaluator('Page.obj.length-1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 2

    def test_intermediate_object_has_length(self):
        ttv = PageExtractor({
            'mainFilterCondition': {
                'condition': {
                    'length': {'conflict': True},
                    'conditions': [
                        {'conditions': [1, 2, 3, 4]},
                    ],
                },
            },
        })
        ev = ExpressionEvaluator(
            'Page.mainFilterCondition.condition.conditions[0].conditions.length-1'
        )
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 3


class TestLengthSubtractionAlternatives:
    """Alternative patterns with parentheses."""

    def test_with_parentheses_and_spaces(self):
        ttv = PageExtractor({'items': [1, 2, 3]})
        ev = ExpressionEvaluator('(Page.items.length - 1) > 0')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) is True

    def test_with_parentheses_no_spaces(self):
        ttv = PageExtractor({'items': [1, 2, 3]})
        ev = ExpressionEvaluator('(Page.items.length-1) > 0')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) is True

    def test_literal_subtraction_and_comparison(self):
        ttv = PageExtractor({})
        ev = ExpressionEvaluator('(5 - 1) > 0')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) is True

    def test_chained_subtraction(self):
        ttv = PageExtractor({})
        ev = ExpressionEvaluator('(5 - 1) - 2')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 2

    def test_length_in_ternary(self):
        ttv = PageExtractor({'items': [1, 2, 3, 4]})
        ev = ExpressionEvaluator("(Page.items.length - 1) > 2 ? 'big' : 'small'")
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 'big'

    def test_subtraction_equality(self):
        ttv = PageExtractor({'items': [1, 2, 3]})
        ev = ExpressionEvaluator('(Page.items.length - 1) = 2')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) is True


class TestLengthSubtractionFixed:
    """Previously known issues now fixed."""

    def test_comparison_without_parentheses(self):
        ttv = PageExtractor({'items': [1, 2, 3]})
        ev = ExpressionEvaluator('Page.items.length - 1 > 0')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) is True

    def test_simple_subtraction_comparison(self):
        ttv = PageExtractor({})
        ev = ExpressionEvaluator('5 - 1 > 0')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) is True

    def test_chained_subtraction_associativity(self):
        ttv = PageExtractor({})
        ev = ExpressionEvaluator('5 - 1 - 2')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 2

    def test_subtraction_equality_without_parentheses(self):
        ttv = PageExtractor({'items': [1, 2, 3]})
        ev = ExpressionEvaluator('Page.items.length - 1 = 2')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) is True

    def test_length_ternary_without_parentheses(self):
        ttv = PageExtractor({'items': [1, 2, 3, 4]})
        ev = ExpressionEvaluator("Page.items.length - 1 > 2 ? 'big' : 'small'")
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 'big'
