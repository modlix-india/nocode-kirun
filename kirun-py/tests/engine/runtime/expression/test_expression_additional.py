"""
Ported from:
  - ExpressionArrayStringIndexing.ts
  - ExpressionEqualityTest.ts
  - ExpressionEvaluatorStringLiteralTest.ts
  - ExpressionEvaluatorTernaryOperatorTest.ts
  - ExpressionParsingTest.ts
"""
from __future__ import annotations

from typing import Any, Dict

import pytest

from kirun_py.runtime.expression.expression import Expression
from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor
from kirun_py.runtime.tokenextractor.arguments_token_value_extractor import ArgumentsTokenValueExtractor
from kirun_py.runtime.tokenextractor.output_map_token_value_extractor import OutputMapTokenValueExtractor
from kirun_py.util.map_util import MapUtil


# ---------------------------------------------------------------------------
# Helper classes
# ---------------------------------------------------------------------------

class TestTokenValueExtractor(TokenValueExtractor):
    """Generic token extractor with configurable prefix, ported from TestTokenValueExtractor.ts"""

    def __init__(self, prefix: str, store: Any):
        super().__init__()
        self._prefix = prefix
        self._store = store

    def get_value_internal(self, token: str) -> Any:
        parts = token.split('.')
        return self.retrieve_element_from(token, parts, 1, self._store)

    def get_prefix(self) -> str:
        return self._prefix

    def get_store(self) -> Any:
        return self._store


class StepsTokenValueExtractor(TokenValueExtractor):
    """Steps extractor mirroring the TS StepsTokenValueExtractor from string literal test."""

    def __init__(self, data: Dict[str, Any]):
        super().__init__()
        self._data = data

    def get_value_internal(self, token: str) -> Any:
        parts = TokenValueExtractor.split_path(token)
        key = parts[1]
        b_index = key.find('[')
        from_index = 2
        if b_index != -1:
            key = parts[1][:b_index]
            parts = list(parts)
            parts[1] = parts[1][b_index:]
            from_index = 1
        return self.retrieve_element_from(token, parts, from_index, self._data.get(key))

    def get_prefix(self) -> str:
        return 'Steps.'

    def get_store(self) -> Any:
        return {k: v for k, v in self._data.items()}


# ---------------------------------------------------------------------------
# ExpressionArrayStringIndexing (ported from ExpressionArrayStringIndexing.ts)
# ---------------------------------------------------------------------------

