from __future__ import annotations

import pytest

from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.function.system.array.binary_search import BinarySearch
from kirun_py.function.system.array.frequency import Frequency
from kirun_py.function.system.array.last_index_of import LastIndexOf
from kirun_py.function.system.array.index_of_array import IndexOfArray
from kirun_py.function.system.array.last_index_of_array import LastIndexOfArray
from kirun_py.function.system.array.mis_match import MisMatch
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


def make_fep(args: dict) -> FunctionExecutionParameters:
    return FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments(args)


# ---------------------------------------------------------------------------
# BinarySearch tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_binary_search_1():
    bsearch = BinarySearch()
    src = [1, 4, 6, 7, 10, 14, 16, 20]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): src,
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 1,
        AbstractArrayFunction.PARAMETER_FIND_PRIMITIVE.get_parameter_name(): 16,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 6,
    })
    result = (await bsearch.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_INDEX_NAME] == 6


@pytest.mark.asyncio
async def test_binary_search_2():
    bsearch = BinarySearch()
    src = [1, 4, 6, 7, 10, 14, 16, 20]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): src,
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 1,
        AbstractArrayFunction.PARAMETER_FIND_PRIMITIVE.get_parameter_name(): 78,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): len(src) - 2,
    })
    result = (await bsearch.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_INDEX_NAME] == -1


@pytest.mark.asyncio
async def test_binary_search_3():
    """Length exceeding array bounds should raise."""
    bsearch = BinarySearch()
    src = [1, 4, 6, 7, 10, 14, 16, 20]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): src,
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 1,
        AbstractArrayFunction.PARAMETER_FIND_PRIMITIVE.get_parameter_name(): 78,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 100,
    })
    with pytest.raises(Exception):
        await bsearch.execute(fep)


@pytest.mark.asyncio
async def test_binary_search_6():
    """Search value beyond subset range returns -1."""
    bsearch = BinarySearch()
    src = [1, 4, 6, 7, 10, 14, 17, 20]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): src,
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 0,
        AbstractArrayFunction.PARAMETER_FIND_PRIMITIVE.get_parameter_name(): 17,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 5,
    })
    result = (await bsearch.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_INDEX_NAME] == -1


@pytest.mark.asyncio
async def test_binary_search_4():
    """String array, search for last element."""
    bsearch = BinarySearch()
    src = ['a', 'b', 'd', 'f', 'h', 'k', 'z']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): src,
        AbstractArrayFunction.PARAMETER_FIND_PRIMITIVE.get_parameter_name(): 'z',
    })
    result = (await bsearch.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_INDEX_NAME] == len(src) - 1


@pytest.mark.asyncio
async def test_binary_search_5():
    """Search for 's' within first len-1 elements of a longer char array."""
    bsearch = BinarySearch()
    arr = ['a', 'b', 'c', 'd', 'e', 'g', 'i', 'j', 'k', 'r', 's', 'z']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_FIND_PRIMITIVE.get_parameter_name(): 's',
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): len(arr) - 1,
    })
    result = (await bsearch.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_INDEX_NAME] == 10


# ---------------------------------------------------------------------------
# Frequency tests
# ---------------------------------------------------------------------------

FREQ_ARRAY = [
    'test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
    'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement',
]


@pytest.mark.asyncio
async def test_frequency_1():
    freq = Frequency()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(FREQ_ARRAY),
        AbstractArrayFunction.PARAMETER_ANY.get_parameter_name(): 'I',
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 2,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 10,
    })
    result = (await freq.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 2


@pytest.mark.asyncio
async def test_frequency_2():
    """Negative srcFrom raises; length exceeding array also raises."""
    freq = Frequency()

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(FREQ_ARRAY),
        AbstractArrayFunction.PARAMETER_ANY.get_parameter_name(): 'developement',
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): -2,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 20,
    })
    with pytest.raises(Exception):
        await freq.execute(fep)

    fep.set_arguments({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(FREQ_ARRAY),
        AbstractArrayFunction.PARAMETER_ANY.get_parameter_name(): 'developement',
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 2,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 20,
    })
    with pytest.raises(Exception):
        await freq.execute(fep)


