from __future__ import annotations

from typing import Optional

from kirun_py.dsl.lexer.dsl_token import SourceLocation


class LexerError(Exception):
    """Lexer error with position information."""

    def __init__(
        self,
        message: str,
        location: Optional[SourceLocation] = None,
        context: Optional[str] = None,
    ) -> None:
        self.location = location
        self.context = context

        formatted = message
        if location:
            formatted = f"{message} at {location}"
            if context:
                formatted += f"\n\n{self._format_context(context, location)}"

        super().__init__(formatted)

    @staticmethod
    def _format_context(input_text: str, location: SourceLocation) -> str:
        lines = input_text.split('\n')
        line_index = location.line - 1

        if line_index < 0 or line_index >= len(lines):
            return ''

        line = lines[line_index]
        pointer = ' ' * (location.column - 1) + '^'

        return f"{line}\n{pointer}"