class TestExpressionArrayStringIndexing:

    def _build_values_map(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'arr': [0, 1, 2, 3, 4, 5, 6],
        })
        return MapUtil.of(atv.get_prefix(), atv)

    def test_string_access(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a').evaluate(vm) == 'kirun '

    def test_string_positive_index(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a[2]').evaluate(vm) == 'r'

    def test_string_negative_index(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a[-2]').evaluate(vm) == 'n'

    def test_array_slice(self):
        vm = self._build_values_map()
        result = ExpressionEvaluator('Arguments.arr[2..4]').evaluate(vm)
        assert result == [2, 3]

    def test_string_slice(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a[2..4]').evaluate(vm) == 'ru'

    def test_string_slice_with_expression(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a[(4-2)..(6-2)]').evaluate(vm) == 'ru'

    def test_string_slice_from_start(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a[..4]').evaluate(vm) == 'kiru'

    def test_string_slice_to_end(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a[2..]').evaluate(vm) == 'run '

    def test_array_slice_from_start(self):
        vm = self._build_values_map()
        result = ExpressionEvaluator('Arguments.arr[..4]').evaluate(vm)
        assert result == [0, 1, 2, 3]

    def test_array_slice_with_expression(self):
        vm = self._build_values_map()
        result = ExpressionEvaluator('Arguments.arr[(4-2)..7]').evaluate(vm)
        assert result == [2, 3, 4, 5, 6]

    def test_string_slice_to_negative(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a[..-4]').evaluate(vm) == 'ki'

    def test_string_slice_to_negative_expression(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a[..(-8+4)]').evaluate(vm) == 'ki'

    def test_string_slice_negative_both(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a[-4..-1]').evaluate(vm) == 'run'

    def test_string_slice_negative_to_end(self):
        vm = self._build_values_map()
        assert ExpressionEvaluator('Arguments.a[-4..]').evaluate(vm) == 'run '

    def test_array_slice_to_negative(self):
        vm = self._build_values_map()
        result = ExpressionEvaluator('Arguments.arr[..-1]').evaluate(vm)
        assert result == [0, 1, 2, 3, 4, 5]

    def test_array_slice_negative_to_end(self):
        vm = self._build_values_map()
        result = ExpressionEvaluator('Arguments.arr[-2..]').evaluate(vm)
        assert result == [5, 6]


# ---------------------------------------------------------------------------
# ExpressionEqualityTest (ported from ExpressionEqualityTest.ts)
# ---------------------------------------------------------------------------

class TestExpressionEquality:

    def _build_values_map(self):
        obj = {
            'number': 20,
            'zero': 0,
            'booleanTrue': True,
            'booleanFalse': False,
            'string': 'Hello',
            'emptyString': '',
            'nullValue': None,
            'emptyObject': {},
            'emptyArray': [],
        }

        in_map = {'name': 'Kiran', 'obj': obj}
        output = {'step1': {'output': in_map}}

        omtv = OutputMapTokenValueExtractor(output)
        return {omtv.get_prefix(): omtv}

    def test_number_not_equal_zero(self):
        vm = self._build_values_map()
        exp = ExpressionEvaluator(
            'Steps.step1.output.obj.number = Steps.step1.output.obj.zero'
        )
        assert exp.evaluate(vm) is False

    def test_boolean_false_equals_not_number(self):
        vm = self._build_values_map()
        exp = ExpressionEvaluator(
            'Steps.step1.output.obj.booleanFalse = (not Steps.step1.output.obj.number)'
        )
        assert exp.evaluate(vm) is True

    def test_boolean_false_equals_not_empty_string(self):
        vm = self._build_values_map()
        exp = ExpressionEvaluator(
            'Steps.step1.output.obj.booleanFalse = (not Steps.step1.output.obj.emptyString)'
        )
        assert exp.evaluate(vm) is True

    def test_boolean_true_equals_not_zero(self):
        vm = self._build_values_map()
        exp = ExpressionEvaluator(
            'Steps.step1.output.obj.booleanTrue = (not Steps.step1.output.obj.zero)'
        )
        assert exp.evaluate(vm) is True

    def test_empty_string_equals_literal(self):
        vm = self._build_values_map()
        exp = ExpressionEvaluator("Steps.step1.output.obj.emptyString = ''")
        assert exp.evaluate(vm) is True

    def test_empty_string_not_equals_literal(self):
        vm = self._build_values_map()
        exp = ExpressionEvaluator("Steps.step1.output.obj.emptyString != ''")
        assert exp.evaluate(vm) is False

    def test_string_not_equals_empty(self):
        vm = self._build_values_map()
        exp = ExpressionEvaluator("Steps.step1.output.obj.string != ''")
        assert exp.evaluate(vm) is True

    def test_string_not_equals_empty_reversed(self):
        vm = self._build_values_map()
        exp = ExpressionEvaluator("Steps.step1.output.obj.string = ''")
        assert exp.evaluate(vm) is False


# ---------------------------------------------------------------------------
# ExpressionEvaluatorStringLiteralTest (ported from ExpressionEvaluatorStringLiteralTest.ts)
# ---------------------------------------------------------------------------

class TestExpressionStringLiteral:

    def _build_args_values_map(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        return MapUtil.of(atv.get_prefix(), atv)

    def test_string_literal_concatenation(self):
        ex = Expression("'ki/run'+'ab'")
        ev = ExpressionEvaluator(ex)
        assert ev.evaluate(MapUtil.of()) == 'ki/runab'

    def test_unterminated_string_raises(self):
        evt = ExpressionEvaluator('"Steps.a')
        vm = self._build_args_values_map()
        with pytest.raises(Exception):
            evt.evaluate(vm)

    def test_argument_plus_string_literal(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator("Arguments.a+'kiran'")
        assert ev.evaluate(vm) == 'kirun kiran'

    def test_number_plus_string_literal(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator("Arguments.b+'kiran'")
        assert ev.evaluate(vm) == '2kiran'

    def test_bool_plus_string_with_quotes(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator("Arguments.c+'k\"ir\"an'")
        assert ev.evaluate(vm) == 'truek"ir"an'

    def test_number_plus_mixed_quotes(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator("Arguments.b+\"'kir\" + ' an'")
        assert ev.evaluate(vm) == "2'kir an"

    def test_argument_plus_string_plus_argument(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator("Arguments.a+'kiran'+ Arguments.b")
        assert ev.evaluate(vm) == 'kirun kiran2'

    def test_string_multiply(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator("'a' * 10")
        assert ev.evaluate(vm) == 'aaaaaaaaaa'

    def test_float_multiply_string(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator('2.5*Arguments.a')
        assert ev.evaluate(vm) == 'kirun kirun kir'

    def test_negative_float_multiply_string(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator('-0.5*Arguments.a')
        assert ev.evaluate(vm) == 'rik'

    def test_string_multiply_negative(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator("'asdf' * -1")
        assert ev.evaluate(vm) == 'fdsa'

    def test_string_multiply_zero(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator("'asdf' * 0")
        assert ev.evaluate(vm) == ''

    def test_invalid_path_returns_none(self):
        vm = self._build_args_values_map()
        ev = ExpressionEvaluator('2.val')
        assert ev.evaluate(vm) is None

    def test_string_length(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 'hello', 'b': ''},
            'd': 1.5,
        })
        vm = MapUtil.of(atv.get_prefix(), atv)
        ev = ExpressionEvaluator('Arguments.a.length')
        assert ev.evaluate(vm) == 6

    def test_number_length_raises(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 'hello', 'b': ''},
            'd': 1.5,
        })
        vm = MapUtil.of(atv.get_prefix(), atv)
        ev = ExpressionEvaluator('Arguments.b.length')
        with pytest.raises(Exception):
            ev.evaluate(vm)

    def test_string_length_multiply_string(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 'hello', 'b': ''},
            'd': 1.5,
        })
        vm = MapUtil.of(atv.get_prefix(), atv)
        ev = ExpressionEvaluator('Arguments.c.a.length * "f"')
        assert ev.evaluate(vm) == 'fffff'

    def test_empty_string_length_ternary(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 'hello', 'b': ''},
            'd': 1.5,
        })
        vm = MapUtil.of(atv.get_prefix(), atv)
        ev = ExpressionEvaluator('Arguments.c.b.length ? "f" : "t"')
        assert ev.evaluate(vm) == 't'

    def test_string_length_with_object(self):
        json_obj = {'greeting': 'hello', 'name': 'surendhar'}
        atv = ArgumentsTokenValueExtractor({
            'a': 'surendhar ',
            'b': 2,
            'c': True,
            'd': 1.5,
            'obj': json_obj,
        })
        vm = MapUtil.of(atv.get_prefix(), atv)

        assert ExpressionEvaluator('Arguments.a.length').evaluate(vm) == 10
        with pytest.raises(Exception):
            ExpressionEvaluator('Arguments.b.length').evaluate(vm)
        assert ExpressionEvaluator('Arguments.obj.greeting.length * "S"').evaluate(vm) == 'SSSSS'
        assert ExpressionEvaluator('Arguments.obj.greeting.length * "SP"').evaluate(vm) == 'SPSPSPSPSP'
        assert ExpressionEvaluator('Arguments.obj.name.length ? "fun" : "not Fun"').evaluate(vm) == 'fun'

    def test_string_length_with_square_brackets(self):
        json_obj = {'greeting': 'hello', 'name': 'surendhar'}
        atv = ArgumentsTokenValueExtractor({
            'a': 'surendhar ',
            'b': 2,
            'c': True,
            'd': 1.5,
            'obj': json_obj,
        })
        vm = MapUtil.of(atv.get_prefix(), atv)

        assert ExpressionEvaluator('Arguments.a["length"]').evaluate(vm) == 10
        with pytest.raises(Exception):
            ExpressionEvaluator('Arguments.b["length"]').evaluate(vm)
        assert ExpressionEvaluator('Arguments.obj.greeting["length"] * "S"').evaluate(vm) == 'SSSSS'
        assert ExpressionEvaluator('Arguments.obj.greeting["length"] * "SP"').evaluate(vm) == 'SPSPSPSPSP'
        assert ExpressionEvaluator('Arguments.obj["greeting"]["length"] * "S"').evaluate(vm) == 'SSSSS'
        assert ExpressionEvaluator('Arguments.obj["greeting"]["length"] * "SP"').evaluate(vm) == 'SPSPSPSPSP'
        assert ExpressionEvaluator('Arguments.obj.name["length"] ? "fun" : "not Fun"').evaluate(vm) == 'fun'

    def test_template_interpolation_in_string_literal(self):
        steps_atv = StepsTokenValueExtractor({
            'countLoop': {'iteration': {'index': 1}},
            'index': 5,
        })
        args_atv = ArgumentsTokenValueExtractor({
            'a': 'test',
            'b': 10,
            'c': 15,
        })
        vm: Dict[str, TokenValueExtractor] = {
            'Steps.': steps_atv,
            'Arguments.': args_atv,
        }

        ev = ExpressionEvaluator(
            "'Page.appDefinitions.content[{{Steps.countLoop.iteration.index}}].stringValue'"
        )
        assert ev.evaluate(vm) == 'Page.appDefinitions.content[1].stringValue'

        ev = ExpressionEvaluator(
            '"Page.appDefinitions.content[{{Steps.countLoop.iteration.index}}].stringValue"'
        )
        assert ev.evaluate(vm) == 'Page.appDefinitions.content[1].stringValue'

        ev = ExpressionEvaluator("Arguments.a + ' - ' + 'Path: {{Steps.index}}'")
        assert ev.evaluate(vm) == 'test - Path: 5'

        ev = ExpressionEvaluator("'{{Arguments.a}} + {{Arguments.b}} = {{Arguments.c}}'")
        assert ev.evaluate(vm) == 'test + 10 = 15'

        ev = ExpressionEvaluator("'Result: {{Arguments.b + Arguments.c}}!'")
        assert ev.evaluate(vm) == 'Result: 25!'

        ev = ExpressionEvaluator("'Item {{Steps.countLoop.iteration.index}} of {{Arguments.c}}'")
        assert ev.evaluate(vm) == 'Item 1 of 15'


# ---------------------------------------------------------------------------
# ExpressionEvaluatorTernaryOperatorTest (ported from ExpressionEvaluatorTernaryOperatorTest.ts)
# ---------------------------------------------------------------------------

class TestTernaryOperator:

    def _build_values_map(self):
        x = {'a': 2, 'b': [True, False], 'c': {'x': 'Arguments.b2'}}
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'b1': 4,
            'b2': 4,
            'c': x,
            'd': 'c',
        })
        return MapUtil.of(atv.get_prefix(), atv), x

    def test_expression_ternary_parse_without_space(self):
        exp = Expression('a > 10 ?  a - 2 : a + 3'.replace(' ', ''))
        assert str(exp) == '((a>10)?(a-2):(a+3))'

    def test_expression_ternary_parse_with_space(self):
        exp = Expression('a > 10 ?  a - 2 : a + 3')
        assert str(exp) == '((a>10)?(a-2):(a+3))'

    def test_expression_nested_ternary_parse(self):
        exp = Expression('a > 10 ? a > 15 ? a + 2 : a - 2 : a + 3')
        assert str(exp) == '((a>10)?((a>15)?(a+2):(a-2)):(a+3))'

    def test_ternary_undefined_equals_null_returns_object_property(self):
        vm, _ = self._build_values_map()
        ev = ExpressionEvaluator('Arguments.e = null ? Arguments.c.a : 3 ')
        assert ev.evaluate(vm) == 2

    def test_ternary_missing_arg_returns_else(self):
        vm, _ = self._build_values_map()
        ev = ExpressionEvaluator('Arguments.f ? Arguments.c.a : 3 ')
        assert ev.evaluate(vm) == 3

    def test_ternary_null_eq_null_returns_object(self):
        vm, x = self._build_values_map()
        ev = ExpressionEvaluator('Arguments.e = null ? Arguments.c : 3 ')
        result = ev.evaluate(vm)
        assert result == x


# ---------------------------------------------------------------------------
# ExpressionParsingTest (ported from ExpressionParsingTest.ts)
# ---------------------------------------------------------------------------

class TestExpressionParsing:

    def test_multiplication_with_template(self):
        steps_ext = TestTokenValueExtractor('Steps.', {
            'floorWeekOne': {'output': {'value': 7}},
        })
        page_ext = TestTokenValueExtractor('Page.', {'secondsInDay': 86400})
        vm = {
            steps_ext.get_prefix(): steps_ext,
            page_ext.get_prefix(): page_ext,
        }

        expr = Expression('Steps.floorWeekOne.output.value * {{Page.secondsInDay}}')
        assert expr is not None
        assert not expr.get_operations().is_empty()

        ev = ExpressionEvaluator('Steps.floorWeekOne.output.value * {{Page.secondsInDay}}')
        assert ev.evaluate(vm) == 7 * 86400

    def test_nullish_coalescing_with_value(self):
        parent_ext = TestTokenValueExtractor('Parent.', {
            'projectInfo': {'projectType': 'Commercial'},
        })
        vm = {parent_ext.get_prefix(): parent_ext}

        expr = Expression("Parent.projectInfo.projectType?? '-'")
        assert expr is not None

        ev = ExpressionEvaluator("Parent.projectInfo.projectType ?? '-'")
        assert ev.evaluate(vm) == 'Commercial'

    def test_nullish_coalescing_missing_key(self):
        parent_ext = TestTokenValueExtractor('Parent.', {
            'projectInfo': {'projectType': 'Commercial'},
        })
        vm = {parent_ext.get_prefix(): parent_ext}
        ev = ExpressionEvaluator("Parent.projectInfo.projectType1?? '-'")
        assert ev.evaluate(vm) == '-'

    def test_nullish_coalescing_null_value(self):
        parent_ext = TestTokenValueExtractor('Parent.', {
            'projectInfo': {'projectType': None},
        })
        vm = {parent_ext.get_prefix(): parent_ext}
        ev = ExpressionEvaluator("Parent.projectInfo.projectType ?? '-'")
        assert ev.evaluate(vm) == '-'

    def test_nullish_coalescing_missing_property(self):
        parent_ext = TestTokenValueExtractor('Parent.', {'projectInfo': {}})
        vm = {parent_ext.get_prefix(): parent_ext}
        ev = ExpressionEvaluator("Parent.projectInfo.projectType ?? '-'")
        assert ev.evaluate(vm) == '-'

    def test_nullish_coalescing_concat(self):
        page_ext = TestTokenValueExtractor('Page.', {
            'userFirstName': 'John',
            'userLastName': 'Doe',
        })
        vm = {page_ext.get_prefix(): page_ext}

        ev = ExpressionEvaluator("(Page.userFirstName??'') +' '+ (Page.userLastName??'')")
        assert ev.evaluate(vm) == 'John Doe'

    def test_nullish_coalescing_concat_both_empty(self):
        page_ext = TestTokenValueExtractor('Page.', {})
        vm = {page_ext.get_prefix(): page_ext}
        ev = ExpressionEvaluator("(Page.userFirstName??'') +' '+ (Page.userLastName??'')")
        assert ev.evaluate(vm) == ' '

    def test_nullish_coalescing_concat_first_only(self):
        page_ext = TestTokenValueExtractor('Page.', {'userFirstName': 'Jane'})
        vm = {page_ext.get_prefix(): page_ext}
        ev = ExpressionEvaluator("(Page.userFirstName??'') +' '+ (Page.userLastName??'')")
        assert ev.evaluate(vm) == 'Jane '

    def test_nullish_coalescing_concat_last_only(self):
        page_ext = TestTokenValueExtractor('Page.', {'userLastName': 'Smith'})
        vm = {page_ext.get_prefix(): page_ext}
        ev = ExpressionEvaluator("(Page.userFirstName??'') +' '+ (Page.userLastName??'')")
        assert ev.evaluate(vm) == ' Smith'

    def test_dynamic_array_index_with_dot_access(self):
        parent_ext = TestTokenValueExtractor('Parent.', {
            'perCount': [
                {'value': {'Percentage': 10}},
                {'value': {'Percentage': 25}},
                {'value': {'Percentage': 50}},
            ],
            'index': 1,
        })
        vm = {parent_ext.get_prefix(): parent_ext}

        expr = Expression("Parent.perCount[Parent.index].value.Percentage + '%'")
        assert expr is not None

        ev = ExpressionEvaluator("Parent.perCount[Parent.index].value.Percentage + '%'")
        assert ev.evaluate(vm) == '25%'

    def test_nested_parent_index(self):
        parent_ext = TestTokenValueExtractor('Parent.', {
            'perCount': [
                {'value': {'Percentage': 10}},
                {'value': {'Percentage': 25}},
                {'value': {'Percentage': 50}},
            ],
            'Parent': {'__index': 2},
        })
        vm = {parent_ext.get_prefix(): parent_ext}

        ev = ExpressionEvaluator("Parent.perCount[Parent.Parent.__index].value.Percentage + '%'")
        assert ev.evaluate(vm) == '50%'

    def test_less_than_comparison_true(self):
        page_ext = TestTokenValueExtractor('Page.', {
            'dealData': {'size': 10, 'totalElements': 100},
        })
        vm = {page_ext.get_prefix(): page_ext}

        expr = Expression('Page.dealData.size < Page.dealData.totalElements')
        assert expr is not None

        ev = ExpressionEvaluator('Page.dealData.size < Page.dealData.totalElements')
        assert ev.evaluate(vm) is True

    def test_less_than_comparison_equal(self):
        page_ext = TestTokenValueExtractor('Page.', {
            'dealData': {'size': 100, 'totalElements': 100},
        })
        vm = {page_ext.get_prefix(): page_ext}
        ev = ExpressionEvaluator('Page.dealData.size < Page.dealData.totalElements')
        assert ev.evaluate(vm) is False

    def test_less_than_comparison_greater(self):
        page_ext = TestTokenValueExtractor('Page.', {
            'dealData': {'size': 150, 'totalElements': 100},
        })
        vm = {page_ext.get_prefix(): page_ext}
        ev = ExpressionEvaluator('Page.dealData.size < Page.dealData.totalElements')
        assert ev.evaluate(vm) is False

    def test_extra_spaces_in_comparison(self):
        page_ext = TestTokenValueExtractor('Page.', {
            'dealData': {'size': 5, 'totalElements': 20},
        })
        vm = {page_ext.get_prefix(): page_ext}

        expr = Expression('Page.dealData.size <  Page.dealData.totalElements')
        assert expr is not None

        ev = ExpressionEvaluator('Page.dealData.size <  Page.dealData.totalElements')
        assert ev.evaluate(vm) is True

    def test_expression_tostring_preserves_structure(self):
        expr1 = Expression('Page.dealData.size')
        assert 'Page' in str(expr1)
        assert 'dealData' in str(expr1)

        expr2 = Expression('Page.dealData.size < Page.dealData.totalElements')
        assert '<' in str(expr2)

        expr3 = Expression('Steps.floorWeekOne.output.value * 86400')
        assert '*' in str(expr3)

        expr4 = Expression("Parent.projectInfo.projectType ?? '-'")
        assert '??' in str(expr4)

    def test_all_expressions_parse_without_throwing(self):
        expressions = [
            'Steps.floorWeekOne.output.value * {{Page.secondsInDay}}',
            "Parent.projectInfo.projectType?? '-'",
            'Page.dealData.size <  Page.dealData.totalElements',
        ]
        for expression in expressions:
            expr = Expression(expression)
            assert expr is not None

    def test_complex_dynamic_array_access(self):
        parent_ext = TestTokenValueExtractor('Parent.', {
            'perCount': [
                {'value': {'Percentage': 15}},
                {'value': {'Percentage': 30}},
                {'value': {'Percentage': 45}},
            ],
            'index': 2,
        })
        vm = {parent_ext.get_prefix(): parent_ext}

        ev = ExpressionEvaluator("Parent.perCount[Parent.index].value.Percentage + '%'")
        assert ev.evaluate(vm) == '45%'

    def test_dynamic_bracket_index(self):
        parent_ext = TestTokenValueExtractor('Parent.', {'__index': 2})
        page_ext = TestTokenValueExtractor('Page.', {
            'items': ['first', 'second', 'third', 'fourth'],
        })
        vm = {
            parent_ext.get_prefix(): parent_ext,
            page_ext.get_prefix(): page_ext,
        }

        ev = ExpressionEvaluator('Page.items[Parent.__index]')
        assert ev.evaluate(vm) == 'third'

    def test_nested_dynamic_bracket_index(self):
        parent_ext = TestTokenValueExtractor('Parent.', {'index': 1})
        page_ext = TestTokenValueExtractor('Page.', {
            'matrix': [
                ['a', 'b', 'c'],
                ['d', 'e', 'f'],
                ['g', 'h', 'i'],
            ],
        })
        vm = {
            parent_ext.get_prefix(): parent_ext,
            page_ext.get_prefix(): page_ext,
        }

        ev = ExpressionEvaluator('Page.matrix[Parent.index][Parent.index]')
        assert ev.evaluate(vm) == 'e'

    def test_simplified_nested_ternary(self):
        page_ext = TestTokenValueExtractor('Page.', {'a': 'test'})
        vm = {page_ext.get_prefix(): page_ext}

        expression = "Page.a != undefined ? 'A' : Page.b != undefined ? 'B' : Page.c != undefined ? 'C' : '-'"
        ev = ExpressionEvaluator(expression)
        assert ev.evaluate(vm) == 'A'

    def test_nested_ternary_with_dynamic_keys(self):
        page_ext = TestTokenValueExtractor('Page.', {
            'kycs': {'123': {'a': 'test'}},
        })
        parent_ext = TestTokenValueExtractor('Parent.', {'id': '123'})
        vm = {
            page_ext.get_prefix(): page_ext,
            parent_ext.get_prefix(): parent_ext,
        }

        expression = "Page.kycs.{{Parent.id}}.a != undefined ? 'A' : Page.kycs.{{Parent.id}}.b != undefined ? 'B' : '-'"
        ev = ExpressionEvaluator(expression)
        assert ev.evaluate(vm) == 'A'

    def test_simple_ternary_with_string_concat(self):
        ev = ExpressionEvaluator("true ? 'a' +' '+ 'b' : false ? 'c' +' '+ 'd' : '-'")
        assert ev.evaluate({}) == 'a b'

    def test_ternary_with_property_access(self):
        page_ext = TestTokenValueExtractor('Page.', {'a': 'Hello', 'b': 'World'})
        vm = {page_ext.get_prefix(): page_ext}

        ev = ExpressionEvaluator("true ? Page.a +' '+ Page.b : false ? Page.c +' '+ Page.d : '-'")
        assert ev.evaluate(vm) == 'Hello World'

    def test_ternary_with_deep_paths(self):
        page_ext = TestTokenValueExtractor('Page.', {
            'x': {'a': {'first': 'John', 'last': 'Doe'}},
        })
        vm = {page_ext.get_prefix(): page_ext}

        ev = ExpressionEvaluator("true ? Page.x.a.first +' '+ Page.x.a.last : '-'")
        assert ev.evaluate(vm) == 'John Doe'

    def test_nested_ternary_with_deep_paths(self):
        page_ext = TestTokenValueExtractor('Page.', {
            'x': {'a': {'first': 'John', 'last': 'Doe'}},
        })
        vm = {page_ext.get_prefix(): page_ext}

        ev = ExpressionEvaluator(
            "true ? Page.x.a.first +' '+ Page.x.a.last : false ? Page.x.b.first +' '+ Page.x.b.last : '-'"
        )
        assert ev.evaluate(vm) == 'John Doe'

    def test_expression_trimming(self):
        expr1 = Expression('   Page.value   ')
        assert expr1.get_expression() == 'Page.value'

        expr2 = Expression('  true ? "yes" : "no"  ')
        assert expr2.get_expression() == 'true ? "yes" : "no"'

        page_ext = TestTokenValueExtractor('Page.', {'value': 'test', 'x': 10})
        vm = {page_ext.get_prefix(): page_ext}

        ev1 = ExpressionEvaluator('   Page.value   ')
        assert ev1.evaluate(vm) == 'test'

        ev2 = ExpressionEvaluator('  Page.x + 5  ')
        assert ev2.evaluate(vm) == 15

    def test_complex_nested_ternary_with_template(self):
        expression = (
            '10 - {{{{Parent.id}} < 10 ? 1 : {{Parent.id}} < 100 ? 2 : '
            '{{Parent.id}} < 1000 ? 3 : {{Parent.id}} < 10000 ? 4 : '
            '{{Parent.id}} < 100000 ? 5 : {{Parent.id}} < 1000000 ? 6 : '
            '{{Parent.id}} < 10000000 ? 7 : {{Parent.id}} < 100000000 ? 8 : '
            '{{Parent.id}} < 1000000000 ? 9 :10}}'
        )

        # Verify parsing succeeds
        expr = Expression(expression)
        assert expr is not None
        assert not expr.get_operations().is_empty()

    def test_complex_nested_ternary_evaluation(self):
        expression = (
            '10 - {{{{Parent.id}} < 10 ? 1 : {{Parent.id}} < 100 ? 2 : '
            '{{Parent.id}} < 1000 ? 3 : {{Parent.id}} < 10000 ? 4 : '
            '{{Parent.id}} < 100000 ? 5 : {{Parent.id}} < 1000000 ? 6 : '
            '{{Parent.id}} < 10000000 ? 7 : {{Parent.id}} < 100000000 ? 8 : '
            '{{Parent.id}} < 1000000000 ? 9 :10}}'
        )

        test_cases = [
            (5, 10 - 1),
            (50, 10 - 2),
            (500, 10 - 3),
            (5000, 10 - 4),
            (50000, 10 - 5),
            (500000, 10 - 6),
            (5000000, 10 - 7),
            (50000000, 10 - 8),
            (500000000, 10 - 9),
            (5000000000, 10 - 10),
        ]

        for id_val, expected in test_cases:
            parent_ext = TestTokenValueExtractor('Parent.', {'id': id_val})
            vm = {parent_ext.get_prefix(): parent_ext}
            ev = ExpressionEvaluator(expression)
            result = ev.evaluate(vm)
            assert result == expected, f'id={id_val}: expected {expected}, got {result}'

    def test_store_dynamic_page_nullish_coalesce(self):
        expression = (
            'Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? '
            '{{Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.location.expression}}'
        )

        # Verify parsing succeeds
        expr = Expression(expression)
        assert expr is not None
        assert not expr.get_operations().is_empty()

        store_with_value = TestTokenValueExtractor('Store.', {
            'application': {'properties': {'title': 'My Application Title'}},
            'urlDetails': {'pageName': 'home'},
            'pageDefinition': {
                'home': {
                    'properties': {
                        'title': {
                            'name': {
                                'location': {
                                    'expression': "'Application : ' + Store.application.properties.title"
                                },
                            },
                        },
                    },
                },
            },
        })
        vm_with_value = {store_with_value.get_prefix(): store_with_value}
        ev = ExpressionEvaluator(expression)
        assert ev.evaluate(vm_with_value) == 'Application : My Application Title'

    def test_numeric_property_path_segment(self):
        page_ext = TestTokenValueExtractor('Page.', {
            'x': {'123': {'a': {'first': 'John', 'last': 'Doe'}}},
        })
        vm = {page_ext.get_prefix(): page_ext}

        ev = ExpressionEvaluator("true ? Page.x.123.a.first +' '+ Page.x.123.a.last : '-'")
        assert ev.evaluate(vm) == 'John Doe'

    def test_alphanumeric_property_path_segment(self):
        page_ext = TestTokenValueExtractor('Page.', {
            'x': {'507f1f77bcf86cd799439011': {'a': {'first': 'Jane', 'last': 'Smith'}}},
        })
        vm = {page_ext.get_prefix(): page_ext}

        ev = ExpressionEvaluator(
            "true ? Page.x.507f1f77bcf86cd799439011.a.first +' '+ Page.x.507f1f77bcf86cd799439011.a.last : '-'"
        )
        assert ev.evaluate(vm) == 'Jane Smith'
