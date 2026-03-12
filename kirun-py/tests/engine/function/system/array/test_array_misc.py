from __future__ import annotations

import pytest

from kirun_py.function.system.array.reverse import Reverse
from kirun_py.function.system.array.sub_array import SubArray
from kirun_py.function.system.array.copy import Copy
from kirun_py.function.system.array.fill import Fill
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository

SOURCE = AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
SRC_FROM = AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name()
FIND_FROM = AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name()
LENGTH = AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
DEEP_COPY = AbstractArrayFunction.PARAMETER_BOOLEAN_DEEP_COPY.get_parameter_name()
ELEMENT = AbstractArrayFunction.PARAMETER_ANY.get_parameter_name()
RESULT = AbstractArrayFunction.EVENT_RESULT_NAME


def make_fep(args: dict) -> FunctionExecutionParameters:
    return FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments(args)


# ---------------------------------------------------------------------------
# Reverse tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_reverse_test_1():
    rev = Reverse()
    src = [4, 5, 6, 7]
    expected = [5, 4, 6, 7]
    fep = make_fep({SOURCE: src, SRC_FROM: 0, LENGTH: 2})
    result = (await rev.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_reverse_test_2_negative_length_raises():
    rev = Reverse()
    src = ['I', 'am', 'using', 'eclipse', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    fep = make_fep({SOURCE: src, LENGTH: -2})
    with pytest.raises(Exception):
        await rev.execute(fep)


@pytest.mark.asyncio
async def test_reverse_test_3_length_exceeds_raises():
    rev = Reverse()
    arr = ['a', 'b', 'c', 'd', 'a', 'b', 'c', 'd']
    fep = make_fep({SOURCE: arr, SRC_FROM: 2, LENGTH: len(arr)})
    with pytest.raises(Exception):
        await rev.execute(fep)


@pytest.mark.asyncio
async def test_reverse_test_4():
    rev = Reverse()
    array1 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    array2 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with']
    array3 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    array4 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']

    arr = [array1, array3, array2, array4, array1]
    expected1 = [array1, array4, array2, array3, array1]

    fep = make_fep({SOURCE: arr, SRC_FROM: 1, LENGTH: len(arr) - 2})
    arr_result = (await rev.execute(fep)).all_results()[0].get_result()[RESULT]
    assert arr_result == expected1

    # Reverse again on the result
    expected2 = [array1, array1, array3, array2, array4]
    fep2 = make_fep({SOURCE: arr_result, SRC_FROM: 1, LENGTH: len(arr_result) - 1})
    result2 = (await rev.execute(fep2)).all_results()[0].get_result()
    assert result2[RESULT] == expected2


@pytest.mark.asyncio
async def test_reverse_test_5():
    rev = Reverse()
    arr = ['a', 'b', 'a', 'c', 'd', 'a', 'b', 'c', 'd']
    expected = ['a', 'b', 'd', 'c', 'b', 'a', 'd', 'c', 'a']
    fep = make_fep({SOURCE: list(arr), SRC_FROM: 2})
    result = (await rev.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected

    # length == len(arr) should raise
    fep2 = make_fep({SOURCE: list(arr), SRC_FROM: 2, LENGTH: len(arr)})
    with pytest.raises(Exception):
        await rev.execute(fep2)


# ---------------------------------------------------------------------------
# SubArray tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_subarray_test_1():
    sub = SubArray()
    array = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    expected = ['am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with']
    fep = make_fep({SOURCE: array, FIND_FROM: 4, LENGTH: 9})
    result = (await sub.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_subarray_test_2_none_source_raises():
    sub = SubArray()
    fep = make_fep({SOURCE: None, FIND_FROM: 4, LENGTH: 9})
    with pytest.raises(Exception):
        await sub.execute(fep)


@pytest.mark.asyncio
async def test_subarray_test_3_out_of_bounds_raises():
    sub = SubArray()
    array = ['a', 'b', 'c', 'd', 'l', 'd', 'a', 'b', 'c', 'e', 'd']
    fep = make_fep({SOURCE: array, FIND_FROM: 1123, LENGTH: 7})
    with pytest.raises(Exception):
        await sub.execute(fep)


@pytest.mark.asyncio
async def test_subarray_test_4_no_find_from():
    sub = SubArray()
    array1 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    array2 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with']
    array3 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    array4 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']

    array = [array2, array4, array1, array1, array1, array3, array2, array4, array1, array1, array4]
    expected = [array2, array4, array1]

    # No FIND_FROM provided (default 0), length=3
    fep = make_fep({SOURCE: array, LENGTH: 3})
    result = (await sub.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_subarray_test_5():
    sub = SubArray()
    array = ['a', 'b', 'c', 'd', 'l', 'd', 'a', 'b', 'c', 'e', 'd']
    expected = ['b', 'c', 'd', 'l', 'd', 'a', 'b']
    fep = make_fep({SOURCE: array, FIND_FROM: 1, LENGTH: 7})
    result = (await sub.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


# ---------------------------------------------------------------------------
# Copy tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_copy_test():
    cp = Copy()

    # source too small: srcFrom=2, length=4, but source only has 5 elements → should raise
    source = [1, 2, 3, 4, 5]
    fep1 = make_fep({SOURCE: source, SRC_FROM: 2, LENGTH: 4})
    with pytest.raises(Exception):
        await cp.execute(fep1)

    # source now has 6 elements → should succeed
    source.append(6)
    expected = [3, 4, 5, 6]
    fep2 = make_fep({SOURCE: source, SRC_FROM: 2, LENGTH: 4})
    result = (await cp.execute(fep2)).all_results()[0].get_result()
    assert result[RESULT] == expected

    # Deep copy by default: objects are equal in value but not the same identity
    source2 = [{'name': 'Kiran'}, {'name': 'Kumar'}]
    fep3 = make_fep({SOURCE: source2})
    fo = await cp.execute(fep3)
    result3 = fo.all_results()[0].get_result()[RESULT]
    expected3 = [{'name': 'Kiran'}, {'name': 'Kumar'}]
    assert result3 == expected3
    # Deep copy: not the same object
    assert source2[0] is not result3[0]

    # Shallow copy: objects should be the same identity
    fep4 = make_fep({SOURCE: source2, DEEP_COPY: False})
    fo4 = await cp.execute(fep4)
    result4 = fo4.all_results()[0].get_result()[RESULT]
    assert source2[0] is result4[0]


# ---------------------------------------------------------------------------
# Fill tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_fill_test():
    fill = Fill()

    # Fill [0, 1] entirely with 3 → [3, 3]
    array = [0, 1]
    fep = make_fep({SOURCE: array, ELEMENT: 3})
    array = (await fill.execute(fep)).all_results()[0].get_result()[RESULT]
    assert array == [3, 3]

    # Fill starting from index 2, length 5 → extends array to [3, 3, 5, 5, 5, 5, 5]
    fep2 = make_fep({SOURCE: array, ELEMENT: 5, SRC_FROM: 2, LENGTH: 5})
    array = (await fill.execute(fep2)).all_results()[0].get_result()[RESULT]
    assert array == [3, 3, 5, 5, 5, 5, 5]

    # Fill starting from index 5, no length → fills from 5 to end [3, 3, 5, 5, 5, 25, 25]
    fep3 = make_fep({SOURCE: array, ELEMENT: 25, SRC_FROM: 5})
    array = (await fill.execute(fep3)).all_results()[0].get_result()[RESULT]
    assert array == [3, 3, 5, 5, 5, 25, 25]

    # Negative srcFrom should raise
    fep4 = make_fep({SOURCE: array, ELEMENT: 20, SRC_FROM: -1})
    with pytest.raises(Exception):
        await fill.execute(fep4)
