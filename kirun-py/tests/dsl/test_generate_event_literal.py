"""Port of kirun-js GenerateEventLiteral.spec.ts: a literal `value` in a GenerateEvent result
compiles to `{isExpression: false, value}`, prints back as the literal, and runs."""

import pytest

from kirun_py.dsl.dsl_compiler import DSLCompiler
from kirun_py.model.function_definition import FunctionDefinition
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.runtime.ki_runtime import KIRuntime

SOURCE = """FUNCTION Parity
    NAMESPACE Test
    PARAMETERS
        n AS INTEGER
    EVENTS
        output
            kind AS STRING
            n AS INTEGER
            tags AS ARRAY OF STRING
            sum AS INTEGER
    LOGIC
        out: System.GenerateEvent(results = {name: "kind", value: "odd"}, results = {name: "n", value: 5}, results = {name: "tags", value: ["a", "b"]}, results = {name: "sum", value: Arguments.n + 1})"""


def _results(fn):
    refs = sorted(fn['steps']['out']['parameterMap']['results'].values(), key=lambda r: r['order'])
    return [r['value'] for r in refs]


def test_a_literal_compiles_to_a_value_object():
    assert _results(DSLCompiler.compile(SOURCE)) == [
        {'name': 'kind', 'value': {'isExpression': False, 'value': 'odd'}},
        {'name': 'n', 'value': {'isExpression': False, 'value': 5}},
        {'name': 'tags', 'value': {'isExpression': False, 'value': ['a', 'b']}},
        {'name': 'sum', 'value': {'isExpression': True, 'value': 'Arguments.n + 1'}},
    ]


@pytest.mark.asyncio
async def test_prints_back_as_the_literal_and_round_trips():
    fn = DSLCompiler.compile(SOURCE)
    printed = await DSLCompiler.decompile(fn)
    assert '{name: "kind", value: "odd"}' in printed
    assert '{name: "n", value: 5}' in printed
    assert '{name: "tags", value: ["a", "b"]}' in printed
    assert _results(DSLCompiler.compile(printed)) == _results(fn)


@pytest.mark.asyncio
async def test_runs_and_returns_every_named_value():
    fd = FunctionDefinition.from_value(DSLCompiler.compile(SOURCE))
    out = (
        await KIRuntime(fd).execute(
            FunctionExecutionParameters(KIRunFunctionRepository(), KIRunSchemaRepository()).set_arguments({'n': 2})
        )
    ).all_results()
    result = out[0].get_result()
    assert result['kind'] == 'odd'
    assert result['n'] == 5
    assert result['tags'] == ['a', 'b']
    assert result['sum'] == 3
