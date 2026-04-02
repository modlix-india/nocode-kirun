from __future__ import annotations
from typing import Any, Dict, Tuple

from kirun_py.json.schema.schema import Schema


class Event:
    OUTPUT: str = 'output'
    ERROR: str = 'error'
    ITERATION: str = 'iteration'
    TRUE: str = 'true'
    FALSE: str = 'false'

    def __init__(self, evn, parameters: Dict[str, Schema] = None):
        if isinstance(evn, Event):
            self._name = evn._name
            self._parameters = {k: Schema(v) for k, v in evn._parameters.items()}
        else:
            self._name: str = evn
            if parameters is None:
                raise ValueError('Parameters required')
            self._parameters: Dict[str, Schema] = parameters

    def get_name(self) -> str:
        return self._name

    def set_name(self, name: str) -> Event:
        self._name = name
        return self

    def get_parameters(self) -> Dict[str, Schema]:
        return self._parameters

    def set_parameters(self, parameters: Dict[str, Schema]) -> Event:
        self._parameters = parameters
        return self

    @staticmethod
    def output_event_map_entry(parameters: Dict[str, Schema]) -> Tuple[str, Event]:
        return Event.event_map_entry(Event.OUTPUT, parameters)

    @staticmethod
    def event_map_entry(event_name: str, parameters: Dict[str, Schema]) -> Tuple[str, Event]:
        return (event_name, Event(event_name, parameters))

    @staticmethod
    def from_value(json: dict) -> Event:
        params = {}
        for k, v in (json.get('parameters') or {}).items():
            s = Schema.from_value(v)
            if s is not None:
                params[k] = s
        return Event(json.get('name', ''), params)

    def to_json(self) -> dict:
        return {
            'name': self._name,
            'parameters': {k: v for k, v in self._parameters.items()},
        }
