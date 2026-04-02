"""
Ported from:
  - AnyOfAllOfOneOfValidatorTest.ts
  - ArrayContainsValidatorTest.ts
  - ArraySchemaAdapterTypeTest.ts
  - ArraySchemaTypeTest.ts
  - NotValidatorTest.ts
  - NullValidatorTest.ts
  - ObjectPropertiesTest.ts
  - SchemaAnyOfValidatorTest.ts
  - StringFormatSchemaValidatorTest.ts
  - TypeValidatorTest.ts
"""
from __future__ import annotations

from typing import Optional, List

import pytest

from kirun_py.json.schema.schema import Schema, AdditionalType
from kirun_py.json.schema.array.array_schema_type import ArraySchemaType
from kirun_py.json.schema.validator.schema_validator import SchemaValidator
from kirun_py.json.schema.validator.any_of_all_of_one_of_validator import AnyOfAllOfOneOfValidator
from kirun_py.json.schema.validator.array_validator import ArrayValidator
from kirun_py.json.schema.validator.type_validator import TypeValidator
from kirun_py.json.schema.validator.number_validator import NumberValidator
from kirun_py.json.schema.validator.string_validator import StringValidator
from kirun_py.json.schema.validator.boolean_validator import BooleanValidator
from kirun_py.json.schema.validator.null_validator import NullValidator
from kirun_py.json.schema.string.string_format import StringFormat
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository
from kirun_py.hybrid_repository import HybridRepository

repo = KIRunSchemaRepository()


# ---------------------------------------------------------------------------
# AnyOfAllOfOneOfValidatorTest (ported from AnyOfAllOfOneOfValidatorTest.ts)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_any_of_all_of_one_of_validator_integer():
    schema = Schema().set_type(TypeUtil.of(SchemaType.INTEGER))
    assert await AnyOfAllOfOneOfValidator.validate([], schema, repo, 10) == 10


@pytest.mark.asyncio
async def test_any_of_all_of_one_of_validator_array():
    array_schema = Schema().set_type(TypeUtil.of(SchemaType.ARRAY))
    result = await AnyOfAllOfOneOfValidator.validate([], array_schema, repo, [1, 2, 3])
    assert result == [1, 2, 3]


@pytest.mark.asyncio
async def test_any_of_all_of_one_of_validator_object():
    obj_schema = Schema.of_object('testObj').set_properties(
        {'key': Schema.of_string('key')}
    )
    result = await AnyOfAllOfOneOfValidator.validate([], obj_schema, repo, {'key': 'value'})
    assert result == {'key': 'value'}


@pytest.mark.asyncio
async def test_any_of_all_of_one_of_validator_null():
    null_schema = Schema().set_type(TypeUtil.of(SchemaType.NULL))
    assert await AnyOfAllOfOneOfValidator.validate([], null_schema, repo, None) is None


@pytest.mark.asyncio
async def test_any_of_all_of_one_of_validator_boolean_null():
    bool_schema = Schema().set_type(TypeUtil.of(SchemaType.BOOLEAN))
    assert await AnyOfAllOfOneOfValidator.validate([], bool_schema, repo, None) is None


