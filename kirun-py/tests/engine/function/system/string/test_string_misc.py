from __future__ import annotations

import pytest
from kirun_py.function.system.string.split import Split
from kirun_py.function.system.string.trim_to import TrimTo
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


# ---------------------------------------------------------------------------
# Split tests
# ---------------------------------------------------------------------------

spli = Split()


@pytest.mark.asyncio
async def test_split_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'string': 'I am using eclipse to test the changes with test Driven developement',
        'searchString': ' ',
    })

    expected = [
        'I', 'am', 'using', 'eclipse', 'to', 'test',
        'the', 'changes', 'with', 'test', 'Driven', 'developement',
    ]
    result = (await spli.execute(fep)).all_results()[0].get_result()['result']
    assert result == expected


@pytest.mark.asyncio
async def test_split_2():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'string': 'I am using eclipse to test the changes with test Driven developement',
        'searchString': 'e',
    })

    expected = [
        'I am using ',
        'clips',
        ' to t',
        'st th',
        ' chang',
        's with t',
        'st Driv',
        'n d',
        'v',
        'lop',
        'm',
        'nt',
    ]
    result = (await spli.execute(fep)).all_results()[0].get_result()['result']
    assert result == expected


# ---------------------------------------------------------------------------
# TrimTo tests
# ---------------------------------------------------------------------------

trim = TrimTo()


@pytest.mark.asyncio
async def test_trim_to_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        TrimTo.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        TrimTo.PARAMETER_LENGTH_NAME: 14,
    })

    result = (await trim.execute(fep)).all_results()[0].get_result()[TrimTo.EVENT_RESULT_NAME]
    assert result == ' THIScompatY I'


@pytest.mark.asyncio
async def test_trim_to_2():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        TrimTo.PARAMETER_STRING_NAME: ' THIScompatY IS A NOcoDE plATFNORM',
        TrimTo.PARAMETER_LENGTH_NAME: 0,
    })

    result = (await trim.execute(fep)).all_results()[0].get_result()[TrimTo.EVENT_RESULT_NAME]
    assert result == ''
