from __future__ import annotations

KEYWORDS: set = {
    # Function structure keywords
    'FUNCTION',
    'NAMESPACE',
    'PARAMETERS',
    'EVENTS',
    'LOGIC',

    # Type and schema keywords
    'AS',
    'OF',
    'WITH',
    'DEFAULT',
    'VALUE',

    # Control flow modifiers
    'AFTER',
    'IF',

    # Primitive types
    'INTEGER',
    'LONG',
    'FLOAT',
    'DOUBLE',
    'STRING',
    'BOOLEAN',
    'NULL',
    'ANY',
    'ARRAY',
    'OBJECT',

    # Boolean literals (also keywords)
    'true',
    'false',
    'null',
}

BLOCK_NAMES: set = {
    'iteration',
    'true',
    'false',
    'output',
    'error',
}

PRIMITIVE_TYPES: set = {
    'INTEGER',
    'LONG',
    'FLOAT',
    'DOUBLE',
    'STRING',
    'BOOLEAN',
    'NULL',
    'ANY',
}


def is_keyword(s: str) -> bool:
    """Check if a string is a keyword."""
    return s in KEYWORDS


def is_block_name(s: str) -> bool:
    """Check if a string is a block name."""
    return s in BLOCK_NAMES


def is_primitive_type(s: str) -> bool:
    """Check if a string is a primitive type."""
    return s in PRIMITIVE_TYPES