# ---------------------------------------------------------------------------
# ArrayContainsValidatorTest (ported from ArrayContainsValidatorTest.ts)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_schema_array_contains_object():
    tuple_s = [
        Schema.of_string('item1'),
        Schema.of_integer('item2'),
        Schema.of_object('item3'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)

    schema = (
        Schema.of_array('arraySchema')
        .set_items(ast)
        .set_contains(Schema.of_object('containsS'))
    )

    obj = {'val': False}
    array = ['jimmy', 31, obj]

    result = await ArrayValidator.validate([], schema, repo, array)
    assert result == array


@pytest.mark.asyncio
async def test_schema_array_contains_error():
    tuple_s = [
        Schema.of_string('item1'),
        Schema.of_integer('item2'),
        Schema.of_object('item3'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)

    schema = (
        Schema.of_array('arraySchema')
        .set_items(ast)
        .set_contains(Schema.of_boolean('containsS'))
    )

    obj = {'val': False}
    array = ['jimmy', 31, obj]

    with pytest.raises(Exception):
        await ArrayValidator.validate([], schema, repo, array)


@pytest.mark.asyncio
async def test_schema_array_min_contains():
    tuple_s = [
        Schema.of_string('item1'),
        Schema.of_integer('item2'),
        Schema.of_object('item3'),
        Schema.of_boolean('item4'),
        Schema.of_object('item5'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)

    schema = (
        Schema.of_array('arraySchema')
        .set_items(ast)
        .set_contains(Schema.of_object('containsS'))
        .set_min_contains(2)
    )

    obj = {'val': False}
    obj1 = {'name': 'mcgill'}
    array = ['jimmy', 31, obj, True, obj1]

    result = await ArrayValidator.validate([], schema, repo, array)
    assert result == array


@pytest.mark.asyncio
async def test_schema_array_max_contains():
    tuple_s = [
        Schema.of_string('item1'),
        Schema.of_integer('item2'),
        Schema.of_object('item3'),
        Schema.of_boolean('item4'),
        Schema.of_object('item5'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)

    schema = (
        Schema.of_array('arraySchema')
        .set_items(ast)
        .set_contains(Schema.of_object('containsS'))
        .set_max_contains(3)
    )

    obj = {'val': False}
    obj1 = {'name': 'mcgill'}
    array = ['jimmy', 31, obj, True, obj1]

    result = await ArrayValidator.validate([], schema, repo, array)
    assert result == array


@pytest.mark.asyncio
async def test_schema_array_min_contains_too_few_fails():
    tuple_s = [
        Schema.of_string('item1'),
        Schema.of_integer('item2'),
        Schema.of_object('item3'),
        Schema.of_boolean('item4'),
        Schema.of_object('item5'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)

    schema = (
        Schema.of_array('arraySchema')
        .set_items(ast)
        .set_contains(Schema.of_object('containsS'))
        .set_min_contains(4)
    )

    obj = {'val': False}
    obj1 = {'name': 'mcgill'}
    array = ['jimmy', 31, obj, True, obj1]

    with pytest.raises(Exception):
        await ArrayValidator.validate([], schema, repo, array)


@pytest.mark.asyncio
async def test_schema_array_max_contains_too_many_fails():
    tuple_s = [
        Schema.of_string('item1'),
        Schema.of_integer('item2'),
        Schema.of_object('item3'),
        Schema.of_boolean('item4'),
        Schema.of_object('item5'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)

    schema = (
        Schema.of_array('arraySchema')
        .set_items(ast)
        .set_contains(Schema.of_object('containsS'))
        .set_max_contains(1)
    )

    obj = {'val': False}
    obj1 = {'name': 'mcgill'}
    array = ['jimmy', 31, obj, True, obj1]

    with pytest.raises(Exception):
        await ArrayValidator.validate([], schema, repo, array)


@pytest.mark.asyncio
async def test_schema_array_min_max_contains_valid():
    tuple_s = [
        Schema.of_string('item1'),
        Schema.of_integer('item2'),
        Schema.of_object('item3'),
        Schema.of_boolean('item4'),
        Schema.of_object('item5'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)

    schema = (
        Schema.of_array('arraySchema')
        .set_items(ast)
        .set_contains(Schema.of_object('containsS'))
        .set_min_contains(1)
        .set_max_contains(3)
    )

    obj = {'val': False}
    obj1 = {'name': 'mcgill'}
    array = ['jimmy', 31, obj, True, obj1]

    result = await ArrayValidator.validate([], schema, repo, array)
    assert result is array


@pytest.mark.asyncio
async def test_schema_array_min_max_without_contains_valid():
    """min/max contains without a contains schema is always valid."""
    tuple_s = [
        Schema.of_string('item1'),
        Schema.of_integer('item2'),
        Schema.of_object('item3'),
        Schema.of_boolean('item4'),
        Schema.of_object('item5'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)

    schema = (
        Schema.of_array('arraySchema')
        .set_items(ast)
        .set_min_contains(5)
        .set_max_contains(45)
    )

    obj = {'val': False}
    obj1 = {'name': 'mcgill'}
    array = ['jimmy', 31, obj, True, obj1]

    result = await ArrayValidator.validate([], schema, repo, array)
    assert result is array


# ---------------------------------------------------------------------------
# ArraySchemaAdapterTypeTest (ported from ArraySchemaAdapterTypeTest.ts)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_schema_array_single_test_with_additional_false_fails():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': {
            'singleSchema': {
                'type': 'OBJECT',
                'properties': {'name': {'type': 'STRING'}, 'age': {'type': 'INTEGER'}},
            },
        },
        'additionalItems': False,
    })

    obj = [
        {'name': 'amigo1'},
        {'age': 24},
        False,
        'exampleString',
    ]

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, obj)


@pytest.mark.asyncio
async def test_schema_array_without_single_fails():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': {
            'type': 'OBJECT',
            'properties': {'name': {'type': 'STRING'}, 'age': {'type': 'INTEGER'}},
        },
        'additionalItems': False,
    })

    obj = [
        {'name': 'amigo1'},
        {'age': 24},
        False,
        'exampleString',
    ]

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, obj)


