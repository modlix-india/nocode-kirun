from __future__ import annotations

import pytest
from kirun_py.function.system.math.minimum import Minimum
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


min_fn = Minimum()


@pytest.mark.asyncio
async def test_minimum_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': [3, 2, 3, 5, 3]})

    result = (await min_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == 2


@pytest.mark.asyncio
async def test_minimum_2_string_raises():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': ['3', 2, 3, 5, 3]})

    with pytest.raises(Exception):
        await min_fn.execute(fep)


@pytest.mark.asyncio
async def test_minimum_3():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': [-1, -2, -3, -5, -3]})

    result = (await min_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == -5
