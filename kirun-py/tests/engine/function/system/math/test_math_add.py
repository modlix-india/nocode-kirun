from __future__ import annotations

import pytest
from kirun_py.function.system.math.add import Add
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


add = Add()


@pytest.mark.asyncio
async def test_add_1():
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'value': [1, 2, 3, 4, 5, 6, 5.5]})

    result = (await add.execute(fep)).all_results()[0].get_result()['value']
    assert result == 26.5