@pytest.mark.asyncio
async def test_schema_array_with_single_pass():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': {
            'singleSchema': {
                'type': 'OBJECT',
                'properties': {'name': {'type': 'STRING'}, 'age': {'type': 'INTEGER'}},
            },
        },
        'additionalItems': False,
    })

    obj = [{'name': 'amigo1'}, {'age': 24}]

    result = await SchemaValidator.validate([], schema, repo, obj)
    assert result is obj


@pytest.mark.asyncio
async def test_schema_array_with_tuple():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': {
            'tupleSchema': [
                {
                    'type': 'OBJECT',
                    'properties': {'name': {'type': 'STRING'}, 'age': {'type': 'INTEGER'}},
                    'required': ['age'],
                },
                {'type': 'STRING', 'minLength': 2},
                {'type': 'INTEGER', 'minimum': 10},
            ],
        },
        'additionalItems': True,
    })

    obj = [
        {'name': 'amigo1', 'age': 24},
        'string type',
        11,
        False,
        12.34,
        'mla',
    ]

    result = await SchemaValidator.validate([], schema, repo, obj)
    assert result is obj


@pytest.mark.asyncio
async def test_schema_array_without_tuple_with_additional():
    schema = Schema.from_value({
        'type': 'ARRAY',
        'items': [
            {
                'type': 'OBJECT',
                'properties': {'name': {'type': 'STRING'}, 'age': {'type': 'INTEGER'}},
                'required': ['age'],
            },
            {'type': 'STRING', 'minLength': 2},
            {'type': 'ARRAY', 'items': {'type': 'INTEGER'}, 'additionalItems': False},
        ],
        'additionalItems': True,
    })

    obj = [
        {'name': 'amigo1', 'age': 21},
        'second string',
        [1, 2, 31231],
        'additional items was added here with true and false',
        True,
        False,
    ]

    result = await SchemaValidator.validate([], schema, repo, obj)
    assert result is obj


def test_regular_json_schema_parsing():
    schema = Schema.from_value({
        'type': 'object',
        'properties': {
            'productId': {
                'description': 'The unique identifier for a product',
                'type': 'integer',
            },
        },
    })

    assert schema.get_type().contains(SchemaType.OBJECT) is True
    assert schema.get_type().contains(SchemaType.INTEGER) is False
    assert schema.get_properties()['productId'].get_type().contains(SchemaType.INTEGER) is True


def test_only_ref_schema():
    schema = Schema.from_value({'ref': 'System.any'})
    assert schema.get_type() is None
    assert schema.get_ref() == 'System.any'