@pytest.mark.asyncio
async def test_frequency_3():
    """Empty source array returns 0."""
    freq = Frequency()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): [],
        AbstractArrayFunction.PARAMETER_ANY.get_parameter_name(): 'I',
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 2,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 10,
    })
    result = (await freq.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 0


# ---------------------------------------------------------------------------
# LastIndexOf tests
# ---------------------------------------------------------------------------

LIND_ARRAY = [
    'test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
    'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement',
]


@pytest.mark.asyncio
async def test_last_index_of_1():
    lind = LastIndexOf()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(LIND_ARRAY),
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): 'test',
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 2,
    })
    result = (await lind.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 13


@pytest.mark.asyncio
async def test_last_index_of_2():
    """Negative findFrom with non-empty array raises."""
    lind = LastIndexOf()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(LIND_ARRAY),
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): None,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): -2,
    })
    with pytest.raises(Exception):
        await lind.execute(fep)


@pytest.mark.asyncio
async def test_last_index_of_3():
    """Null source array raises."""
    lind = LastIndexOf()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): None,
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): None,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 2,
    })
    with pytest.raises(Exception):
        await lind.execute(fep)


@pytest.mark.asyncio
async def test_last_index_of_4():
    lind = LastIndexOf()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(LIND_ARRAY),
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): 'developement',
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 12,
    })
    result = (await lind.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 15

    fep.set_arguments({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(LIND_ARRAY),
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): 'newas',
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 12,
    })
    result = (await lind.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == -1


@pytest.mark.asyncio
async def test_last_index_of_5():
    """Negative findFrom raises."""
    lind = LastIndexOf()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(LIND_ARRAY),
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): 'changes',
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): -2,
    })
    with pytest.raises(Exception):
        await lind.execute(fep)


@pytest.mark.asyncio
async def test_last_index_of_6():
    """Objects/dicts — find last occurrence of js4."""
    lind = LastIndexOf()
    array1 = ['test', 'Driven', 'developement', 'I', 'am']
    js1 = {'boolean': False, 'array': array1, 'char': 'o'}
    js2 = {'boolean': False, 'array': array1, 'char': 'asd'}
    js3 = {'array': array1}
    js4 = {'boolean': False, 'array': array1, 'char': 'asdsd'}

    arr = [js1, js2, js4, js3, js4, js1, js1]

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): js4,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 1,
    })
    result = (await lind.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 4


# ---------------------------------------------------------------------------
# IndexOfArray tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_index_of_array_1():
    ioa = IndexOfArray()
    arr = ['a', 'b', 'c', 'd', 'a', 'b', 'c', 'e', 'd']
    res = ['b', 'c', 'd']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 1,
    })
    result = (await ioa.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 1


@pytest.mark.asyncio
async def test_index_of_array_2():
    ioa = IndexOfArray()
    arr = ['a', 'b', 'c', 'd', 'a', 'b', 'c', 'e', 'd']
    res = ['b', 'c', 'e', 'd']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 4,
    })
    result = (await ioa.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 5


@pytest.mark.asyncio
async def test_index_of_array_3():
    ioa = IndexOfArray()
    array1 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
              'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    array2 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
              'I', 'to', 'test', 'the', 'changes', 'with']
    array3 = list(array1)
    array4 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']

    arr = [array2, array4, array1, array1, array3, array2, array4, array1, array1, array4]
    res = [array1, array1, array4]

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 2,
    })
    result = (await ioa.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 7


@pytest.mark.asyncio
async def test_index_of_array_4():
    ioa = IndexOfArray()
    arr = ['a', 'b', 'c', 'd', 'a', 'b', 'c', 'e', 'd']
    res = ['b', 'e', 'd']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 4,
    })
    result = (await ioa.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == -1


@pytest.mark.asyncio
async def test_index_of_array_5():
    ioa = IndexOfArray()
    arr = ['a', 'b', 'c', 'd', 'a', 'b', 'c', 'e', 'd']
    as_ = ['c', 'e']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): as_,
    })
    result = (await ioa.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 6


@pytest.mark.asyncio
async def test_index_of_array_6():
    """None/undefined second source raises."""
    ioa = IndexOfArray()
    arr = ['a', 'b', 'c']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): None,
    })
    with pytest.raises(Exception):
        await ioa.execute(fep)


