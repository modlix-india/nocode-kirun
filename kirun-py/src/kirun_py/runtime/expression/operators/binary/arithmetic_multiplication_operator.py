from __future__ import annotations

import math
from typing import Any

from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


class ArithmeticMultiplicationOperator(BinaryOperator):

    def apply(self, t: Any, u: Any) -> Any:
        from kirun_py.runtime.expression.operation import Operation
        self.null_check(t, u, Operation.MULTIPLICATION)

        is_t_string = isinstance(t, str)
        is_u_string = isinstance(u, str)

        if is_t_string or is_u_string:
            s: str = t if is_t_string else u
            num: float = float(u if is_t_string else t)

            reverse = num < 0
            num = abs(num)

            times = math.floor(num)
            sb = s * times

            frac = num - math.floor(num)
            chrs = math.floor(len(s) * frac)
            if chrs < 0:
                chrs = -chrs

            if chrs != 0:
                sb += s[:chrs]

            if reverse:
                return sb[::-1]

            return sb

        return t * u