# ---------------------------------------------------------------------------
# ArraySchemaTypeTest (ported from ArraySchemaTypeTest.ts)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_array_validator_single_schema():
    ast = ArraySchemaType()
    ast.set_single_schema(Schema.of_integer('ast'))
    schema = Schema.of_array('schema').set_items(ast)
    arr = [12, 23, 54, 45]
    result = await ArrayValidator.validate([], schema, repo, arr)
    assert result == arr


@pytest.mark.asyncio
async def test_array_validator_tuple_schema():
    tuple_s = [
        Schema.of_integer('item1'),
        Schema.of_string('item2'),
        Schema.of_object('item3'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)
    schema = Schema.of_array('schema').set_items(ast)
    arr = [12, 'surendhar', {'a': 'val1', 'b': 'val2'}]
    result = await ArrayValidator.validate([], schema, repo, arr)
    assert result == arr


@pytest.mark.asyncio
async def test_array_validator_single_with_additional_false():
    ast = ArraySchemaType()
    ast.set_single_schema(Schema.of_integer('ast'))
    schema = (
        Schema.of_array('schema')
        .set_items(ast)
        .set_additional_items(AdditionalType().set_boolean_value(False))
    )
    arr = [12, 23, 54, 45]
    result = await ArrayValidator.validate([], schema, repo, arr)
    assert result == arr


@pytest.mark.asyncio
async def test_array_validator_tuple_with_additional_true():
    tuple_s = [
        Schema.of_integer('item1'),
        Schema.of_string('item2'),
        Schema.of_object('item3'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)
    schema = (
        Schema.of_array('schema')
        .set_items(ast)
        .set_additional_items(AdditionalType().set_boolean_value(True))
    )
    arr = [12, 'surendhar', {'a': 'val1', 'b': 'val2'}, 1, 2, 4]
    result = await ArrayValidator.validate([], schema, repo, arr)
    assert result == arr


@pytest.mark.asyncio
async def test_array_validator_tuple_with_add_schema():
    tuple_s = [
        Schema.of_integer('item1'),
        Schema.of_string('item2'),
        Schema.of_object('item3'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)
    schema = (
        Schema.of_array('schema')
        .set_items(ast)
        .set_additional_items(AdditionalType().set_schema_value(Schema.of_integer('itemInt')))
    )
    arr = [12, 'surendhar', {'a': 'val1', 'b': 'val2'}, 1, 2]
    result = await ArrayValidator.validate([], schema, repo, arr)
    assert result == arr


@pytest.mark.asyncio
async def test_array_validator_tuple_with_add_schema_fail():
    tuple_s = [
        Schema.of_integer('item1'),
        Schema.of_string('item2'),
        Schema.of_object('item3'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)
    schema = (
        Schema.of_array('schema')
        .set_items(ast)
        .set_additional_items(AdditionalType().set_schema_value(Schema.of_integer('itemInt')))
    )
    arr = [12, 'surendhar', {'a': 'val1', 'b': 'val2'}, 1, 2, 4, 'surendhar']
    with pytest.raises(Exception):
        await ArrayValidator.validate([], schema, repo, arr)


@pytest.mark.asyncio
async def test_array_validator_single_with_additional_fails():
    ast = ArraySchemaType()
    ast.set_single_schema(Schema.of_integer('ast'))
    schema = (
        Schema.of_array('schema')
        .set_items(ast)
        .set_additional_items(AdditionalType().set_boolean_value(True))
    )
    arr = [12, 23, 54, 45, 'abcd', 'df']
    with pytest.raises(Exception):
        await ArrayValidator.validate([], schema, repo, arr)


@pytest.mark.asyncio
async def test_array_validator_tuple_without_additional_fails():
    tuple_s = [
        Schema.of_integer('item1'),
        Schema.of_string('item2'),
        Schema.of_object('item3'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)
    schema = Schema.of_array('schema').set_items(ast)
    arr = [12, 'surendhar', {'a': 'val1', 'b': 'val2'}, 'add']
    with pytest.raises(Exception):
        await ArrayValidator.validate([], schema, repo, arr)


@pytest.mark.asyncio
async def test_array_validator_tuple_schema_with_wrong_types():
    tuple_s = [
        Schema.of_integer('item1'),
        Schema.of_string('item2'),
        Schema.of_object('item3'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)
    schema = Schema.of_array('schema').set_items(ast)
    obj = [1, 'asd', {'val': 'stringtype'}, 'stringOnemore']
    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, obj)


@pytest.mark.asyncio
async def test_array_validator_tuple_schema_similar_fails():
    tuple_s = [
        Schema.of_integer('item1'),
        Schema.of_string('item2'),
        Schema.of_boolean('item3'),
    ]
    ast = ArraySchemaType()
    ast.set_tuple_schema(tuple_s)
    obj_sc = Schema.of_object('obj')
    schema = (
        Schema.of_array('schema')
        .set_items(ast)
        .set_additional_items(AdditionalType().set_schema_value(obj_sc))
    )
    obj = [1, 'asd', True, {'val': 'stringtype'}, False]
    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, obj)


# ---------------------------------------------------------------------------
# NotValidatorTest (ported from NotValidatorTest.ts)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_not_validation_with_not_string():
    sch = (
        Schema.of('Not Schema', SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT,
                  SchemaType.DOUBLE, SchemaType.STRING)
        .set_default_value(1)
        .set_not(Schema.of('Not String', SchemaType.STRING))
    )

    value = await SchemaValidator.validate(None, sch, None, 0)
    assert value == 0


@pytest.mark.asyncio
async def test_not_validation_rejects_integer():
    sch = (
        Schema.of('Not Schema', SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT,
                  SchemaType.DOUBLE, SchemaType.STRING)
        .set_default_value(1)
        .set_not(Schema.of('Not Integer', SchemaType.INTEGER))
    )

    with pytest.raises(Exception):
        await SchemaValidator.validate(None, sch, None, 0)


@pytest.mark.asyncio
async def test_not_validation_with_constant_zero():
    sch = (
        Schema.of('Not Schema', SchemaType.INTEGER, SchemaType.LONG, SchemaType.FLOAT)
        .set_default_value(1)
        .set_not(Schema().set_constant(0))
    )

    with pytest.raises(Exception):
        await SchemaValidator.validate(None, sch, None, 0)

    # null should use default value
    result = await SchemaValidator.validate(None, sch, None, None)
    assert result == 1

    # value 2 passes since it's not the constant 0
    result = await SchemaValidator.validate(None, sch, None, 2)
    assert result == 2


@pytest.mark.asyncio
async def test_constant_validation_valid():
    sch = Schema.of('Constant Schema', SchemaType.INTEGER).set_constant(1)
    value = await SchemaValidator.validate(None, sch, None, 1)
    assert value == 1


@pytest.mark.asyncio
async def test_constant_validation_invalid():
    sch = Schema.of('Constant Schema', SchemaType.INTEGER).set_constant(1)
    with pytest.raises(Exception):
        await SchemaValidator.validate(None, sch, None, 0)


# ---------------------------------------------------------------------------
# NullValidatorTest (ported from NullValidatorTest.ts)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_check_for_valid_null_value_rejects_integer():
    schema = Schema.from_value({'type': 'NULL'})
    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, None, 23)


@pytest.mark.asyncio
async def test_check_for_valid_null_value_rejects_zero():
    schema = Schema.from_value({'type': 'NULL'})
    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, None, 0)


@pytest.mark.asyncio
async def test_check_for_valid_null_value_rejects_empty_string():
    schema = Schema.from_value({'type': 'NULL'})
    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, None, '')


@pytest.mark.asyncio
async def test_check_for_valid_null_value_accepts_none():
    schema = Schema.from_value({'type': 'NULL'})
    result = await SchemaValidator.validate([], schema, None, None)
    assert result is None


# ---------------------------------------------------------------------------
# ObjectPropertiesTest (ported from ObjectPropertiesTest.ts)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_object_validator_with_additional_array():
    schema = Schema.from_value({
        'type': 'OBJECT',
        'properties': {'name': {'type': 'STRING'}},
        'additionalProperties': {'schemaValue': {'type': 'ARRAY'}},
    })
    assert schema.get_type().contains(SchemaType.OBJECT) is True

    obj = {'name': 'Kiran', 'num': [1, 2, 3]}
    result = await SchemaValidator.validate([], schema, repo, obj)
    assert result == obj


@pytest.mark.asyncio
async def test_object_validator_additional_non_array_fails():
    schema = Schema.from_value({
        'type': 'OBJECT',
        'properties': {'name': {'type': 'STRING'}},
        'additionalProperties': {'schemaValue': {'type': 'ARRAY'}},
    })

    with pytest.raises(Exception):
        await SchemaValidator.validate([], schema, repo, {
            'name': 'Kiran',
            'num': 23,
            'lastName': 'grandhi',
        })


# ---------------------------------------------------------------------------
# SchemaAnyOfValidatorTest (ported from SchemaAnyOfValidatorTest.ts)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_filter_condition_with_ref_schema():
    filter_operator = (
        Schema.of_string('filterOperator')
        .set_namespace('Test')
        .set_enums(['EQUALS', 'LESS_THAN', 'GREATER_THAN', 'LESS_THAN_EQUAL'])
    )

    filter_condition = (
        Schema.of_object('filterCondition')
        .set_namespace('Test')
        .set_properties({
            'negate': Schema.of_boolean('negate'),
            'filterConditionOperator': Schema.of_ref('Test.filterOperator'),
            'field': Schema.of_string('field'),
            'value': Schema.of_any('value'),
            'toValue': Schema.of_any('toValue'),
            'multiValue': Schema.of_array('multiValue').set_items(
                ArraySchemaType().set_single_schema(Schema.of_any('singleType'))
            ),
            'isValue': Schema.of_boolean('isValue'),
            'isToValue': Schema.of_boolean('isToValue'),
        })
    )

    schema_map = {
        'filterOperator': filter_operator,
        'filterCondition': filter_condition,
    }

    class CustomRepo:
        async def find(self, namespace: str, name: str):
            if namespace != 'Test':
                return None
            return schema_map.get(name)

        async def filter(self, name: str):
            return [
                s.get_full_name()
                for s in schema_map.values()
                if name.lower() in s.get_full_name().lower()
            ]

    custom_repo = HybridRepository(CustomRepo(), KIRunSchemaRepository())

    temp_ob = {
        'field': 'a.b.c.d',
        'value': 'surendhar',
        'filterConditionOperator': 'LESS_THAN',
        'negate': True,
        'isValue': False,
    }

    result = await SchemaValidator.validate(
        [], Schema.of_ref('Test.filterCondition'), custom_repo, temp_ob
    )
    assert result is temp_ob


# ---------------------------------------------------------------------------
# StringFormatSchemaValidatorTest (ported from StringFormatSchemaValidatorTest.ts)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_schema_validator_integer_basic():
    schema = Schema().set_type(TypeUtil.of(SchemaType.INTEGER))
    assert await SchemaValidator.validate([], schema, repo, 2) == 2


@pytest.mark.asyncio
async def test_schema_validator_fail_with_date_format_missing_type():
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'name': Schema.of_string('name'),
            'date': Schema().set_format(StringFormat.DATE),
        })
        .set_required(['name', 'date'])
    )

    date_obj = {'name': 'surendhar.s', 'date': '1999-13-12'}
    with pytest.raises(Exception, match='Type is missing in schema for declared DATE format'):
        await SchemaValidator.validate([], obj_schema, repo, date_obj)


