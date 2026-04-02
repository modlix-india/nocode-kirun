from __future__ import annotations
from typing import Any, Optional

_BOOLEAN_MAP = {
    'true': True,
    't': True,
    'yes': True,
    'y': True,
    '1': True,
    'false': False,
    'f': False,
    'no': False,
    'n': False,
    '0': False,
}


class BooleanConvertor:

    @staticmethod
    def convert(element: Any) -> Optional[bool]:
        """Converts a value to bool.

        Maps string values: 'true'/'t'/'yes'/'y'/'1' -> True,
        'false'/'f'/'no'/'n'/'0' -> False.
        Passes through bool values directly.
        Numbers: 0 -> False, 1 -> True.
        Returns None for unconvertible values.
        """
        if element is None:
            return None

        if isinstance(element, bool):
            return element

        if isinstance(element, str):
            trimmed = element.lower().strip()
            return _BOOLEAN_MAP.get(trimmed)

        if isinstance(element, (int, float)):
            if element == 0:
                return False
            if element == 1:
                return True
            return None

        return None
