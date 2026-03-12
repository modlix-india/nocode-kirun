from __future__ import annotations

import pytest
from kirun_py.function.system.math.math_function_repository import MathFunctionRepository
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


# ---------------------------------------------------------------------------
# RandomFloat tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_random_float_1():
    rand = await MathFunctionRepository().find(Namespaces.MATH, 'RandomFloat')
    assert rand is not None

    min_val = 1
    max_val = 10
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'minValue': min_val, 'maxValue': max_val})

    num = (await rand.execute(fep)).all_results()[0].get_result()['value']
    assert num >= min_val
    assert num <= max_val


@pytest.mark.asyncio
async def test_random_float_2():
    rand = await MathFunctionRepository().find(Namespaces.MATH, 'RandomFloat')
    assert rand is not None

    min_val = 0.1
    max_val = 0.9
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'minValue': min_val, 'maxValue': max_val})

    num = (await rand.execute(fep)).all_results()[0].get_result()['value']
    assert num >= min_val
    assert num <= max_val


@pytest.mark.asyncio
async def test_random_float_3_only_min():
    rand = await MathFunctionRepository().find(Namespaces.MATH, 'RandomFloat')
    assert rand is not None

    min_val = 1
    max_val = 2147483647
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'minValue': min_val})

    num = (await rand.execute(fep)).all_results()[0].get_result()['value']
    assert num >= min_val
    assert num <= max_val


@pytest.mark.asyncio
async def test_random_float_4():
    rand = await MathFunctionRepository().find(Namespaces.MATH, 'RandomFloat')
    assert rand is not None

    min_val = 1.1
    max_val = 1.2
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'minValue': min_val, 'maxValue': max_val})

    num = (await rand.execute(fep)).all_results()[0].get_result()['value']
    assert num >= min_val
    assert num <= max_val


# ---------------------------------------------------------------------------
# RandomInt tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_random_int_1():
    rand = await MathFunctionRepository().find(Namespaces.MATH, 'RandomInt')
    assert rand is not None

    min_val = 100
    max_val = 1000123
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'minValue': min_val, 'maxValue': max_val})

    num = (await rand.execute(fep)).all_results()[0].get_result()['value']
    assert num >= min_val
    assert num <= max_val


@pytest.mark.asyncio
async def test_random_int_2_only_min():
    rand = await MathFunctionRepository().find(Namespaces.MATH, 'RandomInt')
    assert rand is not None

    min_val = 100
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'minValue': min_val})

    num = (await rand.execute(fep)).all_results()[0].get_result()['value']
    assert num >= min_val
    assert num <= 2147483647


@pytest.mark.asyncio
async def test_random_int_3():
    rand = await MathFunctionRepository().find(Namespaces.MATH, 'RandomInt')
    assert rand is not None

    min_val = 100
    max_val = 101
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'minValue': min_val, 'maxValue': max_val})

    num = (await rand.execute(fep)).all_results()[0].get_result()['value']
    assert num >= min_val
    assert num <= max_val
