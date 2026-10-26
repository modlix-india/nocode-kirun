from __future__ import annotations

import math
from typing import Any

from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


def _is_whole(v: Any) -> bool:
    return isinstance(v, int) and not isinstance(v, bool)


def _is_number(v: Any) -> bool:
    return isinstance(v, (int, float)) and not isinstance(v, bool)


class ArithmeticDivisionOperator(BinaryOperator):
    """JS `/`: true division. An exact whole division stays an int (6 / 3 is 2, not 2.0)."""

    def apply(self, t: Any, u: Any) -> Any:
        from kirun_py.runtime.expression.operation import Operation
        self.null_check(t, u, Operation.DIVISION)
        if _is_whole(t) and _is_whole(u) and u != 0 and t % u == 0:
            return t // u
        return t / u
