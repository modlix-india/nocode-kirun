"""The decompiler's layout. Port of kirun-js ReadableLayout.spec.ts.

`output` chains stay at one level, long calls take one argument per line,
expressions inside JSON print bare, and large objects move to a VALUES section
referenced as `@name`. None of it may change the compiled JSON.
"""

import re

import pytest

from kirun_py.dsl.dsl_compiler import DSLCompiler
from kirun_py.dsl.parser.dsl_parser_error import DSLParserError


def _shape(fn):
    """Steps with ParameterReference keys dropped and refs listed in order: keys are random per compile."""
    steps = {}
    for name, step in (fn.get('steps') or {}).items():
        parameter_map = {}
        for param, refs in (step.get('parameterMap') or {}).items():
            parameter_map[param] = [
                {k: v for k, v in ref.items() if k != 'key'}
                for ref in sorted(refs.values(), key=lambda r: r.get('order') or 0)
            ]
        steps[name] = {**step, 'parameterMap': parameter_map}
    return {**fn, 'steps': steps}


def _value(fn, step, param):
    return list(fn['steps'][step]['parameterMap'][param].values())[0]


async def _round_trip(text):
    json_obj = DSLCompiler.compile(text)
    printed = await DSLCompiler.decompile(json_obj)
    assert _shape(DSLCompiler.compile(printed)) == _shape(json_obj)
    return printed


SOURCE = '''FUNCTION activity
    NAMESPACE Test
    LOGIC
        c: Test.context()
        auth: System.If(condition = Steps.c.output.ctx.isAuthenticated = true) AFTER Steps.c.output
            true
                dg: Test.rowGet(ctx = Steps.c.output.ctx, ref = Arguments.id, storage = "deal", entity = "Ticket", mode = "strict") AFTER Steps.auth.true
                    error
                        dgE: System.GenerateEvent(eventName = "error", results = {"name": "status", "value": {"isExpression": true, "value": "Steps.dg.error.status"}}) AFTER Steps.dg.error
                    output
                        cfg: System.Make(resultShape = {"rels": [{"field": "taskId", "storage": "task", "spec": "task"}, {"field": "noteId", "storage": "note", "spec": "note"}]}) AFTER Steps.dg.output
                            output
                                done: System.GenerateEvent(results = {"name": "result", "value": {"isExpression": true, "value": "Steps.cfg.output.value"}}) AFTER Steps.cfg.output
            false
                authE: System.GenerateEvent(eventName = "error") AFTER Steps.auth.false'''


def _step(name, ns, fn, param_map):
    return {'statementName': name, 'namespace': ns, 'name': fn, 'parameterMap': param_map}


async def test_output_chains_print_at_one_level_branches_indent():
    printed = await _round_trip(SOURCE)

    def indent_of(step):
        line = next(l for l in printed.split('\n') if l.lstrip().startswith(f'{step}:'))
        return len(line) - len(line.lstrip())

    assert indent_of('auth') == indent_of('c')
    assert indent_of('cfg') == indent_of('dg')
    assert indent_of('done') == indent_of('dg')
    assert indent_of('dg') == indent_of('auth') + 8
    assert indent_of('dgE') == indent_of('dg') + 8
    assert indent_of('authE') == indent_of('dg')


async def test_expressions_inside_json_print_bare():
    printed = await _round_trip(SOURCE)
    assert 'results = {name: "status", value: Steps.dg.error.status}' in printed
    assert 'isExpression' not in printed


async def test_a_large_object_moves_to_values_and_compiles_to_the_inline_value():
    printed = await _round_trip(SOURCE)
    assert 'cfg: System.Make(resultShape = @cfg.resultShape) AFTER Steps.dg.output' in printed
    assert re.search(r'\n {4}VALUES\n {8}@cfg\.resultShape = \{', printed)
    assert _value(DSLCompiler.compile(printed), 'cfg', 'resultShape')['value'] == {
        'rels': [
            {'field': 'taskId', 'storage': 'task', 'spec': 'task'},
            {'field': 'noteId', 'storage': 'note', 'spec': 'note'},
        ],
    }


async def test_a_long_call_takes_one_argument_per_line():
    printed = await _round_trip(SOURCE)
    assert re.search(r'dg: Test\.rowGet\(\n\s+ctx = Steps\.c\.output\.ctx,\n', printed)
    assert re.search(r'\n\s+mode = "strict"\n\s+\) AFTER Steps\.auth\.true', printed)


def test_the_last_argument_before_a_closing_paren_on_its_own_line_does_not_keep_the_line_break():
    json_obj = DSLCompiler.compile('''FUNCTION f
    LOGIC
        a: System.Print(
            values = Arguments.x
        )''')
    assert _value(json_obj, 'a', 'values')['expression'] == 'Arguments.x'


