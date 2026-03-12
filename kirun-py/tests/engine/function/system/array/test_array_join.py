from __future__ import annotations

import pytest

from kirun_py.function.system.array.join import Join
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository

OUTPUT = 'result'


def make_fep(args: dict) -> FunctionExecutionParameters:
    return (
        FunctionExecutionParameters(
            KIRunFunctionRepository(),
            KIRunSchemaRepository(),
        )
        .set_arguments(args)
        .set_steps({})
        .set_context({})
    )


@pytest.mark.asyncio
async def test_join_test_1():
    join = Join()

    array = [
        'test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
        'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement',
    ]

    fep = make_fep({'source': array, 'delimiter': ' '})

    expected = (
        'test Driven developement I am using eclipse I to test '
        'the changes with test Driven developement'
    )

    result = (await join.execute(fep)).all_results()[0].get_result()
    assert result[OUTPUT] == expected


@pytest.mark.asyncio
async def test_join_test_without_delimiter():
    join = Join()

    array = [
        'test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
        'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement',
    ]

    fep = make_fep({'source': array})

    expected = 'testDrivendevelopementIamusingeclipseItotestthechangeswithtestDrivendevelopement'

    result = (await join.execute(fep)).all_results()[0].get_result()
    assert result[OUTPUT] == expected


@pytest.mark.asyncio
async def test_join_test_with_mixed_data_types():
    join = Join()

    array = [
        'test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
        'I', 'to', 'test', 'the', 'changes', 'with', 'test', 4, 5,
    ]

    fep = make_fep({'source': array})

    expected = 'testDrivendevelopementIamusingeclipseItotestthechangeswithtest45'

    result = (await join.execute(fep)).all_results()[0].get_result()
    assert result[OUTPUT] == expected


@pytest.mark.asyncio
async def test_join_test_with_none():
    join = Join()

    array = [
        'test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
        'I', 'to', 'test', 'the', None, 'with', 'test', 4, 5,
    ]

    fep = make_fep({'source': array})

    expected = 'testDrivendevelopementIamusingeclipseItotestthewithtest45'

    result = (await join.execute(fep)).all_results()[0].get_result()
    assert result[OUTPUT] == expected
