from __future__ import annotations
from typing import Any, List, Optional, TYPE_CHECKING

from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.validator.exception.schema_validation_exception import SchemaValidationException
from kirun_py.util.error_message_formatter import ErrorMessageFormatter
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema

# Integer range constants (Java int: -2^31 to 2^31 - 1)
_INT_MIN = -(2 ** 31)
_INT_MAX = (2 ** 31) - 1


class NumberValidator:

    @staticmethod
    def validate(
        schema_type: SchemaType,
        parents: List[Schema],
        schema: Schema,
        element: Any,
    ) -> Any:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if is_null_value(element):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                'Expected a number but found null',
            )

        if isinstance(element, bool) or not isinstance(element, (int, float)):
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                f'Expected a {schema_type.value} but found {ErrorMessageFormatter.format_value(element)}',
            )

        n = NumberValidator._extract_number(schema_type, parents, schema, element)

        NumberValidator._check_range(parents, schema, element, n)
        NumberValidator._check_multiple_of(parents, schema, element, n)

        return element

    @staticmethod
    def _extract_number(
        schema_type: SchemaType,
        parents: List[Schema],
        schema: Schema,
        element: Any,
    ) -> float:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        n = element

        if schema_type == SchemaType.INTEGER:
            if not isinstance(element, int) or isinstance(element, bool):
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    str(element) + ' is not a number of type ' + schema_type.value,
                )
            if element < _INT_MIN or element > _INT_MAX:
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    str(element) + ' is not a number of type ' + schema_type.value,
                )
            n = element

        elif schema_type == SchemaType.LONG:
            if not isinstance(element, int) or isinstance(element, bool):
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    str(element) + ' is not a number of type ' + schema_type.value,
                )
            n = element

        elif schema_type in (SchemaType.FLOAT, SchemaType.DOUBLE):
            if isinstance(element, bool) or not isinstance(element, (int, float)):
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    str(element) + ' is not a number of type ' + schema_type.value,
                )
            try:
                n = float(element)
            except (OverflowError, ValueError):
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    str(element) + ' is not a number of type ' + schema_type.value,
                )

        return n

    @staticmethod
    def _check_range(
        parents: List[Schema],
        schema: Schema,
        element: Any,
        n: float,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if not is_null_value(schema.get_minimum()) and n < schema.get_minimum():
            details = schema.get_details()
            msg = (
                details.get_validation_message('minimum')
                if details else None
            )
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                msg or (str(element) + ' should be greater than or equal to ' + str(schema.get_minimum())),
            )

        if not is_null_value(schema.get_maximum()) and n > schema.get_maximum():
            details = schema.get_details()
            msg = (
                details.get_validation_message('maximum')
                if details else None
            )
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                msg or (str(element) + ' should be less than or equal to ' + str(schema.get_maximum())),
            )

        if not is_null_value(schema.get_exclusive_minimum()) and n <= schema.get_exclusive_minimum():
            details = schema.get_details()
            msg = (
                details.get_validation_message('exclusiveMinimum')
                if details else None
            )
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                msg or (str(element) + ' should be greater than ' + str(schema.get_exclusive_minimum())),
            )

        if not is_null_value(schema.get_exclusive_maximum()) and n > schema.get_exclusive_maximum():
            details = schema.get_details()
            msg = (
                details.get_validation_message('exclusiveMaximum')
                if details else None
            )
            raise SchemaValidationException(
                SchemaValidator.path(parents),
                msg or (str(element) + ' should be less than ' + str(schema.get_exclusive_maximum())),
            )

    @staticmethod
    def _check_multiple_of(
        parents: List[Schema],
        schema: Schema,
        element: Any,
        n: float,
    ) -> None:
        from kirun_py.json.schema.validator.schema_validator import SchemaValidator

        if schema.get_multiple_of():
            l1 = n
            l2 = schema.get_multiple_of()

            if l1 % l2 != 0:
                details = schema.get_details()
                msg = (
                    details.get_validation_message('multipleOf')
                    if details else None
                )
                raise SchemaValidationException(
                    SchemaValidator.path(parents),
                    msg or (str(element) + ' is not multiple of ' + str(schema.get_multiple_of())),
                )
