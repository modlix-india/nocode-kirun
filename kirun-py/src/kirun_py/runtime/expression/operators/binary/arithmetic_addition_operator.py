from __future__ import annotations

from typing import Any

from kirun_py.util.null_check import is_null_value
from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


def _to_js_string(v: Any) -> str:
    if isinstance(v, bool):
        return 'true' if v else 'false'
    if v is None:
        return 'null'
    return str(v)


class ArithmeticAdditionOperator(BinaryOperator):

    def apply(self, t: Any, u: Any) -> Any:
        if is_null_value(t):
            return u
        if is_null_value(u):
            return t
        # JS-like string coercion: if either operand is a string, concatenate
        if isinstance(t, str) or isinstance(u, str):
            return _to_js_string(t) + _to_js_string(u)
        return t + u
