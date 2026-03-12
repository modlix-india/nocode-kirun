from __future__ import annotations

import pytest

from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.function.system.array.max_ import Max
from kirun_py.function.system.array.min_ import Min
from kirun_py.function.system.array.remove_duplicates import RemoveDuplicates
from kirun_py.function.system.array.rotate import Rotate
from kirun_py.function.system.array.compare import Compare
from kirun_py.function.system.array.disjoint import Disjoint
from kirun_py.function.system.array.equals import Equals
from kirun_py.function.system.array.array_to_object import ArrayToObject
from kirun_py.function.system.array.array_to_array_of_objects import ArrayToArrayOfObjects
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


def make_fep(args: dict) -> FunctionExecutionParameters:
    return FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments(args)


# ---------------------------------------------------------------------------
# Max tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_max_1():
    max_ = Max()
    arr = [None, 12]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
    })
    result = (await max_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 12


@pytest.mark.asyncio
async def test_max_2():
    """Empty array raises."""
    max_ = Max()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): [],
    })
    with pytest.raises(Exception):
        await max_.execute(fep)


@pytest.mark.asyncio
async def test_max_3():
    max_ = Max()
    arr = [12, 15, None, 98, 1]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
    })
    result = (await max_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 98


@pytest.mark.asyncio
async def test_max_4():
    max_ = Max()
    arr = ['nocode', 'NoCode', 'platform']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
    })
    result = (await max_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 'platform'


@pytest.mark.asyncio
async def test_max_6():
    """Null source raises."""
    max_ = Max()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): None,
    })
    with pytest.raises(Exception):
        await max_.execute(fep)


@pytest.mark.asyncio
async def test_max_5():
    """Mixed numeric and string array — string max wins."""
    max_ = Max()
    arr = [456, 'nocode', 'NoCode', 'platform', 123]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
    })
    result = (await max_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 'platform'


@pytest.mark.asyncio
async def test_max_7():
    max_ = Max()
    arr1 = ['c', 'r', 'd', 's']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr1,
    })
    result = (await max_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 's'


@pytest.mark.asyncio
async def test_max_8():
    max_ = Max()
    arr = ['surendhar']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
    })
    result = (await max_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 'surendhar'


# ---------------------------------------------------------------------------
# Min tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_min_1():
    min_ = Min()
    arr = [None, 12]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
    })
    result = (await min_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 12


@pytest.mark.asyncio
async def test_min_2():
    """Empty array raises."""
    min_ = Min()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): [],
    })
    with pytest.raises(Exception):
        await min_.execute(fep)


@pytest.mark.asyncio
async def test_min_3():
    min_ = Min()
    arr = [12, 15, None, 98, 1]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
    })
    result = (await min_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 1


@pytest.mark.asyncio
async def test_min_4():
    min_ = Min()
    arr = ['nocode', 'NoCode', 'platform']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
    })
    result = (await min_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 'NoCode'


@pytest.mark.asyncio
async def test_min_5():
    """Null source raises."""
    min_ = Min()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): None,
    })
    with pytest.raises(Exception):
        await min_.execute(fep)


@pytest.mark.asyncio
async def test_min_6():
    """Mixed numeric and string array — numeric min wins."""
    min_ = Min()
    arr = [456, 'nocode', 'NoCode', 'platform', 123, 1]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
    })
    result = (await min_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 1


@pytest.mark.asyncio
async def test_min_7():
    min_ = Min()
    arr1 = ['c', 'r', 'd', 's']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr1,
    })
    result = (await min_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 'c'


@pytest.mark.asyncio
async def test_min_8():
    min_ = Min()
    arr = ['surendhar']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE_PRIMITIVE.get_parameter_name(): arr,
    })
    result = (await min_.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 'surendhar'


# ---------------------------------------------------------------------------
# RemoveDuplicates tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_remove_duplicates():
    rd = RemoveDuplicates()

    source = [2, 2, 2, 2, 2]

    # Out-of-bounds range raises
    fep1 = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(source),
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 2,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 4,
    })
    with pytest.raises(Exception):
        await rd.execute(fep1)

    source.append(6)
    expected = [2, 2, 2, 6]

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(source),
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 2,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 4,
    })
    result = (await rd.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == expected

    source2 = [{'name': 'Kiran'}, {'name': 'Kiran'}, {'name': 'Kiran'}, {'name': 'Kumar'}]
    expected2 = [{'name': 'Kiran'}, {'name': 'Kumar'}]

    fep2 = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): source2,
    })
    result2 = (await rd.execute(fep2)).all_results()[0].get_result()
    assert result2[AbstractArrayFunction.EVENT_RESULT_NAME] == expected2


# ---------------------------------------------------------------------------
# Rotate tests
# ---------------------------------------------------------------------------

