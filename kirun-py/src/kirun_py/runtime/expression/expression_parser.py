from __future__ import annotations

from typing import Optional

from kirun_py.runtime.expression.exception.expression_evaluation_exception import ExpressionEvaluationException
from kirun_py.runtime.expression.expression_token import ExpressionToken
from kirun_py.runtime.expression.expression_token_value import ExpressionTokenValue
from kirun_py.runtime.expression.expression_lexer import ExpressionLexer, Token, TokenType
from kirun_py.runtime.expression.operation import Operation


class ExpressionParser:

    def __init__(self, expression: str):
        self._original_expression: str = expression
        self._lexer: ExpressionLexer = ExpressionLexer(expression)
        self._current_token: Optional[Token] = self._lexer.next_token()
        self._previous_token_value: Optional[Token] = None
        self._parse_depth: int = 0

    def _create_parser_error(self, message: str) -> ExpressionEvaluationException:
        """Create a detailed error message with context about where parsing failed."""
        position = self._lexer.get_position()
        context_start = max(0, position - 20)
        context_end = min(len(self._original_expression), position + 20)
        context = self._original_expression[context_start:context_end]

        current_str = (
            f'{self._current_token.type.value}("{self._current_token.value}")'
            if self._current_token else 'null'
        )
        previous_str = (
            f'{self._previous_token_value.type.value}("{self._previous_token_value.value}")'
            if self._previous_token_value else 'null'
        )

        error_details = (
            f'Parser Error: {message}\n'
            f'Expression: {self._original_expression}\n'
            f'Position: {position}\n'
            f'Context: ...{context}...\n'
            f'Current token: {current_str}\n'
            f'Previous token: {previous_str}\n'
            f'Parse depth: {self._parse_depth}'
        )

        # Print to stderr for debugging
        import sys
        print('\n' + error_details + '\n', file=sys.stderr)

        return ExpressionEvaluationException(
            self._original_expression,
            f'{message} at position {position}',
        )

    def parse(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        if self._current_token is None:
            raise self._create_parser_error('Empty expression')

        try:
            expr = self._parse_expression()

            # Ensure we consumed all tokens
            if self._current_token is not None and self._current_token.type != TokenType.EOF:
                raise self._create_parser_error(
                    f'Unexpected token "{self._current_token.value}" - expected end of expression'
                )

            return expr
        except ExpressionEvaluationException as e:
            if 'Parser Error:' not in str(e):
                raise self._create_parser_error(str(e))
            raise

    def _parse_expression(self) -> 'Expression':
        return self._parse_ternary()

    # Ternary: condition ? trueExpr : falseExpr (precedence 12)
    def _parse_ternary(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_logical_or()

        if self._match_token(TokenType.QUESTION):
            true_expr = self._parse_ternary()
            self._expect_token(TokenType.COLON)
            false_expr = self._parse_ternary()
            return Expression.create_ternary(expr, true_expr, false_expr)

        return expr

    # Logical OR: expr or expr (precedence 11)
    def _parse_logical_or(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_nullish_coalescing()

        while self._match_operator('or'):
            right = self._parse_nullish_coalescing()
            expr = Expression('', expr, right, Operation.OR)

        return expr

    # Nullish coalescing: expr ?? expr
    def _parse_nullish_coalescing(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_logical_and()

        while self._match_operator('??'):
            right = self._parse_logical_and()
            expr = Expression('', expr, right, Operation.NULLISH_COALESCING_OPERATOR)

        return expr

    # Logical AND: expr and expr (precedence 10)
    def _parse_logical_and(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_logical_not()

        while self._match_operator('and'):
            right = self._parse_logical_not()
            expr = Expression('', expr, right, Operation.AND)

        return expr

    # Logical NOT: not expr (precedence 10, unary)
    def _parse_logical_not(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        if self._match_operator('not'):
            expr = self._parse_logical_not()  # Right-associative for unary
            return Expression('', expr, None, Operation.UNARY_LOGICAL_NOT)

        return self._parse_comparison()

    # Comparison: <, <=, >, >=, =, != (precedence 5-6)
    def _parse_comparison(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_bitwise_or()

        while True:
            op: Optional[Operation] = None

            if self._match_operator('<'):
                op = Operation.LESS_THAN
            elif self._match_operator('<='):
                op = Operation.LESS_THAN_EQUAL
            elif self._match_operator('>'):
                op = Operation.GREATER_THAN
            elif self._match_operator('>='):
                op = Operation.GREATER_THAN_EQUAL
            elif self._match_operator('='):
                op = Operation.EQUAL
            elif self._match_operator('!='):
                op = Operation.NOT_EQUAL
            else:
                break

            right = self._parse_bitwise_or()
            expr = Expression('', expr, right, op)

        return expr

    # Bitwise OR: | (precedence 9)
    def _parse_bitwise_or(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_bitwise_xor()

        while self._match_operator('|'):
            right = self._parse_bitwise_xor()
            expr = Expression('', expr, right, Operation.BITWISE_OR)

        return expr

    # Bitwise XOR: ^ (precedence 8)
    def _parse_bitwise_xor(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_bitwise_and()

        while self._match_operator('^'):
            right = self._parse_bitwise_and()
            expr = Expression('', expr, right, Operation.BITWISE_XOR)

        return expr

    # Bitwise AND: & (precedence 7)
    def _parse_bitwise_and(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_shift()

        while self._match_operator('&'):
            right = self._parse_shift()
            expr = Expression('', expr, right, Operation.BITWISE_AND)

        return expr

    # Shift: <<, >>, >>> (precedence 4)
    def _parse_shift(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_additive()

        while True:
            op: Optional[Operation] = None

            if self._match_operator('<<'):
                op = Operation.BITWISE_LEFT_SHIFT
            elif self._match_operator('>>'):
                op = Operation.BITWISE_RIGHT_SHIFT
            elif self._match_operator('>>>'):
                op = Operation.BITWISE_UNSIGNED_RIGHT_SHIFT
            else:
                break

            right = self._parse_additive()
            expr = Expression('', expr, right, op)

        return expr

    # Additive: +, - (precedence 3)
    def _parse_additive(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_multiplicative()

        while True:
            op: Optional[Operation] = None

            if self._match_operator('+'):
                op = Operation.ADDITION
            elif self._match_operator('-'):
                op = Operation.SUBTRACTION
            else:
                break

            right = self._parse_multiplicative()
            expr = Expression('', expr, right, op)

        return expr

    # Multiplicative: *, /, //, % (precedence 2)
    # Right-associative to match old parser behavior
    def _parse_multiplicative(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_unary()

        if self._match_operator('*'):
            right = self._parse_multiplicative()
            return Expression('', expr, right, Operation.MULTIPLICATION)
        elif self._match_operator('/'):
            right = self._parse_multiplicative()
            return Expression('', expr, right, Operation.DIVISION)
        elif self._match_operator('//'):
            right = self._parse_multiplicative()
            return Expression('', expr, right, Operation.INTEGER_DIVISION)
        elif self._match_operator('%'):
            right = self._parse_multiplicative()
            return Expression('', expr, right, Operation.MOD)

        return expr

    # Unary: +, -, ~, not (precedence 1)
    def _parse_unary(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        if self._match_operator('+'):
            expr = self._parse_unary()
            return Expression('', expr, None, Operation.UNARY_PLUS)

        if self._match_operator('-'):
            expr = self._parse_unary()
            return Expression('', expr, None, Operation.UNARY_MINUS)

        if self._match_operator('~'):
            expr = self._parse_unary()
            return Expression('', expr, None, Operation.UNARY_BITWISE_COMPLEMENT)

        return self._parse_postfix()

    # Postfix: member access, array access (precedence 1)
    def _parse_postfix(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        expr = self._parse_primary()

        while True:
            # Object member access: .identifier
            if self._match_token(TokenType.DOT):
                right = self._parse_postfix_right_side()
                expr = Expression('', expr, right, Operation.OBJECT_OPERATOR)
                continue
            # Array access: [expression]
            elif self._match_token(TokenType.LEFT_BRACKET):
                index_expr = self._parse_bracket_content()
                self._expect_token(TokenType.RIGHT_BRACKET)
                expr = Expression('', expr, index_expr, Operation.ARRAY_OPERATOR)
            # Range operator: ..
            elif self._match_operator('..'):
                right = self._parse_primary()
                expr = Expression('', expr, right, Operation.ARRAY_RANGE_INDEX_OPERATOR)
            else:
                break

        return expr

    def _parse_postfix_right_side(self) -> 'Expression':
        """Parse the right side of a dot operator.

        Handles identifiers with static array access, dynamic array access,
        and chained dot access.
        """
        from kirun_py.runtime.expression.expression import Expression

        self._parse_depth += 1

        # Expect an identifier or number
        if (self._current_token is None or
                (self._current_token.type != TokenType.IDENTIFIER and
                 self._current_token.type != TokenType.NUMBER)):
            self._parse_depth -= 1
            token_type = self._current_token.type.value if self._current_token else 'EOF'
            raise self._create_parser_error(
                f'Expected identifier or number after dot, but found {token_type}'
            )

        identifier_value = self._current_token.value
        self._advance()

        # Handle ObjectId-like values (e.g., 507f1f77bcf86cd799439011)
        # The lexer splits these into NUMBER + IDENTIFIER
        if (self._current_token is not None and
                self._current_token.type == TokenType.IDENTIFIER and
                len(self._current_token.value) > 0 and
                (self._current_token.value[0].isalpha() or self._current_token.value[0] == '_')):
            identifier_value += self._current_token.value
            self._advance()

        # Check if the identifier contains static bracket notation
        bracket_index = identifier_value.find('[')

        if bracket_index == -1:
            expr = Expression.create_leaf(identifier_value)
        else:
            expr = self._parse_static_bracket_identifier(identifier_value)

        # Check for dynamic array access following the identifier
        while self._match_token(TokenType.LEFT_BRACKET):
            index_expr = self._parse_bracket_content()
            self._expect_token(TokenType.RIGHT_BRACKET)
            expr = Expression('', expr, index_expr, Operation.ARRAY_OPERATOR)

        # Consume subsequent DOTs to group all property accesses on the right side
        while self._match_token(TokenType.DOT):
            right = self._parse_postfix_right_side()  # Recursive call
            expr = Expression('', expr, right, Operation.OBJECT_OPERATOR)

        self._parse_depth -= 1
        return expr

    def _parse_static_bracket_identifier(self, identifier_value: str) -> 'Expression':
        """Parse an identifier that contains static bracket notation.

        E.g., 'obj["key"]' or 'a[9]' or 'a[9]["key"]'
        """
        from kirun_py.runtime.expression.expression import Expression

        bracket_index = identifier_value.find('[')

        # Extract base identifier
        base_identifier = identifier_value[:bracket_index]
        expr = Expression.create_leaf(base_identifier)

        # Parse all static bracket expressions
        remaining = identifier_value[bracket_index:]
        bracket_start = 0

        while bracket_start < len(remaining) and remaining[bracket_start] == '[':
            # Find the matching closing bracket
            bracket_count = 1
            end_index = bracket_start + 1
            in_string = False
            string_char = ''

            while end_index < len(remaining) and bracket_count > 0:
                c = remaining[end_index]

                if in_string:
                    if c == string_char and (end_index == 0 or remaining[end_index - 1] != '\\'):
                        in_string = False
                else:
                    if c == '"' or c == "'":
                        in_string = True
                        string_char = c
                    elif c == '[':
                        bracket_count += 1
                    elif c == ']':
                        bracket_count -= 1

                end_index += 1

            # Extract bracket content (without the brackets)
            bracket_content = remaining[bracket_start + 1:end_index - 1]

            # Create expression for this bracket content
            if ((bracket_content.startswith('"') and bracket_content.endswith('"')) or
                    (bracket_content.startswith("'") and bracket_content.endswith("'"))):
                # It's a string literal - preserve quotes
                quote_char = bracket_content[0]
                str_value = bracket_content[1:-1]
                index_expr = Expression(
                    '',
                    ExpressionTokenValue(quote_char + str_value + quote_char, str_value),
                    None, None,
                )
            else:
                # It's a number or range
                range_index = bracket_content.find('..')
                if range_index != -1:
                    start_expr = (
                        Expression.create_leaf('0')
                        if range_index == 0
                        else Expression.create_leaf(bracket_content[:range_index])
                    )
                    end_expr = (
                        Expression.create_leaf('')
                        if range_index == len(bracket_content) - 2
                        else Expression.create_leaf(bracket_content[range_index + 2:])
                    )
                    index_expr = Expression(
                        '', start_expr, end_expr, Operation.ARRAY_RANGE_INDEX_OPERATOR,
                    )
                else:
                    index_expr = Expression.create_leaf(bracket_content)

            expr = Expression('', expr, index_expr, Operation.ARRAY_OPERATOR)
            bracket_start = end_index

        return expr

    def _parse_identifier_path(self) -> 'Expression':
        """Parse an identifier path."""
        from kirun_py.runtime.expression.expression import Expression

        if self._current_token is None or self._current_token.type != TokenType.IDENTIFIER:
            token_type = self._current_token.type.value if self._current_token else 'EOF'
            raise self._create_parser_error(
                f'Expected identifier but found {token_type}'
            )

        path = self._current_token.value
        self._advance()

        return Expression.create_leaf(path)

    def _parse_bracket_content(self) -> 'Expression':
        """Parse bracket content - can be expression, string literal, or identifier."""
        from kirun_py.runtime.expression.expression import Expression

        # If it's a string literal (quoted), preserve it
        if self._current_token is not None and self._current_token.type == TokenType.STRING:
            token = self._current_token
            self._advance()
            str_value = token.value[1:-1]
            return Expression(
                '', ExpressionTokenValue(token.value, str_value), None, None,
            )

        # Otherwise parse as expression
        return self._parse_expression()

    # Primary: literals, identifiers, parentheses
    def _parse_primary(self) -> 'Expression':
        from kirun_py.runtime.expression.expression import Expression

        # Number literal
        if self._match_token(TokenType.NUMBER):
            token = self._previous_token()
            return Expression('', ExpressionToken(token.value), None, None)

        # String literal
        if self._match_token(TokenType.STRING):
            token = self._previous_token()
            str_value = token.value[1:-1]
            return Expression(
                '', ExpressionTokenValue(token.value, str_value), None, None,
            )

        # Identifier
        if self._current_token is not None and self._current_token.type == TokenType.IDENTIFIER:
            return self._parse_identifier_path()

        # Parenthesized expression
        if self._match_token(TokenType.LEFT_PAREN):
            expr = self._parse_expression()
            self._expect_token(TokenType.RIGHT_PAREN)
            return expr

        token_type = self._current_token.type.value if self._current_token else 'EOF'
        raise self._create_parser_error(
            f"Unexpected token in expression - expected number, string, identifier, or '(' but found {token_type}"
        )

    # Helper methods

    def _match_token(self, type_: TokenType) -> bool:
        if self._current_token is not None and self._current_token.type == type_:
            self._advance()
            return True
        return False

    def _match_operator(self, op: str) -> bool:
        if (self._current_token is not None and
                self._current_token.type == TokenType.OPERATOR and
                self._current_token.value == op):
            self._advance()
            return True
        return False

    def _expect_token(self, type_: TokenType) -> Token:
        if self._current_token is None or self._current_token.type != type_:
            token_type = self._current_token.type.value if self._current_token else 'EOF'
            token_value = f' ("{self._current_token.value}")' if self._current_token else ''
            raise self._create_parser_error(
                f'Expected {type_.value}, but found {token_type}{token_value}'
            )
        token = self._current_token
        self._advance()
        return token

    def _advance(self) -> None:
        self._previous_token_value = self._current_token
        self._current_token = self._lexer.next_token()

    def _previous_token(self) -> Optional[Token]:
        return self._previous_token_value