@pytest.mark.asyncio
async def test_schema_validator_pass_with_date():
    int_schema = Schema().set_type(TypeUtil.of(SchemaType.INTEGER)).set_minimum(100)

    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'intSchema': int_schema,
            'name': Schema.of_string('name'),
            'date': Schema().set_format(StringFormat.DATE).set_type(TypeUtil.of(SchemaType.STRING)),
        })
        .set_required(['intSchema', 'date'])
    )

    date_obj = {'intSchema': 1231, 'date': '1999-09-12'}
    result = await SchemaValidator.validate([], obj_schema, repo, date_obj)
    assert result is date_obj


@pytest.mark.asyncio
async def test_schema_validator_fail_with_time_missing_type():
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'intSchema': Schema().set_type(TypeUtil.of(SchemaType.INTEGER)).set_maximum(100).set_multiple_of(5),
            'name': Schema.of_string('name').set_min_length(10),
            'time': Schema().set_format(StringFormat.TIME),
        })
        .set_required(['intSchema', 'time', 'name'])
    )

    time_obj = {'intSchema': 95, 'time': '22:23:61', 'name': 's.surendhar'}
    with pytest.raises(Exception, match='Type is missing in schema for declared TIME format'):
        await SchemaValidator.validate([], obj_schema, repo, time_obj)


