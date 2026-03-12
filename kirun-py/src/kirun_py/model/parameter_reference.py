from __future__ import annotations
import copy
from typing import Any, Optional, Tuple

from kirun_py.model.parameter_reference_type import ParameterReferenceType
from kirun_py.util.null_check import is_null_value
from kirun_py.util.uuid_ import uuid


class ParameterReference:
    def __init__(self, type_or_ref):
        if isinstance(type_or_ref, ParameterReference):
            pv = type_or_ref
            self._key = pv._key
            self._type = pv._type
            self._value = copy.deepcopy(pv._value) if not is_null_value(pv._value) else None
            self._expression = pv._expression
            self._order = pv._order
        else:
            self._type: ParameterReferenceType = type_or_ref
            self._key: str = uuid()
            self._value: Any = None
            self._expression: Optional[str] = None
            self._order: Optional[int] = None

    def get_type(self) -> ParameterReferenceType:
        return self._type

    def set_type(self, type_: ParameterReferenceType) -> ParameterReference:
        self._type = type_
        return self

    def get_key(self) -> str:
        return self._key

    def set_key(self, key: str) -> ParameterReference:
        self._key = key
        return self

    def get_value(self) -> Any:
        return self._value

    def set_value(self, value: Any) -> ParameterReference:
        self._value = value
        return self

    def get_expression(self) -> Optional[str]:
        return self._expression

    def set_expression(self, expression: str) -> ParameterReference:
        self._expression = expression
        return self

    def set_order(self, order: int) -> ParameterReference:
        self._order = order
        return self

    def get_order(self) -> Optional[int]:
        return self._order

    @staticmethod
    def of_expression(value: str) -> Tuple[str, ParameterReference]:
        param = ParameterReference(ParameterReferenceType.EXPRESSION).set_expression(value)
        return (param.get_key(), param)

    @staticmethod
    def of_value(value: Any) -> Tuple[str, ParameterReference]:
        param = ParameterReference(ParameterReferenceType.VALUE).set_value(value)
        return (param.get_key(), param)

    @staticmethod
    def from_value(e: dict) -> ParameterReference:
        return (
            ParameterReference(ParameterReferenceType(e.get('type', 'VALUE')))
            .set_value(e.get('value'))
            .set_expression(e.get('expression', ''))
            .set_key(e.get('key', uuid()))
            .set_order(e.get('order', 0))
        )

    def to_json(self) -> dict:
        return {
            'key': self._key,
            'type': self._type.value if self._type else None,
            'value': self._value,
            'expression': self._expression,
            'order': self._order,
        }