@pytest.mark.asyncio
async def test_index_of_array_7():
    """findFrom equal to array length returns -1."""
    ioa = IndexOfArray()
    array1 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
              'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    array2 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
              'I', 'to', 'test', 'the', 'changes', 'with']
    array3 = list(array1)
    array4 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']

    arr = [array2, array4, array1, array1, array3, array2, array4, array1, array1, array4]
    res = [array1, array1, array4]

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): len(arr),
    })
    result = (await ioa.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == -1


# ---------------------------------------------------------------------------
# LastIndexOfArray tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_last_index_of_array_1():
    larr = LastIndexOfArray()
    array = ['a', 'b', 'c', 'd', 'a', 'b', 'c', 'e', 'd', 'b', 'c', 'd']
    res = ['b', 'c', 'd']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): array,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 1,
    })
    result = (await larr.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 9


@pytest.mark.asyncio
async def test_last_index_of_array_2():
    larr = LastIndexOfArray()
    arr = ['b', 'c', 'd', 'a', 'b', 'c', 'e', 'd', 'b', 'c', 'd']
    res = ['b', 'd']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 1,
    })
    result = (await larr.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == -1


@pytest.mark.asyncio
async def test_last_index_of_array_3():
    """Undefined/None second source raises; null source also raises."""
    larr = LastIndexOfArray()
    arr = ['a', 'b', 'c', 'd', 'a', 'b', 'c', 'e', 'd', 'b', 'c', 'd']

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): None,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 1,
    })
    with pytest.raises(Exception):
        await larr.execute(fep)

    fep1 = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): None,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): None,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 1,
    })
    with pytest.raises(Exception):
        await larr.execute(fep1)


@pytest.mark.asyncio
async def test_last_index_of_array_4():
    larr = LastIndexOfArray()
    array1 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
              'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    array2 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
              'I', 'to', 'test', 'the', 'changes', 'with']
    array3 = list(array1)
    array4 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']

    arr = [array2, array1, array1, array4, array3, array2, array4, array1, array1, array4]
    res = [array1, array1, array4]

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 2,
    })
    result = (await larr.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 7


# ---------------------------------------------------------------------------
# MisMatch tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_mismatch_1():
    mismatch = MisMatch()
    arr = ['a', 'b', 'c', 'd', 'l', 'd', 'a', 'b', 'c', 'e', 'd']
    res = ['b', 'c', 'd']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 7,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM.get_parameter_name(): 0,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 3,
    })
    result = (await mismatch.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 2


@pytest.mark.asyncio
async def test_mismatch_2():
    """Length exceeds second source bounds raises."""
    mismatch = MisMatch()
    arr = ['a', 'b', 'c', 'd', 'l', 'd', 'a', 'b', 'c', 'e', 'd']
    res = ['b', 'c', 'd']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 0,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM.get_parameter_name(): 2,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 5,
    })
    with pytest.raises(Exception):
        await mismatch.execute(fep)


@pytest.mark.asyncio
async def test_mismatch_3():
    """Nested array elements."""
    mismatch = MisMatch()
    array1 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
              'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    array2 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
              'I', 'to', 'test', 'the', 'changes', 'with']
    array3 = list(array1)
    array4 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']

    arr = [array2, array4, array1, array1, array1, array3, array2, array4, array1, array1, array4]
    res = [array1, array1, array4]

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 2,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM.get_parameter_name(): 3,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 3,
    })
    result = (await mismatch.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 2


@pytest.mark.asyncio
async def test_mismatch_4():
    """All elements match — result should be -1."""
    mismatch = MisMatch()
    arr = ['a', 'b', 'c', 'd', 'l', 'd', 'a', 'b', 'c', 'e', 'd']
    res = ['b', 'c', 'd']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 1,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): res,
        AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM.get_parameter_name(): 0,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 3,
    })
    result = (await mismatch.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == -1
