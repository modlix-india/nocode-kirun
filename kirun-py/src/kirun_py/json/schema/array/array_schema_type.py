from __future__ import annotations
from typing import Optional, List, TYPE_CHECKING

if TYPE_CHECKING:
    from kirun_py.json.schema.schema import Schema


class ArraySchemaType:
    def __init__(self, ast: Optional[ArraySchemaType] = None):
        self._single_schema: Optional[Schema] = None
        self._tuple_schema: Optional[List[Schema]] = None

        if ast is not None:
            from kirun_py.json.schema.schema import Schema as SchemaClass
            if ast._single_schema is not None:
                self._single_schema = SchemaClass(ast._single_schema)
            if ast._tuple_schema is not None:
                self._tuple_schema = [SchemaClass(e) for e in ast._tuple_schema]

    def set_single_schema(self, schema: Schema) -> ArraySchemaType:
        self._single_schema = schema
        return self

    def set_tuple_schema(self, schemas: List[Schema]) -> ArraySchemaType:
        self._tuple_schema = schemas
        return self

    def get_single_schema(self) -> Optional[Schema]:
        return self._single_schema

    def get_tuple_schema(self) -> Optional[List[Schema]]:
        return self._tuple_schema

    def is_single_type(self) -> bool:
        return self._single_schema is not None

    @staticmethod
    def of(*schemas: Schema) -> ArraySchemaType:
        if len(schemas) == 1:
            return ArraySchemaType().set_single_schema(schemas[0])
        return ArraySchemaType().set_tuple_schema(list(schemas))

    @staticmethod
    def from_value(obj) -> Optional[ArraySchemaType]:
        if obj is None:
            return None

        from kirun_py.json.schema.schema import Schema

        if isinstance(obj, list):
            return ArraySchemaType().set_tuple_schema(Schema.from_list_of_schemas(obj) or [])

        if isinstance(obj, dict):
            keys = list(obj.keys())
            if 'singleSchema' in keys:
                s = Schema.from_value(obj['singleSchema'])
                if s:
                    return ArraySchemaType().set_single_schema(s)
            elif 'tupleSchema' in keys:
                return ArraySchemaType().set_tuple_schema(
                    Schema.from_list_of_schemas(obj['tupleSchema']) or []
                )

            x = Schema.from_value(obj)
            if x is None:
                return None
            return ArraySchemaType().set_single_schema(x)

        return None