ROTATE_ARRAY = ['I', 'am', 'using', 'eclipse', 'to', 'test', 'the',
                'changes', 'with', 'test', 'Driven', 'developement']


@pytest.mark.asyncio
async def test_rotate_1():
    rotate = Rotate()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(ROTATE_ARRAY),
        AbstractArrayFunction.PARAMETER_ROTATE_LENGTH.get_parameter_name(): 16,
    })
    res = ['to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement',
           'I', 'am', 'using', 'eclipse']
    result = (await rotate.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == res


@pytest.mark.asyncio
async def test_rotate_2():
    rotate = Rotate()
    src = list(ROTATE_ARRAY)
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(src),
        AbstractArrayFunction.PARAMETER_ROTATE_LENGTH.get_parameter_name(): len(src) - 1,
    })
    res = ['developement', 'I', 'am', 'using', 'eclipse', 'to', 'test',
           'the', 'changes', 'with', 'test', 'Driven']
    result = (await rotate.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == res


@pytest.mark.asyncio
async def test_rotate_3():
    """Default rotateLength=1."""
    rotate = Rotate()
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(ROTATE_ARRAY),
    })
    res = ['am', 'using', 'eclipse', 'to', 'test', 'the', 'changes',
           'with', 'test', 'Driven', 'developement', 'I']
    result = (await rotate.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == res


# ---------------------------------------------------------------------------
# Compare tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_compare_1():
    """Direct call to _compare method."""
    compare = Compare()

    source = [2, 2, 3, 4, 5]
    find = [2, 2, 2, 3, 4, 5]
    assert compare._compare(source, 0, 2, find, 1, 3) == 0

    find = [2, 2, 3, 4, 5]
    assert compare._compare(source, 0, len(source), find, 0, len(find)) == 0

    source = [True, True]
    find = [True, None]
    assert compare._compare(source, 0, len(source), find, 0, len(find)) == 1


@pytest.mark.asyncio
async def test_compare_2():
    """Via execute: [4,5] vs [4,6] → 5-6 = -1."""
    compare = Compare()
    source = [4, 5]
    find = [4, 6]
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): source,
        AbstractArrayFunction.PARAMETER_ARRAY_FIND.get_parameter_name(): find,
    })
    result = (await compare.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 5 - 6


# ---------------------------------------------------------------------------
# Disjoint tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_disjoint_1():
    dis = Disjoint()
    arr = ['a', 'b', 'c', 'd', 'e', 'f']
    arr2 = ['a', 'b', 'p', 'a', 'f', 'f', 'e']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 2,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): arr2,
        AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM.get_parameter_name(): 0,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 4,
    })
    res = (await dis.execute(fep)).all_results()[0].get_result()
    result_set = set(res[AbstractArrayFunction.EVENT_RESULT_NAME])
    expected_set = {'c', 'd', 'e', 'f', 'a', 'b', 'p'}
    assert result_set == expected_set


@pytest.mark.asyncio
async def test_disjoint_2():
    """Negative srcFrom raises."""
    dis = Disjoint()
    arr = ['a', 'b', 'c', 'd', 'e', 'f']
    arr2 = ['a', 'b', 'a', 'b', 'c', 'd', 'e', 'f']
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): -12,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): arr2,
        AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM.get_parameter_name(): 2,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 3,
    })
    with pytest.raises(Exception):
        await dis.execute(fep)


@pytest.mark.asyncio
async def test_disjoint_3():
    """Object/dict elements — symmetric difference."""
    dis = Disjoint()
    array1 = ['test', 'Driven', 'developement', 'I', 'am']
    js1 = {'boolean': False, 'array': array1, 'char': 'o'}
    js2 = {'boolean': False, 'array': array1, 'char': 'asd'}
    js3 = {'array': array1}
    js4 = {'boolean': False, 'array': array1, 'char': 's'}
    js5 = {'boolean': False, 'array': array1, 'char': 'b'}
    js6 = {'booleasan': False, 'arraay': array1, 'char': 'o'}
    js7 = {'booleasan': False, 'arrrraay': array1, 'char': 'o'}

    arr = [js1, js2, js3, js4, js1]
    arr2 = [js5, js6, js7, js1]
    expected_items = [js2, js3, js4, js5, js6, js7]

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 1,
        AbstractArrayFunction.PARAMETER_ARRAY_SECOND_SOURCE.get_parameter_name(): arr2,
        AbstractArrayFunction.PARAMETER_INT_SECOND_SOURCE_FROM.get_parameter_name(): 0,
        AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name(): 4,
    })
    res_list = (await dis.execute(fep)).all_results()[0].get_result()[
        AbstractArrayFunction.EVENT_RESULT_NAME
    ]
    # Compare as sets of ids (same object references)
    assert set(id(x) for x in res_list) == set(id(x) for x in expected_items)


