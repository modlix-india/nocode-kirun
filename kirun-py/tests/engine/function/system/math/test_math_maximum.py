from __future__ import annotations

import pytest
from kirun_py.function.system.math.maximum import Maximum
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


max_fn = Maximum()


@pytest.mark.asyncio
async def test_maximum_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': [3, 2, 3, 5, 3]})

    result = (await max_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == 5


@pytest.mark.asyncio
async def test_maximum_2():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': [-1, -1, 0, -1, -2]})

    result = (await max_fn.execute(fep)).all_results()[0].get_result()['value']
    assert result == 0


@pytest.mark.asyncio
async def test_maximum_3_string_raises():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': ['-1', -1, 0, -1, -2]})

    with pytest.raises(Exception):
        await max_fn.execute(fep)
