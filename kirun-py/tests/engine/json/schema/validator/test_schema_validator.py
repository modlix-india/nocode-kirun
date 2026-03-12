from __future__ import annotations

import pytest
from typing import Optional, List

from kirun_py.json.schema.schema import Schema, AdditionalType
from kirun_py.json.schema.validator.schema_validator import SchemaValidator
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.hybrid_repository import HybridRepository
from kirun_py.repository import Repository

repo = KIRunSchemaRepository()


@pytest.mark.asyncio
async def test_schema_validator_test_1():
    schema = Schema().set_type(TypeUtil.of(SchemaType.INTEGER))

    assert await SchemaValidator.validate([], schema, repo, 2) == 2

    obj = {'name': 'shagil'}
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({'name': Schema.of_string('name')})
        .set_required(['name'])
    )
    assert await SchemaValidator.validate([], obj_schema, repo, obj) is obj

    with pytest.raises(Exception):
        await SchemaValidator.validate([], obj_schema, repo, {'name': 123})


@pytest.mark.asyncio
async def test_schema_validation_when_ref_of_ref():
    location_schema = Schema.from_value({
        'name': 'Location',
        'namespace': 'Test',
        'type': 'OBJECT',
        'properties': {
            'url': {'name': 'url', 'type': 'STRING'},
        },
        'required': ['url'],
    })

    url_params_schema = (
        Schema.of_object('UrlParameters')
        .set_namespace('Test')
        .set_additional_properties(AdditionalType().set_schema_value(Schema.of_ref('Test.Location')))
        .set_default_value({})
    )

    test_schema = (
        Schema.of_object('TestSchema')
        .set_namespace('Test')
        .set_additional_properties(AdditionalType().set_schema_value(Schema.of_ref('Test.UrlParameters')))
        .set_default_value({})
    )

    schema_map = {
        'Location': location_schema,
        'UrlParameters': url_params_schema,
        'TestSchema': test_schema,
    }

    class X:
        async def find(self, namespace: str, name: str) -> Optional[Schema]:
            if namespace != 'Test':
                return None
            return schema_map.get(name)

        async def filter(self, name: str) -> List[str]:
            return [
                s.get_full_name()
                for s in schema_map.values()
                if name.lower() in s.get_full_name().lower()
            ]

    custom_repo = HybridRepository(X(), KIRunSchemaRepository())
    query_params = {
        'obj': {'obj': {'url': 'http://xxxxxx.com'}},
    }

    result = await SchemaValidator.validate(
        None,
        Schema.of_ref('Test.TestSchema'),
        custom_repo,
        query_params,
    )
    assert result is query_params


@pytest.mark.asyncio
async def test_schema_validator_test_2():
    location_schema = Schema.from_value({
        'name': 'Location',
        'namespace': 'Test',
        'type': 'OBJECT',
        'properties': {
            'url': {'name': 'url', 'type': 'STRING'},
        },
        'required': ['url'],
    })

    class CustomRepo:
        async def find(self, namespace: str, name: str) -> Optional[Schema]:
            if namespace == 'Test' and name == 'Location':
                return location_schema
            return None

        async def filter(self, name: str) -> List[str]:
            return [
                n for n in [location_schema.get_full_name()]
                if name.lower() in n.lower()
            ]

    custom_repo = HybridRepository(CustomRepo(), KIRunSchemaRepository())
    obj = {'url': 'http://xxxx.com'}

    result = await SchemaValidator.validate(None, Schema.of_ref('Test.Location'), custom_repo, obj)
    assert result is obj


@pytest.mark.asyncio
async def test_validate_null_for_of_any_schema():
    result = await SchemaValidator.validate(
        None,
        Schema.of_any('ofanyundefined'),
        None,
        None,
    )
    assert result is None


@pytest.mark.asyncio
async def test_schema_validator_test_3():
    obj = {'url': 'http://xxxx.com'}

    location_schema = Schema.from_value({
        'name': 'Location',
        'namespace': 'Test',
        'type': 'OBJECT',
        'properties': {
            'url': {'name': 'url', 'type': 'String'},
        },
        'required': ['url'],
        'defaultValue': obj,
    })

    class CustomRepo:
        async def find(self, namespace: str, name: str) -> Optional[Schema]:
            if namespace == 'Test' and name == 'Location':
                return location_schema
            return None

        async def filter(self, name: str) -> List[str]:
            return [
                n for n in [location_schema.get_full_name()]
                if name.lower() in n.lower()
            ]

    custom_repo = HybridRepository(CustomRepo(), KIRunSchemaRepository())
    obj1 = {'url': 'http://yyyy.com'}

    result = await SchemaValidator.validate(
        None,
        Schema.of_ref('Test.Location').set_default_value(obj1),
        custom_repo,
        None,
    )
    assert result == obj1
