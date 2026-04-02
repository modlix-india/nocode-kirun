from __future__ import annotations

import pytest
import pytest_asyncio
from kirun_py.function.system.if_ import If
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


if_function = If()


@pytest.mark.asyncio
async def test_if_true():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fo = await if_function.execute(fep.set_arguments({'condition': True}))
    assert fo.all_results()[0].get_name() == 'true'


@pytest.mark.asyncio
async def test_if_false():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fo = await if_function.execute(fep.set_arguments({'condition': False}))
    assert fo.all_results()[0].get_name() == 'false'


@pytest.mark.asyncio
async def test_if_none():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fo = await if_function.execute(fep.set_arguments({'condition': None}))
    assert fo.all_results()[0].get_name() == 'false'


@pytest.mark.asyncio
async def test_if_empty_string():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    # empty string is truthy in KIRun (condition_value = bool(condition) or condition == '')
    fo = await if_function.execute(fep.set_arguments({'condition': ''}))
    assert fo.all_results()[0].get_name() == 'true'


@pytest.mark.asyncio
async def test_if_space_string():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fo = await if_function.execute(fep.set_arguments({'condition': ' '}))
    assert fo.all_results()[0].get_name() == 'true'


@pytest.mark.asyncio
async def test_if_nonempty_string():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fo = await if_function.execute(fep.set_arguments({'condition': 'abc'}))
    assert fo.all_results()[0].get_name() == 'true'


@pytest.mark.asyncio
async def test_if_zero():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fo = await if_function.execute(fep.set_arguments({'condition': 0}))
    assert fo.all_results()[0].get_name() == 'false'


@pytest.mark.asyncio
async def test_if_one():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fo = await if_function.execute(fep.set_arguments({'condition': 1}))
    assert fo.all_results()[0].get_name() == 'true'


@pytest.mark.asyncio
async def test_if_negative_one():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fo = await if_function.execute(fep.set_arguments({'condition': -1}))
    assert fo.all_results()[0].get_name() == 'true'


@pytest.mark.asyncio
async def test_if_empty_dict():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    # JS: Boolean({}) is true — empty objects are truthy in JS
    fo = await if_function.execute(fep.set_arguments({'condition': {}}))
    assert fo.all_results()[0].get_name() == 'true'


@pytest.mark.asyncio
async def test_if_empty_list():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    # JS: Boolean([]) is true — empty arrays are truthy in JS
    fo = await if_function.execute(fep.set_arguments({'condition': []}))
    assert fo.all_results()[0].get_name() == 'true'
