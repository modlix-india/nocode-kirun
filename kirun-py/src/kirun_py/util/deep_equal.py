from typing import Any
from collections import deque


def deep_equal(x: Any, y: Any) -> bool:
    xa: deque = deque()
    xa.append(x)
    yb: deque = deque()
    yb.append(y)

    while xa and yb:
        a = xa.popleft()
        b = yb.popleft()

        if a is b:
            continue

        if a is None or b is None:
            if not a and not b:
                continue
            return False

        type_a = type(a)
        type_b = type(b)

        # JS-like type discrimination: different types are never equal
        # (e.g. 0 !== false, 1 !== true in JS strict equality)
        if type_a is not type_b:
            # Allow int/float cross-comparison (both are 'number' in JS)
            if not ({type_a, type_b} <= {int, float}):
                return False

        if isinstance(a, list):
            if not isinstance(b, list) or len(a) != len(b):
                return False
            for i in range(len(a)):
                xa.append(a[i])
                yb.append(b[i])
            continue

        if isinstance(a, dict):
            if not isinstance(b, dict):
                return False
            entries_a = list(a.items())
            entries_b = list(b.items())
            if len(entries_a) != len(entries_b):
                return False
            for k, v in entries_a:
                xa.append(v)
                yb.append(b.get(k))
            continue

        if a != b:
            return False

    return True
