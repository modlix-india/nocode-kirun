from __future__ import annotations

from typing import Any

from kirun_py.exception.execution_exception import ExecutionException
from kirun_py.util.null_check import is_null_value
from kirun_py.util.primitive.primitive_util import PrimitiveUtil
from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator


class LogicalLessThanEqualOperator(BinaryOperator):

    def apply(self, t: Any, u: Any) -> Any:
        from kirun_py.json.schema.type.schema_type import SchemaType

        if is_null_value(t) or is_null_value(u):
            return None

        t_type = PrimitiveUtil.find_primitive_null_as_boolean(t)
        u_type = PrimitiveUtil.find_primitive_null_as_boolean(u)

        if t_type.get_t1() == SchemaType.BOOLEAN or u_type.get_t1() == SchemaType.BOOLEAN:
            raise ExecutionException(
                StringFormatter.format(
                    'Cannot compare <= with the values $ and $',
                    t_type.get_t2(),
                    u_type.get_t2(),
                )
            )

        return t_type.get_t2() <= u_type.get_t2()
