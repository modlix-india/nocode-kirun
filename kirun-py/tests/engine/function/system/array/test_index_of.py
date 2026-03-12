from __future__ import annotations

import pytest

from kirun_py.function.system.array.index_of import IndexOf
from kirun_py.function.system.array.abstract_array_function import AbstractArrayFunction
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository

ARRAY = [
    'test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse',
    'I', 'to', 'test', 'the', 'changes', 'with', 'test', 'Driven', 'developement',
]


def make_fep(args: dict) -> FunctionExecutionParameters:
    return FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments(args)


@pytest.mark.asyncio
async def test_index_of_test_1():
    ind = IndexOf()
    find = 'with'

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(ARRAY),
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): find,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 2,
    })

    result = (await ind.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 12


@pytest.mark.asyncio
async def test_index_of_test_2():
    ind = IndexOf()
    find = 'with'

    # Negative find_from on non-empty array raises exception
    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(ARRAY),
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): find,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): -2,
    })

    with pytest.raises(Exception):
        await ind.execute(fep)

    # Negative find_from on empty array returns -1
    fep.set_arguments({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): [],
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): find,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): -2,
    })

    result = (await ind.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == -1


@pytest.mark.asyncio
async def test_index_of_test_3():
    ind = IndexOf()
    find = 'witah'

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): list(ARRAY),
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): find,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 0,
    })

    result = (await ind.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == -1


@pytest.mark.asyncio
async def test_index_of_test_4():
    ind = IndexOf()

    array2 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to', 'test', 'the', 'changes', 'with']
    array3 = list(ARRAY)
    array4 = ['test', 'Driven', 'developement', 'I', 'am', 'using', 'eclipse', 'I', 'to']

    arr = [array3, list(ARRAY), array3, array2, array4, list(ARRAY)]
    find = list(ARRAY)

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): find,
        AbstractArrayFunction.PARAMETER_INT_FIND_FROM.get_parameter_name(): 2,
    })

    result = (await ind.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 2


@pytest.mark.asyncio
async def test_index_of_test_5():
    ind = IndexOf()

    array1 = ['test', 'Driven', 'developement', 'I', 'am']

    js1 = {'boolean': False, 'array': array1, 'char': 'o'}
    js2 = {'boolean': False, 'array': array1, 'char': 'asd'}
    js3 = {'array': array1}
    js4 = {'boolean': False, 'array': array1, 'char': 'oa'}

    arr = [js1, js2, js1, js3, js3, js4, js1]
    find = js4

    fep = make_fep({
        AbstractArrayFunction.PARAMETER_ARRAY_SOURCE.get_parameter_name(): arr,
        AbstractArrayFunction.PARAMETER_ANY_ELEMENT_OBJECT.get_parameter_name(): find,
    })

    result = (await ind.execute(fep)).all_results()[0].get_result()
    assert result[AbstractArrayFunction.EVENT_RESULT_NAME] == 5

    # Empty arguments should raise
    fep.set_arguments({})
    with pytest.raises(Exception):
        await ind.execute(fep)
