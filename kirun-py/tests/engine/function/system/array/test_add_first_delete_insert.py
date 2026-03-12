from __future__ import annotations

import pytest

from kirun_py.function.system.array.add_first import AddFirst
from kirun_py.function.system.array.delete import Delete
from kirun_py.function.system.array.delete_first import DeleteFirst
from kirun_py.function.system.array.delete_from import DeleteFrom
from kirun_py.function.system.array.delete_last import DeleteLast
from kirun_py.function.system.array.insert import Insert
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository

SOURCE = AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name()
ELEMENT = AbstractArrayFunction.PARAMETER_ANY.get_parameter_name()
VAR_ARGS = AbstractArrayFunction.PARAMETER_ANY_VAR_ARGS.get_parameter_name()
OFFSET = AbstractArrayFunction.PARAMETER_INT_OFFSET.get_parameter_name()
SRC_FROM = AbstractArrayFunction.PARAMETER_INT_SOURCE_FROM.get_parameter_name()
LENGTH = AbstractArrayFunction.PARAMETER_INT_LENGTH.get_parameter_name()
RESULT = AbstractArrayFunction.EVENT_RESULT_NAME


def make_fep(args: dict) -> FunctionExecutionParameters:
    return FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments(args)


# ---------------------------------------------------------------------------
# AddFirst tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_add_first_test_1():
    add_first = AddFirst()
    source = ['c', 'p', 3, 4, 5]
    expected = ['a', 'c', 'p', 3, 4, 5]
    fep = make_fep({SOURCE: source, ELEMENT: 'a'})
    result = (await add_first.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_add_first_test_2():
    add_first = AddFirst()
    source = ['a', 'b', 'c', 'd', 'a', 'b', 'c', 'e', 'd']
    expected = ['surendhar', 'a', 'b', 'c', 'd', 'a', 'b', 'c', 'e', 'd']
    fep = make_fep({SOURCE: source, ELEMENT: 'surendhar'})
    result = (await add_first.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_add_first_test_3():
    add_first = AddFirst()
    source = ['doing', 'test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']
    expected = ['surendhar', 'doing', 'test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']
    fep = make_fep({SOURCE: source, ELEMENT: 'surendhar'})
    result = (await add_first.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_add_first_test_4_null_source_raises():
    add_first = AddFirst()
    # null source should raise
    fep = make_fep({SOURCE: None, ELEMENT: 'a'})
    with pytest.raises(Exception):
        await add_first.execute(fep)


@pytest.mark.asyncio
async def test_add_first_test_5_null_element():
    add_first = AddFirst()
    source = ['a', 'c', 'p', None, 3, 4, 5]
    expected = [None, 'a', 'c', 'p', None, 3, 4, 5]
    fep = make_fep({SOURCE: list(source), ELEMENT: None})
    result = (await add_first.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_add_first_test_array_of_arrays():
    add_first = AddFirst()
    source1 = ['doing', 'test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']
    source2 = source1 + ['with', 'typescript']
    source3 = source2 + ['newone', 'framework']
    source4 = ['am', 'using', 'eclipse', 'I', 'to', 'with', 'typescript', 'newone']

    source = [source1, source4, source1, source2, source3, source4, source1, source2, source3]
    obj = {'fname': 'surendhar', 'lname': 's', 'age': 23, 'company': ' Fincity Corporation '}
    expected = [obj, source1, source4, source1, source2, source3, source4, source1, source2, source3]

    fep = make_fep({SOURCE: source, ELEMENT: obj})
    result = (await add_first.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


# ---------------------------------------------------------------------------
# DeleteFirst tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_delete_first_test_1():
    df = DeleteFirst()
    fep = make_fep({SOURCE: [12, 14, 15, 9]})
    result = (await df.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == [14, 15, 9]


@pytest.mark.asyncio
async def test_delete_first_test_2():
    df = DeleteFirst()
    fep = make_fep({SOURCE: ['c', 'p', 'i', 'e']})
    result = (await df.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == ['p', 'i', 'e']


@pytest.mark.asyncio
async def test_delete_first_test_3_empty_raises():
    df = DeleteFirst()
    fep = make_fep({SOURCE: []})
    with pytest.raises(Exception):
        await df.execute(fep)


@pytest.mark.asyncio
async def test_delete_first_test_4_objects():
    df = DeleteFirst()
    array1 = ['test', 'Driven', 'developement', 'I', 'am']
    js1 = {'boolean': False, 'array': array1, 'char': 'o'}
    js2 = {'boolean': False, 'array': array1, 'char': 'asd'}
    js3 = {'array': array1}
    js4 = {'boolean': False, 'array': array1, 'char': 'ocbfr'}
    arr = [js1, js2, js3, js4, js1]
    expected = [js2, js3, js4, js1]
    fep = make_fep({SOURCE: arr})
    result = (await df.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_delete_first_test_5_null_raises():
    df = DeleteFirst()
    fep = make_fep({SOURCE: None})
    with pytest.raises(Exception):
        await df.execute(fep)


# ---------------------------------------------------------------------------
# DeleteLast tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_delete_last_test_1():
    dl = DeleteLast()
    fep = make_fep({SOURCE: [12, 14, 15, 9]})
    result = (await dl.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == [12, 14, 15]


@pytest.mark.asyncio
async def test_delete_last_test_2():
    dl = DeleteLast()
    fep = make_fep({SOURCE: ['c', 'p', 'i', 'e']})
    result = (await dl.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == ['c', 'p', 'i']


@pytest.mark.asyncio
async def test_delete_last_test_3_empty_raises():
    dl = DeleteLast()
    fep = make_fep({SOURCE: []})
    with pytest.raises(Exception):
        await dl.execute(fep)


@pytest.mark.asyncio
async def test_delete_last_test_4_objects():
    dl = DeleteLast()
    array1 = ['test', 'Driven', 'developement', 'I', 'am']
    js1 = {'boolean': False, 'array': array1, 'char': 'o'}
    js2 = {'boolean': False, 'array': array1, 'char': 'asd'}
    js3 = {'array': array1}
    js4 = {'boolean': False, 'array': array1, 'char': 'ocbfr'}
    arr = [js1, js2, js3, js4, js1]
    expected = [js1, js2, js3, js4]
    fep = make_fep({SOURCE: arr})
    result = (await dl.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_delete_last_test_5_null_raises():
    dl = DeleteLast()
    fep = make_fep({SOURCE: None})
    with pytest.raises(Exception):
        await dl.execute(fep)


# ---------------------------------------------------------------------------
# Delete tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_delete_test_1():
    d = Delete()
    source = [12, 14, 15, 9]
    to_delete = [14, 15]
    expected = [12, 9]
    fep = make_fep({SOURCE: source, VAR_ARGS: to_delete})
    result = (await d.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_delete_test_2():
    d = Delete()
    source = ['nocode', 'platform', 14]
    to_delete = ['platform']
    expected = ['nocode', 14]
    fep = make_fep({SOURCE: source, VAR_ARGS: to_delete})
    result = (await d.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_delete_test_3_null_source_raises():
    d = Delete()
    fep = make_fep({SOURCE: None, VAR_ARGS: ['platform']})
    with pytest.raises(Exception):
        await d.execute(fep)


@pytest.mark.asyncio
async def test_delete_test_3b_null_var_args_raises():
    d = Delete()
    fep = make_fep({SOURCE: ['platform'], VAR_ARGS: None})
    with pytest.raises(Exception):
        await d.execute(fep)


@pytest.mark.asyncio
async def test_delete_test_4_empty_var_args_raises():
    d = Delete()
    fep = make_fep({SOURCE: ['platform'], VAR_ARGS: []})
    with pytest.raises(Exception):
        await d.execute(fep)


@pytest.mark.asyncio
async def test_delete_test_5_objects():
    d = Delete()
    arr1 = ['nocode', 'platform', 14]
    arr2 = ['nocode', 'platiform', 14]
    obj = {'arr': arr1, 'sri': 'krishna', 'name': 'surendhar'}

    arr = [arr1, arr2, obj, arr2, obj]
    del_arr = [obj, '2', []]
    expected = [arr1, arr2, arr2]

    fep = make_fep({SOURCE: arr, VAR_ARGS: del_arr})
    result = (await d.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


# ---------------------------------------------------------------------------
# DeleteFrom tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_delete_from_1():
    df = DeleteFrom()
    arr = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'a', 'a', 'a']
    expected = ['a', 'b', 'c', 'd', 'e', 'f']
    fep = make_fep({SOURCE: arr, SRC_FROM: 6, LENGTH: 6})
    result = (await df.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_delete_from_2():
    df = DeleteFrom()
    arr = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'a', 'a', 'a']
    expected = ['a', 'b', 'c', 'd', 'e', 'f', 'a', 'a', 'a']
    fep = make_fep({SOURCE: arr, SRC_FROM: 6, LENGTH: 3})
    result = (await df.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_delete_from_3_mixed():
    df = DeleteFrom()
    array1 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    array2 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with']
    array3 = array1  # same content as array1
    array4 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']
    obj = {'fname': 'surendhar', 'lname': 's', 'age': 23, 'company': 'fincity'}

    arr = [obj, array2, array4, array1, array1, array3, array2, array4, array1, array1, array4]
    expected = [obj, array2, array4, array1, array1, array1, array1, array4]

    fep = make_fep({SOURCE: arr, SRC_FROM: 5, LENGTH: 3})
    result = (await df.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_delete_from_4_no_length():
    df = DeleteFrom()
    arr = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'a', 'a', 'a']
    expected = ['a', 'b', 'c']
    # No LENGTH provided means delete from srcFrom to end (length default -1)
    fep = make_fep({SOURCE: arr, SRC_FROM: 3})
    result = (await df.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


# ---------------------------------------------------------------------------
# Insert tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_insert_test_1():
    ins = Insert()
    array = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    expected = ['test', 'Driven', 'developement', 'I', ['this is an array'], 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement']
    fep = make_fep({SOURCE: array, OFFSET: 4, ELEMENT: ['this is an array']})
    result = (await ins.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_insert_test_2():
    ins = Insert()
    arr = [1, 2, 3, 4, 5, 6, 7, 8]
    expected = [1, 2, ['this is an array'], 3, 4, 5, 6, 7, 8]
    fep = make_fep({SOURCE: arr, OFFSET: 2, ELEMENT: ['this is an array']})
    result = (await ins.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_insert_test_3_offset_zero():
    ins = Insert()
    arr = [1, 2, 3, 4, 5, 6, 7, 8]
    expected = [['this is an array'], 1, 2, 3, 4, 5, 6, 7, 8]
    fep = make_fep({SOURCE: arr, OFFSET: 0, ELEMENT: ['this is an array']})
    result = (await ins.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_insert_test_4_offset_end():
    ins = Insert()
    arr = [1, 2, 3, 4, 5, 6, 7, 8]
    expected = [1, 2, 3, 4, 5, 6, 7, 8, ['this is an array']]
    fep = make_fep({SOURCE: arr, OFFSET: len(arr), ELEMENT: ['this is an array']})
    result = (await ins.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_insert_test_5_empty_array():
    ins = Insert()
    expected = [['this is an array']]
    fep = make_fep({SOURCE: [], ELEMENT: ['this is an array']})
    result = (await ins.execute(fep)).all_results()[0].get_result()
    assert result[RESULT] == expected


@pytest.mark.asyncio
async def test_insert_test_6_null_source_raises():
    ins = Insert()
    fep = make_fep({SOURCE: None, OFFSET: 0, ELEMENT: ['this is an array']})
    with pytest.raises(Exception):
        await ins.execute(fep)
