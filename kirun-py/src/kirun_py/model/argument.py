from __future__ import annotations
from typing import Any


class Argument:
    def __init__(self, argument_index: int, name: str, value: Any = None):
        self._argument_index = argument_index
        self._name = name
        self._value = value

    def get_argument_index(self) -> int:
        return self._argument_index

    def set_argument_index(self, argument_index: int) -> Argument:
        self._argument_index = argument_index
        return self

    def get_name(self) -> str:
        return self._name

    def set_name(self, name: str) -> Argument:
        self._name = name
        return self

    def get_value(self) -> Any:
        return self._value

    def set_value(self, value: Any) -> Argument:
        self._value = value
        return self

    @staticmethod
    def of(name: str, value: Any) -> Argument:
        return Argument(0, name, value)
