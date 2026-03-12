from __future__ import annotations
from typing import Any, List, TYPE_CHECKING

from kirun_py.json.schema.validator.exception.schema_validation_exception import SchemaValidationException
from kirun_py.util.error_message_formatter import ErrorMessageFormatter

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema


class BooleanValidator:

    @staticmethod
    def validate(parents: List[Schema], schema: Schema, element: Any) -> Any:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if element is None:
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected a boolean but found null',
            )

        if not isinstance(element, bool):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                f'Expected a boolean but found {ErrorMessageFormatter.format_value(element)}',
            )

        return element
