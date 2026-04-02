from __future__ import annotations
from typing import Any, Dict, Optional, Tuple

from kirun_py.model.abstract_statement import AbstractStatement
from kirun_py.model.parameter_reference import ParameterReference
from kirun_py.model.position import Position


class Statement(AbstractStatement):
    SCHEMA_NAME: str = 'Statement'

    def __init__(
        self,
        sn: str,
        namespace: Optional[str] = None,
        name: Optional[str] = None,
    ):
        if isinstance(sn, Statement):
            super().__init__(sn)
            x = sn
            self._statement_name = x._statement_name
            self._name = x._name
            self._namespace = x._namespace
            self._parameter_map: Optional[Dict[str, Dict[str, ParameterReference]]] = None
            self._dependent_statements: Optional[Dict[str, bool]] = None
            self._execute_if_true: Optional[Dict[str, bool]] = None

            if x._parameter_map is not None:
                self._parameter_map = {
                    k: {ik: ParameterReference(iv) for ik, iv in v.items()}
                    for k, v in x._parameter_map.items()
                }
            if x._dependent_statements is not None:
                self._dependent_statements = dict(x._dependent_statements)
            if x._execute_if_true is not None:
                self._execute_if_true = dict(x._execute_if_true)
        else:
            super().__init__()
            if namespace is None or name is None:
                raise ValueError('namespace and name are required')
            self._statement_name: str = sn
            self._namespace: str = namespace
            self._name: str = name
            self._parameter_map: Optional[Dict[str, Dict[str, ParameterReference]]] = None
            self._dependent_statements: Optional[Dict[str, bool]] = None
            self._execute_if_true: Optional[Dict[str, bool]] = None

    def get_statement_name(self) -> str:
        return self._statement_name

    def set_statement_name(self, statement_name: str) -> Statement:
        self._statement_name = statement_name
        return self

    def get_namespace(self) -> str:
        return self._namespace

    def set_namespace(self, namespace: str) -> Statement:
        self._namespace = namespace
        return self

    def get_name(self) -> str:
        return self._name

    def set_name(self, name: str) -> Statement:
        self._name = name
        return self

    def get_parameter_map(self) -> Dict[str, Dict[str, ParameterReference]]:
        if self._parameter_map is None:
            self._parameter_map = {}
        return self._parameter_map

    def set_parameter_map(
        self, parameter_map: Dict[str, Dict[str, ParameterReference]]
    ) -> Statement:
        self._parameter_map = parameter_map
        return self

    def get_dependent_statements(self) -> Dict[str, bool]:
        return self._dependent_statements if self._dependent_statements is not None else {}

    def set_dependent_statements(self, dependent_statements: Dict[str, bool]) -> Statement:
        self._dependent_statements = dependent_statements
        return self

    def get_execute_if_true(self) -> Dict[str, bool]:
        return self._execute_if_true if self._execute_if_true is not None else {}

    def set_execute_if_true(self, execute_if_true: Dict[str, bool]) -> Statement:
        self._execute_if_true = execute_if_true
        return self

    def __eq__(self, other: Any) -> bool:
        if not isinstance(other, Statement):
            return False
        return other._statement_name == self._statement_name

    def __hash__(self) -> int:
        return hash(self._statement_name)

    @staticmethod
    def of_entry(statement: Statement) -> Tuple[str, Statement]:
        return (statement._statement_name, statement)

    def to_json(self) -> dict:
        parameter_map_obj: Dict[str, Dict[str, Any]] = {}
        if self._parameter_map:
            for k, v in self._parameter_map.items():
                parameter_map_obj[k] = {}
                for ik, iv in v.items():
                    parameter_map_obj[k][ik] = iv.to_json()

        return {
            'statementName': self._statement_name,
            'namespace': self._namespace,
            'name': self._name,
            'parameterMap': parameter_map_obj,
            'dependentStatements': dict(self._dependent_statements)
            if self._dependent_statements
            else {},
            'executeIftrue': dict(self._execute_if_true)
            if self._execute_if_true
            else {},
            'position': self.get_position().to_json() if self.get_position() else None,
            'comment': self.get_comment(),
            'description': self.get_description(),
            'override': self.is_override(),
        }

    @staticmethod
    def from_value(json: dict) -> Statement:
        stmt = Statement(
            json.get('statementName', ''),
            json.get('namespace', ''),
            json.get('name', ''),
        )

        # Parse parameter map
        param_map: Dict[str, Dict[str, ParameterReference]] = {}
        for k, v in (json.get('parameterMap') or {}).items():
            inner: Dict[str, ParameterReference] = {}
            for _, iv in (v or {}).items():
                pr = ParameterReference.from_value(iv)
                inner[pr.get_key()] = pr
            param_map[k] = inner
        stmt.set_parameter_map(param_map)

        stmt.set_dependent_statements(
            {k: bool(v) for k, v in (json.get('dependentStatements') or {}).items()}
        )
        stmt.set_execute_if_true(
            {k: bool(v) for k, v in (json.get('executeIftrue') or {}).items()}
        )

        pos = Position.from_value(json.get('position'))
        if pos:
            stmt.set_position(pos)

        if json.get('comment'):
            stmt.set_comment(json['comment'])
        if json.get('description'):
            stmt.set_description(json['description'])
        stmt.set_override(json.get('override', False))

        return stmt
