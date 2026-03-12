from __future__ import annotations
from typing import Any, Optional


class NullConvertor:

    @staticmethod
    def convert(element: Any) -> None:
        """Converts a value to None.

        Accepts None or the string 'null' (case-insensitive).
        Returns None for valid inputs, raises ValueError otherwise.
        """
        if element is None:
            return None

        if isinstance(element, str) and element.lower().strip() == 'null':
            return None

        raise ValueError(
            f'Unable to convert {element!r} to null'
        )
