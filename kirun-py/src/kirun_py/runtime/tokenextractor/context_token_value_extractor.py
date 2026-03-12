from __future__ import annotations

from typing import Any, Dict, List, Optional

from kirun_py.runtime.context_element import ContextElement
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor
from kirun_py.util.null_check import is_null_value


class ContextTokenValueExtractor(TokenValueExtractor):

    PREFIX: str = 'Context.'

    def __init__(self, context: Dict[str, ContextElement]) -> None:
        super().__init__()
        self._context: Dict[str, ContextElement] = context

    def get_value_internal(self, token: str) -> Any:
        parts: List[str] = TokenValueExtractor.split_path(token)

        key: str = parts[1]
        b_index: int = key.find('[')
        from_index = 2

        if b_index != -1:
            key = parts[1][:b_index]
            parts = list(parts)  # Copy since we're modifying
            parts[1] = parts[1][b_index:]
            from_index = 1

        context_element: Optional[ContextElement] = self._context.get(key)
        element = context_element.get_element() if context_element is not None else None

        return self.retrieve_element_from(token, parts, from_index, element)

    def get_prefix(self) -> str:
        return ContextTokenValueExtractor.PREFIX

    def get_store(self) -> Any:
        if is_null_value(self._context):
            return self._context

        result: Dict[str, Any] = {}
        for key, value in self._context.items():
            if is_null_value(value):
                continue
            result[key] = value.get_element()
        return result
