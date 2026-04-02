from __future__ import annotations

import pytest

from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.validator.schema_validator import SchemaValidator
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository

repo = KIRunSchemaRepository()


@pytest.mark.asyncio
async def test_tuple_schema_additional_items_boolean_different_datatype():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': [
            {'type': 'INTEGER'},
            {'type': 'STRING'},
            {'type': 'BOOLEAN'},
            {'type': 'OBJECT'},
        ],
        'additionalItems': {
            'schemaValue': {'type': 'OBJECT'},
        },
    })

    obj = [1, 'asd', {'val': 'stringtype'}, 'stringOnemore']

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, obj)


@pytest.mark.asyncio
async def test_tuple_schema_additional_items_boolean_true_datatype():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': [
            {'type': 'STRING'},
            {'type': 'BOOLEAN'},
        ],
        'additionalItems': {
            'schemaValue': {'type': 'OBJECT'},
        },
    })

    obj = ['asd', True, {'a': 'b'}]

    result = await SchemaValidator.validate([], schema, repo, obj)
    assert result == obj


@pytest.mark.asyncio
async def test_additional_items_boolean_false_different_datatype():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': {'type': 'INTEGER'},
        'additionalItems': {'booleanValue': False},
    })
    obj = [1, 2, 3, 4, 'stringtype', True]

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, obj)


@pytest.mark.asyncio
async def test_additional_items_boolean_true_different_datatype():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': {'type': 'INTEGER'},
        'additionalItems': {
            'schemaValue': {'type': 'STRING'},
        },
    })

    obj = [1, 2, 3, 'stringtype', True]

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, obj)


@pytest.mark.asyncio
async def test_tuple_schema_additional_items_mismatched_types():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': [
            {'type': 'INTEGER'},
            {'type': 'STRING'},
            {'type': 'BOOLEAN'},
        ],
        'additionalItems': {
            'schemaValue': {'type': 'OBJECT'},
        },
    })

    obj = [1, 'asd', {'val': 'stringtype'}, 'stringOnemore']

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, obj)


@pytest.mark.asyncio
async def test_multi_level_validation_inner_object_pollution():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': {
            'type': 'OBJECT',
            'properties': {
                'x': {'type': 'INTEGER'},
            },
        },
        'defaultValue': [{'x': 20}, {'x': 30}],
    })

    xschema = Schema.from_value({
        'type': 'ARRAY',
        'items': {
            'type': 'OBJECT',
            'properties': {
                'x': {'type': 'INTEGER'},
                'y': {'type': 'STRING', 'defaultValue': 'Kiran'},
            },
            'required': ['x'],
        },
    })

    value = await SchemaValidator.validate(
        None,
        xschema,
        repo,
        await SchemaValidator.validate(None, schema, repo, None),
    )

    # The original schema's default value should NOT be polluted with 'y'
    assert schema.get_default_value()[0].get('y') is None
    assert value[0]['y'] == 'Kiran'
