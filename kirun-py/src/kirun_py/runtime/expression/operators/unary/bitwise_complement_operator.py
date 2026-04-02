from __future__ import annotations

from typing import Any

from kirun_py.exception.execution_exception import ExecutionException
from kirun_py.util.primitive.primitive_util import PrimitiveUtil
from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.runtime.expression.operators.unary.unary_operator import UnaryOperator


class BitwiseComplementOperator(UnaryOperator):

    def apply(self, t: Any) -> Any:
        from kirun_py.runtime.expression.operation import Operation
        from kirun_py.json.schema.type.schema_type import SchemaType

        self.null_check(t, Operation.UNARY_BITWISE_COMPLEMENT)

        t_type = PrimitiveUtil.find_primitive_number_type(t)

        if t_type.get_t1() != SchemaType.INTEGER and t_type.get_t1() != SchemaType.LONG:
            raise ExecutionException(
                StringFormatter.format(
                    'Unable to apply bitwise operator on $', t
                )
            )

        return ~t
