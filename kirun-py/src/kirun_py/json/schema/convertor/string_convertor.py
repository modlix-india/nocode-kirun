from __future__ import annotations
from typing import Any, Optional


class StringConvertor:

    @staticmethod
    def convert(element: Any) -> Optional[str]:
        """Converts a value to string.

        Accepts any non-None, non-dict, non-list value and calls str().
        Returns None for None, dict, or list values.
        """
        if element is None:
            return None

        if isinstance(element, (dict, list)):
            return None

        return str(element)
