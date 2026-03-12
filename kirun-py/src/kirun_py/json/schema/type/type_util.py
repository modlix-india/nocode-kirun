from __future__ import annotations
from typing import Optional

from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_ import Type
from kirun_py.json.schema.type.single_type import SingleType
from kirun_py.json.schema.type.multiple_type import MultipleType


class TypeUtil:

    @staticmethod
    def of(*types: SchemaType) -> Type:
        if len(types) == 1:
            return SingleType(types[0])
        return MultipleType(set(types))

    @staticmethod
    def from_value(types) -> Optional[Type]:
        if types is None:
            return None
        if isinstance(types, str):
            return SingleType(SchemaType[TypeUtil._from_json_type(types)])
        if isinstance(types, list):
            return MultipleType(
                set(SchemaType[TypeUtil._from_json_type(t)] for t in types)
            )
        return None

    @staticmethod
    def _from_json_type(jtype: str) -> str:
        n_type = jtype.upper()
        if n_type == 'NUMBER':
            return 'DOUBLE'
        return n_type
