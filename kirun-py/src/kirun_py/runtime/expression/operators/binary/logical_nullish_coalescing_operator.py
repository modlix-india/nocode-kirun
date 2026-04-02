from __future__ import annotations

from typing import Any

from kirun_py.util.null_check import is_null_value
from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


class LogicalNullishCoalescingOperator(BinaryOperator):

    def apply(self, t: Any, u: Any) -> Any:
        return u if is_null_value(t) else t
