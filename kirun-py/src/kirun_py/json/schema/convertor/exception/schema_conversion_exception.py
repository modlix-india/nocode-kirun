from __future__ import annotations
from typing import Optional, Any, TYPE_CHECKING

if TYPE_CHECKING:
    from kirun_py.json.schema.convertor.enums.conversion_mode import ConversionMode


class SchemaConversionException(Exception):
    def __init__(
        self,
        schema_path: str,
        source: Any = None,
        mode: Optional[ConversionMode] = None,
        cause: Optional[Exception] = None,
        message: str = '',
    ):
        super().__init__(
            message or f'Schema conversion failed at {schema_path}'
        )
        self._schema_path = schema_path
        self._source = source
        self._mode = mode
        self._cause = cause

    def get_schema_path(self) -> str:
        return self._schema_path

    def get_source(self) -> Any:
        return self._source

    def get_mode(self) -> Optional[ConversionMode]:
        return self._mode

    def get_cause(self) -> Optional[Exception]:
        return self._cause
