from __future__ import annotations
import copy
from typing import Any, Optional


class ContextElement:
    def __init__(self, element: Any = None, index: Optional[int] = None):
        self._element = element
        self._index = index

    def get_element(self) -> Any:
        return self._element

    def set_element(self, element: Any) -> ContextElement:
        self._element = element
        return self

    def get_index(self) -> Optional[int]:
        return self._index

    def set_index(self, index: int) -> ContextElement:
        self._index = index
        return self

    def duplicate(self) -> ContextElement:
        return ContextElement(copy.deepcopy(self._element), self._index)
