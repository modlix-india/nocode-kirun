from __future__ import annotations

import pytest
import pytest_asyncio
from kirun_py.function.system.context.set_function import SetFunction
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.runtime.context_element import ContextElement
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


@pytest.mark.asyncio
async def test_set_function_nested_path():
    set_function = SetFunction()

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )

    context_map = {'a': ContextElement(element={})}
    fep.set_context(context_map)
    fep.set_arguments({
        'name': 'Context.a.b',
        'value': 20,
    })

    await set_function.execute(fep)
    assert context_map['a'].get_element()['b'] == 20

    fep.set_arguments({
        'name': 'Context.a.c[2].d',
        'value': 25,
    })

    await set_function.execute(fep)
    assert context_map['a'].get_element()['c'][2]['d'] == 25


@pytest.mark.asyncio
async def test_set_function_array_element():
    set_function = SetFunction()

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )

    context_map = {'a': ContextElement(element=[])}
    fep.set_context(context_map)
    fep.set_arguments({
        'name': 'Context.a[1]',
        'value': 240,
    })

    await set_function.execute(fep)
    assert context_map['a'].get_element()[1] == 240


@pytest.mark.asyncio
async def test_set_function_object_array_nested():
    set_function = SetFunction()

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )

    context_map = {'a': ContextElement(element={})}
    fep.set_context(context_map)
    fep.set_arguments({
        'name': 'Context.a.b[1]',
        'value': 240,
    })

    await set_function.execute(fep)
    assert context_map['a'].get_element()['b'][1] == 240
