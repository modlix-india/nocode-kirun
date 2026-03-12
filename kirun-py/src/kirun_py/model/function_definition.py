from __future__ import annotations
from typing import Dict, List, Optional

from kirun_py.model.event import Event
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.model.statement import Statement
from kirun_py.model.statement_group import StatementGroup


class FunctionDefinition(FunctionSignature):
    SCHEMA_NAME: str = 'FunctionDefinition'

    def __init__(self, name: str):
        super().__init__(name)
        self._version: int = 1
        self._steps: Optional[Dict[str, Statement]] = None
        self._step_groups: Optional[Dict[str, StatementGroup]] = None
        self._parts: Optional[List[FunctionDefinition]] = None

    def get_version(self) -> int:
        return self._version

    def set_version(self, version: int) -> FunctionDefinition:
        self._version = version
        return self

    def get_steps(self) -> Dict[str, Statement]:
        return self._steps if self._steps is not None else {}

    def set_steps(self, steps: Dict[str, Statement]) -> FunctionDefinition:
        self._steps = steps
        return self

    def get_step_groups(self) -> Optional[Dict[str, StatementGroup]]:
        return self._step_groups

    def set_step_groups(self, step_groups: Dict[str, StatementGroup]) -> FunctionDefinition:
        self._step_groups = step_groups
        return self

    def get_parts(self) -> Optional[List[FunctionDefinition]]:
        return self._parts

    def set_parts(self, parts: List[FunctionDefinition]) -> FunctionDefinition:
        self._parts = parts
        return self

    @staticmethod
    def from_value(json: dict) -> FunctionDefinition:
        if not json:
            return FunctionDefinition('unknown')

        fd = FunctionDefinition(json.get('name', 'unknown'))

        # Steps
        steps: Dict[str, Statement] = {}
        for v in (json.get('steps') or {}).values():
            if v:
                stmt = Statement.from_value(v)
                steps[stmt.get_statement_name()] = stmt
        fd.set_steps(steps)

        # Step groups
        step_groups: Dict[str, StatementGroup] = {}
        for v in (json.get('stepGroups') or {}).values():
            if v:
                sg = StatementGroup.from_value(v)
                step_groups[sg.get_statement_group_name()] = sg
        fd.set_step_groups(step_groups)

        # Parts
        parts = [
            FunctionDefinition.from_value(e)
            for e in (json.get('parts') or [])
            if e
        ]
        fd.set_parts(parts)

        fd.set_version(json.get('version', 1))

        # Events
        events = {}
        for v in (json.get('events') or {}).values():
            if v:
                evt = Event.from_value(v)
                events[evt.get_name()] = evt
        fd.set_events(events)

        # Parameters
        params = {}
        for v in (json.get('parameters') or {}).values():
            if v:
                p = Parameter.from_value(v)
                params[p.get_parameter_name()] = p
        fd.set_parameters(params)

        fd.set_namespace(json.get('namespace', '_'))

        return fd

    def to_json(self) -> dict:
        base = super().to_json()
        base['version'] = self._version
        base['steps'] = (
            {k: v.to_json() for k, v in self._steps.items()}
            if self._steps
            else {}
        )
        base['stepGroups'] = (
            {k: v.to_json() for k, v in self._step_groups.items()}
            if self._step_groups
            else {}
        )
        base['parts'] = (
            [p.to_json() for p in self._parts]
            if self._parts
            else []
        )
        return base
