from __future__ import annotations

import pytest
from typing import Optional, List

from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.validator.schema_validator import SchemaValidator
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.hybrid_repository import HybridRepository

repo = KIRunSchemaRepository()


@pytest.mark.asyncio
async def test_object_validator_boolean_value():
    schema = Schema.from_value({
        'type': 'OBJECT',
        'properties': {'name': {'type': 'STRING'}},
        'additionalProperties': False,
    })
    assert schema.get_type().contains(SchemaType.OBJECT) is True

    result = await SchemaValidator.validate([], schema, repo, {'name': 'Kiran'})
    assert result == {'name': 'Kiran'}

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, {'name': 'Kiran', 'lastName': 'Grandhi'})


@pytest.mark.asyncio
async def test_object_validator_schema_based():
    schema = Schema.from_value({
        'type': 'OBJECT',
        'properties': {'name': {'type': 'STRING'}},
        'additionalProperties': {'type': 'INTEGER'},
    })
    assert schema.get_type().contains(SchemaType.OBJECT) is True

    result = await SchemaValidator.validate([], schema, repo, {'name': 'Kiran', 'num': 23})
    assert result == {'name': 'Kiran', 'num': 23}

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, {'name': 'Kiran', 'num': 23, 'lastName': 'grandhi'})


@pytest.mark.asyncio
async def test_object_validator_boolean_value_old_style():
    schema = Schema.from_value({
        'type': 'OBJECT',
        'properties': {'name': {'type': 'STRING'}},
        'additionalProperties': {'booleanValue': False},
    })

    result = await SchemaValidator.validate([], schema, repo, {'name': 'Kiran'})
    assert result == {'name': 'Kiran'}

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, {'name': 'Kiran', 'lastName': 'Grandhi'})


@pytest.mark.asyncio
async def test_object_validator_schema_based_old_style():
    schema = Schema.from_value({
        'type': 'OBJECT',
        'properties': {'name': {'type': 'STRING'}},
        'additionalProperties': {'schemaValue': {'type': 'INTEGER'}},
    })
    assert schema.get_type().contains(SchemaType.OBJECT) is True

    result = await SchemaValidator.validate([], schema, repo, {'name': 'Kiran', 'num': 23})
    assert result == {'name': 'Kiran', 'num': 23}

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, {'name': 'Kiran', 'num': 23, 'lastName': 'grandhi'})


@pytest.mark.asyncio
async def test_object_validator_schema_based_old_array_style():
    schema = Schema.from_value({
        'type': 'OBJECT',
        'properties': {'name': {'type': 'STRING'}},
        'additionalProperties': {'schemaValue': {'type': 'ARRAY'}},
    })
    assert schema.get_type().contains(SchemaType.OBJECT) is True

    obj = {'name': 'Kiran', 'num': [1, 2, 3]}
    result = await SchemaValidator.validate([], schema, repo, obj)
    assert result == obj

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, {'name': 'Kiran', 'num': 23, 'lastName': 'grandhi'})


@pytest.mark.asyncio
async def test_object_validator_schema_based_old_object_style():
    schema = Schema.from_value({
        'type': 'OBJECT',
        'properties': {
            'name': {'type': 'STRING'},
            'lastname': {'type': 'STRING'},
            'age': {'type': 'INTEGER'},
        },
        'additionalProperties': {'schemaValue': {'type': 'OBJECT'}},
    })

    obj = {'name': 'Kiran', 'age': 23}
    result = await SchemaValidator.validate([], schema, repo, obj)
    assert result == obj

    obj_with_additional = {
        'name': 'Kiran',
        'lastname': 'grandhi',
        'addresses': {
            'area': 'j.p.nagar',
            'city': 'banga',
        },
    }
    result2 = await SchemaValidator.validate([], schema, repo, obj_with_additional)
    assert result2 == obj_with_additional

    obj_with_more_additional = {
        'name': 'Kiran',
        'lastname': 'grandhi',
        'addresses': {
            'area': 'j.p.nagar',
            'city': 'banga',
        },
        'city': 'kakinada',
    }
    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, obj_with_more_additional)


@pytest.mark.asyncio
async def test_schema_with_object_value_null_for_any():
    filter_operator = Schema.from_value({
        'namespace': 'test',
        'name': 'filterOperator',
        'version': 1,
        'type': 'STRING',
        'defaultValue': 'EQUALS',
        'enums': ['EQUALS', 'LESS_THAN', 'GREATER_THAN', 'LESS_THAN_EQUAL', 'BETWEEN', 'IN'],
    })

    filter_condition = Schema.from_value({
        'namespace': 'test',
        'name': 'FilterCondition',
        'version': 1,
        'type': 'OBJECT',
        'properties': {
            'field': {
                'namespace': '_',
                'name': 'field',
                'version': 1,
                'type': 'STRING',
            },
            'multiValue': {
                'namespace': '_',
                'name': 'multiValue',
                'version': 1,
                'type': 'ARRAY',
                'items': {
                    'namespace': '_',
                    'name': 'singleType',
                    'version': 1,
                    'type': ['FLOAT', 'BOOLEAN', 'STRING', 'DOUBLE', 'INTEGER', 'LONG', 'NULL', 'ARRAY', 'OBJECT'],
                },
            },
            'isValue': {
                'namespace': '_',
                'name': 'isValue',
                'version': 1,
                'type': 'BOOLEAN',
                'defaultValue': False,
            },
            'toValue': {
                'namespace': '_',
                'name': 'toValue',
                'version': 1,
                'type': ['FLOAT', 'BOOLEAN', 'STRING', 'DOUBLE', 'INTEGER', 'LONG', 'NULL', 'ARRAY', 'OBJECT'],
            },
            'operator': {
                'namespace': '_',
                'version': 1,
                'ref': 'test.filterOperator',
            },
            'negate': {
                'namespace': '_',
                'name': 'negate',
                'version': 1,
                'type': 'BOOLEAN',
                'defaultValue': False,
            },
            'value': {
                'namespace': '_',
                'name': 'value',
                'version': 1,
                'type': ['FLOAT', 'BOOLEAN', 'STRING', 'DOUBLE', 'INTEGER', 'LONG', 'NULL', 'ARRAY', 'OBJECT'],
            },
            'isToValue': {
                'namespace': '_',
                'name': 'isToValue',
                'version': 1,
                'type': 'BOOLEAN',
                'defaultValue': False,
            },
        },
        'additionalProperties': False,
        'required': ['operator', 'field'],
    })

    schema_map = {
        'filterOperator': filter_operator,
        'FilterCondition': filter_condition,
    }

    class TestRepository:
        async def find(self, namespace: str, name: str) -> Optional[Schema]:
            if not namespace:
                return None
            return schema_map.get(name)

        async def filter(self, name: str) -> List[str]:
            return []

    custom_repo = HybridRepository(TestRepository(), KIRunSchemaRepository())

    temp_ob2 = {'field': 'nullcheck', 'operator': 'LESS_THAN', 'value': None, 'isValue': True}

    res3 = await SchemaValidator.validate(None, filter_condition, custom_repo, temp_ob2)
    assert res3 == temp_ob2
