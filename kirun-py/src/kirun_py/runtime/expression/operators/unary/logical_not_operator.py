from __future__ import annotations

from typing import Any

from kirun_py.runtime.expression.operators.unary.unary_operator import UnaryOperator


class LogicalNotOperator(UnaryOperator):

    def apply(self, t: Any) -> Any:
        # JS truthiness: arrays and objects are always truthy (even empty ones)
        if isinstance(t, (list, dict)):
            return False
        return not t and t != ''
