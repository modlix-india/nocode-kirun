from __future__ import annotations
from typing import Set, Union

from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_ import Type


class SingleType(Type):
    def __init__(self, type_: Union[SchemaType, SingleType]):
        if isinstance(type_, SingleType):
            self._type = type_._type
        else:
            self._type = type_

    def get_type(self) -> SchemaType:
        return self._type

    def get_allowed_schema_types(self) -> Set[SchemaType]:
        return {self._type}

    def contains(self, type_: SchemaType) -> bool:
        return self._type == type_
