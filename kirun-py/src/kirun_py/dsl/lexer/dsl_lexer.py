from __future__ import annotations

from typing import List, Optional

from kirun_py.dsl.lexer.dsl_token import DSLToken, DSLTokenType, SourceLocation
from kirun_py.dsl.lexer.keywords import is_keyword
from kirun_py.dsl.lexer.lexer_error import LexerError


class DSLLexer:
    """
    DSL Lexer - Tokenizes DSL text into tokens.

    Features:
    - Keyword recognition
    - String literals with escape sequences
    - Number literals (integers and floats)
    - Comment stripping (block comments)
    - Position tracking for error messages
    """

    def __init__(self, input_text: str) -> None:
        self._input = input_text
        self._pos: int = 0
        self._line: int = 1
        self._column: int = 1
        self._tokens: List[DSLToken] = []

    def tokenize(self) -> List[DSLToken]:
        """Tokenize the input string."""
        while self._pos < len(self._input):
            self._skip_whitespace()

            if self._pos >= len(self._input):
                break

            token = self._read_token()
            if token is not None:
                if token.type != DSLTokenType.WHITESPACE:
                    self._tokens.append(token)

        # Add EOF token
        self._tokens.append(
            DSLToken(
                DSLTokenType.EOF,
                '',
                SourceLocation(self._line, self._column, self._pos, self._pos),
            )
        )

        return self._tokens

    def _skip_whitespace(self) -> None:
        """Skip whitespace (spaces, tabs, newlines)."""
        while self._pos < len(self._input):
            ch = self._input[self._pos]
            if ch in (' ', '\t', '\r'):
                self._advance()
            elif ch == '\n':
                self._advance()
                self._line += 1
                self._column = 1
            else:
                break

    def _read_token(self) -> Optional[DSLToken]:
        """Read a single token."""
        start_pos = self._pos
        start_line = self._line
        start_column = self._column

        ch = self._peek()

        # Block comments only
        if ch == '/' and self._peek_ahead(1) == '*':
            return self._read_block_comment()

        # String literals
        if ch in ('"', "'"):
            return self._read_string_literal(ch)

        # Backtick strings (for expressions)
        if ch == '`':
            return self._read_backtick_string()

        # Numbers
        if self._is_digit(ch) or (ch == '-' and self._is_digit(self._peek_ahead(1))):
            return self._read_number()

        # Identifiers and keywords
        if self._is_identifier_start(ch):
            return self._read_identifier()

        # Single-character tokens
        single_char = self._read_single_char_token()
        if single_char is not None:
            return single_char

        # Unknown character
        raise LexerError(
            f"Unexpected character '{ch}'",
            SourceLocation(start_line, start_column, start_pos, self._pos),
            self._input,
        )

    def _read_single_char_token(self) -> Optional[DSLToken]:
        """Read single-character or multi-character tokens."""
        start_pos = self._pos
        start_line = self._line
        start_column = self._column
        ch = self._peek()

        # Check for multi-character operators first
        multi_char_op = self._try_read_multi_char_operator()
        if multi_char_op is not None:
            return DSLToken(
                DSLTokenType.OPERATOR,
                multi_char_op,
                SourceLocation(start_line, start_column, start_pos, self._pos),
            )

        # Advance past single character
        self._advance()
        token_type: Optional[DSLTokenType] = None

        char_to_type = {
            ':': DSLTokenType.COLON,
            ',': DSLTokenType.COMMA,
            '.': DSLTokenType.DOT,
            '=': DSLTokenType.EQUALS,
            '(': DSLTokenType.LEFT_PAREN,
            ')': DSLTokenType.RIGHT_PAREN,
            '{': DSLTokenType.LEFT_BRACE,
            '}': DSLTokenType.RIGHT_BRACE,
            '[': DSLTokenType.LEFT_BRACKET,
            ']': DSLTokenType.RIGHT_BRACKET,
        }

        operator_chars = {'+', '-', '*', '/', '%', '<', '>', '!', '?', '&', '|', '@', '^', '~', '#', '\\'}

        if ch in char_to_type:
            token_type = char_to_type[ch]
        elif ch in operator_chars:
            token_type = DSLTokenType.OPERATOR
        else:
            return None

        return DSLToken(
            token_type,
            ch,
            SourceLocation(start_line, start_column, start_pos, self._pos),
        )

    def _try_read_multi_char_operator(self) -> Optional[str]:
        """Try to read multi-character operators like !=, ==, <=, >=, &&, ||, ??"""
        ch = self._peek()
        next_ch = self._peek_ahead(1)

        two_char_ops = [
            '!=', '==', '<=', '>=', '&&', '||', '??', '?.', '++', '--',
            '+=', '-=', '*=', '/=',
        ]
        two_char = ch + next_ch
        if two_char in two_char_ops:
            self._advance()
            self._advance()
            return two_char

        return None

    def _read_identifier(self) -> DSLToken:
        """Read identifier or keyword."""
        start_pos = self._pos
        start_line = self._line
        start_column = self._column
        value = ''

        while self._pos < len(self._input):
            ch = self._peek()
            if self._is_identifier_part(ch):
                value += self._advance()
            else:
                break

        location = SourceLocation(start_line, start_column, start_pos, self._pos)

        # Check if it's a keyword
        if is_keyword(value):
            return DSLToken(DSLTokenType.KEYWORD, value, location)

        # Check if it's a boolean literal
        if value in ('true', 'false'):
            return DSLToken(DSLTokenType.BOOLEAN, value, location)

        # Check if it's null
        if value == 'null':
            return DSLToken(DSLTokenType.NULL, value, location)

        # Otherwise, it's an identifier
        return DSLToken(DSLTokenType.IDENTIFIER, value, location)

    def _read_number(self) -> DSLToken:
        """Read number literal (integer or float)."""
        start_pos = self._pos
        start_line = self._line
        start_column = self._column
        value = ''

        # Handle negative numbers
        if self._peek() == '-':
            value += self._advance()

        # Read integer part
        while self._pos < len(self._input) and self._is_digit(self._peek()):
            value += self._advance()

        # Check for decimal point
        if self._peek() == '.' and self._is_digit(self._peek_ahead(1)):
            value += self._advance()  # consume '.'
            while self._pos < len(self._input) and self._is_digit(self._peek()):
                value += self._advance()

        return DSLToken(
            DSLTokenType.NUMBER,
            value,
            SourceLocation(start_line, start_column, start_pos, self._pos),
        )

    def _read_string_literal(self, quote_char: str) -> DSLToken:
        """Read string literal with escape sequences."""
        start_pos = self._pos
        start_line = self._line
        start_column = self._column
        value = ''

        # Consume opening quote
        self._advance()
        value += quote_char

        while self._pos < len(self._input):
            ch = self._peek()

            # End of string
            if ch == quote_char:
                value += self._advance()
                break

            # Escape sequences
            if ch == '\\':
                value += self._advance()  # backslash
                if self._pos < len(self._input):
                    value += self._advance()  # escaped character
                continue

            # Regular character
            value += self._advance()

        # Check if string was closed
        if not value.endswith(quote_char):
            raise LexerError(
                'Unterminated string literal',
                SourceLocation(start_line, start_column, start_pos, self._pos),
                self._input,
            )

        return DSLToken(
            DSLTokenType.STRING,
            value,
            SourceLocation(start_line, start_column, start_pos, self._pos),
        )

    def _read_backtick_string(self) -> DSLToken:
        """
        Read backtick string literal (used for expressions).
        Content between backticks is treated as an expression.
        Only \\` (escaped backtick) and \\\\ (escaped backslash) are special.
        All other escape sequences are passed through as-is.
        """
        start_pos = self._pos
        start_line = self._line
        start_column = self._column
        value = ''

        # Consume opening backtick
        self._advance()

        while self._pos < len(self._input):
            ch = self._peek()

            # End of string
            if ch == '`':
                self._advance()
                break

            # Escape sequences - only handle \` and \\
            if ch == '\\':
                next_ch = self._peek_ahead(1)
                if next_ch in ('`', '\\'):
                    # Consume backslash and add the escaped character
                    self._advance()
                    value += self._advance()
                    continue
                # For all other escapes, keep the backslash
                value += self._advance()
                continue

            # Regular character
            value += self._advance()

        return DSLToken(
            DSLTokenType.BACKTICK_STRING,
            value,
            SourceLocation(start_line, start_column, start_pos, self._pos),
        )

    def _read_line_comment(self) -> DSLToken:
        """Read single-line comment // ..."""
        start_pos = self._pos
        start_line = self._line
        start_column = self._column
        value = ''

        # Consume //
        value += self._advance()
        value += self._advance()

        # Read until end of line
        while self._pos < len(self._input) and self._peek() != '\n':
            value += self._advance()

        return DSLToken(
            DSLTokenType.COMMENT,
            value,
            SourceLocation(start_line, start_column, start_pos, self._pos),
        )

    def _read_block_comment(self) -> DSLToken:
        """Read block comment /* ... */"""
        start_pos = self._pos
        start_line = self._line
        start_column = self._column
        value = ''

        # Consume /*
        value += self._advance()
        value += self._advance()

        # Read until */
        while self._pos < len(self._input):
            if self._peek() == '*' and self._peek_ahead(1) == '/':
                value += self._advance()  # *
                value += self._advance()  # /
                break

            ch = self._advance()
            value += ch

            # Track line numbers
            if ch == '\n':
                self._line += 1
                self._column = 1

        # Check if comment was closed
        if not value.endswith('*/'):
            raise LexerError(
                'Unterminated block comment',
                SourceLocation(start_line, start_column, start_pos, self._pos),
                self._input,
            )

        return DSLToken(
            DSLTokenType.COMMENT,
            value,
            SourceLocation(start_line, start_column, start_pos, self._pos),
        )

    def _peek(self) -> str:
        """Helper: peek at current character."""
        if self._pos < len(self._input):
            return self._input[self._pos]
        return ''

    def _peek_ahead(self, n: int) -> str:
        """Helper: peek ahead n characters."""
        idx = self._pos + n
        if idx < len(self._input):
            return self._input[idx]
        return ''

    def _advance(self) -> str:
        """Helper: advance position and return current character."""
        ch = self._input[self._pos]
        self._pos += 1
        self._column += 1
        return ch

    @staticmethod
    def _is_digit(ch: str) -> bool:
        """Helper: check if character is a digit."""
        return '0' <= ch <= '9'

    @staticmethod
    def _is_identifier_start(ch: str) -> bool:
        """Helper: check if character can start an identifier."""
        return ('a' <= ch <= 'z') or ('A' <= ch <= 'Z') or ch == '_'

    @staticmethod
    def _is_identifier_part(ch: str) -> bool:
        """Helper: check if character can be part of an identifier."""
        return DSLLexer._is_identifier_start(ch) or DSLLexer._is_digit(ch)
