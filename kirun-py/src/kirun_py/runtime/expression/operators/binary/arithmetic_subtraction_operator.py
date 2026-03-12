from __future__ import annotations

from typing import Any

from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


class ArithmeticSubtractionOperator(BinaryOperator):

    def apply(self, t: Any, u: Any) -> Any:
        from kirun_py.runtime.expression.operation import Operation
        self.null_check(t, u, Operation.SUBTRACTION)
        return t - u
