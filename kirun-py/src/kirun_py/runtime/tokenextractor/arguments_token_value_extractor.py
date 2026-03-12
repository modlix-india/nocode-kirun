from __future__ import annotations

from typing import Any, Dict, List

from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor
from kirun_py.util.null_check import is_null_value


class ArgumentsTokenValueExtractor(TokenValueExtractor):

    PREFIX: str = 'Arguments.'

    def __init__(self, args: Dict[str, Any]) -> None:
        super().__init__()
        self._args: Dict[str, Any] = args

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

        return self.retrieve_element_from(token, parts, from_index, self._args.get(key))

    def get_prefix(self) -> str:
        return ArgumentsTokenValueExtractor.PREFIX

    def get_store(self) -> Any:
        if is_null_value(self._args):
            return self._args

        result: Dict[str, Any] = {}
        for key, value in self._args.items():
            result[key] = value
        return result
