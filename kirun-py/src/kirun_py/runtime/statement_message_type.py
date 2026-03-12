from __future__ import annotations

from enum import Enum


class StatementMessageType(str, Enum):
    ERROR = 'ERROR'
    WARNING = 'WARNING'
    INFO = 'INFO'
