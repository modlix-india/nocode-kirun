from __future__ import annotations

import pytest
from kirun_py.function.system.math.math_function_repository import MathFunctionRepository
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


math_repo = MathFunctionRepository()


@pytest.mark.asyncio
async def test_math_functions_1_ceiling():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': 1.2})

    func = await math_repo.find(Namespaces.MATH, 'Ceiling')
    assert func is not None
    result = (await func.execute(fep)).all_results()[0].get_result()['value']
    assert result == 2


@pytest.mark.asyncio
async def test_math_functions_2_absolute_string_raises():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': '-1.2'})

    func = await math_repo.find(Namespaces.MATH, 'Absolute')
    assert func is not None
    with pytest.raises(Exception):
        await func.execute(fep)


@pytest.mark.asyncio
async def test_math_functions_3_arc_cosine_not_found():
    func = await math_repo.find(Namespaces.MATH, 'ACosine')
    assert func is None


@pytest.mark.asyncio
async def test_math_functions_4_wrong_namespace():
    result = await math_repo.find(Namespaces.STRING, 'ASine')
    assert result is None


@pytest.mark.asyncio
async def test_math_functions_5_arc_tangent_not_found():
    func = await math_repo.find(Namespaces.MATH, 'ATangent')
    assert func is None


@pytest.mark.asyncio
async def test_math_functions_6_cosine():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': 1})

    func = await math_repo.find(Namespaces.MATH, 'Cosine')
    assert func is not None
    result = (await func.execute(fep)).all_results()[0].get_result()['value']
    assert result == pytest.approx(0.5403023058681398)


@pytest.mark.asyncio
async def test_math_functions_7_power():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value1': 2, 'value2': 3})

    func = await math_repo.find(Namespaces.MATH, 'Power')
    assert func is not None
    result = (await func.execute(fep)).all_results()[0].get_result()['value']
    assert result == 8


@pytest.mark.asyncio
async def test_math_functions_8_power_string_raises():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value1': '1', 'value2': '1'})

    func = await math_repo.find(Namespaces.MATH, 'Power')
    assert func is not None
    with pytest.raises(Exception):
        await func.execute(fep)


@pytest.mark.asyncio
async def test_math_functions_9_add():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': [3, 2, 3, 5, 3]})

    func = await math_repo.find(Namespaces.MATH, 'Add')
    assert func is not None
    result = (await func.execute(fep)).all_results()[0].get_result()['value']
    assert result == 16


@pytest.mark.asyncio
async def test_math_functions_10_hypotenuse():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': [3, 2]})

    func = await math_repo.find(Namespaces.MATH, 'Hypotenuse')
    assert func is not None
    result = (await func.execute(fep)).all_results()[0].get_result()['value']
    assert result == pytest.approx(3.605551275463989)
