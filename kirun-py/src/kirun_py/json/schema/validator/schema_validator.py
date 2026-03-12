from __future__ import annotations
import copy
import json
from typing import Any, Dict, List, Optional, TYPE_CHECKING

from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.validator.exception.schema_validation_exception import SchemaValidationException
from kirun_py.util.deep_equal import deep_equal
from kirun_py.util.null_check import is_null_value
from kirun_py.util.string.string_util import StringUtil

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema
    from kirun_py.repository import Repository

# Ordering for type validation priority
_ORDER: Dict[str, int] = {
    SchemaType.OBJECT.value: 0,
    SchemaType.ARRAY.value: 1,
    SchemaType.DOUBLE.value: 2,
    SchemaType.FLOAT.value: 3,
    SchemaType.LONG.value: 4,
    SchemaType.INTEGER.value: 5,
    SchemaType.STRING.value: 6,
    SchemaType.BOOLEAN.value: 7,
    SchemaType.NULL.value: 8,
}


class SchemaValidator:

    @staticmethod
    def path(parents: Optional[List[Schema]]) -> str:
        if not parents:
            return ''

        parts: List[str] = []
        for e in parents:
            title = e.get_title()
            if title:
                parts.append(title)

        result = ''
        for i, part in enumerate(parts):
            if i > 0:
                result += '.'
            result += part
        return result

    @staticmethod
    async def validate(
        parents: Optional[List[Schema]],
        schema: Optional[Schema],
        repository: Optional[Repository],
        element: Any,
    ) -> Any:
        from kirun_py.json.schema.validator.type_validator import TypeValidator
        from kirun_py.json.schema.validator.any_of_all_of_one_of_validator import AnyOfAllOfOneOfValidator
        from kirun_py.json.schema.schema_util import SchemaUtil

        if schema is None:
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'No schema found to validate',
            )

        if parents is None:
            parents = []
        parents.append(schema)

        # Default value substitution
        if is_null_value(element) and not is_null_value(schema.get_default_value()):
            return copy.deepcopy(schema.get_default_value())

        # Constant validation
        if not is_null_value(schema.get_constant()):
            return SchemaValidator._constant_validation(parents, schema, element)

        # Enum validation
        enums = schema.get_enums()
        if enums and len(enums) > 0:
            return SchemaValidator._enum_check(parents, schema, element)

        # Format without type check
        if schema.get_format() and is_null_value(schema.get_type()):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'Type is missing in schema for declared '
                + schema.get_format().name
                + ' format.',
            )

        # Type validation
        if schema.get_type():
            element = await SchemaValidator._type_validation(
                parents, schema, repository, element,
            )

        # $ref resolution
        if not StringUtil.is_null_or_blank(schema.get_ref()):
            resolved = await SchemaUtil.get_schema_from_ref(
                parents[0], repository, schema.get_ref(),
            )
            return await SchemaValidator.validate(
                parents, resolved, repository, element,
            )

        # Composition (oneOf, allOf, anyOf)
        if schema.get_one_of() or schema.get_all_of() or schema.get_any_of():
            element = await AnyOfAllOfOneOfValidator.validate(
                parents, schema, repository, element,
            )

        # Not validation
        if schema.get_not():
            flag = False
            try:
                await SchemaValidator.validate(
                    parents, schema.get_not(), repository, element,
                )
                flag = True
            except SchemaValidationException:
                flag = False

            if flag:
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    'Schema validated value in not condition.',
                )

        return element

    @staticmethod
    def _constant_validation(
        parents: List[Schema],
        schema: Schema,
        element: Any,
    ) -> Any:
        if not deep_equal(schema.get_constant(), element):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'Expecting a constant value : ' + str(element),
            )
        return element

    @staticmethod
    def _enum_check(
        parents: List[Schema],
        schema: Schema,
        element: Any,
    ) -> Any:
        for e in (schema.get_enums() or []):
            if e == element:
                return element

        raise SchemaValidationException(
            SchemaValidator.path(parents),
            'Value is not one of ' + str(schema.get_enums()),
        )

    @staticmethod
    async def _type_validation(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        element: Any,
    ) -> Any:
        from kirun_py.json.schema.validator.type_validator import TypeValidator

        type_obj = schema.get_type()
        if type_obj is None:
            return element

        allowed_types = list(type_obj.get_allowed_schema_types())
        allowed_types.sort(
            key=lambda t: _ORDER.get(t.value, 999)
        )

        errors: List[SchemaValidationException] = []

        for st in allowed_types:
            try:
                return await TypeValidator.validate(
                    parents, st, schema, repository, element,
                )
            except SchemaValidationException as err:
                errors.append(err)

        if len(errors) == 1:
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                str(errors[0]),
            )

        raise SchemaValidationException(
            SchemaValidator.path(parents),
            'Value '
            + json.dumps(element, default=str)
            + ' is not of valid type(s)',
        )
