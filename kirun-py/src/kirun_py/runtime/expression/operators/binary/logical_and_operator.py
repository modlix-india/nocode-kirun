from __future__ import annotations

from typing import Any

from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


class LogicalAndOperator(BinaryOperator):

    def apply(self, t: Any, u: Any) -> Any:
        return bool(t) and t != '' and bool(u) and u != ''
