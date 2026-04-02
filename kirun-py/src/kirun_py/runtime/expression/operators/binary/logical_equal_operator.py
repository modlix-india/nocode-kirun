from __future__ import annotations

from typing import Any

from kirun_py.util.deep_equal import deep_equal
from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


class LogicalEqualOperator(BinaryOperator):

    def apply(self, t: Any, u: Any) -> Any:
        return deep_equal(t, u)
