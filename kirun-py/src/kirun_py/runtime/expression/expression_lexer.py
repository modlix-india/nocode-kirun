from __future__ import annotations

from enum import Enum
from typing import Optional

from kirun_py.runtime.expression.exception.expression_evaluation_exception import ExpressionEvaluationException


class TokenType(Enum):
    IDENTIFIER = 'IDENTIFIER'
    NUMBER = 'NUMBER'
    STRING = 'STRING'
    OPERATOR = 'OPERATOR'
    LEFT_PAREN = 'LEFT_PAREN'
    RIGHT_PAREN = 'RIGHT_PAREN'
    LEFT_BRACKET = 'LEFT_BRACKET'
    RIGHT_BRACKET = 'RIGHT_BRACKET'
    DOT = 'DOT'
    QUESTION = 'QUESTION'
    COLON = 'COLON'
    WHITESPACE = 'WHITESPACE'
    EOF = 'EOF'


class Token:

    def __init__(self, type_: TokenType, value: str, start_pos: int, end_pos: int):
        self.type = type_
        self.value = value
        self.start_pos = start_pos
        self.end_pos = end_pos

    def __str__(self) -> str:
        return f'Token({self.type.value}, "{self.value}", {self.start_pos}-{self.end_pos})'


class ExpressionLexer:

    def __init__(self, expression: str):
        self._pos: int = 0
        self._input: str = expression
        self._length: int = len(expression)

    def get_position(self) -> int:
        return self._pos

    def peek(self) -> Optional[Token]:
        saved_pos = self._pos
        token = self.next_token()
        self._pos = saved_pos
        return token

    def next_token(self) -> Optional[Token]:
        if self._pos >= self._length:
            return Token(TokenType.EOF, '', self._pos, self._pos)

        # Skip whitespace
        while self._pos < self._length and self._is_whitespace(self._input[self._pos]):
            self._pos += 1

        if self._pos >= self._length:
            return Token(TokenType.EOF, '', self._pos, self._pos)

        start_pos = self._pos
        char = self._input[self._pos]

        # Handle string literals
        if char == '"' or char == "'":
            return self._read_string_literal(char, start_pos)

        # Handle numbers
        if self._is_digit(char):
            return self._read_number(start_pos)

        # Handle operators and special characters
        if char == '(':
            self._pos += 1
            return Token(TokenType.LEFT_PAREN, '(', start_pos, self._pos)
        if char == ')':
            self._pos += 1
            return Token(TokenType.RIGHT_PAREN, ')', start_pos, self._pos)
        if char == '[':
            self._pos += 1
            return Token(TokenType.LEFT_BRACKET, '[', start_pos, self._pos)
        if char == ']':
            self._pos += 1
            return Token(TokenType.RIGHT_BRACKET, ']', start_pos, self._pos)
        if char == '.':
            # Check for '..' range operator
            if self._pos + 1 < self._length and self._input[self._pos + 1] == '.':
                self._pos += 2
                return Token(TokenType.OPERATOR, '..', start_pos, self._pos)
            self._pos += 1
            return Token(TokenType.DOT, '.', start_pos, self._pos)
        if char == '?':
            # Check for ?? (nullish coalescing) before single ? (ternary)
            if self._pos + 1 < self._length and self._input[self._pos + 1] == '?':
                self._pos += 2
                return Token(TokenType.OPERATOR, '??', start_pos, self._pos)
            self._pos += 1
            return Token(TokenType.QUESTION, '?', start_pos, self._pos)
        if char == ':':
            self._pos += 1
            return Token(TokenType.COLON, ':', start_pos, self._pos)

        # Handle multi-character operators
        operator = self._read_operator(start_pos)
        if operator is not None:
            return operator

        # Handle identifiers and keywords
        return self._read_identifier(start_pos)

    def _read_string_literal(self, quote_char: str, start_pos: int) -> Token:
        self._pos += 1  # Skip opening quote
        value = ''
        escaped = False

        while self._pos < self._length:
            char = self._input[self._pos]

            if escaped:
                value += char
                escaped = False
                self._pos += 1
                continue

            if char == '\\':
                escaped = True
                value += char
                self._pos += 1
                continue

            if char == quote_char:
                self._pos += 1  # Skip closing quote
                end_pos = self._pos
                # Return the full string including quotes for bracket notation detection
                return Token(TokenType.STRING, quote_char + value + quote_char, start_pos, end_pos)

            value += char
            self._pos += 1

        raise ExpressionEvaluationException(
            self._input,
            f'Missing string ending marker {quote_char}',
        )

    def _read_number(self, start_pos: int) -> Token:
        value = ''
        has_decimal = False

        while self._pos < self._length:
            char = self._input[self._pos]

            if self._is_digit(char):
                value += char
                self._pos += 1
            elif (char == '.' and not has_decimal and
                  self._pos + 1 < self._length and self._is_digit(self._input[self._pos + 1])):
                value += char
                has_decimal = True
                self._pos += 1
            else:
                break

        return Token(TokenType.NUMBER, value, start_pos, self._pos)

    def _read_operator(self, start_pos: int) -> Optional[Token]:
        # Try longest operators first
        operators = [
            '>>>', '<<', '>>', '<=', '>=', '!=', '==', '//',
            '+', '-', '*', '/', '%', '=', '<', '>', '&', '|', '^', '~',
        ]

        for op in operators:
            if self._pos + len(op) <= self._length:
                candidate = self._input[self._pos:self._pos + len(op)]
                if candidate == op:
                    self._pos += len(op)
                    return Token(TokenType.OPERATOR, op, start_pos, self._pos)

        return None

    def _read_identifier(self, start_pos: int) -> Token:
        value = ''

        while self._pos < self._length:
            char = self._input[self._pos]

            # Handle bracket notation: only include STATIC bracket content in identifier
            if char == '[':
                if self._is_static_bracket_content():
                    value += self._read_static_bracket_content()
                    continue
                else:
                    break

            # Stop at non-identifier characters
            # Dots are NOT included - they are handled as DOT tokens for OBJECT_OPERATOR
            if not self._is_identifier_char(char):
                break

            value += char
            self._pos += 1

        if len(value) == 0:
            raise ExpressionEvaluationException(
                self._input,
                f'Unexpected character: {self._input[self._pos]}',
            )

        # Check for keywords that are operators
        keyword_operators = ['and', 'or', 'not']
        if value.lower() in keyword_operators:
            return Token(TokenType.OPERATOR, value.lower(), start_pos, self._pos)

        return Token(TokenType.IDENTIFIER, value, start_pos, self._pos)

    def _is_static_bracket_content(self) -> bool:
        """Check if the bracket content starting at current position is static.

        Static content: numeric literals ([9], [-1]) or quoted strings (["key"], ['key'])
        Dynamic content: identifiers ([Page.id]) or expressions ([expr + 1])
        """
        peek_pos = self._pos + 1  # Skip the '['

        # Skip whitespace
        while peek_pos < self._length and self._is_whitespace(self._input[peek_pos]):
            peek_pos += 1

        if peek_pos >= self._length:
            return False

        first_char = self._input[peek_pos]

        # Check for quoted string
        if first_char == '"' or first_char == "'":
            return True

        # Check for numeric literal (including negative numbers)
        if self._is_digit(first_char) or (
            first_char == '-' and peek_pos + 1 < self._length and
            self._is_digit(self._input[peek_pos + 1])
        ):
            # Verify it's a pure number followed by ] or ..
            num_end = peek_pos
            if self._input[num_end] == '-':
                num_end += 1
            while num_end < self._length and (
                self._is_digit(self._input[num_end]) or self._input[num_end] == '.'
            ):
                # Check for range operator (..)
                if (self._input[num_end] == '.' and num_end + 1 < self._length and
                        self._input[num_end + 1] == '.'):
                    return True
                num_end += 1
            # Skip whitespace
            while num_end < self._length and self._is_whitespace(self._input[num_end]):
                num_end += 1
            # Check if followed by ] or ..
            if num_end < self._length and (
                self._input[num_end] == ']' or
                (self._input[num_end] == '.' and num_end + 1 < self._length and
                 self._input[num_end + 1] == '.')
            ):
                return True
            return False

        # Anything else is dynamic
        return False

    def _read_static_bracket_content(self) -> str:
        """Read static bracket content and return it as a string (including brackets).

        Assumes _is_static_bracket_content() returned True.
        """
        content = '['
        self._pos += 1  # Skip the '['

        bracket_count = 1
        while self._pos < self._length and bracket_count > 0:
            c = self._input[self._pos]
            content += c
            self._pos += 1

            if c == '[':
                bracket_count += 1
            elif c == ']':
                bracket_count -= 1
            elif c == '"' or c == "'":
                # Handle string literals inside brackets
                quote_char = c
                while self._pos < self._length:
                    next_char = self._input[self._pos]
                    content += next_char
                    self._pos += 1
                    if (next_char == quote_char and self._pos > 1 and
                            self._input[self._pos - 2] != '\\'):
                        break

        return content

    @staticmethod
    def _is_digit(char: str) -> bool:
        return '0' <= char <= '9'

    @staticmethod
    def _is_whitespace(char: str) -> bool:
        return char in (' ', '\t', '\n', '\r')

    @staticmethod
    def _is_identifier_char(char: str) -> bool:
        return (
            ('a' <= char <= 'z') or
            ('A' <= char <= 'Z') or
            ('0' <= char <= '9') or
            char == '_'
        )
