from __future__ import annotations

import pytest

from kirun_py.runtime.expression.expression import Expression


class TestExpression:
    """Ported from ExpressionTest.ts"""

    def test_expression_basic_arithmetic(self):
        assert str(Expression('2+3')) == '(2+3)'
        assert str(Expression('2.234 + 3 * 1.22243')) == '(2.234+(3*1.22243))'
        assert str(Expression('10*11+12*13*14/7')) == '((10*11)+(12*(13*(14/7))))'
        assert str(Expression('34 << 2 = 8 ')) == '((34<<2)=8)'

    def test_expression_context_with_steps(self):
        ex = Expression(
            'Context.a[Steps.loop.iteration.index - 1]+ Context.a[Steps.loop.iteration.index - 2]'
        )
        assert str(ex) == (
            '((Context.(a[((Steps.(loop.(iteration.index)))-1)]))'
            '+(Context.(a[((Steps.(loop.(iteration.index)))-2)])))'
        )

    def test_expression_steps_with_array_access(self):
        ex = Expression('Steps.step1.output.obj.array[Steps.step1.output.obj.num +1]+2')
        assert str(ex) == (
            '((Steps.(step1.(output.(obj.(array[((Steps.(step1.(output.(obj.num))))+1)])))))+2)'
        )

    def test_expression_multi_dimensional_array(self):
        arrays = Expression(
            'Context.a[Steps.loop.iteration.index][Steps.loop.iteration.index + 1]'
        )
        assert str(arrays) == (
            '(Context.(a[(Steps.(loop.(iteration.index)))]'
            '[((Steps.(loop.(iteration.index)))+1)]))'
        )

    def test_expression_deep_object(self):
        deep_object = Expression('Context.a.b.c')
        assert str(deep_object) == '(Context.(a.(b.c)))'

    def test_expression_deep_object_with_array(self):
        deep_object_with_array = Expression('Context.a.b[2].c')
        assert str(deep_object_with_array) == '(Context.(a.(b[2].c)))'

    def test_expression_operator_in_name(self):
        op_in_name = Expression('Store.a.b.c or Store.c.d.x')
        assert str(op_in_name) == '((Store.(a.(b.c)))or(Store.(c.(d.x))))'

    def test_expression_operator_concatenated_in_name(self):
        op_in_name = Expression('Store.a.b.corStore.c.d.x')
        assert str(op_in_name) == '(Store.(a.(b.(corStore.(c.(d.x))))))'

    def test_expression_bracket_notation_with_double_quotes(self):
        expr = Expression('Context.obj["mail.props.port"]')
        assert str(expr) == '(Context.(obj["mail.props.port"]))'

    def test_expression_bracket_notation_with_single_quotes(self):
        expr = Expression("Context.obj['api.key.secret']")
        assert str(expr) == "(Context.(obj['api.key.secret']))"

    def test_expression_bracket_notation_with_comparison(self):
        expr = Expression('Context.obj["mail.props.port"] = 587')
        assert str(expr) == '((Context.(obj["mail.props.port"]))=587)'

        expr = Expression('Context.obj["mail.props.port"] != 500')
        assert str(expr) == '((Context.(obj["mail.props.port"]))!=500)'

        expr = Expression('Context.obj["mail.props.port"] > 500')
        assert str(expr) == '((Context.(obj["mail.props.port"]))>500)'

    def test_expression_bracket_notation_with_ternary(self):
        expr = Expression('Context.obj["mail.props.port"] > 500 ? "high" : "low"')
        assert str(expr) == '(((Context.(obj["mail.props.port"]))>500)?"high":"low")'

    def test_expression_bracket_dot_mix(self):
        expr = Expression('Context.obj["mail.props.port"].value')
        assert str(expr) == '(Context.(obj["mail.props.port"].value))'


class TestExpressionTernary:
    """Ported from ExpressionEvaluatorTernaryOperatorTest.ts - Expression parsing part."""

    def test_ternary_expression_parsing(self):
        exp = Expression('a > 10 ?  a - 2 : a + 3'.replace(' ', ''))
        assert str(exp) == '((a>10)?(a-2):(a+3))'

        exp = Expression('a > 10 ?  a - 2 : a + 3')
        assert str(exp) == '((a>10)?(a-2):(a+3))'

    def test_nested_ternary_expression_parsing(self):
        exp = Expression('a > 10 ? a > 15 ? a + 2 : a - 2 : a + 3')
        assert str(exp) == '((a>10)?((a>15)?(a+2):(a-2)):(a+3))'


class TestExpressionParsingStructure:
    """Ported from ExpressionParsingTest.ts - parsing / toString tests."""

    def test_expression_to_string_preserves_structure(self):
        expr1 = Expression('Page.dealData.size')
        s1 = str(expr1)
        assert 'Page' in s1
        assert 'dealData' in s1

        expr2 = Expression('Page.dealData.size < Page.dealData.totalElements')
        assert '<' in str(expr2)

        expr3 = Expression('Steps.floorWeekOne.output.value * 86400')
        assert '*' in str(expr3)

        expr4 = Expression("Parent.projectInfo.projectType ?? '-'")
        assert '??' in str(expr4)

    def test_all_original_expressions_parse_without_throwing(self):
        expressions = [
            'Steps.floorWeekOne.output.value * {{Page.secondsInDay}}',
            "Parent.projectInfo.projectType?? '-'",
            'Page.dealData.size <  Page.dealData.totalElements',
            "Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.value ?? {{Store.pageDefinition.{{Store.urlDetails.pageName}}.properties.title.name.location.expression}}",
        ]
        for expression in expressions:
            # Should not raise
            Expression(expression)

    def test_complex_nested_ternary_parses_without_throwing(self):
        expression = (
            '10 - {{{{Parent.id}} < 10 ? 1 : {{Parent.id}} < 100 ? 2 : '
            '{{Parent.id}} < 1000 ? 3 : {{Parent.id}} < 10000 ? 4 : '
            '{{Parent.id}} < 100000 ? 5 : {{Parent.id}} < 1000000 ? 6 : '
            '{{Parent.id}} < 10000000 ? 7 : {{Parent.id}} < 100000000 ? 8 : '
            '{{Parent.id}} < 1000000000 ? 9 :10}}'
        )
        expr = Expression(expression)
        assert expr is not None
        assert not expr.get_operations().is_empty()

    def test_expression_trimming(self):
        expr1 = Expression('   Page.value   ')
        assert expr1.get_expression() == 'Page.value'

        expr2 = Expression('  true ? "yes" : "no"  ')
        assert expr2.get_expression() == 'true ? "yes" : "no"'

    def test_nullish_coalescing_expression_parsing(self):
        expr = Expression("Parent.projectInfo.projectType?? '-'")
        assert expr is not None
        assert '??' in str(expr)

    def test_extra_spaces_in_comparison(self):
        expr = Expression('Page.dealData.size <  Page.dealData.totalElements')
        assert expr is not None
        assert '<' in str(expr)
