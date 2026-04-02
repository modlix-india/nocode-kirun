from __future__ import annotations
from typing import Any, Optional, Union


class NumberConvertor:

    @staticmethod
    def convert(element: Any) -> Optional[Union[int, float]]:
        """Converts a value to a number.

        Strings are converted via int()/float().
        Bools, dicts, and lists are rejected (returns None).
        Returns None for None or unconvertible values.
        """
        if element is None:
            return None

        if isinstance(element, bool):
            return None

        if isinstance(element, (dict, list)):
            return None

        if isinstance(element, int):
            return element

        if isinstance(element, float):
            return element

        if isinstance(element, str):
            try:
                return int(element)
            except ValueError:
                pass
            try:
                return float(element)
            except ValueError:
                return None

        return None
