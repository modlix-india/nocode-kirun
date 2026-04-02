from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Set, TYPE_CHECKING

if TYPE_CHECKING:
    from kirun_py.json.schema.type.schema_type import SchemaType


class Type(ABC):
    @abstractmethod
    def get_allowed_schema_types(self) -> Set[SchemaType]:
        ...

    @abstractmethod
    def contains(self, type_: SchemaType) -> bool:
        ...
