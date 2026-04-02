from __future__ import annotations
import copy
from typing import Any, Dict, Optional

from kirun_py.model.event import Event
from kirun_py.model.parameter import Parameter
from kirun_py.util.duplicate import duplicate


class FunctionSignature:
    SCHEMA_NAME: str = 'FunctionSignature'

    def __init__(self, value):
        if isinstance(value, FunctionSignature):
            self._name = value._name
            self._namespace = value._namespace
            self._parameters: Dict[str, Parameter] = {
                k: Parameter(v) for k, v in value._parameters.items()
            }
            self._events: Dict[str, Event] = {
                k: Event(v) for k, v in value._events.items()
            }
            self._description = value._description
            self._documentation = value._documentation
            self._metadata = duplicate(value._metadata) if value._metadata else None
        else:
            self._name: str = value
            self._namespace: str = '_'
            self._parameters: Dict[str, Parameter] = {}
            self._events: Dict[str, Event] = {}
            self._description: Optional[str] = None
            self._documentation: Optional[str] = None
            self._metadata: Optional[Dict[str, Any]] = None

    def get_namespace(self) -> str:
        return self._namespace

    def set_namespace(self, namespace: str) -> FunctionSignature:
        self._namespace = namespace
        return self

    def get_name(self) -> str:
        return self._name

    def set_name(self, name: str) -> FunctionSignature:
        self._name = name
        return self

    def get_parameters(self) -> Dict[str, Parameter]:
        return self._parameters

    def set_parameters(self, parameters: Dict[str, Parameter]) -> FunctionSignature:
        self._parameters = parameters
        return self

    def get_events(self) -> Dict[str, Event]:
        return self._events

    def set_events(self, events: Dict[str, Event]) -> FunctionSignature:
        self._events = events
        return self

    def get_full_name(self) -> str:
        return self._namespace + '.' + self._name

    def get_description(self) -> Optional[str]:
        return self._description

    def set_description(self, description: str) -> FunctionSignature:
        self._description = description
        return self

    def get_documentation(self) -> Optional[str]:
        return self._documentation

    def set_documentation(self, documentation: str) -> FunctionSignature:
        self._documentation = documentation
        return self

    def get_metadata(self) -> Optional[Dict[str, Any]]:
        return self._metadata

    def set_metadata(self, metadata: Dict[str, Any]) -> FunctionSignature:
        self._metadata = metadata
        return self

    def to_json(self) -> dict:
        return {
            'namespace': self._namespace,
            'name': self._name,
            'parameters': {k: v.to_json() for k, v in self._parameters.items()},
            'events': {k: v.to_json() for k, v in self._events.items()},
            'description': self._description,
            'documentation': self._documentation,
            'metadata': duplicate(self._metadata) if self._metadata else None,
        }
