from __future__ import annotations

import json
from enum import Enum
from typing import Optional


class DSLTokenType(Enum):
    """Token types for DSL lexer."""

    # Keywords
    KEYWORD = 'KEYWORD'

    # Identifiers and literals
    IDENTIFIER = 'IDENTIFIER'
    NUMBER = 'NUMBER'
    STRING = 'STRING'
    BACKTICK_STRING = 'BACKTICK_STRING'
    BOOLEAN = 'BOOLEAN'
    NULL = 'NULL'

    # Operators and delimiters
    COLON = 'COLON'
    COMMA = 'COMMA'
    DOT = 'DOT'
    EQUALS = 'EQUALS'
    OPERATOR = 'OPERATOR'

    # Brackets and parentheses
    LEFT_PAREN = 'LEFT_PAREN'
    RIGHT_PAREN = 'RIGHT_PAREN'
    LEFT_BRACE = 'LEFT_BRACE'
    RIGHT_BRACE = 'RIGHT_BRACE'
    LEFT_BRACKET = 'LEFT_BRACKET'
    RIGHT_BRACKET = 'RIGHT_BRACKET'

    # Special
    NEWLINE = 'NEWLINE'
    COMMENT = 'COMMENT'
    WHITESPACE = 'WHITESPACE'
    EOF = 'EOF'


class SourceLocation:
    """Position information for tokens."""

    def __init__(
        self,
        line: int,
        column: int,
        start_pos: int,
        end_pos: int,
    ) -> None:
        self.line = line
        self.column = column
        self.start_pos = start_pos
        self.end_pos = end_pos

    def __str__(self) -> str:
        return f"Line {self.line}, Column {self.column} (pos {self.start_pos}-{self.end_pos})"

    def __repr__(self) -> str:
        return self.__str__()


class DSLToken:
    """Token class with value and position tracking."""

    def __init__(
        self,
        type: DSLTokenType,
        value: str,
        location: SourceLocation,
    ) -> None:
        self.type = type
        self.value = value
        self.location = location

    def __str__(self) -> str:
        return f"{self.type.value}({json.dumps(self.value)}) at {self.location}"

    def __repr__(self) -> str:
        return self.__str__()

    def is_token(self, type: DSLTokenType, value: Optional[str] = None) -> bool:
        if value is not None:
            return self.type == type and self.value == value
        return self.type == type