# ---------------------------------------------------------------------------
# Equals tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_equals():
    eq = Equals()

    src_array = [30, 31, 32, 33, 34]
    find_array = [30, 31, 32, 33, 34]

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(src_array),
        AbstractArrayFunction.PARAMETER_ARRAY_FIND.get_parameter_name(): list(find_array),
    })
    result = (await eq.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] is True

    find_array[1] = 41
    fep.set_arguments({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(src_array),
        AbstractArrayFunction.PARAMETER_ARRAY_FIND.get_parameter_name(): list(find_array),
    })
    result = (await eq.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] is False

    # Compare from offset 2 onward (src[2:] = [32,33,34], find[2:] = [41,33,34])
    # After mutation find_array[1]=41, so find_array=[30,41,32,33,34]
    # find_array[2:] = [32,33,34] which equals src_array[2:] = [32,33,34]
    find_array = [30, 41, 32, 33, 34]
    fep2 = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(src_array),
        AbstractArrayFunction.PARAMETER_ARRAY_FIND.get_parameter_name(): list(find_array),
        AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name(): 2,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 2,
    })
    result = (await eq.execute(fep2)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] is True

    src_array2 = [True, True, False]
    find_array2 = [True, True, False]
    fep3 = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): src_array2,
        AbstractArrayFunction.PARAMETER_ARRAY_FIND.get_parameter_name(): find_array2,
    })
    result = (await eq.execute(fep3)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] is True


