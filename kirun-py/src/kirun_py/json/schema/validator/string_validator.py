from __future__ import annotations
import re
from typing import Any, List, Optional, TYPE_CHECKING

from kirun_py.json.schema.string.string_format import StringFormat
from kirun_py.json.schema.validator.exception.schema_validation_exception import SchemaValidationException
from kirun_py.util.error_message_formatter import ErrorMessageFormatter
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema

# Pre-compiled format patterns
_TIME_PATTERN = re.compile(
    r'^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?(\.\d{1,3})?(Z|[+-][01][0-9]:[0-5][0-9])?$'
)

_DATE_PATTERN = re.compile(
    r'^[0-9]{4}-([0][0-9]|[1][0-2])-(0[1-9]|[1-2][0-9]|3[01])$'
)

_DATETIME_PATTERN = re.compile(
    r'^[0-9]{4}-([0][0-9]|[1][0-2])-(0[1-9]|[1-2][0-9]|3[01])T([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?(\.\d{1,3})?(Z|[+-][01][0-9]:[0-5][0-9])?$'
)

_EMAIL_PATTERN = re.compile(
    r"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$"
)


class StringValidator:

    @staticmethod
    def validate(parents: List[Schema], schema: Schema, element: Any) -> Any:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if is_null_value(element):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                f'Expected a string but found {ErrorMessageFormatter.format_value(element)}',
            )

        if not isinstance(element, str):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                f'Expected a string but found {ErrorMessageFormatter.format_value(element)}',
            )

        fmt = schema.get_format()

        if fmt == StringFormat.TIME:
            StringValidator._pattern_matcher(
                parents, schema, element, _TIME_PATTERN, 'time pattern',
            )
        elif fmt == StringFormat.DATE:
            StringValidator._pattern_matcher(
                parents, schema, element, _DATE_PATTERN, 'date pattern',
            )
        elif fmt == StringFormat.DATETIME:
            StringValidator._pattern_matcher(
                parents, schema, element, _DATETIME_PATTERN, 'date time pattern',
            )
        elif fmt == StringFormat.EMAIL:
            StringValidator._pattern_matcher(
                parents, schema, element, _EMAIL_PATTERN, 'email pattern',
            )
        elif fmt == StringFormat.REGEX:
            # For REGEX format, validate that the string is a valid regex
            try:
                re.compile(element)
            except re.error:
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    element + ' is not a valid regular expression',
                )
        elif schema.get_pattern():
            try:
                compiled = re.compile(schema.get_pattern())
            except re.error:
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    'Invalid regex pattern: ' + schema.get_pattern(),
                )
            StringValidator._pattern_matcher(
                parents, schema, element, compiled, 'pattern ' + schema.get_pattern(),
            )

        length = len(element)

        if schema.get_min_length() is not None and length < schema.get_min_length():
            details = schema.get_details()
            msg = (
                details.get_validation_message('minLength')
                if details else None
            )
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                msg or ('Expected a minimum of ' + str(schema.get_min_length()) + ' characters'),
            )

        if schema.get_max_length() is not None and length > schema.get_max_length():
            details = schema.get_details()
            msg = (
                details.get_validation_message('maxLength')
                if details else None
            )
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                msg or ('Expected a maximum of ' + str(schema.get_max_length()) + ' characters'),
            )

        return element

    @staticmethod
    def _pattern_matcher(
        parents: List[Schema],
        schema: Schema,
        element: Any,
        pattern: re.Pattern,
        message: str,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        matched = pattern.search(element) is not None
        if not matched:
            details = schema.get_details()
            msg = (
                details.get_validation_message('pattern')
                if details else None
            )
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                msg or (str(element) + ' is not matched with the ' + message),
            )
