from __future__ import annotations
from typing import Any

from kirun_py.exception import ExecutionException
from kirun_py.util.null_check import is_null_value
from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.util.tuples import Tuple2


class PrimitiveUtil:

    @staticmethod
    def find_primitive_null_as_boolean(element: Any) -> Tuple2:
        from kirun_py.json.schema.type.schema_type import SchemaType

        if is_null_value(element):
            return Tuple2(SchemaType.BOOLEAN, False)

        if isinstance(element, dict) or isinstance(element, list):
            raise ExecutionException(
                StringFormatter.format('$ is not a primitive type', element)
            )

        if isinstance(element, bool):
            return Tuple2(SchemaType.BOOLEAN, element)

        if isinstance(element, str):
            return Tuple2(SchemaType.STRING, element)

        return PrimitiveUtil.find_primitive_number_type(element)

    @staticmethod
    def find_primitive(element: Any) -> Tuple2:
        from kirun_py.json.schema.type.schema_type import SchemaType

        if is_null_value(element):
            return Tuple2(SchemaType.NULL, None)

        if isinstance(element, dict) or isinstance(element, list):
            raise ExecutionException(
                StringFormatter.format('$ is not a primitive type', element)
            )

        if isinstance(element, bool):
            return Tuple2(SchemaType.BOOLEAN, element)

        if isinstance(element, str):
            return Tuple2(SchemaType.STRING, element)

        return PrimitiveUtil.find_primitive_number_type(element)

    @staticmethod
    def find_primitive_number_type(element: Any) -> Tuple2:
        from kirun_py.json.schema.type.schema_type import SchemaType

        if is_null_value(element) or isinstance(element, list) or isinstance(element, dict):
            raise ExecutionException(
                StringFormatter.format('Unable to convert $ to a number.', element)
            )

        try:
            num = element
            if isinstance(num, bool):
                raise ExecutionException(
                    StringFormatter.format('Unable to convert $ to a number.', element)
                )
            if isinstance(num, int):
                return Tuple2(SchemaType.LONG, num)
            if isinstance(num, float):
                if num == int(num) and not (num != num):  # not NaN
                    return Tuple2(SchemaType.LONG, int(num))
                return Tuple2(SchemaType.DOUBLE, num)
            raise ExecutionException(
                StringFormatter.format('Unable to convert $ to a number.', element)
            )
        except ExecutionException:
            raise
        except Exception as err:
            raise ExecutionException(
                StringFormatter.format('Unable to convert $ to a number.', element),
                err,
            )

    @staticmethod
    def compare(a: Any, b: Any) -> int:
        if a is b or a == b:
            return 0

        if is_null_value(a) or is_null_value(b):
            return -1 if is_null_value(a) else 1

        if isinstance(a, list) or isinstance(b, list):
            if isinstance(a, list) and isinstance(b, list):
                if len(a) != len(b):
                    return len(a) - len(b)
                for i in range(len(a)):
                    cmp = PrimitiveUtil.compare(a[i], b[i])
                    if cmp != 0:
                        return cmp
                return 0
            return -1 if isinstance(a, list) else 1

        if isinstance(a, dict) or isinstance(b, dict):
            if isinstance(a, dict) and isinstance(b, dict):
                if len(a) != len(b):
                    return len(a) - len(b)
                for key in a:
                    if key not in b:
                        return 1
                    cmp = PrimitiveUtil.compare(a.get(key), b.get(key))
                    if cmp != 0:
                        return cmp
                return 0
            return -1 if isinstance(a, dict) else 1

        return PrimitiveUtil.compare_primitive(a, b)

    @staticmethod
    def compare_primitive(oa: Any, ob: Any) -> int:
        if is_null_value(oa) or is_null_value(ob):
            if is_null_value(oa) and is_null_value(ob):
                return 0
            return -1 if is_null_value(oa) else 1

        if oa == ob:
            return 0

        if isinstance(oa, bool) or isinstance(ob, bool):
            return -1 if oa else 1

        if isinstance(oa, str) or isinstance(ob, str):
            return -1 if str(oa) < str(ob) else 1

        if isinstance(oa, (int, float)) or isinstance(ob, (int, float)):
            return oa - ob

        return 0

    @staticmethod
    def to_primitive_type(e: Any) -> int | float:
        return e
