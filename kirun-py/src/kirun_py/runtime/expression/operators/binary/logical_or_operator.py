from __future__ import annotations

from typing import Any

from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


class LogicalOrOperator(BinaryOperator):

    def apply(self, t: Any, u: Any) -> Any:
        return (bool(t) and t != '') or (bool(u) and u != '')
