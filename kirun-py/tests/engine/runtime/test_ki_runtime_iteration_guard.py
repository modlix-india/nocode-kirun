"""Regression tests for the runaway-execution guard.

The counter used to be incremented only when a queue size changed, which disabled the guard
in exactly the case it exists for: a graph making no progress leaves the queue sizes
untouched, so the counter never advanced and _execute_graph could spin forever. The check was
also `==` rather than `>=`, so a skipped boundary value would never trip it.
"""

from __future__ import annotations

import pytest

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.model.function_definition import FunctionDefinition
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.runtime.ki_runtime import KIRuntime


# A CountLoop with a step in its body, so execution takes many statement iterations.
LOOPING_DEFINITION = {
    'name': 'loops',
    'namespace': 'TestUI',
    'steps': {
        'loop': {
            'statementName': 'loop',
            'name': 'CountLoop',
            'namespace': 'System.Loop',
            'parameterMap': {
                'count': {'one': {'key': 'one', 'type': 'VALUE', 'value': 40}},
            },
        },
        'printer': {
            'statementName': 'printer',
            'name': 'Print',
            'namespace': 'System',
            'dependentStatements': {'Steps.loop.iteration': True},
            'parameterMap': {
                'values': {
                    'one': {
                        'key': 'one',
                        'type': 'EXPRESSION',
                        'expression': 'Steps.loop.iteration.index',
                    }
                },
            },
        },
    },
}


# A linear chain pops one vertex and pushes its successor, so the queue size is unchanged
# across passes even though real work happened. Under the old "only count when a queue size
# changed" rule the counter stayed at zero here, which is precisely why a stuck graph could
# never trip the guard.
LINEAR_CHAIN = {
    'name': 'linear',
    'namespace': 'TestUI',
    'steps': {
        'first': {
            'statementName': 'first',
            'name': 'Print',
            'namespace': 'System',
            'parameterMap': {'values': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'a'}}},
        },
        'second': {
            'statementName': 'second',
            'name': 'Print',
            'namespace': 'System',
            'dependentStatements': {'Steps.first.output': True},
            'parameterMap': {'values': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'b'}}},
        },
        'third': {
            'statementName': 'third',
            'name': 'Print',
            'namespace': 'System',
            'dependentStatements': {'Steps.second.output': True},
            'parameterMap': {'values': {'one': {'key': 'one', 'type': 'VALUE', 'value': 'c'}}},
        },
    },
}


def _params() -> FunctionExecutionParameters:
    return FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({})


@pytest.fixture(autouse=True)
def restore_cap():
    original = KIRuntime.MAX_EXECUTION_ITERATIONS
    yield original
    KIRuntime.MAX_EXECUTION_ITERATIONS = original


def test_default_cap_is_one_million(restore_cap):
    assert restore_cap == 1_000_000


async def test_loop_exceeding_the_cap_is_stopped():
    # Low enough that a 40-pass loop is guaranteed to cross it. If the counter were still
    # gated on a queue-size change this would run to completion instead of raising.
    KIRuntime.MAX_EXECUTION_ITERATIONS = 5

    with pytest.raises(KIRuntimeException, match='Execution locked in an infinite loop'):
        await KIRuntime(FunctionDefinition.from_value(LOOPING_DEFINITION)).execute(_params())


async def test_same_loop_completes_under_the_cap(restore_cap):
    KIRuntime.MAX_EXECUTION_ITERATIONS = restore_cap

    out = await KIRuntime(FunctionDefinition.from_value(LOOPING_DEFINITION)).execute(_params())

    assert out is not None


async def test_counts_passes_even_when_queue_size_never_changes():
    params = _params()

    await KIRuntime(FunctionDefinition.from_value(LINEAR_CHAIN)).execute(params)

    assert params.get_count() >= 3
