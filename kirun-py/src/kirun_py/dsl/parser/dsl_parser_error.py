from __future__ import annotations

from typing import List, Optional

from kirun_py.dsl.lexer.dsl_token import DSLToken, SourceLocation


class DSLParserError(Exception):
    """Parser error with position information and expected tokens."""

    def __init__(
        self,
        message: str,
        location: Optional[SourceLocation] = None,
        expected_tokens: Optional[List[str]] = None,
        actual_token: Optional[DSLToken] = None,
    ) -> None:
        self.location = location
        self.expected_tokens = expected_tokens
        self.actual_token = actual_token

        formatted = message
        if location:
            formatted = f"{message} at {location}"

        if expected_tokens and len(expected_tokens) > 0:
            formatted += f"\nExpected: {', '.join(expected_tokens)}"

        if actual_token:
            formatted += f"\nActual: {actual_token.type.value} ({actual_token.value})"

        super().__init__(formatted)
