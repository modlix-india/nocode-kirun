from __future__ import annotations

from typing import Any, Dict, List

import pytest

from kirun_py.runtime.expression.expression import Expression
from kirun_py.runtime.expression.expression_evaluator import ExpressionEvaluator
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor
from kirun_py.runtime.tokenextractor.arguments_token_value_extractor import ArgumentsTokenValueExtractor
from kirun_py.runtime.tokenextractor.output_map_token_value_extractor import OutputMapTokenValueExtractor
from kirun_py.util.map_util import MapUtil


# --- Helper TokenValueExtractor classes ---

class TestTokenValueExtractor(TokenValueExtractor):
    """Generic test token value extractor with configurable prefix."""

    def __init__(self, store: Any, prefix: str = 'Test.'):
        super().__init__()
        self._store = store
        self._prefix = prefix

    def get_value_internal(self, token: str) -> Any:
        parts = token.split('.')
        return self.retrieve_element_from(token, parts, 1, self._store)

    def get_prefix(self) -> str:
        return self._prefix

    def get_store(self) -> Any:
        return self._store


class StoreTokenValueExtractor(TokenValueExtractor):
    """Store-prefixed token value extractor."""

    def __init__(self, store: Any):
        super().__init__()
        self._store = store

    def get_value_internal(self, token: str) -> Any:
        parts = token.split('.')
        return self.retrieve_element_from(token, parts, 1, self._store)

    def get_prefix(self) -> str:
        return 'Store.'

    def get_store(self) -> Any:
        return self._store


# --- Tests ported from ExpressionEvaluationTest.ts ---