@pytest.mark.asyncio
async def test_schema_validator_fail_with_invalid_time():
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'intSchema': Schema().set_type(TypeUtil.of(SchemaType.INTEGER)).set_maximum(100).set_multiple_of(5),
            'name': Schema.of_string('name').set_min_length(10),
            'time': Schema().set_format(StringFormat.TIME).set_type(TypeUtil.of(SchemaType.STRING)),
        })
        .set_required(['intSchema', 'time', 'name'])
    )

    time_obj = {'intSchema': 95, 'time': '22:23:61', 'name': 's.surendhar'}
    with pytest.raises(Exception, match='22:23:61 is not matched with the time pattern'):
        await SchemaValidator.validate([], obj_schema, repo, time_obj)


@pytest.mark.asyncio
async def test_schema_validator_pass_with_time():
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'intSchema': Schema().set_type(TypeUtil.of(SchemaType.INTEGER)).set_maximum(100).set_multiple_of(5),
            'name': Schema.of_string('name').set_min_length(10),
            'time': Schema().set_format(StringFormat.TIME).set_type(TypeUtil.of(SchemaType.STRING)),
        })
        .set_required(['intSchema', 'time', 'name'])
    )

    time_obj = {'intSchema': 95, 'time': '22:23:24', 'name': 's.surendhar'}
    result = await SchemaValidator.validate([], obj_schema, repo, time_obj)
    assert result is time_obj


