from __future__ import annotations

from typing import Any

from kirun_py.runtime.expression.operators.ternary.ternary_operator import TernaryOperator


class ConditionalTernaryOperator(TernaryOperator):

    def apply(self, t: Any, u: Any, v: Any) -> Any:
        return u if t else v