async def test_trailing_spaces_on_one_line_are_kept():
    fn = {
        'name': 'f',
        'steps': {
            'a': _step('a', 'System', 'Print', {
                'values': {'k': {'key': 'k', 'type': 'EXPRESSION', 'expression': ' Arguments.x  ', 'order': 1}},
            }),
        },
    }
    back = DSLCompiler.compile(await DSLCompiler.decompile(fn))
    assert _value(back, 'a', 'values')['expression'] == ' Arguments.x  '


async def test_an_event_result_expression_the_parser_would_misread_goes_in_backticks():
    results = [
        {'name': 'a', 'value': {'isExpression': True, 'value': "'x' + Arguments.y"}},
        {'name': 'b', 'value': {'isExpression': True, 'value': '0 + 400'}},
        {'name': 'c', 'value': {'isExpression': True, 'value': 'max(1, 2)'}},
        {'name': 'd', 'value': {'isExpression': True, 'value': 'Arguments.a, Arguments.b'}},
        {'name': 'e', 'value': {'isExpression': True, 'value': 'true'}},
    ]
    fn = {
        'name': 'f',
        'steps': {
            'a': _step('a', 'System', 'GenerateEvent', {
                'results': {
                    f'k{i}': {'key': f'k{i}', 'type': 'VALUE', 'value': value, 'order': i + 1}
                    for i, value in enumerate(results)
                },
            }),
        },
    }
    printed = await DSLCompiler.decompile(fn)
    assert '{name: "a", value: `\'x\' + Arguments.y`}' in printed
    assert '{name: "b", value: `0 + 400`}' in printed
    assert '{name: "c", value: max(1, 2)}' in printed
    assert '{name: "d", value: `Arguments.a, Arguments.b`}' in printed
    assert '{name: "e", value: `true`}' in printed
    back = DSLCompiler.compile(printed)
    refs = sorted(back['steps']['a']['parameterMap']['results'].values(), key=lambda r: r['order'])
    assert [r['value'] for r in refs] == results


async def test_an_expression_object_outside_event_results_stays_written_out():
    # The runtime does not evaluate it there, so it must not look like a live expression
    value = {'title': {'isExpression': True, 'value': 'Page.t'}, 'done': True}
    fn = {
        'name': 'f',
        'steps': {
            'a': _step('a', 'CoreServices.Storage', 'Create', {
                'dataObject': {'k': {'key': 'k', 'type': 'VALUE', 'value': value, 'order': 1}},
            }),
        },
    }
    printed = await DSLCompiler.decompile(fn)
    assert 'dataObject = {title: {isExpression: true, value: "Page.t"}, done: true}' in printed
    assert _value(DSLCompiler.compile(printed), 'a', 'dataObject')['value'] == value


def test_a_value_used_twice_is_copied_not_shared():
    json_obj = DSLCompiler.compile('''FUNCTION f
    LOGIC
        a: System.Make(resultShape = @shape)
        b: System.Make(resultShape = @shape)
    VALUES
        @shape = {list: [1, 2]}''')
    a = _value(json_obj, 'a', 'resultShape')['value']
    b = _value(json_obj, 'b', 'resultShape')['value']
    assert a == {'list': [1, 2]}
    assert b == a
    assert b is not a


async def test_any_value_name_compiles_and_decompiling_renames_it_to_step_parameter():
    # Over INLINE_VALUE_WIDTH (100) on one line, so the printer moves it to VALUES
    shape = '{list: [1, 2], label: "a value long enough to be moved out of the step line", note: "by the printer"}'
    printed = await DSLCompiler.decompile(DSLCompiler.compile(f'''FUNCTION f
    LOGIC
        a: System.Make(resultShape = @shape)
        b: System.Make(resultShape = @shape)
    VALUES
        @shape = {shape}'''))
    assert 'a: System.Make(resultShape = @a.resultShape)' in printed
    assert 'b: System.Make(resultShape = @b.resultShape)' in printed
    assert re.search(r'\n {8}@a\.resultShape = \{', printed)
    assert re.search(r'\n {8}@b\.resultShape = \{', printed)
    assert '@shape' not in printed

    back = DSLCompiler.compile(printed)
    expected = {
        'list': [1, 2],
        'label': 'a value long enough to be moved out of the step line',
        'note': 'by the printer',
    }
    assert _value(back, 'a', 'resultShape')['value'] == expected
    assert _value(back, 'b', 'resultShape')['value'] == expected


def test_an_undefined_value_name_is_an_error():
    with pytest.raises(DSLParserError, match='Value @missing is not defined in VALUES'):
        DSLCompiler.compile('''FUNCTION f
    LOGIC
        a: System.Make(resultShape = @missing)''')


def test_values_is_still_an_ordinary_name_where_it_is_not_followed_by_at():
    json_obj = DSLCompiler.compile('''FUNCTION f
    LOGIC
        VALUES: System.Print(values = Arguments.x)''')
    assert json_obj['steps']['VALUES']['name'] == 'Print'