@pytest.mark.asyncio
async def test_schema_validator_fail_with_email_missing_type():
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'intSchema': Schema().set_type(TypeUtil.of(SchemaType.INTEGER)).set_maximum(100),
            'name': Schema.of_string('name').set_min_length(10),
            'email': Schema().set_format(StringFormat.EMAIL),
        })
        .set_required(['intSchema', 'email', 'name'])
    )

    email_obj = {'intSchema': 95, 'email': 'iosdjfdf123--@gmail.com', 'name': 's.surendhar'}
    with pytest.raises(Exception, match='Type is missing in schema for declared EMAIL format'):
        await SchemaValidator.validate([], obj_schema, repo, email_obj)


@pytest.mark.asyncio
async def test_schema_validator_fail_with_invalid_email():
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'intSchema': Schema().set_type(TypeUtil.of(SchemaType.INTEGER)).set_maximum(3).set_multiple_of(5),
            'name': Schema.of_string('name').set_min_length(10),
            'email': Schema().set_format(StringFormat.EMAIL).set_type(TypeUtil.of(SchemaType.STRING)),
        })
        .set_required(['intSchema', 'email'])
    )

    email_obj = {'intSchema': 0, 'email': 'asdasdf@@*.com'}
    with pytest.raises(Exception, match='asdasdf@@\\*\\.com is not matched with the email pattern'):
        await SchemaValidator.validate([], obj_schema, repo, email_obj)


