from __future__ import annotations
import re
from typing import Any, Dict, List, Optional, Set, TYPE_CHECKING

from kirun_py.json.schema.validator.exception.schema_validation_exception import SchemaValidationException
from kirun_py.util.error_message_formatter import ErrorMessageFormatter
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema, AdditionalType
    from kirun_py.repository import Repository


class ObjectValidator:

    @staticmethod
    async def validate(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        element: Any,
    ) -> Any:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if is_null_value(element):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected an object but found null',
            )

        if not isinstance(element, dict) or isinstance(element, list):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                f'Expected an object but found {ErrorMessageFormatter.format_value(element)}',
            )

        json_object: dict = element
        keys: set = set(json_object.keys())

        ObjectValidator._check_min_max_properties(parents, schema, keys)

        if schema.get_property_names():
            await ObjectValidator._check_property_name_schema(
                parents, schema, repository, keys,
            )

        if schema.get_required():
            ObjectValidator._check_required(parents, schema, json_object)

        if schema.get_properties():
            await ObjectValidator._check_properties(
                parents, schema, repository, json_object, keys,
            )

        if schema.get_pattern_properties():
            await ObjectValidator._check_pattern_properties(
                parents, schema, repository, json_object, keys,
            )

        if schema.get_additional_properties():
            await ObjectValidator._check_additional_properties(
                parents, schema, repository, json_object, keys,
            )

        return json_object

    @staticmethod
    async def _check_property_name_schema(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        keys: set,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        for key in list(keys):
            try:
                await SchemaValidator.validate(
                    parents,
                    schema.get_property_names(),
                    repository,
                    key,
                )
            except SchemaValidationException:
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    "Property name '" + str(key) + "' does not fit the property schema",
                )

    @staticmethod
    def _check_required(
        parents: List[Schema],
        schema: Schema,
        json_object: dict,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        for key in (schema.get_required() or []):
            if is_null_value(json_object.get(key)):
                props = schema.get_properties()
                msg = None
                if props and key in props:
                    details = props[key].get_details()
                    if details:
                        msg = details.get_validation_message('mandatory')
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    msg or (key + ' is mandatory'),
                )

    @staticmethod
    async def _check_additional_properties(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        json_object: dict,
        keys: set,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        apt = schema.get_additional_properties()

        if apt.get_schema_value():
            for key in list(keys):
                new_parents: List[Schema] = list(parents) if parents else []
                json_object[key] = await SchemaValidator.validate(
                    new_parents,
                    apt.get_schema_value(),
                    repository,
                    json_object[key],
                )
        else:
            if apt.get_boolean_value() is False and len(keys) > 0:
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    ', '.join(keys) + ' is/are additional properties which are not allowed.',
                )

    @staticmethod
    async def _check_pattern_properties(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        json_object: dict,
        keys: set,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        pattern_props = schema.get_pattern_properties()
        compiled_patterns: Dict[str, re.Pattern] = {}
        for key_pattern in pattern_props.keys():
            compiled_patterns[key_pattern] = re.compile(key_pattern)

        for key in list(keys):
            new_parents: List[Schema] = list(parents) if parents else []

            for pat_key, compiled in compiled_patterns.items():
                if compiled.search(key):
                    json_object[key] = await SchemaValidator.validate(
                        new_parents,
                        pattern_props[pat_key],
                        repository,
                        json_object[key],
                    )
                    keys.discard(key)
                    break

    @staticmethod
    async def _check_properties(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        json_object: dict,
        keys: set,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator
        from kirun_py.json.schema.schema_util import SchemaUtil

        properties = schema.get_properties()

        for prop_name, prop_schema in properties.items():
            value = json_object.get(prop_name)

            if prop_name not in json_object and is_null_value(value):
                def_value = SchemaUtil.get_default_value(prop_schema, repository)
                if is_null_value(def_value):
                    continue

            new_parents: List[Schema] = list(parents) if parents else []
            json_object[prop_name] = await SchemaValidator.validate(
                new_parents,
                prop_schema,
                repository,
                value,
            )
            keys.discard(prop_name)

    @staticmethod
    def _check_min_max_properties(
        parents: List[Schema],
        schema: Schema,
        keys: set,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if (
            schema.get_min_properties() is not None
            and len(keys) < schema.get_min_properties()
        ):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'Object should have minimum of ' + str(schema.get_min_properties()) + ' properties',
            )

        if (
            schema.get_max_properties() is not None
            and len(keys) > schema.get_max_properties()
        ):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'Object can have maximum of ' + str(schema.get_max_properties()) + ' properties',
            )
