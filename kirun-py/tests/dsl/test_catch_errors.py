"""`CATCH ERRORS`: the step's `catchErrors` (a kirun-rust runtime extension), compiled and
printed back. The two words reserve nothing: a step named CATCH still parses."""

import pytest

from kirun_py.dsl.dsl_compiler import DSLCompiler

SOURCE = '''FUNCTION Catching
    NAMESPACE Test
    LOGIC
        call: Test.Child(value = 1) AFTER Steps.start.output CATCH ERRORS /* the job */
        start: System.Make(resultShape = {})
        CATCH: System.Make(resultShape = {}) AFTER Steps.call.error
        plain: System.Make(resultShape = {}) AFTER Steps.CATCH.output'''


@pytest.mark.asyncio
async def test_compiles_to_the_property_and_prints_back():
    json = DSLCompiler.compile(SOURCE)
    steps = json['steps']
    assert steps['call']['catchErrors'] is True
    assert steps['call']['comment'] == 'the job'
    assert steps['call']['dependentStatements'] == {'Steps.start.output': True}
    for other in ('start', 'CATCH', 'plain'):
        assert 'catchErrors' not in steps[other], other
    assert steps['CATCH']['dependentStatements'] == {'Steps.call.error': True}

    printed = await DSLCompiler.decompile(json)
    assert 'AFTER Steps.start.output CATCH ERRORS /* the job */' in printed, printed
    assert printed.count('CATCH ERRORS') == 1, printed
    again = DSLCompiler.compile(printed)
    assert again['steps']['call']['catchErrors'] is True
    assert await DSLCompiler.decompile(again) == printed


@pytest.mark.asyncio
async def test_a_definition_without_it_prints_as_before():
    json = DSLCompiler.compile(SOURCE.replace(' CATCH ERRORS', ''))
    assert all('catchErrors' not in s for s in json['steps'].values())
    assert 'CATCH ERRORS' not in await DSLCompiler.decompile(json)
