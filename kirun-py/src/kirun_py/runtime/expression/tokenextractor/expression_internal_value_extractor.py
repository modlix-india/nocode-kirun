from __future__ import annotations

from typing import Any, Dict, List

from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor


class ExpressionInternalValueExtractor(TokenValueExtractor):

    PREFIX: str = '_internal.'

    def __init__(self) -> None:
        super().__init__()
        self._values: Dict[str, Any] = {}

    def add_value(self, key: str, value: Any) -> None:
        self._values[key] = value

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

        return self.retrieve_element_from(token, parts, from_index, self._values.get(key))

    def get_prefix(self) -> str:
        return ExpressionInternalValueExtractor.PREFIX

    def get_store(self) -> Any:
        return None
