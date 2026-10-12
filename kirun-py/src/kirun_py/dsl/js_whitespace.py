from __future__ import annotations

# JavaScript's whitespace (`\s`, `trim`), which differs from str.strip's: Python
# also strips \x1c-\x1f and \x85, JS also strips ﻿. The DSL ports must cut
# the same characters from expressions, or the same text compiles differently.
JS_WHITESPACE = (
    '\t\n\v\f\r          '
    '        　﻿'
)


def js_lstrip(text: str) -> str:
    """`String.prototype.trimStart`."""
    return text.lstrip(JS_WHITESPACE)


def js_rstrip(text: str) -> str:
    """`String.prototype.trimEnd`."""
    return text.rstrip(JS_WHITESPACE)


def js_strip(text: str) -> str:
    """`String.prototype.trim`."""
    return text.strip(JS_WHITESPACE)
