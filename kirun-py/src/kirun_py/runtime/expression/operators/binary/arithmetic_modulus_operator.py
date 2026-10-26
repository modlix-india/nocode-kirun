from __future__ import annotations

import math
from typing import Any

from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


def _is_whole(v: Any) -> bool:
    return isinstance(v, int) and not isinstance(v, bool)


def _is_number(v: Any) -> bool:
    return isinstance(v, (int, float)) and not isinstance(v, bool)


class ArithmeticModulusOperator(BinaryOperator):
    """JS `%`: the remainder takes the dividend's sign (-7 % 3 is -1; Python's % gives 2)."""

    def apply(self, t: Any, u: Any) -> Any:
        from kirun_py.runtime.expression.operation import Operation
        self.null_check(t, u, Operation.MOD)
        if _is_whole(t) and _is_whole(u):
            r = abs(t) % abs(u)
            return -r if t < 0 else r
        if _is_number(t) and _is_number(u):
            return math.fmod(t, u)
        return t % u
