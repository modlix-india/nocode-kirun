"""Python ports of GROUP 2 system function tests:

- ValidateSchemaTest.ts
- WaitTest.ts
"""
from __future__ import annotations

import time
import pytest

from kirun_py.function.system.validate_schema import ValidateSchema
from kirun_py.function.system.wait import Wait
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


# ---------------------------------------------------------------------------
# ValidateSchema tests
# ---------------------------------------------------------------------------

async def _expect_validation(schema: dict, source, expected_result: bool) -> None:
    validator = ValidateSchema()
    context = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({
        'source': source,
        'schema': schema,
    })
    result = await validator.execute(context)
    is_valid = result.all_results()[0].get_result()['isValid']
    assert is_valid is expected_result


@pytest.mark.asyncio
async def test_validate_schema_complex_object_passes():
    schema = {
        'name': 'lead',
        'type': ['OBJECT'],
        'version': 1,
        'properties': {
            'name': {
                'type': ['STRING'],
                'minLength': 3,
            },
            'mobileNumber': {
                'type': ['STRING'],
                'minLength': 3,
            },
            'formType': {
                'type': ['STRING'],
                'enums': ['LEAD_FORM', 'CONTACT_FORM'],
            },
            'email': {
                'type': ['STRING'],
            },
            'address': {
                'type': ['OBJECT'],
                'properties': {
                    'street': {
                        'type': ['STRING'],
                        'minLength': 5,
                    },
                    'city': {
                        'type': ['STRING'],
                    },
                    'state': {
                        'type': ['STRING'],
                    },
                    'postalCode': {
                        'type': ['STRING'],
                        'pattern': '^[0-9]{6}$',
                    },
                },
                'required': ['street', 'city', 'state'],
            },
            'employment': {
                'type': ['OBJECT'],
                'properties': {
                    'company': {
                        'type': ['STRING'],
                    },
                    'position': {
                        'type': ['STRING'],
                    },
                    'experience': {
                        'type': ['INTEGER'],
                        'minimum': 0,
                    },
                    'skills': {
                        'type': ['ARRAY'],
                        'items': {
                            'type': ['STRING'],
                        },
                        'minItems': 1,
                    },
                },
                'required': ['company', 'position'],
            },
        },
        'required': ['name', 'email', 'formType'],
    }

    source = {
        'name': 'John Doe',
        'mobileNumber': '9876543210',
        'formType': 'LEAD_FORM',
        'email': 'john.doe@example.com',
        'address': {
            'street': '123 Main Street',
            'city': 'New York',
            'state': 'NY',
            'postalCode': '100001',
        },
        'employment': {
            'company': 'Tech Corp',
            'position': 'Senior Developer',
            'experience': 5,
            'skills': ['Java', 'Spring', 'React'],
        },
    }

    await _expect_validation(schema, source, True)


@pytest.mark.asyncio
async def test_validate_schema_simple_string_passes():
    schema = {
        'type': ['STRING'],
        'minLength': 3,
        'maxLength': 10,
    }
    await _expect_validation(schema, 'Hello', True)


@pytest.mark.asyncio
async def test_validate_schema_number_passes():
    schema = {
        'type': ['INTEGER'],
        'minimum': 0,
        'maximum': 100,
    }
    await _expect_validation(schema, 50, True)


@pytest.mark.asyncio
async def test_validate_schema_simple_array_passes():
    schema = {
        'type': ['ARRAY'],
        'items': {
            'type': ['STRING'],
        },
        'minItems': 1,
        'maxItems': 3,
    }
    await _expect_validation(schema, ['item1', 'item2'], True)


@pytest.mark.asyncio
async def test_validate_schema_invalid_input_fails():
    schema = {
        'type': ['STRING'],
        'minLength': 5,
    }
    await _expect_validation(schema, 'Hi', False)


# ---------------------------------------------------------------------------
# Wait tests
# ---------------------------------------------------------------------------

wait_fn = Wait()


@pytest.mark.asyncio
async def test_wait_1000ms():
    wait_time = 1000
    start = time.time()
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'millis': wait_time})
    await wait_fn.execute(fep)
    elapsed_ms = (time.time() - start) * 1000
    assert elapsed_ms >= wait_time


@pytest.mark.asyncio
async def test_wait_immediately():
    start = time.time()
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({})
    await wait_fn.execute(fep)
    elapsed_ms = (time.time() - start) * 1000
    assert elapsed_ms < 50


@pytest.mark.asyncio
async def test_wait_negative_raises():
    wait_time = -3500
    fep = FunctionExecutionParameters(
        KIRunFunctionRepository(),
        KIRunSchemaRepository(),
    ).set_arguments({'millis': wait_time})
    with pytest.raises(Exception):
        await wait_fn.execute(fep)
