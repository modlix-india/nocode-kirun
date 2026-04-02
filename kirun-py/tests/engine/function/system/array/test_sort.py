from __future__ import annotations

import pytest

from kirun_py.function.system.array.sort import Sort
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository

sort = Sort()

# Python Sort registers PARAMETER_ARRAY_SOURCE (parameter name: 'source')
SOURCE = AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
FIND_FROM = AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name()
LENGTH = AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
ASCENDING = AbstractArrayFunction.PARAMETER_BOOLEAN_ASCENDING.get_parameter_name()
KEY_PATH = AbstractArrayFunction.PARAMETER_KEY_PATH.get_parameter_name()
RESULT = AbstractArrayFunction.EVENT_RESULT_NAME


def make_fep(args: dict) -> FunctionExecutionParameters:
    return FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments(args)


@pytest.mark.asyncio
async def test_sort_test_1():
    arr = [12, 15, 98, 1]

    fep = make_fep({
        SOURCE: arr,
        FIND_FROM: 0,
        LENGTH: len(arr),
    })

    expected = [1, 12, 15, 98]
    result = (await sort.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_sort_test_2():
    arr = [12, 15, 98, 1]

    fep = make_fep({
        SOURCE: arr,
        FIND_FROM: 1,
        ASCENDING: False,
    })

    expected = [12, 98, 15, 1]
    result = (await sort.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_sort_test_3_mixed_types_raises():
    # JS can compare mixed int/str (string coercion), Python cannot.
    # Python raises TypeError when comparing int and str, which is
    # wrapped in KIRuntimeException by execute().
    arr = [12, 15, 98, 1, 'sure', 'c']

    fep = make_fep({
        SOURCE: arr,
        FIND_FROM: 2,
        ASCENDING: True,
    })

    with pytest.raises(Exception):
        await sort.execute(fep)


@pytest.mark.asyncio
async def test_sort_test_4_mixed_types_raises():
    # JS can compare mixed int/str/None, Python raises TypeError for int vs str.
    arr = [12, None, None, 15, 98, 1, 'sure', 'c']

    fep = make_fep({
        SOURCE: arr,
        FIND_FROM: 2,
        ASCENDING: False,
    })

    with pytest.raises(Exception):
        await sort.execute(fep)


@pytest.mark.asyncio
async def test_sort_test_5():
    arr = ['Banana', 'Orange', 'Apple', 'Mango']

    fep = make_fep({
        SOURCE: arr,
        FIND_FROM: 1,
        LENGTH: 3,
    })

    expected = ['Banana', 'Apple', 'Mango', 'Orange']
    result = (await sort.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_sort_test_6():
    arr = [
        {'order': 13},
        {'order': 3},
        {'order': 130},
        {'order': 10},
        {'order': 21},
        {'order': 1},
    ]

    fep = make_fep({
        SOURCE: arr,
        FIND_FROM: 0,
        LENGTH: len(arr),
        KEY_PATH: 'order',
    })

    expected = [
        {'order': 1},
        {'order': 3},
        {'order': 10},
        {'order': 13},
        {'order': 21},
        {'order': 130},
    ]
    result = (await sort.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_sort_test_7():
    arr = [
        {'order': {'order': 13}},
        {'order': {'order': 3}},
        {'order': {'order': 130}},
        {'order': {'order': 10}},
        {'order': {'order': 21}},
        {'order': {'order': 1}},
    ]

    fep = make_fep({
        SOURCE: arr,
        FIND_FROM: 0,
        LENGTH: len(arr),
        KEY_PATH: 'order.order',
    })

    expected = [
        {'order': {'order': 1}},
        {'order': {'order': 3}},
        {'order': {'order': 10}},
        {'order': {'order': 13}},
        {'order': {'order': 21}},
        {'order': {'order': 130}},
    ]
    result = (await sort.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_sort_test_8():
    arr = [
        {'order': {'order': 13}},
        {'order': {'order': 3}},
        {'order': {'order': 130}},
        {'order': {'order': 10}},
        {'order': {'order': 21}},
        {'order': {'order': 1}},
    ]

    fep = make_fep({
        SOURCE: arr,
        FIND_FROM: 0,
        LENGTH: len(arr),
        KEY_PATH: 'order.order',
        ASCENDING: False,
    })

    expected = [
        {'order': {'order': 130}},
        {'order': {'order': 21}},
        {'order': {'order': 13}},
        {'order': {'order': 10}},
        {'order': {'order': 3}},
        {'order': {'order': 1}},
    ]
    result = (await sort.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected
