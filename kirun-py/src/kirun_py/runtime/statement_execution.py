from __future__ import annotations

from typing import Any, List, Set

from kirun_py.model.statement import Statement
from kirun_py.runtime.statement_message import StatementMessage
from kirun_py.runtime.statement_message_type import StatementMessageType


class StatementExecution:

    def __init__(self, statement: Statement) -> None:
        self._statement: Statement = statement
        self._messages: List[StatementMessage] = []
        self._dependencies: Set[str] = set()

    def get_statement(self) -> Statement:
        return self._statement

    def set_statement(self, statement: Statement) -> StatementExecution:
        self._statement = statement
        return self

    def get_messages(self) -> List[StatementMessage]:
        return self._messages

    def set_messages(self, messages: List[StatementMessage]) -> StatementExecution:
        self._messages = messages
        return self

    def get_dependencies(self) -> Set[str]:
        return self._dependencies

    def set_dependencies(self, dependencies: Set[str]) -> StatementExecution:
        self._dependencies = dependencies
        return self

    def get_unique_key(self) -> str:
        return self._statement.get_statement_name()

    def add_message(self, type_: StatementMessageType, message: str) -> None:
        self._messages.append(StatementMessage(type_, message))

    def add_dependency(self, dependency: str) -> None:
        self._dependencies.add(dependency)

    def get_depenedencies(self) -> Set[str]:
        return self._dependencies

    def __eq__(self, other: Any) -> bool:
        if not isinstance(other, StatementExecution):
            return False
        return other._statement == self._statement

    def __hash__(self) -> int:
        return hash(self._statement)
