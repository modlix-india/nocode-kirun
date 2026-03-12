from __future__ import annotations
from typing import TypeVar, Any, Generic

K = TypeVar('K')
V = TypeVar('V')


class MapEntry(Generic[K, V]):
    def __init__(self, k: K, v: V):
        self.k = k
        self.v = v


class MapUtil:

    @staticmethod
    def of(*args: Any) -> dict:
        result: dict = {}
        for i in range(0, len(args) - 1, 2):
            k = args[i]
            v = args[i + 1]
            if k is not None and v is not None:
                result[k] = v
        return result

    @staticmethod
    def of_array_entries(*entries: tuple) -> dict:
        result: dict = {}
        for k, v in entries:
            result[k] = v
        return result

    @staticmethod
    def entry(k: Any, v: Any) -> MapEntry:
        return MapEntry(k, v)

    @staticmethod
    def of_entries(*entries: MapEntry) -> dict:
        result: dict = {}
        for e in entries:
            result[e.k] = e.v
        return result

    @staticmethod
    def of_entries_array(*entries: tuple) -> dict:
        result: dict = {}
        for k, v in entries:
            result[k] = v
        return result
