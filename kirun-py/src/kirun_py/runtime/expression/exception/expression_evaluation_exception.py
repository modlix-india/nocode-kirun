from __future__ import annotations

from typing import Optional

from kirun_py.util.string.string_formatter import StringFormatter


class ExpressionEvaluationException(Exception):

    def __init__(self, expression: str, message: str, cause: Optional[Exception] = None):
        super().__init__(StringFormatter.format('$ : $', expression, message))
        self._cause = cause

    def get_cause(self) -> Optional[Exception]:
        return self._cause
