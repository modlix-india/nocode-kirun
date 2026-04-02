from __future__ import annotations
from typing import Any, Optional, TYPE_CHECKING

from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.util.null_check import is_null_value

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema
    from kirun_py.repository import Repository


class SchemaUtil:

    @staticmethod
    def get_default_value(schema: Schema, schema_repository: Optional[Repository] = None) -> Any:
        if schema is None:
            return None

        if schema.get_constant() is not None:
            return schema.get_constant()

        if schema.get_default_value() is not None:
            return schema.get_default_value()

        ref = schema.get_ref()
        if ref and schema_repository:
            import asyncio
            resolved = asyncio.get_event_loop().run_until_complete(
                SchemaUtil.get_schema_from_ref(schema, schema_repository, ref)
            )
            if resolved:
                return SchemaUtil.get_default_value(resolved, schema_repository)

        return None

    @staticmethod
    def has_default_value_or_null_schema_type(schema: Schema) -> bool:
        if schema.get_default_value() is not None or schema.get_constant() is not None:
            return True
        type_ = schema.get_type()
        if type_ is None:
            return False
        return type_.contains(SchemaType.NULL)

    @staticmethod
    async def get_schema_from_ref(
        schema: Schema,
        schema_repository: Repository,
        ref: str,
    ) -> Optional[Schema]:
        from kirun_py.json.schema.schema import Schema as SchemaClass

        count = 0
        current_ref = ref
        current_schema = schema

        while current_ref and count < 20:
            count += 1

            # Internal reference
            if current_ref.startswith('#/'):
                resolved = SchemaUtil._resolve_internal_schema(current_schema, current_ref)
                if resolved is None:
                    return None
                current_ref = resolved.get_ref()
                if current_ref is None:
                    return resolved
                current_schema = resolved
                continue

            # External reference
            parts = current_ref.split('/')
            ns_name = parts[0]
            dot_pos = ns_name.rfind('.')
            if dot_pos == -1:
                return None

            namespace = ns_name[:dot_pos]
            name = ns_name[dot_pos + 1:]

            resolved = await schema_repository.find(namespace, name)
            if resolved is None:
                return None

            if len(parts) > 1:
                path = '/'.join(parts[1:])
                resolved = SchemaUtil._resolve_internal_schema(resolved, f'#/{path}')

            if resolved is None:
                return None

            current_ref = resolved.get_ref()
            if current_ref is None:
                return resolved
            current_schema = resolved

        return None

    @staticmethod
    def _resolve_internal_schema(schema: Schema, ref: str) -> Optional[Schema]:
        if not ref or not ref.startswith('#/'):
            return None

        path = ref[2:]  # Remove '#/'
        parts = path.split('/')

        current = schema
        for part in parts:
            if part == '$defs':
                defs = current.get_defs()
                if defs is None:
                    return None
                continue
            elif part == 'properties':
                props = current.get_properties()
                if props is None:
                    return None
                continue
            else:
                # Try $defs first, then properties
                defs = current.get_defs()
                if defs and part in defs:
                    current = defs[part]
                    continue
                props = current.get_properties()
                if props and part in props:
                    current = props[part]
                    continue
                return None

        return current
