from __future__ import annotations
from typing import Any, List, Optional, TYPE_CHECKING

from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.validator.exception.schema_validation_exception import SchemaValidationException

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema
    from kirun_py.repository import Repository


class TypeValidator:

    @staticmethod
    async def validate(
        parents: List[Schema],
        schema_type: SchemaType,
        schema: Schema,
        repository: Optional[Repository],
        element: Any,
    ) -> Any:
        from kirun_py.json.schema.validator.object_validator import ObjectValidator
        from kirun_py.json.schema.validator.array_validator import ArrayValidator
        from kirun_py.json.schema.validator.number_validator import NumberValidator
        from kirun_py.json.schema.validator.string_validator import StringValidator
        from kirun_py.json.schema.validator.boolean_validator import BooleanValidator
        from kirun_py.json.schema.validator.null_validator import NullValidator

        if schema_type == SchemaType.OBJECT:
            return await ObjectValidator.validate(
                parents, schema, repository, element,
            )

        if schema_type == SchemaType.ARRAY:
            return await ArrayValidator.validate(
                parents, schema, repository, element,
            )

        if schema_type == SchemaType.STRING:
            return StringValidator.validate(parents, schema, element)

        if schema_type in (
            SchemaType.INTEGER,
            SchemaType.LONG,
            SchemaType.FLOAT,
            SchemaType.DOUBLE,
        ):
            return NumberValidator.validate(schema_type, parents, schema, element)

        if schema_type == SchemaType.BOOLEAN:
            return BooleanValidator.validate(parents, schema, element)

        if schema_type == SchemaType.NULL:
            return NullValidator.validate(parents, schema, element)

        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        raise SchemaValidationException(
            SchemaValidator.path(parents),
            str(schema_type) + ' is not a valid type.',
        )
