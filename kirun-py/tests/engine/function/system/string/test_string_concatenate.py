from __future__ import annotations

import pytest
from kirun_py.function.system.string.concatenate import Concatenate
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


cat = Concatenate()


@pytest.mark.asyncio
async def test_concatenate_1():
    array = [
        'I ', 'am ', 'using ', 'eclipse ', 'to ', 'test ',
        'the ', 'changes ', 'with ', 'test ', 'Driven ', 'developement',
    ]

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': array})

    result = (await cat.execute(fep)).all_results()[0].get_result()['value']
    assert result == 'I am using eclipse to test the changes with test Driven developement'


@pytest.mark.asyncio
async def test_concatenate_2():
    lst = [
        'no code ',
        ' Kirun ',
        ' true ',
        '"\'this is between the strings qith special characters\'"',
        ' PLATform ',
        '2',
    ]

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': lst})

    result = (await cat.execute(fep)).all_results()[0].get_result()['value']
    assert result == 'no code  Kirun  true "\'this is between the strings qith special characters\'" PLATform 2'
