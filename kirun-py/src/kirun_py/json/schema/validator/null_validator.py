from __future__ import annotations
from typing import Any, List, TYPE_CHECKING

from kirun_py.json.schema.validator.exception.schema_validation_exception import SchemaValidationException

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema


class NullValidator:

    @staticmethod
    def validate(parents: List[Schema], schema: Schema, element: Any) -> Any:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if element is None:
            return element

        raise SchemaValidationException(
            SchemaValidator.path(parents),
            'Expected a null but found ' + str(element),
        )
