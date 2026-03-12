from __future__ import annotations

import pytest
import pytest_asyncio
from kirun_py.function.system.make import Make
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository


make = Make()


def create_output_map(data: dict) -> dict:
    """Build steps map: {step1: {output: {key: value, ...}}}"""
    return {
        'step1': {
            'output': data,
        }
    }


@pytest.mark.asyncio
async def test_simple_object_with_expression():
    source = {'name': 'John', 'age': 30}

    result_shape = {
        'fullName': '{{Steps.step1.output.source.name}}',
        'userAge': '{{Steps.step1.output.source.age}}',
    }

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fep.set_arguments({'resultShape': result_shape})
    fep.set_context({})
    fep.set_steps(create_output_map({'source': source}))

    result = await make.execute(fep)
    value = result.all_results()[0].get_result()['value']

    assert value == {
        'fullName': 'John',
        'userAge': 30,
    }


@pytest.mark.asyncio
async def test_nested_object_with_expressions():
    source = {
        'user': {'firstName': 'John', 'lastName': 'Doe'},
        'address': {'city': 'NYC', 'zip': '10001'},
    }

    result_shape = {
        'person': {
            'name': '{{Steps.step1.output.source.user.firstName}}',
            'surname': '{{Steps.step1.output.source.user.lastName}}',
        },
        'location': {
            'cityName': '{{Steps.step1.output.source.address.city}}',
            'postalCode': '{{Steps.step1.output.source.address.zip}}',
        },
    }

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fep.set_arguments({'resultShape': result_shape})
    fep.set_context({})
    fep.set_steps(create_output_map({'source': source}))

    result = await make.execute(fep)
    value = result.all_results()[0].get_result()['value']

    assert value == {
        'person': {
            'name': 'John',
            'surname': 'Doe',
        },
        'location': {
            'cityName': 'NYC',
            'postalCode': '10001',
        },
    }


@pytest.mark.asyncio
async def test_array_with_expressions():
    source = {
        'items': ['apple', 'banana', 'cherry'],
    }

    result_shape = {
        'fruits': [
            '{{Steps.step1.output.source.items[0]}}',
            '{{Steps.step1.output.source.items[1]}}',
            '{{Steps.step1.output.source.items[2]}}',
        ],
    }

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fep.set_arguments({'resultShape': result_shape})
    fep.set_context({})
    fep.set_steps(create_output_map({'source': source}))

    result = await make.execute(fep)
    value = result.all_results()[0].get_result()['value']

    assert value == {
        'fruits': ['apple', 'banana', 'cherry'],
    }


@pytest.mark.asyncio
async def test_deeply_nested_structure_with_arrays():
    source = {
        'data': {
            'users': [
                {'id': 1, 'name': 'Alice'},
                {'id': 2, 'name': 'Bob'},
            ],
        },
    }

    result_shape = {
        'level1': {
            'level2': {
                'level3': {
                    'userList': [
                        {
                            'userId': '{{Steps.step1.output.source.data.users[0].id}}',
                            'userName': '{{Steps.step1.output.source.data.users[0].name}}',
                        },
                        {
                            'userId': '{{Steps.step1.output.source.data.users[1].id}}',
                            'userName': '{{Steps.step1.output.source.data.users[1].name}}',
                        },
                    ],
                },
            },
        },
    }

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fep.set_arguments({'resultShape': result_shape})
    fep.set_context({})
    fep.set_steps(create_output_map({'source': source}))

    result = await make.execute(fep)
    value = result.all_results()[0].get_result()['value']

    assert value == {
        'level1': {
            'level2': {
                'level3': {
                    'userList': [
                        {'userId': 1, 'userName': 'Alice'},
                        {'userId': 2, 'userName': 'Bob'},
                    ],
                },
            },
        },
    }


@pytest.mark.asyncio
async def test_mixed_static_and_dynamic_values():
    source = {'dynamicValue': 'from source'}

    result_shape = {
        'static': 'static string',
        'dynamic': '{{Steps.step1.output.source.dynamicValue}}',
        'nested': {
            'staticNum': 42,
            'dynamicNum': '{{Steps.step1.output.source.dynamicValue}}',
        },
        'array': ['static', '{{Steps.step1.output.source.dynamicValue}}', 123],
    }

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fep.set_arguments({'resultShape': result_shape})
    fep.set_context({})
    fep.set_steps(create_output_map({'source': source}))

    result = await make.execute(fep)
    value = result.all_results()[0].get_result()['value']

    assert value == {
        'static': 'static string',
        'dynamic': 'from source',
        'nested': {
            'staticNum': 42,
            'dynamicNum': 'from source',
        },
        'array': ['static', 'from source', 123],
    }


@pytest.mark.asyncio
async def test_null_handling():
    result_shape = {
        'nullValue': None,
        'nested': {
            'inner': None,
        },
    }

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fep.set_arguments({'resultShape': result_shape})
    fep.set_context({})
    fep.set_steps({})

    result = await make.execute(fep)
    value = result.all_results()[0].get_result()['value']

    assert value == {
        'nullValue': None,
        'nested': {
            'inner': None,
        },
    }


@pytest.mark.asyncio
async def test_array_of_arrays():
    source = {
        'matrix': [
            [1, 2],
            [3, 4],
        ],
    }

    result_shape = {
        'grid': [
            ['{{Steps.step1.output.source.matrix[0][0]}}', '{{Steps.step1.output.source.matrix[0][1]}}'],
            ['{{Steps.step1.output.source.matrix[1][0]}}', '{{Steps.step1.output.source.matrix[1][1]}}'],
        ],
    }

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fep.set_arguments({'resultShape': result_shape})
    fep.set_context({})
    fep.set_steps(create_output_map({'source': source}))

    result = await make.execute(fep)
    value = result.all_results()[0].get_result()['value']

    assert value == {
        'grid': [
            [1, 2],
            [3, 4],
        ],
    }


@pytest.mark.asyncio
async def test_primitive_result_shape():
    source = {'value': 'hello'}

    result_shape = '{{Steps.step1.output.source.value}}'

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fep.set_arguments({'resultShape': result_shape})
    fep.set_context({})
    fep.set_steps(create_output_map({'source': source}))

    result = await make.execute(fep)
    value = result.all_results()[0].get_result()['value']

    assert value == 'hello'


@pytest.mark.asyncio
async def test_array_as_root_result_shape():
    source = {'a': 1, 'b': 2, 'c': 3}

    result_shape = [
        '{{Steps.step1.output.source.a}}',
        '{{Steps.step1.output.source.b}}',
        '{{Steps.step1.output.source.c}}',
    ]

    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    )
    fep.set_arguments({'resultShape': result_shape})
    fep.set_context({})
    fep.set_steps(create_output_map({'source': source}))

    result = await make.execute(fep)
    value = result.all_results()[0].get_result()['value']

    assert value == [1, 2, 3]
