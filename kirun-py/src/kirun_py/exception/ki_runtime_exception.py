from __future__ import annotations


class KIRuntimeException(Exception):
    def __init__(self, message: str, cause: Exception | None = None):
        super().__init__(message)
        self._cause = cause

    def get_cause(self) -> Exception | None:
        return self._cause
