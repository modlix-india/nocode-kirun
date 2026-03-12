from __future__ import annotations

import json
from typing import Any, Union


class SchemaTransformer:
    """
    Schema Transformer.
    Converts between simple schema syntax and JSON Schema.
    """

    _PRIMITIVE_TYPES = {
        'INTEGER',
        'LONG',
        'FLOAT',
        'DOUBLE',
        'STRING',
        'BOOLEAN',
        'NULL',
        'ANY',
        'OBJECT',
    }

    @staticmethod
    def transform(schema_spec: Union[str, dict]) -> Any:
        """
        Transform simple schema syntax to JSON Schema.

        Examples:
            "INTEGER" -> {"type": "INTEGER"}
            "ARRAY OF INTEGER" -> {"type": "ARRAY", "items": {"type": "INTEGER"}}
            {"type": "STRING", "minLength": 5} -> {"type": "STRING", "minLength": 5} (pass through)
        """
        if isinstance(schema_spec, dict):
            return schema_spec

        spec = schema_spec.strip()

        if spec.startswith('ARRAY OF '):
            inner_type = spec[len('ARRAY OF '):].strip()
            return {
                'type': 'ARRAY',
                'items': SchemaTransformer.transform(inner_type),
            }

        if spec in SchemaTransformer._PRIMITIVE_TYPES:
            return {'type': spec}

        # Unknown schema specification, treating as STRING
        return {'type': 'STRING'}

    @staticmethod
    def to_text(schema: Any) -> str:
        """
        Transform JSON Schema back to simple schema syntax (best effort).

        Examples:
            {"type": "INTEGER"} -> "INTEGER"
            {"type": "ARRAY", "items": {"type": "INTEGER"}} -> "ARRAY OF INTEGER"
            Complex schema -> json.dumps(schema)
        """
        if not schema or not isinstance(schema, dict):
            return str(schema)

        if schema.get('type') == 'ARRAY' and 'items' in schema:
            items_text = SchemaTransformer.to_text(schema['items'])
            return f"ARRAY OF {items_text}"

        schema_type = schema.get('type')
        if schema_type and schema_type in SchemaTransformer._PRIMITIVE_TYPES:
            keys = list(schema.keys())
            if len(keys) == 1 and keys[0] == 'type':
                return schema_type

        return json.dumps(schema)

    @staticmethod
    def is_simple_schema(schema: Any) -> bool:
        """Check if a schema specification is simple (can be represented as text)."""
        if not schema or not isinstance(schema, dict):
            return False

        schema_type = schema.get('type')
        if schema_type and schema_type in SchemaTransformer._PRIMITIVE_TYPES:
            if len(schema.keys()) == 1:
                return True

        if schema.get('type') == 'ARRAY' and 'items' in schema:
            return SchemaTransformer.is_simple_schema(schema['items'])

        return False
