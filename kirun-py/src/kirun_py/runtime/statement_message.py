from __future__ import annotations

from typing import Optional

from kirun_py.runtime.statement_message_type import StatementMessageType


class StatementMessage:

    def __init__(
        self,
        message_type: Optional[StatementMessageType] = None,
        message: Optional[str] = None,
    ) -> None:
        self._message_type: Optional[StatementMessageType] = message_type
        self._message: Optional[str] = message

    def get_message_type(self) -> Optional[StatementMessageType]:
        return self._message_type

    def set_message_type(self, message_type: StatementMessageType) -> StatementMessage:
        self._message_type = message_type
        return self

    def get_message(self) -> Optional[str]:
        return self._message

    def set_message(self, message: str) -> StatementMessage:
        self._message = message
        return self

    def __str__(self) -> str:
        return f'{self._message_type} : {self._message}'
