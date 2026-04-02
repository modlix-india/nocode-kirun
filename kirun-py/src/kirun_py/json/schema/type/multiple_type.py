from __future__ import annotations
from typing import Set, Union

from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_ import Type


class MultipleType(Type):
    def __init__(self, type_: Union[Set[SchemaType], MultipleType]):
        if isinstance(type_, MultipleType):
            self._type = set(type_._type)
        else:
            self._type = set(type_)

    def get_type(self) -> Set[SchemaType]:
        return self._type

    def set_type(self, type_: Set[SchemaType]) -> MultipleType:
        self._type = type_
        return self

    def get_allowed_schema_types(self) -> Set[SchemaType]:
        return self._type

    def contains(self, type_: SchemaType) -> bool:
        return type_ in self._type if self._type else False
