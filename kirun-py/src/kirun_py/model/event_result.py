from __future__ import annotations
from typing import Any, Dict

from kirun_py.model.event import Event


class EventResult:
    def __init__(self, name: str, result: Dict[str, Any]):
        self._name = name
        self._result = result

    def get_name(self) -> str:
        return self._name

    def set_name(self, name: str) -> EventResult:
        self._name = name
        return self

    def get_result(self) -> Dict[str, Any]:
        return self._result

    def set_result(self, result: Dict[str, Any]) -> EventResult:
        self._result = result
        return self

    @staticmethod
    def output_of(result: Dict[str, Any]) -> EventResult:
        return EventResult.of(Event.OUTPUT, result)

    @staticmethod
    def of(event_name: str, result: Dict[str, Any]) -> EventResult:
        return EventResult(event_name, result)