@pytest.mark.asyncio
async def test_schema_validator_pass_with_valid_email():
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'intSchema': Schema().set_type(TypeUtil.of(SchemaType.INTEGER)),
            'name': Schema.of_string('name').set_min_length(10),
            'email': Schema().set_format(StringFormat.EMAIL).set_type(TypeUtil.of(SchemaType.STRING)),
        })
        .set_required(['intSchema', 'name'])
    )

    email_obj = {'intSchema': 95, 'email': 'surendhar.s@finc.c', 'name': 's.surendhar'}
    result = await SchemaValidator.validate([], obj_schema, repo, email_obj)
    assert result is email_obj


@pytest.mark.asyncio
async def test_schema_validator_fail_with_datetime_missing_type():
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'intSchema': Schema().set_type(TypeUtil.of(SchemaType.INTEGER)).set_maximum(100),
            'name': Schema.of_string('name').set_min_length(10),
            'dateTime': Schema().set_format(StringFormat.DATETIME),
        })
        .set_required(['intSchema', 'dateTime', 'name'])
    )

    dt_obj = {'intSchema': 95, 'dateTime': '2023-08-21T07:56:45+12:12', 'name': 's.surendhar'}
    with pytest.raises(Exception, match='Type is missing in schema for declared DATETIME format'):
        await SchemaValidator.validate([], obj_schema, repo, dt_obj)


@pytest.mark.asyncio
async def test_schema_validator_fail_with_invalid_datetime():
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'intSchema': Schema().set_type(TypeUtil.of(SchemaType.INTEGER)).set_maximum(3).set_multiple_of(5),
            'name': Schema.of_string('name').set_min_length(10),
            'dateTime': Schema().set_format(StringFormat.DATETIME).set_type(TypeUtil.of(SchemaType.STRING)),
        })
        .set_required(['dateTime'])
    )

    dt_obj = {'dateTime': '2023-08-221T07:56:45+12:12'}
    with pytest.raises(Exception, match='2023-08-221T07:56:45\\+12:12 is not matched with the date time pattern'):
        await SchemaValidator.validate([], obj_schema, repo, dt_obj)


@pytest.mark.asyncio
async def test_schema_validator_pass_with_valid_datetime():
    obj_schema = (
        Schema.of_object('testObj')
        .set_properties({
            'intSchema': Schema().set_type(TypeUtil.of(SchemaType.INTEGER)),
            'name': Schema.of_string('name').set_min_length(10),
            'dateTime': Schema().set_format(StringFormat.DATETIME).set_type(TypeUtil.of(SchemaType.STRING)),
        })
        .set_required(['intSchema', 'dateTime'])
    )

    dt_obj = {'intSchema': 95, 'dateTime': '2023-08-21T07:56:45+12:12', 'name': 's.surendhar'}
    result = await SchemaValidator.validate([], obj_schema, repo, dt_obj)
    assert result is dt_obj


# ---------------------------------------------------------------------------
# TypeValidatorTest (ported from TypeValidatorTest.ts)
# ---------------------------------------------------------------------------

@pytest.mark.asyncio
async def test_type_validator_for_integer():
    element = 123
    schema = Schema()
    result = await TypeValidator.validate([], SchemaType.INTEGER, schema, repo, element)
    assert result == NumberValidator.validate(SchemaType.INTEGER, [], schema, element)


@pytest.mark.asyncio
async def test_type_validator_for_string():
    element = 'string'
    schema = Schema()
    result = await TypeValidator.validate([], SchemaType.STRING, schema, repo, element)
    assert result == StringValidator.validate([], schema, element)


@pytest.mark.asyncio
async def test_type_validator_for_boolean():
    element = True
    schema = Schema()
    result = await TypeValidator.validate([], SchemaType.BOOLEAN, schema, repo, element)
    assert result == BooleanValidator.validate([], schema, element)


@pytest.mark.asyncio
async def test_type_validator_for_array():
    schema = Schema()
    array = ['abc']
    result = await TypeValidator.validate([], SchemaType.ARRAY, schema, repo, array)
    expected = await ArrayValidator.validate([], schema, repo, array)
    assert result == expected


@pytest.mark.asyncio
async def test_type_validator_for_null():
    schema = Schema()
    result = await TypeValidator.validate([], SchemaType.NULL, schema, repo, None)
    assert result is None
