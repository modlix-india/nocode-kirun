from __future__ import annotations

import pytest
from kirun_py.function.system.string.string_function_repository import StringFunctionRepository
from kirun_py.function.system.string.abstract_string_function import AbstractStringFunction
from kirun_py.function.system.string.reverse import Reverse
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


repo = StringFunctionRepository()

# ---------------------------------------------------------------------------
# StringFunctionRepository tests
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_string_repo_trim():
    fun = await repo.find(Namespaces.STRING, 'Trim')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({AbstractStringFunction.PARAMETER_STRING_NAME: ' Kiran '})

    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result == 'Kiran'


@pytest.mark.asyncio
async def test_string_repo_repeat():
    fun = await repo.find(Namespaces.STRING, 'Repeat')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        AbstractStringFunction.PARAMETER_STRING_NAME: ' surendhar ',
        AbstractStringFunction.PARAMETER_INDEX_NAME: 3,
    })

    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result == ' surendhar  surendhar  surendhar '


@pytest.mark.asyncio
async def test_string_repo_lowercase():
    fun = await repo.find(Namespaces.STRING, 'LowerCase')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({AbstractStringFunction.PARAMETER_STRING_NAME: ' SURENDHAR '})

    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result == ' surendhar '


@pytest.mark.asyncio
async def test_string_repo_uppercase():
    fun = await repo.find(Namespaces.STRING, 'UpperCase')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({AbstractStringFunction.PARAMETER_STRING_NAME: ' surendhar '})

    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result == ' SURENDHAR '


@pytest.mark.asyncio
async def test_string_repo_is_blank_true():
    fun = await repo.find(Namespaces.STRING, 'IsBlank')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({AbstractStringFunction.PARAMETER_STRING_NAME: ''})

    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is True


@pytest.mark.asyncio
async def test_string_repo_is_blank_false():
    fun = await repo.find(Namespaces.STRING, 'IsBlank')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({AbstractStringFunction.PARAMETER_STRING_NAME: ' this is a string'})

    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is False


@pytest.mark.asyncio
async def test_string_repo_is_empty_true():
    fun = await repo.find(Namespaces.STRING, 'IsEmpty')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({AbstractStringFunction.PARAMETER_STRING_NAME: ''})

    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is True


@pytest.mark.asyncio
async def test_string_repo_is_empty_false():
    fun = await repo.find(Namespaces.STRING, 'IsEmpty')
    assert fun is not None

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({AbstractStringFunction.PARAMETER_STRING_NAME: ' '})

    result = (await fun.execute(fep)).all_results()[0].get_result()[AbstractStringFunction.EVENT_RESULT_NAME]
    assert result is False


# ---------------------------------------------------------------------------
# Reverse tests (appended from ReverseTest.ts)
# ---------------------------------------------------------------------------

reve = Reverse()


@pytest.mark.asyncio
async def test_reverse_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': ' mr"ofta"lp edoc on a si sihT'})

    result = (await reve.execute(fep)).all_results()[0].get_result()['value']
    assert result == 'This is a no code pl"atfo"rm '


@pytest.mark.asyncio
async def test_reverse_2():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': ' '})

    result = (await reve.execute(fep)).all_results()[0].get_result()['value']
    assert result == ' '
