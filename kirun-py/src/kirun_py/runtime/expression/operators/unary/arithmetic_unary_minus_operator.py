from __future__ import annotations

from typing import Any

from kirun_py.util.primitive.primitive_util import PrimitiveUtil
from kirun_py.runtime.expression.operators.unary.unary_operator import UnaryOperator


class ArithmeticUnaryMinusOperator(UnaryOperator):

    def apply(self, t: Any) -> Any:
        from kirun_py.runtime.expression.operation import Operation
        self.null_check(t, Operation.UNARY_MINUS)
        PrimitiveUtil.find_primitive_number_type(t)
        return -t
