from __future__ import annotations

from typing import Any

from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


class ArrayRangeOperator(BinaryOperator):

    def apply(self, t: Any, u: Any) -> Any:
        left = t if t is not None else ''
        right = u if u is not None else ''
        return f'{left}..{right}'
