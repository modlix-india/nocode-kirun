from __future__ import annotations

from typing import Any

from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.runtime.expression.expression_token import ExpressionToken


class ExpressionTokenValue(ExpressionToken):

    def __init__(self, expression: str, element: Any):
        super().__init__(expression)
        self._element = element

    def get_token_value(self) -> Any:
        return self._element

    def get_element(self) -> Any:
        return self._element

    def __str__(self) -> str:
        return StringFormatter.format('$: $', self.expression, self._element)
