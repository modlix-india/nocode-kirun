from __future__ import annotations

import pytest

from kirun_py.function.system.array.concatenate import Concatenate
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


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
async def test_concatenate_test_1():
    add = Concatenate()

    source = [2, 2, 3, 4, 5]
    second_source = [2, 2, 2, 3, 4, 5]
    expected = [2, 2, 3, 4, 5, 2, 2, 2, 3, 4, 5]

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): source,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): second_source,
    })

    result = (await add.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == expected


@pytest.mark.asyncio
async def test_concatenate_test_2():
    add = Concatenate()

    source = ['nocode', 'platform']
    second_source = []
    expected = ['nocode', 'platform']

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): source,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): second_source,
    })

    result = (await add.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == expected


@pytest.mark.asyncio
async def test_concatenate_test_3():
    add = Concatenate()

    source = []
    second_source = []
    expected = []

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): source,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): second_source,
    })

    result = (await add.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == expected


@pytest.mark.asyncio
async def test_concatenate_test_4_null_source_raises():
    add = Concatenate()

    second_source = []

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): None,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): second_source,
    })

    with pytest.raises(Exception):
        await add.execute(fep)


@pytest.mark.asyncio
async def test_concatenate_test_5_null_second_source_raises():
    add = Concatenate()

    source = []

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): source,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): None,
    })

    with pytest.raises(Exception):
        await add.execute(fep)
