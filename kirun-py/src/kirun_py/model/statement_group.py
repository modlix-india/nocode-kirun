from __future__ import annotations
from typing import Dict, Optional

from kirun_py.model.abstract_statement import AbstractStatement
from kirun_py.model.position import Position


class StatementGroup(AbstractStatement):
    SCHEMA_NAME: str = 'StatementGroup'

    def __init__(
        self,
        statement_group_name: str,
        statements: Optional[Dict[str, bool]] = None,
    ):
        super().__init__()
        self._statement_group_name = statement_group_name
        self._statements: Dict[str, bool] = statements if statements is not None else {}

    def get_statement_group_name(self) -> str:
        return self._statement_group_name

    def set_statement_group_name(self, name: str) -> StatementGroup:
        self._statement_group_name = name
        return self

    def get_statements(self) -> Dict[str, bool]:
        return self._statements

    def set_statements(self, statements: Dict[str, bool]) -> StatementGroup:
        self._statements = statements
        return self

    @staticmethod
    def from_value(json: dict) -> StatementGroup:
        sg = StatementGroup(
            json.get('statementGroupName', ''),
            {
                k: str(v).lower() == 'true'
                for k, v in (json.get('statements') or {}).items()
            },
        )
        pos = Position.from_value(json.get('position'))
        if pos:
            sg.set_position(pos)
        if json.get('comment'):
            sg.set_comment(json['comment'])
        if json.get('description'):
            sg.set_description(json['description'])
        sg.set_override(json.get('override', False))
        return sg

    def to_json(self) -> dict:
        return {
            'statementGroupName': self._statement_group_name,
            'statements': dict(self._statements),
            'position': self.get_position().to_json() if self.get_position() else None,
            'comment': self.get_comment(),
            'description': self.get_description(),
            'override': self.is_override(),
        }
