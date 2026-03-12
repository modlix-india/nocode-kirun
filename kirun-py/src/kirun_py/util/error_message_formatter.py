from __future__ import annotations
import json
from typing import Any, TYPE_CHECKING

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema


class ErrorMessageFormatter:

    @staticmethod
    def format_value(value: Any, max_length: int = 200) -> str:
        if value is None:
            return 'null'

        if isinstance(value, str):
            return f'"{value}"'
        if isinstance(value, (int, float, bool)):
            return str(value).lower() if isinstance(value, bool) else str(value)

        try:
            j = json.dumps(value, default=str)
            formatted = j
            if len(j) > 2:
                formatted = j.replace(',', ', ').replace(':', ': ')
            if len(formatted) > max_length:
                return formatted[:max_length] + '...'
            return formatted
        except Exception:
            return f'[{type(value).__name__}]'

    @staticmethod
    def format_function_name(namespace: str | None, name: str) -> str:
        if not namespace or namespace in ('undefined', 'null', 'None'):
            return name
        return f'{namespace}.{name}'

    @staticmethod
    def format_statement_name(statement_name: str | None) -> str | None:
        if not statement_name or statement_name in ('undefined', 'null', 'None'):
            return None
        return f"'{statement_name}'"

    @staticmethod
    def format_schema_definition(schema: Schema | None) -> str:
        if schema is None:
            return 'any'
        type_ = schema.get_type()
        if type_ is None:
            return 'any'
        allowed_types = type_.get_allowed_schema_types()
        if not allowed_types:
            return 'any'
        if len(allowed_types) == 1:
            single_type = next(iter(allowed_types))
            return ErrorMessageFormatter._format_single_schema_type(schema, single_type)
        return ' | '.join(t.value if hasattr(t, 'value') else str(t) for t in allowed_types)

    @staticmethod
    def _format_single_schema_type(schema: Schema, schema_type: Any) -> str:
        from kirun_py.json.schema.type.schema_type import SchemaType
        type_name = schema_type.value if hasattr(schema_type, 'value') else str(schema_type)

        if schema_type == SchemaType.ARRAY:
            items = schema.get_items()
            if items:
                single_schema = items.get_single_schema()
                if single_schema:
                    item_type = ErrorMessageFormatter.format_schema_definition(single_schema)
                    return f'Array<{item_type}>'
                tuple_schemas = items.get_tuple_schema()
                if tuple_schemas and len(tuple_schemas) > 0:
                    tuple_types = ', '.join(
                        ErrorMessageFormatter.format_schema_definition(s) for s in tuple_schemas
                    )
                    return f'[{tuple_types}]'
            return 'Array'

        enums = schema.get_enums()
        if enums and len(enums) > 0:
            enum_values = ' | '.join(
                ErrorMessageFormatter.format_value(e, 50) for e in enums[:5]
            )
            more = ' | ...' if len(enums) > 5 else ''
            return f'{type_name}({enum_values}{more})'

        return type_name

    @staticmethod
    def build_function_execution_error(
        function_name: str,
        statement_name: str | None,
        error_message: str,
        parameter_name: str | None = None,
        parameter_schema: Schema | None = None,
    ) -> str:
        parameter_part = f"'s parameter {parameter_name}" if parameter_name else ''
        statement_part = f' in statement {statement_name}' if statement_name else ''
        definition_part = (
            f' [Expected: {ErrorMessageFormatter.format_schema_definition(parameter_schema)}]'
            if parameter_schema else ''
        )
        separator = '\n' if error_message.startswith('Error while executing the function ') else ''
        return (
            f'Error while executing the function {function_name}{parameter_part}'
            f'{statement_part}{definition_part}: {separator}{error_message}'
        )

    @staticmethod
    def format_error_message(error: Any) -> str:
        if error is None:
            return 'Unknown error'
        if isinstance(error, str):
            return error
        if hasattr(error, 'args') and error.args:
            message = str(error.args[0])
            if '[object Object]' in message:
                return message.replace('[object Object]', ErrorMessageFormatter.format_value(error))
            return message
        return ErrorMessageFormatter.format_value(error)
