from __future__ import annotations

from kirun_py.json.schema.array.array_schema_type import ArraySchemaType
from kirun_py.json.schema.schema import AdditionalType, Schema


# Mirrors SchemaCopyTest in kirun-java and kirun-js, where the copy constructor read
# `not`, `contains` and tuple items from the object being built instead of the source,
# and crashed copying a boolean-only additionalProperties.


def test_schema_copy_keeps_not_and_contains():
    source = (
        Schema.of_array('list', Schema.of_string('item'))
        .set_not(Schema.of_string('forbidden'))
        .set_contains(Schema.of_string('needed'))
    )

    copy = Schema(source)

    assert copy.get_not().get_name() == 'forbidden'
    assert copy.get_not() is not source.get_not()
    assert copy.get_contains().get_name() == 'needed'


def test_schema_copy_keeps_tuple_items():
    source = Schema.of_array('pair').set_items(
        ArraySchemaType().set_tuple_schema([Schema.of_string('first'), Schema.of_string('second')])
    )

    copy = Schema(source)

    names = [s.get_name() for s in copy.get_items().get_tuple_schema()]
    assert names == ['first', 'second']
    assert copy.get_items().get_tuple_schema()[0] is not source.get_items().get_tuple_schema()[0]


def test_schema_copy_keeps_a_boolean_additional_properties():
    source = Schema.of_object('row').set_additional_properties(AdditionalType().set_boolean_value(False))

    copy = Schema(source)

    assert copy.get_additional_properties().get_boolean_value() is False
    assert copy.get_additional_properties().get_schema_value() is None
