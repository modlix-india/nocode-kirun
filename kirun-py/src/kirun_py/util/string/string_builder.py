from __future__ import annotations
from typing import Any

from kirun_py.exception import KIRuntimeException


class StringBuilder:
    def __init__(self, s: str = ''):
        self._str = s

    def append(self, x: Any) -> StringBuilder:
        self._str += str(x)
        return self

    def __str__(self) -> str:
        return self._str

    def trim(self) -> StringBuilder:
        self._str = self._str.strip()
        return self

    def set_length(self, num: int) -> StringBuilder:
        self._str = self._str[:num]
        return self

    def length(self) -> int:
        return len(self._str)

    def char_at(self, index: int) -> str:
        return self._str[index]

    def delete_char_at(self, index: int) -> StringBuilder:
        self._check_index(index)
        self._str = self._str[:index] + self._str[index + 1:]
        return self

    def insert(self, index: int, s: str) -> StringBuilder:
        self._str = self._str[:index] + s + self._str[index:]
        return self

    def _check_index(self, index: int) -> None:
        if index >= len(self._str):
            raise KIRuntimeException(
                f'Index {index} is greater than or equal to {len(self._str)}'
            )

    def substring(self, start: int, end: int) -> str:
        return self._str[start:end]
