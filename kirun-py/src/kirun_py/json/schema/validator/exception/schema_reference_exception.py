from __future__ import annotations
from typing import Optional


class SchemaReferenceException(Exception):
    def __init__(self, schema_path: str, message: str = '', cause: Optional[Exception] = None):
        super().__init__(message or f'Schema reference resolution failed at {schema_path}')
        self._schema_path = schema_path
        self._cause = cause

    def get_schema_path(self) -> str:
        return self._schema_path

    def get_cause(self) -> Optional[Exception]:
        return self._cause
