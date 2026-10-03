from __future__ import annotations

from enum import Enum
from typing import Any


class ParameterReferenceType(str, Enum):
    VALUE = 'VALUE'
    EXPRESSION = 'EXPRESSION'

    @classmethod
    def of(cls, value: Any) -> ParameterReferenceType:
        """The type of a stored ParameterReference, whatever shape it is in.

        `ParameterReference.SCHEMA` declares `type` as a string with enums
        EXPRESSION | VALUE, but a large amount of stored data carries it as a
        single-element ARRAY, `["EXPRESSION"]` -- that is what modlix-mcp and the
        appbuilder generator tools have always written. The JS runtime never
        minded, because it compares with `==` and `['EXPRESSION'] ==
        'EXPRESSION'` is true in JS.

        Python is stricter in both directions: the comparison is False, and
        `ParameterReferenceType(['EXPRESSION'])` raises `ValueError: ...is not a
        valid ParameterReferenceType`, so loading any such definition failed
        outright rather than running like it does everywhere else.

        An unrecognised value falls back to VALUE, which is what
        `from_value` already assumed for a missing `type`.
        """
        if isinstance(value, (list, tuple)):
            value = value[0] if value else None
        if isinstance(value, cls):
            return value
        try:
            return cls(value)
        except ValueError:
            return cls.VALUE
