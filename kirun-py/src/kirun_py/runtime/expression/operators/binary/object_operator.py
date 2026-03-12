from __future__ import annotations

from typing import Any

from kirun_py.exception.execution_exception import ExecutionException
from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


class ObjectOperator(BinaryOperator):

    def apply(self, t: Any, u: Any) -> Any:
        if t is None:
            raise ExecutionException('Cannot apply array operator on a null value')

        if u is None:
            raise ExecutionException('Cannot retrive null property value')

        if not isinstance(t, (list, str, dict)):
            raise ExecutionException(
                StringFormatter.format(
                    'Cannot retrieve value from a primitive value $', t
                )
            )

        return t[u]
