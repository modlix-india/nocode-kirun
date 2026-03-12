from __future__ import annotations
from typing import Any, List, Optional, TYPE_CHECKING

from kirun_py.json.schema.validator.exception.schema_validation_exception import SchemaValidationException

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema
    from kirun_py.repository import Repository


class AnyOfAllOfOneOfValidator:

    @staticmethod
    async def validate(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        element: Any,
    ) -> Any:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        errors: List[SchemaValidationException] = []

        if schema.get_one_of():
            return await AnyOfAllOfOneOfValidator._one_of(
                parents, schema, repository, element, errors,
            )

        if schema.get_all_of():
            return await AnyOfAllOfOneOfValidator._all_of(
                parents, schema, repository, element, errors,
            )

        if schema.get_any_of():
            return await AnyOfAllOfOneOfValidator._any_of(
                parents, schema, repository, element, errors,
            )

        return element

    @staticmethod
    async def _any_of(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        element: Any,
        errors: List[SchemaValidationException],
    ) -> Any:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        flag = False
        for s in (schema.get_any_of() or []):
            try:
                element = await SchemaValidator.validate(
                    list(parents) if parents else [],
                    s,
                    repository,
                    element,
                )
                flag = True
                break
            except SchemaValidationException as err:
                flag = False
                errors.append(err)

        if flag:
            return element

        raise SchemaValidationException(
            SchemaValidator.path(parents),
            "The value don't satisfy any of the schemas.",
        )

    @staticmethod
    async def _all_of(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        element: Any,
        errors: List[SchemaValidationException],
    ) -> Any:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        flag = 0
        all_schemas = schema.get_all_of() or []
        for s in all_schemas:
            try:
                element = await SchemaValidator.validate(
                    list(parents) if parents else [],
                    s,
                    repository,
                    element,
                )
                flag += 1
            except SchemaValidationException as err:
                errors.append(err)

        if flag == len(all_schemas):
            return element

        raise SchemaValidationException(
            SchemaValidator.path(parents),
            "The value doesn't satisfy some of the schemas.",
        )

    @staticmethod
    async def _one_of(
        parents: List[Schema],
        schema: Schema,
        repository: Optional[Repository],
        element: Any,
        errors: List[SchemaValidationException],
    ) -> Any:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        flag = 0
        result = element
        for s in (schema.get_one_of() or []):
            try:
                result = await SchemaValidator.validate(
                    list(parents) if parents else [],
                    s,
                    repository,
                    element,
                )
                flag += 1
            except SchemaValidationException as err:
                errors.append(err)

        if flag == 1:
            return result

        if flag == 0:
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'The value does not satisfy any schema',
            )

        raise SchemaValidationException(
            SchemaValidator.path(parents),
            'The value satisfy more than one schema',
        )