class TestExpressionEvaluation:
    """Ported from ExpressionEvaluationTest.ts"""

    def _build_parameters_values_map(self) -> Dict[str, TokenValueExtractor]:
        phone = {'phone1': '1234', 'phone2': '5678', 'phone3': '5678'}
        address = {
            'line1': 'Flat 202, PVR Estates',
            'line2': 'Nagvara',
            'city': 'Benguluru',
            'pin': '560048',
            'phone': phone,
        }
        arr = [10, 20, 30]
        obj = {
            'studentName': 'Kumar',
            'math': 20,
            'isStudent': True,
            'address': address,
            'array': arr,
            'num': 1,
        }

        in_map = {'name': 'Kiran', 'obj': obj}

        output = {
            'step1': {'output': in_map},
            'loop': {'iteration': {'index': 2}},
        }

        context_data = [1, 2]

        atv = ArgumentsTokenValueExtractor({})
        omtv = OutputMapTokenValueExtractor(output)

        # Build context extractor
        class ContextExtractor(TokenValueExtractor):
            def __init__(self, data):
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
                return 'Context.'

            def get_store(self) -> Any:
                return self._data

        ctx = ContextExtractor({'a': context_data})

        values_map = {
            atv.get_prefix(): atv,
            omtv.get_prefix(): omtv,
            ctx.get_prefix(): ctx,
        }
        return values_map

    def test_context_array_with_steps_index(self):
        vm = self._build_parameters_values_map()
        result = ExpressionEvaluator(
            'Context.a[Steps.loop.iteration.index - 1] + Context.a[Steps.loop.iteration.index - 2]'
        ).evaluate(vm)
        assert result == 3

    def test_simple_addition(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator('3 + 7').evaluate(vm) == 10

    def test_string_number_concatenation(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator('"asdf"+333').evaluate(vm) == 'asdf333'

    def test_bitwise_right_shift_equality(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator('34 >> 2 = 8 ').evaluate(vm) is True

    def test_arithmetic_precedence(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator('10*11+12*13*14/7').evaluate(vm) == 422

    def test_undefined_path_returns_none(self):
        vm = self._build_parameters_values_map()
        result = ExpressionEvaluator('Steps.step1.output.name1').evaluate(vm)
        assert result is None

    def test_string_equality(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator('"Kiran" = Steps.step1.output.name ').evaluate(vm) is True

    def test_null_equality(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator('null = Steps.step1.output.name1 ').evaluate(vm) is True

    def test_deep_property_access(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator(
            'Steps.step1.output.obj.address.phone.phone2'
        ).evaluate(vm) == '5678'

    def test_deep_property_self_equality(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator(
            'Steps.step1.output.obj.address.phone.phone2 = Steps.step1.output.obj.address.phone.phone2 '
        ).evaluate(vm) is True

    def test_deep_property_inequality(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator(
            'Steps.step1.output.obj.address.phone.phone2 != Steps.step1.output.address.obj.phone.phone1 '
        ).evaluate(vm) is True

    def test_dynamic_array_index_with_addition(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator(
            'Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2'
        ).evaluate(vm) == 32

    def test_dynamic_array_index_sum(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator(
            'Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+'
            'Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]'
        ).evaluate(vm) == 60

    def test_negative_dynamic_array_index(self):
        vm = self._build_parameters_values_map()
        assert ExpressionEvaluator(
            'Steps.step1.output.obj.array[-Steps.step1.output.obj.num + 3]+2'
        ).evaluate(vm) == 32

    def test_floating_point_arithmetic(self):
        vm = self._build_parameters_values_map()
        result = ExpressionEvaluator('2.43*4.22+7.0987').evaluate(vm)
        assert abs(result - 17.3533) < 1e-10


class TestExpressionEvaluationDeep:
    """Ported from ExpressionEvaluationTest.ts - deep tests."""

    def test_arguments_string_vs_number_equality(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.a = Arugments.b')
        assert not ev.evaluate(values_map)

    def test_deep_object_equality(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.c = Arguments.d')
        assert ev.evaluate(values_map)

    def test_undefined_equals_null(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.e = null')
        assert ev.evaluate(values_map)

    def test_undefined_not_equals_null(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.e != null')
        assert not ev.evaluate(values_map)

    def test_undefined_equals_false(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.e = false')
        assert ev.evaluate(values_map)

    def test_object_not_equals_null(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.c != null')
        assert ev.evaluate(values_map)


class TestNullishCoalescing:
    """Ported from ExpressionEvaluationTest.ts - nullish coalescing."""

    def test_nullish_coalescing_first_null(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'b1': 4,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('(Arguments.e ?? Arguments.b ?? Arguments.b1) + 4')
        assert ev.evaluate(values_map) == 6

    def test_nullish_coalescing_first_two_null(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'b1': 4,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('(Arguments.e ?? Arguments.b2 ?? Arguments.b1) + 4')
        assert ev.evaluate(values_map) == 8


class TestNestingExpression:
    """Ported from ExpressionEvaluationTest.ts - nesting expression."""

    def test_nesting_expression_evaluation(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'b1': 4,
            'b2': 4,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'Arguments.b2'}},
            'd': 'c',
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator(
            'Arguments.{{Arguments.d}}.a + {{Arguments.{{Arguments.d}}.c.x}}'
        )
        assert ev.evaluate(values_map) == 6

    def test_nesting_expression_with_string_multiplication(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'b1': 4,
            'b2': 4,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'Arguments.b2'}},
            'd': 'c',
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator(
            "'There are {{{{Arguments.{{Arguments.d}}.c.x}}}} boys in the class room...' * Arguments.b"
        )
        assert ev.evaluate(values_map) == (
            'There are 4 boys in the class room...'
            'There are 4 boys in the class room...'
        )


class TestPartialPathEvaluation:
    """Ported from ExpressionEvaluationTest.ts - partial path."""

    def test_nested_object_array_access(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 1,
            'b1': 4,
            'b2': 4,
            'c': {
                'a': 0,
                'b': [True, False],
                'c': {'x': 'Arguments.b2'},
                'keys': ['a', 'e', {'val': 5}],
            },
            'd': 'c',
            'e': [
                {'name': 'Kiran', 'num': 1},
                {'name': 'Good', 'num': 2},
            ],
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.c.keys[2].val + 3')
        assert ev.evaluate(values_map) == 8

    def test_nullish_coalescing_with_array_access(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 1,
            'b1': 4,
            'b2': 4,
            'c': {
                'a': 0,
                'b': [True, False],
                'c': {'x': 'Arguments.b2'},
                'keys': ['a', 'e', {'val': 5}],
            },
            'd': 'c',
            'e': [
                {'name': 'Kiran', 'num': 1},
                {'name': 'Good', 'num': 2},
            ],
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('(Arguments.f ?? Arguments.e)[1+1-1].num')
        assert ev.evaluate(values_map) == 2


class TestLogicalOperatorsAllTypes:
    """Ported from ExpressionEvaluationTest.ts - logical operators with all value types."""

    @pytest.fixture
    def values_map(self):
        atv = ArgumentsTokenValueExtractor({
            'string': 'kirun ',
            'stringEmpty': '',
            'number': 122.2,
            'number0': 0,
            'booleanTrue': True,
            'booleanFalse': False,
            'null': None,
            'undefined': None,
            'object': {'a': 1, 'b': '2', 'c': True, 'd': None, 'e': None},
            'array': [1, '2', True, None, None],
            'array2': [1, '2', True, None, None],
            'emptyArray': [],
        })
        return MapUtil.of(atv.get_prefix(), atv)

    def test_not_not_object(self, values_map):
        assert ExpressionEvaluator('not not Arguments.object').evaluate(values_map)

    def test_not_not_empty_string(self, values_map):
        assert ExpressionEvaluator('not not Arguments.stringEmpty').evaluate(values_map)

    def test_not_not_number(self, values_map):
        assert ExpressionEvaluator('not not Arguments.number').evaluate(values_map)

    def test_not_not_zero(self, values_map):
        assert not ExpressionEvaluator('not not Arguments.number0').evaluate(values_map)

    def test_not_not_boolean_true(self, values_map):
        assert ExpressionEvaluator('not not Arguments.booleanTrue').evaluate(values_map)

    def test_not_not_boolean_false(self, values_map):
        assert not ExpressionEvaluator('not not Arguments.booleanFalse').evaluate(values_map)

    def test_not_not_null(self, values_map):
        assert not ExpressionEvaluator('not not Arguments.null').evaluate(values_map)

    def test_not_not_undefined(self, values_map):
        assert not ExpressionEvaluator('not not Arguments.undefined').evaluate(values_map)

    def test_not_not_array(self, values_map):
        assert ExpressionEvaluator('not not Arguments.array').evaluate(values_map)

    def test_not_not_empty_array(self, values_map):
        assert ExpressionEvaluator('not not Arguments.emptyArray').evaluate(values_map)

    def test_object_equals_true(self, values_map):
        assert not ExpressionEvaluator('Arguments.object = true').evaluate(values_map)

    def test_object_not_equals_true(self, values_map):
        assert ExpressionEvaluator('Arguments.object != true').evaluate(values_map)

    def test_empty_string_equals_true(self, values_map):
        assert not ExpressionEvaluator('Arguments.stringEmpty = true').evaluate(values_map)

    def test_empty_string_not_equals_false(self, values_map):
        assert ExpressionEvaluator('Arguments.stringEmpty != false').evaluate(values_map)

    def test_zero_equals_true(self, values_map):
        assert not ExpressionEvaluator('Arguments.number0 = true').evaluate(values_map)

    def test_zero_equals_false(self, values_map):
        assert not ExpressionEvaluator('Arguments.number0 = false').evaluate(values_map)

    def test_array_length(self, values_map):
        assert ExpressionEvaluator('Arguments.array.length').evaluate(values_map) == 5

    def test_object_length(self, values_map):
        assert ExpressionEvaluator('Arguments.object.length').evaluate(values_map) == 5

    def test_object_and_array(self, values_map):
        assert ExpressionEvaluator('Arguments.object and Arguments.array').evaluate(values_map)

    def test_object_or_null(self, values_map):
        assert ExpressionEvaluator('Arguments.object or Arguments.null').evaluate(values_map)

    def test_object_and_null(self, values_map):
        assert not ExpressionEvaluator('Arguments.object and Arguments.null').evaluate(values_map)

    def test_ternary_with_object_condition(self, values_map):
        assert ExpressionEvaluator('Arguments.object ? 3 : 4').evaluate(values_map) == 3

    def test_ternary_with_not_object_condition(self, values_map):
        assert ExpressionEvaluator('not Arguments.object ? 3 : 4').evaluate(values_map) == 4

    def test_array_equality(self, values_map):
        assert ExpressionEvaluator('Arguments.array = Arguments.array2').evaluate(values_map)

    def test_ternary_with_zero_condition(self, values_map):
        assert ExpressionEvaluator('Arguments.number0 ? 3 : 4').evaluate(values_map) == 4


class TestFullStore:
    """Ported from ExpressionEvaluationTest.ts - Full Store Test."""

    def test_custom_extractor_simple_access(self):
        ttv = TestTokenValueExtractor({
            'a': 'kirun',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })

        ev = ExpressionEvaluator('Test.a')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 'kirun'

    def test_custom_extractor_primitive_store(self):
        ttv = TestTokenValueExtractor(20)

        ev = ExpressionEvaluator('Test')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 20

    def test_custom_extractor_comparison(self):
        ttv = TestTokenValueExtractor(20)

        ev = ExpressionEvaluator('Test > 10')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) is True

    def test_custom_extractor_undefined_store(self):
        ttv = TestTokenValueExtractor(None)

        ev = ExpressionEvaluator('Test')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) is None


class TestIndexRetrieval:
    """Ported from ExpressionEvaluationTest.ts - index retrieval."""

    def test_simple_value(self):
        ttv = TestTokenValueExtractor({
            'a': 'kirun',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })

        ev = ExpressionEvaluator('Test.a')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 'kirun'

    def test_index_on_property(self):
        ttv = TestTokenValueExtractor({
            'a': 'kirun',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })

        ev = ExpressionEvaluator('Test.b.__index')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 'b'

    def test_index_on_deep_property(self):
        ttv = TestTokenValueExtractor({
            'a': 'kirun',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })

        ev = ExpressionEvaluator('Test.c.c.x.__index')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 'x'

    def test_index_on_array_element(self):
        ttv = TestTokenValueExtractor({
            'a': 'kirun',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })

        ev = ExpressionEvaluator('Test.c.b[1].__index')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 1

    def test_index_on_missing_property(self):
        ttv = TestTokenValueExtractor({
            'a': 'kirun',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
        })

        ev = ExpressionEvaluator('Test.x.c.__index')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 'c'


class TestBackslashEscape:
    """Ported from ExpressionEvaluationTest.ts."""

    def test_backslash_in_string(self):
        ev = ExpressionEvaluator("'\\maza'")
        assert ev.evaluate({}) == '\\maza'


class TestTernaryDisplayValue:
    """Ported from ExpressionEvaluationTest.ts - ternary expression with displayValue."""

    def test_display_value_zero(self):
        ttv = TestTokenValueExtractor({'displayValue': '0'})
        ev = ExpressionEvaluator("(Test.displayValue = '0') ? '1' : (Test.displayValue + '1')")
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == '1'

    def test_display_value_one(self):
        ttv = TestTokenValueExtractor({'displayValue': '1'})
        ev = ExpressionEvaluator("(Test.displayValue = '0') ? '1' : (Test.displayValue + '1')")
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == '11'

    def test_display_value_five(self):
        ttv = TestTokenValueExtractor({'displayValue': '5'})
        ev = ExpressionEvaluator("(Test.displayValue = '0') ? '1' : (Test.displayValue + '1')")
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == '51'

    def test_display_value_ten(self):
        ttv = TestTokenValueExtractor({'displayValue': '10'})
        ev = ExpressionEvaluator("(Test.displayValue = '0') ? '1' : (Test.displayValue + '1')")
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == '101'

    def test_display_value_extra_parens(self):
        ttv = TestTokenValueExtractor({'displayValue': '1'})
        ev = ExpressionEvaluator("((Test.displayValue = '0') ? '1' : (Test.displayValue + '1'))")
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == '11'

    def test_display_value_triple_parens(self):
        ttv = TestTokenValueExtractor({'displayValue': '1'})
        ev = ExpressionEvaluator("(((Test.displayValue = '0') ? '1' : (Test.displayValue + '1')))")
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == '11'

    def test_display_value_nested_parens(self):
        ttv = TestTokenValueExtractor({'displayValue': '1'})
        ev = ExpressionEvaluator(
            "(((Test.displayValue = '0')) ? '1' : (((Test.displayValue) + '1')))"
        )
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == '11'


class TestStringVsNumberEquality:
    """Ported from ExpressionEvaluationTest.ts."""

    def test_direct_comparison_different_types(self):
        stv = StoreTokenValueExtractor({'strNumber': '413', 'number': 413})
        values_map = MapUtil.of(stv.get_prefix(), stv)

        ev = ExpressionEvaluator('Store.strNumber = Store.number')
        assert ev.evaluate(values_map) is False

    def test_nested_expression_substitution(self):
        stv = StoreTokenValueExtractor({'strNumber': '413', 'number': 413})
        values_map = MapUtil.of(stv.get_prefix(), stv)

        ev = ExpressionEvaluator('{{Store.strNumber}} = Store.number')
        assert ev.evaluate(values_map) is True

    def test_string_value_directly(self):
        stv = StoreTokenValueExtractor({'strNumber': '413', 'number': 413})
        values_map = MapUtil.of(stv.get_prefix(), stv)

        ev = ExpressionEvaluator('Store.strNumber')
        assert ev.evaluate(values_map) == '413'

    def test_number_value_directly(self):
        stv = StoreTokenValueExtractor({'strNumber': '413', 'number': 413})
        values_map = MapUtil.of(stv.get_prefix(), stv)

        ev = ExpressionEvaluator('Store.number')
        assert ev.evaluate(values_map) == 413

    def test_string_to_string_comparison(self):
        stv = StoreTokenValueExtractor({'strNumber': '413', 'number': 413})
        values_map = MapUtil.of(stv.get_prefix(), stv)

        ev = ExpressionEvaluator("Store.strNumber = '413'")
        assert ev.evaluate(values_map) is True

    def test_number_to_number_comparison(self):
        stv = StoreTokenValueExtractor({'strNumber': '413', 'number': 413})
        values_map = MapUtil.of(stv.get_prefix(), stv)

        ev = ExpressionEvaluator('Store.number = 413')
        assert ev.evaluate(values_map) is True


class TestUnaryMinus:
    """Ported from ExpressionEvaluationTest.ts - unary minus operator."""

    def test_simple_negative(self):
        ev = ExpressionEvaluator('-5')
        assert ev.evaluate({}) == -5

    def test_negative_via_subtraction(self):
        ttv = TestTokenValueExtractor({'value': 10})
        ev = ExpressionEvaluator('0 - Test.value')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == -10

    def test_negative_in_expression(self):
        ttv = TestTokenValueExtractor({'a': 5, 'b': 3})
        ev = ExpressionEvaluator('Test.a + (0 - Test.b)')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == 2

    def test_multiply_with_negative(self):
        ttv = TestTokenValueExtractor({'a': 5})
        ev = ExpressionEvaluator('Test.a * -1')
        assert ev.evaluate(MapUtil.of(ttv.get_prefix(), ttv)) == -5

    def test_parenthesized_negative(self):
        ev = ExpressionEvaluator('(-5)')
        assert ev.evaluate({}) == -5

    def test_negative_in_complex_expression(self):
        ev = ExpressionEvaluator('10 + (-5) * 2')
        assert ev.evaluate({}) == 0


# --- Tests ported from ExpressionEqualityTest.ts ---

class TestExpressionEquality:
    """Ported from ExpressionEqualityTest.ts"""

    @pytest.fixture
    def values_map(self):
        obj = {
            'number': 20,
            'zero': 0,
            'booleanTrue': True,
            'booleanFalse': False,
            'string': 'Hello',
            'emptyString': '',
            'nullValue': None,
            'undefinedValue': None,
            'emptyObject': {},
            'emptyArray': [],
        }
        in_map = {'name': 'Kiran', 'obj': obj}
        output = {'step1': {'output': in_map}}
        omtv = OutputMapTokenValueExtractor(output)
        return {omtv.get_prefix(): omtv}

    def test_number_not_equal_zero(self, values_map):
        exp = ExpressionEvaluator(
            'Steps.step1.output.obj.number = Steps.step1.output.obj.zero'
        )
        assert exp.evaluate(values_map) is False

    def test_false_equals_not_number(self, values_map):
        exp = ExpressionEvaluator(
            'Steps.step1.output.obj.booleanFalse = (not Steps.step1.output.obj.number)'
        )
        assert exp.evaluate(values_map) is True

    def test_false_equals_not_empty_string(self, values_map):
        exp = ExpressionEvaluator(
            'Steps.step1.output.obj.booleanFalse = (not Steps.step1.output.obj.emptyString)'
        )
        assert exp.evaluate(values_map) is True

    def test_true_equals_not_zero(self, values_map):
        exp = ExpressionEvaluator(
            'Steps.step1.output.obj.booleanTrue = (not Steps.step1.output.obj.zero)'
        )
        assert exp.evaluate(values_map) is True

    def test_empty_string_equals_empty_string(self, values_map):
        exp = ExpressionEvaluator("Steps.step1.output.obj.emptyString = ''")
        assert exp.evaluate(values_map) is True

    def test_empty_string_not_equals_empty_string(self, values_map):
        exp = ExpressionEvaluator("Steps.step1.output.obj.emptyString != ''")
        assert exp.evaluate(values_map) is False

    def test_string_not_equals_empty_string(self, values_map):
        exp = ExpressionEvaluator("Steps.step1.output.obj.string != ''")
        assert exp.evaluate(values_map) is True

    def test_string_equals_empty_string(self, values_map):
        exp = ExpressionEvaluator("Steps.step1.output.obj.string = ''")
        assert exp.evaluate(values_map) is False


# --- Tests ported from ExpressionEvaluatorTernaryOperatorTest.ts ---

class TestTernaryEvaluator:
    """Ported from ExpressionEvaluatorTernaryOperatorTest.ts"""

    @pytest.fixture
    def values_map(self):
        x = {'a': 2, 'b': [True, False], 'c': {'x': 'Arguments.b2'}}
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'b1': 4,
            'b2': 4,
            'c': x,
            'd': 'c',
        })
        return MapUtil.of(atv.get_prefix(), atv)

    def test_ternary_true_branch(self, values_map):
        ev = ExpressionEvaluator('Arguments.e = null ? Arguments.c.a : 3 ')
        assert ev.evaluate(values_map) == 2

    def test_ternary_false_branch(self, values_map):
        ev = ExpressionEvaluator('Arguments.f ? Arguments.c.a : 3 ')
        assert ev.evaluate(values_map) == 3

    def test_ternary_returns_object(self, values_map):
        x = {'a': 2, 'b': [True, False], 'c': {'x': 'Arguments.b2'}}
        ev = ExpressionEvaluator('Arguments.e = null ? Arguments.c : 3 ')
        result = ev.evaluate(values_map)
        assert result == x


# --- Tests ported from ExpressionEvaluatorStringLiteralTest.ts ---

class TestStringLiteral:
    """Ported from ExpressionEvaluatorStringLiteralTest.ts"""

    def test_string_literal_concatenation(self):
        ex = Expression("'ki/run'+'ab'")
        ev = ExpressionEvaluator(ex)
        assert ev.evaluate(MapUtil.of()) == 'ki/runab'

    def test_unclosed_string_throws(self):
        evt = ExpressionEvaluator('"Steps.a')
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)
        with pytest.raises(Exception):
            evt.evaluate(values_map)

    def test_string_concatenation_with_argument(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator("Arguments.a+'kiran'")
        assert ev.evaluate(values_map) == 'kirun kiran'

    def test_number_string_concatenation(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator("Arguments.b+'kiran'")
        assert ev.evaluate(values_map) == '2kiran'

    def test_boolean_string_with_quotes(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.c+\'k"ir"an\'')
        assert ev.evaluate(values_map) == 'truek"ir"an'

    def test_mixed_quotes_concatenation(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator("Arguments.b+\"'kir\" + ' an'")
        assert ev.evaluate(values_map) == "2'kir an"

    def test_string_number_concatenation(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator("Arguments.a+'kiran'+ Arguments.b")
        assert ev.evaluate(values_map) == 'kirun kiran2'


class TestStringLength:
    """Ported from ExpressionEvaluatorStringLiteralTest.ts - length tests."""

    def test_string_length(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 'hello', 'b': ''},
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.a.length')
        assert ev.evaluate(values_map) == 6

    def test_number_length_throws(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 'hello', 'b': ''},
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.b.length')
        with pytest.raises(Exception):
            ev.evaluate(values_map)

    def test_string_length_multiply(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 'hello', 'b': ''},
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.c.a.length * "f"')
        assert ev.evaluate(values_map) == 'fffff'

    def test_empty_string_length_ternary(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 'hello', 'b': ''},
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('Arguments.c.b.length ? "f" : "t"')
        assert ev.evaluate(values_map) == 't'


class TestStringMultiplication:
    """Ported from ExpressionEvaluatorStringLiteralTest.ts - string * number."""

    def test_string_repeat(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator("'a' * 10")
        assert ev.evaluate(values_map) == 'aaaaaaaaaa'

    def test_fractional_string_repeat(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('2.5*Arguments.a')
        assert ev.evaluate(values_map) == 'kirun kirun kir'

    def test_negative_string_repeat(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('-0.5*Arguments.a')
        assert ev.evaluate(values_map) == 'rik'

    def test_reverse_string(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator("'asdf' * -1")
        assert ev.evaluate(values_map) == 'fdsa'

    def test_string_times_zero(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator("'asdf' * 0")
        assert ev.evaluate(values_map) == ''

    def test_number_dot_val(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': True,
            'd': 1.5,
        })
        values_map = MapUtil.of(atv.get_prefix(), atv)

        ev = ExpressionEvaluator('2.val')
        assert ev.evaluate(values_map) is None


class TestStringLengthWithObject:
    """Ported from ExpressionEvaluatorStringLiteralTest.ts."""

    @pytest.fixture
    def values_map(self):
        json_obj = {'greeting': 'hello', 'name': 'surendhar'}
        atv = ArgumentsTokenValueExtractor({
            'a': 'surendhar ',
            'b': 2,
            'c': True,
            'd': 1.5,
            'obj': json_obj,
        })
        return MapUtil.of(atv.get_prefix(), atv)

    def test_string_length(self, values_map):
        ev = ExpressionEvaluator('Arguments.a.length')
        assert ev.evaluate(values_map) == 10

    def test_number_length_throws(self, values_map):
        ev = ExpressionEvaluator('Arguments.b.length')
        with pytest.raises(Exception):
            ev.evaluate(values_map)

    def test_greeting_length_repeat(self, values_map):
        ev = ExpressionEvaluator('Arguments.obj.greeting.length * "S"')
        assert ev.evaluate(values_map) == 'SSSSS'

    def test_greeting_length_repeat_multi_char(self, values_map):
        ev = ExpressionEvaluator('Arguments.obj.greeting.length * "SP"')
        assert ev.evaluate(values_map) == 'SPSPSPSPSP'

    def test_name_length_ternary(self, values_map):
        ev = ExpressionEvaluator('Arguments.obj.name.length ? "fun" : "not Fun"')
        assert ev.evaluate(values_map) == 'fun'


class TestStringLengthSquareBrackets:
    """Ported from ExpressionEvaluatorStringLiteralTest.ts - bracket notation for length."""

    @pytest.fixture
    def values_map(self):
        json_obj = {'greeting': 'hello', 'name': 'surendhar'}
        atv = ArgumentsTokenValueExtractor({
            'a': 'surendhar ',
            'b': 2,
            'c': True,
            'd': 1.5,
            'obj': json_obj,
        })
        return MapUtil.of(atv.get_prefix(), atv)

    def test_string_length_bracket(self, values_map):
        ev = ExpressionEvaluator('Arguments.a["length"]')
        assert ev.evaluate(values_map) == 10

    def test_number_length_bracket_throws(self, values_map):
        ev = ExpressionEvaluator('Arguments.b["length"]')
        with pytest.raises(Exception):
            ev.evaluate(values_map)

    def test_greeting_length_bracket_repeat(self, values_map):
        ev = ExpressionEvaluator('Arguments.obj.greeting["length"] * "S"')
        assert ev.evaluate(values_map) == 'SSSSS'

    def test_greeting_length_bracket_repeat_multi_char(self, values_map):
        ev = ExpressionEvaluator('Arguments.obj.greeting["length"] * "SP"')
        assert ev.evaluate(values_map) == 'SPSPSPSPSP'

    def test_double_bracket_length_repeat(self, values_map):
        ev = ExpressionEvaluator('Arguments.obj["greeting"]["length"] * "S"')
        assert ev.evaluate(values_map) == 'SSSSS'

    def test_double_bracket_length_repeat_multi_char(self, values_map):
        ev = ExpressionEvaluator('Arguments.obj["greeting"]["length"] * "SP"')
        assert ev.evaluate(values_map) == 'SPSPSPSPSP'

    def test_name_length_bracket_ternary(self, values_map):
        ev = ExpressionEvaluator('Arguments.obj.name["length"] ? "fun" : "not Fun"')
        assert ev.evaluate(values_map) == 'fun'


class TestStringLiteralTemplateInterpolation:
    """Ported from ExpressionEvaluatorStringLiteralTest.ts - template interpolation."""

    @pytest.fixture
    def values_map(self):
        class StepsTokenExtractor(TokenValueExtractor):
            def __init__(self, data):
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

        steps_atv = StepsTokenExtractor({
            'countLoop': {'iteration': {'index': 1}},
            'index': 5,
        })
        args_atv = ArgumentsTokenValueExtractor({
            'a': 'test',
            'b': 10,
            'c': 15,
        })
        return {
            'Steps.': steps_atv,
            'Arguments.': args_atv,
        }

    def test_template_inside_string_literal(self, values_map):
        ev = ExpressionEvaluator(
            "'Page.appDefinitions.content[{{Steps.countLoop.iteration.index}}].stringValue'"
        )
        assert ev.evaluate(values_map) == 'Page.appDefinitions.content[1].stringValue'

    def test_template_double_quotes(self, values_map):
        ev = ExpressionEvaluator(
            '"Page.appDefinitions.content[{{Steps.countLoop.iteration.index}}].stringValue"'
        )
        assert ev.evaluate(values_map) == 'Page.appDefinitions.content[1].stringValue'

    def test_template_in_concatenation(self, values_map):
        ev = ExpressionEvaluator(
            "Arguments.a + ' - ' + 'Path: {{Steps.index}}'"
        )
        assert ev.evaluate(values_map) == 'test - Path: 5'

    def test_multiple_templates_in_string(self, values_map):
        ev = ExpressionEvaluator(
            "'{{Arguments.a}} + {{Arguments.b}} = {{Arguments.c}}'"
        )
        assert ev.evaluate(values_map) == 'test + 10 = 15'

    def test_arithmetic_inside_template(self, values_map):
        ev = ExpressionEvaluator(
            "'Result: {{Arguments.b + Arguments.c}}!'"
        )
        assert ev.evaluate(values_map) == 'Result: 25!'

    def test_nested_property_template(self, values_map):
        ev = ExpressionEvaluator(
            "'Item {{Steps.countLoop.iteration.index}} of {{Arguments.c}}'"
        )
        assert ev.evaluate(values_map) == 'Item 1 of 15'


# --- Tests ported from ExpressionParsingTest.ts (evaluation part) ---

class TestExpressionParsingEvaluation:
    """Ported from ExpressionParsingTest.ts - evaluation tests."""

    def _make_extractor(self, prefix: str, store: Any) -> TestTokenValueExtractor:
        return TestTokenValueExtractor(store, prefix)

    def test_multiplication_with_nested_template(self):
        steps = self._make_extractor('Steps.', {
            'floorWeekOne': {'output': {'value': 7}},
        })
        page = self._make_extractor('Page.', {'secondsInDay': 86400})
        values_map = {steps.get_prefix(): steps, page.get_prefix(): page}

        ev = ExpressionEvaluator(
            'Steps.floorWeekOne.output.value * {{Page.secondsInDay}}'
        )
        assert ev.evaluate(values_map) == 7 * 86400

    def test_nullish_coalescing_with_value(self):
        parent = self._make_extractor('Parent.', {
            'projectInfo': {'projectType': 'Commercial'},
        })
        values_map = {parent.get_prefix(): parent}

        ev = ExpressionEvaluator("Parent.projectInfo.projectType ?? '-'")
        assert ev.evaluate(values_map) == 'Commercial'

    def test_nullish_coalescing_with_missing_property(self):
        parent = self._make_extractor('Parent.', {
            'projectInfo': {'projectType': 'Commercial'},
        })
        values_map = {parent.get_prefix(): parent}

        ev1 = ExpressionEvaluator("Parent.projectInfo.projectType1?? '-'")
        assert ev1.evaluate(values_map) == '-'

    def test_nullish_coalescing_with_null_value(self):
        parent = self._make_extractor('Parent.', {
            'projectInfo': {'projectType': None},
        })
        values_map = {parent.get_prefix(): parent}

        ev = ExpressionEvaluator("Parent.projectInfo.projectType ?? '-'")
        assert ev.evaluate(values_map) == '-'

    def test_nullish_coalescing_string_concat(self):
        page = self._make_extractor('Page.', {
            'userFirstName': 'John',
            'userLastName': 'Doe',
        })
        values_map = {page.get_prefix(): page}

        ev = ExpressionEvaluator("(Page.userFirstName??'') +' '+ (Page.userLastName??'')")
        assert ev.evaluate(values_map) == 'John Doe'

    def test_nullish_coalescing_both_missing(self):
        page = self._make_extractor('Page.', {})
        values_map = {page.get_prefix(): page}

        ev = ExpressionEvaluator("(Page.userFirstName??'') +' '+ (Page.userLastName??'')")
        assert ev.evaluate(values_map) == ' '

    def test_dynamic_array_index_with_string_concat(self):
        parent = self._make_extractor('Parent.', {
            'perCount': [
                {'value': {'Percentage': 10}},
                {'value': {'Percentage': 25}},
                {'value': {'Percentage': 50}},
            ],
            'index': 1,
        })
        values_map = {parent.get_prefix(): parent}

        ev = ExpressionEvaluator("Parent.perCount[Parent.index].value.Percentage + '%'")
        assert ev.evaluate(values_map) == '25%'

    def test_less_than_comparison(self):
        page = self._make_extractor('Page.', {
            'dealData': {'size': 10, 'totalElements': 100},
        })
        values_map = {page.get_prefix(): page}

        ev = ExpressionEvaluator('Page.dealData.size < Page.dealData.totalElements')
        assert ev.evaluate(values_map) is True

    def test_less_than_equal(self):
        page = self._make_extractor('Page.', {
            'dealData': {'size': 100, 'totalElements': 100},
        })
        values_map = {page.get_prefix(): page}

        ev = ExpressionEvaluator('Page.dealData.size < Page.dealData.totalElements')
        assert ev.evaluate(values_map) is False

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
            parent = self._make_extractor('Parent.', {'id': id_val})
            values_map = {parent.get_prefix(): parent}
            ev = ExpressionEvaluator(expression)
            assert ev.evaluate(values_map) == expected, f'Failed for id={id_val}'

    def test_dynamic_bracket_index(self):
        parent = self._make_extractor('Parent.', {'__index': 2})
        page = self._make_extractor('Page.', {
            'items': ['first', 'second', 'third', 'fourth'],
        })
        values_map = {parent.get_prefix(): parent, page.get_prefix(): page}

        ev = ExpressionEvaluator('Page.items[Parent.__index]')
        assert ev.evaluate(values_map) == 'third'

    def test_nested_dynamic_bracket_index(self):
        parent = self._make_extractor('Parent.', {'index': 1})
        page = self._make_extractor('Page.', {
            'matrix': [
                ['a', 'b', 'c'],
                ['d', 'e', 'f'],
                ['g', 'h', 'i'],
            ],
        })
        values_map = {parent.get_prefix(): parent, page.get_prefix(): page}

        ev = ExpressionEvaluator('Page.matrix[Parent.index][Parent.index]')
        assert ev.evaluate(values_map) == 'e'

    def test_simple_nested_ternary(self):
        page = self._make_extractor('Page.', {'a': 'test'})
        values_map = {page.get_prefix(): page}

        expression = "Page.a != undefined ? 'A' : Page.b != undefined ? 'B' : Page.c != undefined ? 'C' : '-'"
        ev = ExpressionEvaluator(expression)
        assert ev.evaluate(values_map) == 'A'

    def test_simplest_ternary_with_string_concat(self):
        ev = ExpressionEvaluator("true ? 'a' +' '+ 'b' : false ? 'c' +' '+ 'd' : '-'")
        assert ev.evaluate({}) == 'a b'

    def test_ternary_with_property_access_string_concat(self):
        page = self._make_extractor('Page.', {'a': 'Hello', 'b': 'World'})
        values_map = {page.get_prefix(): page}

        ev = ExpressionEvaluator("true ? Page.a +' '+ Page.b : false ? Page.c +' '+ Page.d : '-'")
        assert ev.evaluate(values_map) == 'Hello World'


# --- Tests ported from ExpressionArrayStringIndexing.ts ---

class TestArrayStringIndexing:
    """Ported from ExpressionArrayStringIndexing.ts"""

    @pytest.fixture
    def values_map(self):
        atv = ArgumentsTokenValueExtractor({
            'a': 'kirun ',
            'b': 2,
            'c': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'd': {'a': 2, 'b': [True, False], 'c': {'x': 'kiran'}},
            'arr': [0, 1, 2, 3, 4, 5, 6],
        })
        return MapUtil.of(atv.get_prefix(), atv)

    def test_string_value(self, values_map):
        ev = ExpressionEvaluator('Arguments.a')
        assert ev.evaluate(values_map) == 'kirun '

    def test_string_index_access(self, values_map):
        ev = ExpressionEvaluator('Arguments.a[2]')
        assert ev.evaluate(values_map) == 'r'

    def test_string_negative_index(self, values_map):
        ev = ExpressionEvaluator('Arguments.a[-2]')
        assert ev.evaluate(values_map) == 'n'

    def test_array_range(self, values_map):
        ev = ExpressionEvaluator('Arguments.arr[2..4]')
        assert ev.evaluate(values_map) == [2, 3]

    def test_string_range(self, values_map):
        ev = ExpressionEvaluator('Arguments.a[2..4]')
        assert ev.evaluate(values_map) == 'ru'

    def test_string_range_with_expression(self, values_map):
        ev = ExpressionEvaluator('Arguments.a[(4-2)..(6-2)]')
        assert ev.evaluate(values_map) == 'ru'

    def test_string_range_from_start(self, values_map):
        ev = ExpressionEvaluator('Arguments.a[..4]')
        assert ev.evaluate(values_map) == 'kiru'

    def test_string_range_to_end(self, values_map):
        ev = ExpressionEvaluator('Arguments.a[2..]')
        assert ev.evaluate(values_map) == 'run '

    def test_array_range_from_start(self, values_map):
        ev = ExpressionEvaluator('Arguments.arr[..4]')
        assert ev.evaluate(values_map) == [0, 1, 2, 3]

    def test_array_range_with_expression(self, values_map):
        ev = ExpressionEvaluator('Arguments.arr[(4-2)..7]')
        assert ev.evaluate(values_map) == [2, 3, 4, 5, 6]

    def test_string_range_negative_end(self, values_map):
        ev = ExpressionEvaluator('Arguments.a[..-4]')
        assert ev.evaluate(values_map) == 'ki'

    def test_string_range_negative_end_expression(self, values_map):
        ev = ExpressionEvaluator('Arguments.a[..(-8+4)]')
        assert ev.evaluate(values_map) == 'ki'

    def test_string_range_negative_both(self, values_map):
        ev = ExpressionEvaluator('Arguments.a[-4..-1]')
        assert ev.evaluate(values_map) == 'run'

    def test_string_range_negative_to_end(self, values_map):
        ev = ExpressionEvaluator('Arguments.a[-4..]')
        assert ev.evaluate(values_map) == 'run '

    def test_array_range_negative_end(self, values_map):
        ev = ExpressionEvaluator('Arguments.arr[..-1]')
        assert ev.evaluate(values_map) == [0, 1, 2, 3, 4, 5]

    def test_array_range_negative_start(self, values_map):
        ev = ExpressionEvaluator('Arguments.arr[-2..]')
        assert ev.evaluate(values_map) == [5, 6]