# ---------------------------------------------------------------------------
# ArrayToObject tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_array_to_object():
    atoo = ArrayToObject()

    source = [
        {'name': 'A', 'num': 1},
        {'name': 'B', 'num': 2},
        None,
        {'name': 'C', 'num': 3},
        {'name': 'D', 'num': 4},
        {'name': 'E', 'num': 4},
        None,
    ]

    fep = make_fep({
        'source': source,
        'keyPath': 'name',
        'valuePath': 'num',
    })
    result = (await atoo.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == {'A': 1, 'B': 2, 'C': 3, 'D': 4, 'E': 4}

    fep2 = make_fep({
        'source': source,
        'keyPath': 'num',
        'valuePath': 'name',
    })
    result2 = (await atoo.execute(fep2)).all_results()[0].get_result()
    assert result2[AbstractArrayFunction.EVENT_RESULT_NAME] == {'1': 'A', '2': 'B', '3': 'C', '4': 'E'}

    fep3 = make_fep({
        'source': source,
        'keyPath': 'num',
        'valuePath': 'name',
        'ignoreDuplicateKeys': True,
    })
    result3 = (await atoo.execute(fep3)).all_results()[0].get_result()
    assert result3[AbstractArrayFunction.EVENT_RESULT_NAME] == {'1': 'A', '2': 'B', '3': 'C', '4': 'D'}


@pytest.mark.asyncio
async def test_array_to_object_invalid_key_path():
    atoo = ArrayToObject()
    source = [
        {'name': 'A', 'num': 1},
        {'name': 'B', 'num': 2},
        {'name': 'C', 'num': 3},
        {'name': 'D', 'num': 4},
        {'name': 'E', 'num': 4},
    ]

    fep = make_fep({
        'source': source,
        'keyPath': 'name1',
        'valuePath': 'num',
    })
    result = (await atoo.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == {}

    fep2 = make_fep({
        'source': source,
        'keyPath': 'name',
        'valuePath': 'num1',
    })
    result2 = (await atoo.execute(fep2)).all_results()[0].get_result()
    assert result2[AbstractArrayFunction.EVENT_RESULT_NAME] == {
        'A': None, 'B': None, 'C': None, 'D': None, 'E': None,
    }

    fep3 = make_fep({
        'source': source,
        'keyPath': 'name',
        'valuePath': 'num1',
        'ignoreNullValues': True,
    })
    result3 = (await atoo.execute(fep3)).all_results()[0].get_result()
    assert result3[AbstractArrayFunction.EVENT_RESULT_NAME] == {}


@pytest.mark.asyncio
async def test_array_to_object_deep_path():
    atoo = ArrayToObject()
    source = [
        {'name': 'A', 'num': 1, 'info': {'age': 10}},
        {'name': 'B', 'num': 2, 'info': {'age': 20}},
        {'name': 'C', 'num': 3, 'info': {'age': 30}},
        {'name': 'D', 'num': 4, 'info': {'age': 40}},
        {'name': 'E', 'num': 4, 'info': {'age': 50}},
    ]
    fep = make_fep({
        'source': source,
        'keyPath': 'info.age',
        'valuePath': 'name',
    })
    result = (await atoo.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == {
        '10': 'A', '20': 'B', '30': 'C', '40': 'D', '50': 'E',
    }


# ---------------------------------------------------------------------------
# ArrayToArrayOfObjects tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_array_to_array_of_objects_basic():
    fun = ArrayToArrayOfObjects()

    # Simple integers with default key
    fep = make_fep({'source': [1, 2, 3]})
    result = (await fun.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == [
        {'value': 1}, {'value': 2}, {'value': 3},
    ]

    # With custom key name
    fep2 = make_fep({'source': [1, 2, 3], 'keyName': ['number']})
    result2 = (await fun.execute(fep2)).all_results()[0].get_result()
    assert result2[AbstractArrayFunction.EVENT_RESULT_NAME] == [
        {'number': 1}, {'number': 2}, {'number': 3},
    ]

    # Nested objects become value
    fep3 = make_fep({'source': [{'number': 1}, {'number': 2}, {'number': 3}]})
    result3 = (await fun.execute(fep3)).all_results()[0].get_result()
    assert result3[AbstractArrayFunction.EVENT_RESULT_NAME] == [
        {'value': {'number': 1}},
        {'value': {'number': 2}},
        {'value': {'number': 3}},
    ]

    # Nested arrays without key — default value1/value2 names
    fep4 = make_fep({'source': [['a', 1], ['b', 2], ['c', '3']]})
    result4 = (await fun.execute(fep4)).all_results()[0].get_result()
    assert result4[AbstractArrayFunction.EVENT_RESULT_NAME] == [
        {'value1': 'a', 'value2': 1},
        {'value1': 'b', 'value2': 2},
        {'value1': 'c', 'value2': '3'},
    ]

    # Nested arrays with extra key names (padded with None)
    fep5 = make_fep({'source': [['a', 1], ['b', 2], ['c', '3']], 'keyName': ['key', 'value', 'other']})
    result5 = (await fun.execute(fep5)).all_results()[0].get_result()
    assert result5[AbstractArrayFunction.EVENT_RESULT_NAME] == [
        {'key': 'a', 'value': 1, 'other': None},
        {'key': 'b', 'value': 2, 'other': None},
        {'key': 'c', 'value': '3', 'other': None},
    ]

    # Only first key used for 2-element sub-arrays
    fep6 = make_fep({'source': [['a', 1], ['b', 2], ['c', '3']], 'keyName': ['maKey']})
    result6 = (await fun.execute(fep6)).all_results()[0].get_result()
    assert result6[AbstractArrayFunction.EVENT_RESULT_NAME] == [
        {'maKey': 'a'}, {'maKey': 'b'}, {'maKey': 'c'},
    ]


@pytest.mark.asyncio
async def test_array_to_array_of_objects_mixed():
    fun = ArrayToArrayOfObjects()
    fep = make_fep({
        'source': [True, 1, 2, ['a', 'b', 'c']],
        'keyName': ['akey', 'bkey', 'ckey'],
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == [
        {'akey': True},
        {'akey': 1},
        {'akey': 2},
        {'akey': 'a', 'bkey': 'b', 'ckey': 'c'},
    ]


@pytest.mark.asyncio
async def test_array_to_array_of_objects_mixed_no_key():
    fun = ArrayToArrayOfObjects()
    fep = make_fep({'source': [True, 1, 2, ['a', 'b', 'c']]})
    result = (await fun.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == [
        {'value': True},
        {'value': 1},
        {'value': 2},
        {'value1': 'a', 'value2': 'b', 'value3': 'c'},
    ]


@pytest.mark.asyncio
async def test_array_to_array_of_objects_deeply_nested_no_key():
    fun = ArrayToArrayOfObjects()
    fep = make_fep({'source': [True, 1, 2, ['a', 'b', 'c', ['d', 'e'], {'obj1': 'val1'}]]})
    result = (await fun.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == [
        {'value': True},
        {'value': 1},
        {'value': 2},
        {
            'value1': 'a',
            'value2': 'b',
            'value3': 'c',
            'value4': ['d', 'e'],
            'value5': {'obj1': 'val1'},
        },
    ]


@pytest.mark.asyncio
async def test_array_to_array_of_objects_with_key_arrays():
    fun = ArrayToArrayOfObjects()
    fep = make_fep({
        'source': [[True, False], [1, 'surendhar'], ['satya']],
        'keyName': ['valueA', 'valueB', 'valueC', 'valueD'],
    })
    result = (await fun.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == [
        {'valueA': True, 'valueB': False, 'valueC': None, 'valueD': None},
        {'valueA': 1, 'valueB': 'surendhar', 'valueC': None, 'valueD': None},
        {'valueA': 'satya', 'valueB': None, 'valueC': None, 'valueD': None},
    ]
