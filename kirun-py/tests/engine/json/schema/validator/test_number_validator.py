from __future__ import annotations

import pytest

from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.validator.schema_validator import SchemaValidator


@pytest.mark.asyncio
async def test_check_for_valid_custom_messages_in_numerical_values():
    schema = Schema.from_value({
        'type': 'INTEGER',
        'minimum': 10,
        'details': {
            'validationMessages': {
                'minimum': 'Minimum value is 10',
            }
        }
    })

    with pytest.raises(Exception) as exc_info:
        await SchemaValidator.validate([], schema, None, -23)

    assert 'Minimum value is 10' in str(exc_info.value)
