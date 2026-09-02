from __future__ import annotations

from typing import Union


def parse_number(text: str) -> Union[int, float]:
    """A DSL numeric literal as the JSON number type it was written as.

    The DSL is generated from stored JSON and compiled straight back into it, so
    a round trip has to preserve int against float. Python distinguishes the two
    where JavaScript does not: `float('1')` is `1.0` and `json.dumps(1.0)` is
    `"1.0"`, so reading every literal as a float silently turned every integer in
    a definition -- every `order`, every page size -- into a double on the way
    through the Python DSL, while the TypeScript port left them alone because JS
    has a single number type. Mongo then stores a double where an int was, which
    is exactly the int/double noise that makes environment definition diffs
    unreadable.

    A literal written with a fraction or an exponent is a float and stays one.
    Integers are read with `int`, not via `float`, so a value beyond float's
    53-bit exact range keeps every digit.

    Raises ValueError on text that is not a number, as `float` did -- callers
    rely on that to fall through to their other literal branches.
    """
    try:
        return int(text)
    except (TypeError, ValueError):
        return float(text)
