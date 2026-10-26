from __future__ import annotations

import math
from typing import Any

from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


def _is_whole(v: Any) -> bool:
    return isinstance(v, int) and not isinstance(v, bool)


def _is_number(v: Any) -> bool:
    return isinstance(v, (int, float)) and not isinstance(v, bool)


class ArithmeticIntegerDivisionOperator(BinaryOperator):
    """JS `Math.floor(t / u)`: 7 // 2 is 3, -7 // 2 is -4, 7.5 // 2 is 3 (an int, not 3.0)."""

    def apply(self, t: Any, u: Any) -> Any:
        from kirun_py.runtime.expression.operation import Operation
        self.null_check(t, u, Operation.INTEGER_DIVISION)
        if _is_whole(t) and _is_whole(u):
            return t // u
        if _is_number(t) and _is_number(u):
            q = t / u
            return math.floor(q) if math.isfinite(q) else q
        return t // u
