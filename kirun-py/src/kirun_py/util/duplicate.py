import copy
from typing import Any


def duplicate(obj: Any) -> Any:
    if obj is None:
        return obj
    return copy.deepcopy(obj)
