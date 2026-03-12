from __future__ import annotations

import pytest
from kirun_py.function.system.object.object_keys import ObjectKeys
from kirun_py.function.system.object.object_values import ObjectValues
from kirun_py.function.system.object.object_entries import ObjectEntries
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


def make_fep(source):
    return (
        FunctionExecutionParameters(KIRunFunctionRepository(), KIRunSchemaRepository())
        .set_arguments({'source': source})
    )


# --- ObjectKeys ---

@pytest.mark.asyncio
async def test_object_keys_dict():
    result = (await ObjectKeys().execute(make_fep({'a': 1, 'b': 2, 'd': ['a', 'b', 'c']}))).all_results()[0].get_result()['value']
    assert result == ['a', 'b', 'd']


@pytest.mark.asyncio
async def test_object_keys_null():
    result = (await ObjectKeys().execute(make_fep(None))).all_results()[0].get_result()['value']
    assert result == []


@pytest.mark.asyncio
async def test_object_keys_int():
    result = (await ObjectKeys().execute(make_fep(423))).all_results()[0].get_result()['value']
    assert result == []


@pytest.mark.asyncio
async def test_object_keys_bool():
    result = (await ObjectKeys().execute(make_fep(False))).all_results()[0].get_result()['value']
    assert result == []


@pytest.mark.asyncio
async def test_object_keys_string():
    result = (await ObjectKeys().execute(make_fep('surendhar'))).all_results()[0].get_result()['value']
    assert result == [str(i) for i in range(9)]


@pytest.mark.asyncio
async def test_object_keys_nested():
    obj = {'a': {'b': {'c': {'d': {'e': [1, 2, 4, 5]}}}}, 'c': ['q', 'w', 'e', 'r']}
    result = (await ObjectKeys().execute(make_fep(obj))).all_results()[0].get_result()['value']
    assert result == ['a', 'c']


@pytest.mark.asyncio
async def test_object_keys_list():
    obj = [1, 2, 3, 4, 1, 12, 3, 4, 5]
    result = (await ObjectKeys().execute(make_fep(obj))).all_results()[0].get_result()['value']
    assert result == [str(i) for i in range(9)]


# --- ObjectValues ---

@pytest.mark.asyncio
async def test_object_values_dict():
    obj = {'a': 1, 'b': 2, 'd': ['a', 'b', 'c']}
    result = (await ObjectValues().execute(make_fep(obj))).all_results()[0].get_result()['value']
    assert result == [1, 2, ['a', 'b', 'c']]


@pytest.mark.asyncio
async def test_object_values_null():
    result = (await ObjectValues().execute(make_fep(None))).all_results()[0].get_result()['value']
    assert result == []


@pytest.mark.asyncio
async def test_object_values_int():
    result = (await ObjectValues().execute(make_fep(423))).all_results()[0].get_result()['value']
    assert result == []


@pytest.mark.asyncio
async def test_object_values_string():
    result = (await ObjectValues().execute(make_fep('surendhar'))).all_results()[0].get_result()['value']
    assert result == list('surendhar')


@pytest.mark.asyncio
async def test_object_values_nested():
    obj = {'a': {'b': {'c': {'d': {'e': [1, 2, 4, 5]}}}}, 'c': ['q', 'w', 'e', 'r']}
    result = (await ObjectValues().execute(make_fep(obj))).all_results()[0].get_result()['value']
    assert result == [{'b': {'c': {'d': {'e': [1, 2, 4, 5]}}}}, ['q', 'w', 'e', 'r']]


@pytest.mark.asyncio
async def test_object_values_list():
    obj = [1, 2, 3, 4, 1, 12, 3, 4, 5]
    result = (await ObjectValues().execute(make_fep(obj))).all_results()[0].get_result()['value']
    assert result == [1, 2, 3, 4, 1, 12, 3, 4, 5]


# --- ObjectEntries ---

@pytest.mark.asyncio
async def test_object_entries_dict():
    obj = {'a': 1, 'b': 2}
    result = (await ObjectEntries().execute(make_fep(obj))).all_results()[0].get_result()['value']
    assert result == [['a', 1], ['b', 2]]


@pytest.mark.asyncio
async def test_object_entries_null():
    result = (await ObjectEntries().execute(make_fep(None))).all_results()[0].get_result()['value']
    assert result == []
