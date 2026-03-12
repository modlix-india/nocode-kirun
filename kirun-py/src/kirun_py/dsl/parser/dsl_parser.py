from __future__ import annotations

import re
from typing import Any, Dict, List, Optional, Union

from kirun_py.dsl.lexer.dsl_token import DSLToken, DSLTokenType
from kirun_py.dsl.parser.ast.argument_node import ArgumentNode, ArgumentValue
from kirun_py.dsl.parser.ast.complex_value_node import ComplexValueNode
from kirun_py.dsl.parser.ast.event_decl_node import EventDeclNode
from kirun_py.dsl.parser.ast.expression_node import ExpressionNode
from kirun_py.dsl.parser.ast.function_call_node import FunctionCallNode
from kirun_py.dsl.parser.ast.function_def_node import FunctionDefNode
from kirun_py.dsl.parser.ast.parameter_decl_node import ParameterDeclNode
from kirun_py.dsl.parser.ast.schema_literal_node import SchemaLiteralNode
from kirun_py.dsl.parser.ast.schema_node import SchemaNode
from kirun_py.dsl.parser.ast.statement_node import StatementNode
from kirun_py.dsl.parser.dsl_parser_error import DSLParserError


class DSLParser:
    """
    DSL Parser - Recursive descent parser.
    Converts tokens into AST.
    """

    def __init__(self, tokens: List[DSLToken], original_input: str = '') -> None:
        self._tokens = tokens
        self._current: int = 0
        self._original_input = original_input

    def parse(self) -> FunctionDefNode:
        """Parse tokens into AST."""
        return self._parse_function_definition()

    def _parse_function_definition(self) -> FunctionDefNode:
        """Parse function definition (top-level)."""
        start_token = self._expect(DSLTokenType.KEYWORD, 'FUNCTION')
        name = self._expect_identifier()

        namespace = self._parse_namespace_decl()
        parameters = self._parse_parameters_decl()
        events = self._parse_events_decl()

        self._expect(DSLTokenType.KEYWORD, 'LOGIC')
        logic = self._parse_logic_block()

        return FunctionDefNode(name, namespace, parameters, events, logic, start_token.location)

    def _parse_namespace_decl(self) -> Optional[str]:
        """Parse optional namespace declaration."""
        if self._match(DSLTokenType.KEYWORD, 'NAMESPACE'):
            self._advance()  # consume NAMESPACE
            return self._expect_dotted_identifier()
        return None

    def _parse_parameters_decl(self) -> List[ParameterDeclNode]:
        """Parse optional parameters declaration."""
        if not self._match(DSLTokenType.KEYWORD, 'PARAMETERS'):
            return []

        self._advance()  # consume PARAMETERS

        parameters: List[ParameterDeclNode] = []

        while (
            not self._match(DSLTokenType.KEYWORD, 'EVENTS')
            and not self._match(DSLTokenType.KEYWORD, 'LOGIC')
            and not self._match(DSLTokenType.EOF)
        ):
            parameters.append(self._parse_parameter_decl())

        return parameters

    def _parse_parameter_decl(self) -> ParameterDeclNode:
        """Parse single parameter declaration."""
        name_token = self._peek()
        name = self._expect_identifier()
        self._expect(DSLTokenType.KEYWORD, 'AS')
        schema = self._parse_schema_spec()

        return ParameterDeclNode(name, schema, name_token.location)

    def _parse_events_decl(self) -> List[EventDeclNode]:
        """Parse optional events declaration."""
        if not self._match(DSLTokenType.KEYWORD, 'EVENTS'):
            return []

        self._advance()  # consume EVENTS

        events: List[EventDeclNode] = []

        while not self._match(DSLTokenType.KEYWORD, 'LOGIC') and not self._match(DSLTokenType.EOF):
            events.append(self._parse_event_decl())

        return events

    def _parse_event_decl(self) -> EventDeclNode:
        """Parse single event declaration."""
        name_token = self._peek()
        name = self._expect_identifier()

        parameters: List[ParameterDeclNode] = []

        while (
            not self._match(DSLTokenType.KEYWORD, 'LOGIC')
            and not self._match(DSLTokenType.EOF)
            and self._peek().type == DSLTokenType.IDENTIFIER
        ):
            ahead = self._peek_ahead(1)
            if ahead is not None and ahead.is_token(DSLTokenType.KEYWORD, 'AS'):
                parameters.append(self._parse_parameter_decl())
            else:
                break

        return EventDeclNode(name, parameters, name_token.location)

    def _parse_logic_block(self) -> List[StatementNode]:
        """Parse logic block (list of statements)."""
        statements: List[StatementNode] = []

        while not self._match(DSLTokenType.EOF):
            statements.append(self._parse_statement())

        return statements

    def _parse_statement(self, min_block_column: int = 0) -> StatementNode:
        """
        Parse single statement.

        Args:
            min_block_column: minimum column for blocks to belong to this statement.
                              Blocks at smaller column positions belong to an ancestor.
        """
        name_token = self._peek()

        # Check if this is an anonymous statement (starts with :)
        if self._match(DSLTokenType.COLON):
            statement_name = f"_anonymous_{self._current}"
        else:
            statement_name = self._expect_identifier()
            self._expect(DSLTokenType.COLON)

        function_call = self._parse_function_call()

        # Parse optional AFTER clause
        after_steps: List[str] = []
        if self._match(DSLTokenType.KEYWORD, 'AFTER'):
            self._advance()  # consume AFTER
            while True:
                after_steps.append(self._expect_step_reference())
                if self._match(DSLTokenType.COMMA):
                    self._advance()
                else:
                    break

        # Parse optional IF clause
        execute_if_steps: List[str] = []
        if self._match(DSLTokenType.KEYWORD, 'IF'):
            self._advance()  # consume IF
            while True:
                execute_if_steps.append(self._expect_step_reference())
                if self._match(DSLTokenType.COMMA):
                    self._advance()
                else:
                    break

        # Parse optional trailing comment
        comment = ''
        if self._match(DSLTokenType.COMMENT):
            comment_token = self._advance()
            comment = re.sub(r'^/\*\s*', '', comment_token.value)
            comment = re.sub(r'\s*\*/$', '', comment)
            comment = comment.strip()

        # Parse nested blocks
        nested_blocks = self._parse_nested_blocks(min_block_column)

        return StatementNode(
            statement_name,
            function_call,
            after_steps,
            execute_if_steps,
            nested_blocks,
            name_token.location,
            comment,
        )

    def _parse_nested_blocks(self, min_block_column: int = 0) -> Dict[str, List[StatementNode]]:
        """
        Parse nested blocks (iteration, true, false, output, error).

        Args:
            min_block_column: minimum column for blocks to belong to this statement.
                              Blocks at smaller column positions belong to an ancestor.
        """
        blocks: Dict[str, List[StatementNode]] = {}

        while not self._match(DSLTokenType.EOF):
            token = self._peek()

            is_block_token = token.type in (
                DSLTokenType.IDENTIFIER,
                DSLTokenType.BOOLEAN,
                DSLTokenType.KEYWORD,
            )
            next_token = self._peek_ahead(1)
            is_followed_by_colon = (
                next_token is not None and next_token.type == DSLTokenType.COLON
            )

            block_column = token.location.column
            if block_column <= min_block_column:
                break

            if is_block_token and not is_followed_by_colon:
                block_name = self._advance().value
                statements: List[StatementNode] = []

                this_block_column = block_column

                while not self._match(DSLTokenType.EOF):
                    next_tok = self._peek()

                    is_next_block_token = next_tok.type in (
                        DSLTokenType.IDENTIFIER,
                        DSLTokenType.BOOLEAN,
                        DSLTokenType.KEYWORD,
                    )
                    if is_next_block_token:
                        next_next = self._peek_ahead(1)
                        next_is_followed_by_colon = (
                            next_next is not None and next_next.type == DSLTokenType.COLON
                        )

                        if not next_is_followed_by_colon:
                            if next_tok.location.column <= this_block_column:
                                break
                        else:
                            if next_tok.location.column <= this_block_column:
                                break

                    if next_tok.type == DSLTokenType.COLON:
                        statements.append(self._parse_statement(this_block_column))
                    elif next_tok.type in (
                        DSLTokenType.IDENTIFIER,
                        DSLTokenType.BOOLEAN,
                        DSLTokenType.KEYWORD,
                    ):
                        statements.append(self._parse_statement(this_block_column))
                    else:
                        break

                blocks[block_name] = statements
            else:
                break

        return blocks

    def _parse_function_call(self) -> FunctionCallNode:
        """Parse function call."""
        start_token = self._peek()
        full_name = self._expect_dotted_identifier()

        parts = full_name.split('.')
        name = parts.pop()
        namespace = '.'.join(parts)

        self._expect(DSLTokenType.LEFT_PAREN)
        arguments_map = self._parse_argument_list()
        self._expect(DSLTokenType.RIGHT_PAREN)

        return FunctionCallNode(namespace, name, arguments_map, start_token.location)

    def _parse_argument_list(self) -> Dict[str, ArgumentNode]:
        """
        Parse argument list.
        Supports multi-value parameters by repeating the parameter name.
        """
        arguments_map: Dict[str, ArgumentNode] = {}

        if self._match(DSLTokenType.RIGHT_PAREN):
            return arguments_map

        while True:
            arg_token = self._peek()
            param_name = self._expect_identifier()
            self._expect(DSLTokenType.EQUALS)

            value = self._parse_argument_value()

            existing = arguments_map.get(param_name)
            if existing is not None:
                existing.values.append(value)
            else:
                arguments_map[param_name] = ArgumentNode(param_name, value, arg_token.location)

            if self._match(DSLTokenType.COMMA):
                self._advance()
            else:
                break

            if self._match(DSLTokenType.RIGHT_PAREN) or self._match(DSLTokenType.EOF):
                break

        return arguments_map

    def _parse_argument_value(self) -> ArgumentValue:
        """Parse argument value (expression, complex value, or schema literal)."""
        token = self._peek()

        # Check for schema literal
        if self._match(DSLTokenType.LEFT_PAREN):
            saved_pos = self._current
            try:
                return self._parse_schema_literal()
            except Exception:
                self._current = saved_pos

        # Check for backtick string
        if self._match(DSLTokenType.BACKTICK_STRING):
            bt_token = self._advance()
            return ExpressionNode(bt_token.value, bt_token.location)

        # Check for string literal
        if self._match(DSLTokenType.STRING):
            str_token = self._peek()
            quote_char = str_token.value[0]

            if quote_char == "'":
                next_token = self._peek_ahead(1)
                if next_token is not None and next_token.type == DSLTokenType.OPERATOR:
                    return self._parse_expression()
                self._advance()
                return ExpressionNode(str_token.value, str_token.location)
            else:
                self._advance()
                str_value = self._unescape_json_string(str_token.value[1:-1])
                return ComplexValueNode(str_value, str_token.location)

        # Check for number literal
        if self._match(DSLTokenType.NUMBER):
            next_token = self._peek_ahead(1)
            if next_token is not None and next_token.type in (DSLTokenType.OPERATOR, DSLTokenType.EQUALS):
                return self._parse_expression()
            num_token = self._advance()
            return ComplexValueNode(float(num_token.value), num_token.location)

        # Check for boolean literal
        if (
            self._match(DSLTokenType.BOOLEAN)
            or (self._match(DSLTokenType.KEYWORD) and token.value in ('true', 'false'))
        ):
            bool_token = self._advance()
            return ComplexValueNode(bool_token.value == 'true', bool_token.location)

        # Check for null literal
        if (
            self._match(DSLTokenType.NULL)
            or (self._match(DSLTokenType.KEYWORD) and token.value == 'null')
        ):
            null_token = self._advance()
            return ComplexValueNode(None, null_token.location)

        # Check for undefined literal
        if self._match(DSLTokenType.IDENTIFIER) and token.value == 'undefined':
            undefined_token = self._advance()
            return ComplexValueNode(None, undefined_token.location)

        # Check for complex value (object or array)
        if self._match(DSLTokenType.LEFT_BRACE):
            next_token = self._peek_ahead(1)
            if next_token is not None and next_token.type != DSLTokenType.LEFT_BRACE:
                return self._parse_complex_value()

        if self._match(DSLTokenType.LEFT_BRACKET):
            return self._parse_complex_value()

        # Otherwise, parse as expression
        return self._parse_expression()

    def _parse_expression(self) -> ExpressionNode:
        """Parse expression (everything until comma, paren, or newline)."""
        start_token = self._peek()
        start_pos = start_token.location.start_pos

        if start_token.type in (
            DSLTokenType.COMMA,
            DSLTokenType.RIGHT_PAREN,
            DSLTokenType.RIGHT_BRACKET,
            DSLTokenType.RIGHT_BRACE,
        ):
            return ExpressionNode('', start_token.location)

        depth = 0
        last_token = start_token
        while not self._match(DSLTokenType.EOF):
            token = self._peek()

            if token.type in (DSLTokenType.LEFT_PAREN, DSLTokenType.LEFT_BRACKET, DSLTokenType.LEFT_BRACE):
                depth += 1
            elif token.type in (DSLTokenType.RIGHT_PAREN, DSLTokenType.RIGHT_BRACKET, DSLTokenType.RIGHT_BRACE):
                if depth == 0:
                    break
                depth -= 1
            elif token.type == DSLTokenType.COMMA and depth == 0:
                break

            last_token = token
            self._advance()

        # Extract exact text from original input
        if self._original_input and start_pos >= 0:
            delimiter_token = self._peek()
            end_pos = delimiter_token.location.start_pos
            if end_pos > start_pos:
                exact_text = self._original_input[start_pos:end_pos]
                return ExpressionNode(exact_text.lstrip(), start_token.location)

        if self._original_input and start_pos >= 0 and last_token.location.end_pos > start_pos:
            exact_text = self._original_input[start_pos:last_token.location.end_pos]
            return ExpressionNode(exact_text.lstrip(), start_token.location)

        return self._reconstruct_expression_from_tokens(start_token, last_token)

    def _reconstruct_expression_from_tokens(
        self, start_token: DSLToken, end_token: DSLToken
    ) -> ExpressionNode:
        """Reconstruct expression text from tokens (fallback)."""
        start_idx = 0
        for i, t in enumerate(self._tokens):
            if t.location.start_pos == start_token.location.start_pos:
                start_idx = i
                break

        expression_text = ''
        depth = 0
        for i in range(start_idx, len(self._tokens)):
            token = self._tokens[i]
            if token.location.start_pos > end_token.location.start_pos:
                break

            if token.type in (DSLTokenType.LEFT_PAREN, DSLTokenType.LEFT_BRACKET, DSLTokenType.LEFT_BRACE):
                depth += 1
            elif token.type in (DSLTokenType.RIGHT_PAREN, DSLTokenType.RIGHT_BRACKET, DSLTokenType.RIGHT_BRACE):
                depth -= 1

            expression_text += token.value

            next_token = self._tokens[i + 1] if i + 1 < len(self._tokens) else None
            if next_token and next_token.location.start_pos <= end_token.location.start_pos:
                if self._needs_space_between(token, next_token, depth):
                    expression_text += ' '

        return ExpressionNode(expression_text.lstrip(), start_token.location)

    def _needs_space_between(self, current: DSLToken, next_tok: DSLToken, depth: int) -> bool:
        """Determine if a space is needed between two tokens in an expression."""
        if current.type == DSLTokenType.LEFT_BRACE and next_tok.type == DSLTokenType.LEFT_BRACE:
            return False
        if current.type == DSLTokenType.RIGHT_BRACE and next_tok.type == DSLTokenType.RIGHT_BRACE:
            return False
        if current.type == DSLTokenType.DOT or next_tok.type == DSLTokenType.DOT:
            return False
        if current.type == DSLTokenType.LEFT_BRACKET or next_tok.type == DSLTokenType.RIGHT_BRACKET:
            return False
        if current.type == DSLTokenType.LEFT_PAREN or next_tok.type == DSLTokenType.RIGHT_PAREN:
            return False
        if current.type == DSLTokenType.LEFT_BRACE or next_tok.type == DSLTokenType.RIGHT_BRACE:
            return False

        if (current.type == DSLTokenType.OPERATOR and current.value == '??') or \
           (next_tok.type == DSLTokenType.OPERATOR and next_tok.value == '??'):
            return False

        if current.type == DSLTokenType.OPERATOR and current.value == '-':
            return False
        if next_tok.type == DSLTokenType.OPERATOR and next_tok.value == '-':
            return False

        if depth > 0:
            arith_ops = {'+', '*', '/', '%'}
            if current.type == DSLTokenType.OPERATOR and current.value in arith_ops:
                return False
            if next_tok.type == DSLTokenType.OPERATOR and next_tok.value in arith_ops:
                return False

        word_types = {DSLTokenType.IDENTIFIER, DSLTokenType.KEYWORD, DSLTokenType.BOOLEAN, DSLTokenType.NUMBER}
        is_current_word = current.type in word_types
        is_next_word = next_tok.type in word_types

        if is_current_word and is_next_word:
            return True

        if current.type == DSLTokenType.EQUALS or next_tok.type == DSLTokenType.EQUALS:
            return True

        if current.type == DSLTokenType.OPERATOR or next_tok.type == DSLTokenType.OPERATOR:
            return True

        return False

    def _parse_schema_spec(self) -> SchemaNode:
        """Parse schema specification."""
        start_token = self._peek()

        if self._match(DSLTokenType.LEFT_BRACE):
            value = self._parse_complex_value()
            return SchemaNode(value.value, start_token.location)

        schema_text = self._parse_simple_schema()
        return SchemaNode(schema_text, start_token.location)

    def _parse_simple_schema(self) -> str:
        """Parse simple schema syntax (INTEGER, ARRAY OF INTEGER, etc.)."""
        if self._match(DSLTokenType.KEYWORD, 'ARRAY'):
            self._advance()
            self._expect(DSLTokenType.KEYWORD, 'OF')
            inner_schema = self._parse_simple_schema()
            return f"ARRAY OF {inner_schema}"

        if self._match(DSLTokenType.KEYWORD, 'OBJECT'):
            return self._advance().value

        token = self._peek()
        if token.type == DSLTokenType.KEYWORD:
            return self._advance().value

        raise DSLParserError(
            'Expected schema type',
            token.location,
            ['INTEGER', 'LONG', 'FLOAT', 'DOUBLE', 'STRING', 'BOOLEAN', 'NULL', 'ANY', 'ARRAY', 'OBJECT'],
            token,
        )

    def _parse_schema_literal(self) -> SchemaLiteralNode:
        """Parse schema literal: (SchemaSpec) WITH DEFAULT VALUE expr."""
        start_token = self._peek()
        self._expect(DSLTokenType.LEFT_PAREN)
        schema = self._parse_schema_spec()
        self._expect(DSLTokenType.RIGHT_PAREN)

        default_value: Optional[ExpressionNode] = None

        if self._match(DSLTokenType.KEYWORD, 'WITH'):
            self._advance()
            self._expect(DSLTokenType.KEYWORD, 'DEFAULT')
            self._expect(DSLTokenType.KEYWORD, 'VALUE')
            default_value = self._parse_expression()

        return SchemaLiteralNode(schema, default_value, start_token.location)

    def _parse_complex_value(self) -> ComplexValueNode:
        """Parse complex value (JSON object or array)."""
        start_token = self._peek()

        if self._match(DSLTokenType.LEFT_BRACE):
            return ComplexValueNode(self._parse_object(), start_token.location)

        if self._match(DSLTokenType.LEFT_BRACKET):
            return ComplexValueNode(self._parse_array(), start_token.location)

        raise DSLParserError('Expected { or [', start_token.location, ['{', '['], start_token)

    def _parse_object(self) -> dict:
        """Parse JSON object."""
        self._expect(DSLTokenType.LEFT_BRACE)
        obj: dict = {}

        if self._match(DSLTokenType.RIGHT_BRACE):
            self._advance()
            return obj

        while True:
            if self._match(DSLTokenType.STRING):
                str_token = self._advance()
                key = str_token.value[1:-1]
            else:
                key = self._expect_identifier()

            self._expect(DSLTokenType.COLON)
            obj[key] = self._parse_json_value()

            if self._match(DSLTokenType.COMMA):
                self._advance()
            else:
                break

            if self._match(DSLTokenType.RIGHT_BRACE) or self._match(DSLTokenType.EOF):
                break

        self._expect(DSLTokenType.RIGHT_BRACE)
        return obj

    def _parse_array(self) -> list:
        """Parse JSON array."""
        self._expect(DSLTokenType.LEFT_BRACKET)
        arr: list = []

        if self._match(DSLTokenType.RIGHT_BRACKET):
            self._advance()
            return arr

        while True:
            arr.append(self._parse_json_value())

            if self._match(DSLTokenType.COMMA):
                self._advance()
            else:
                break

            if self._match(DSLTokenType.RIGHT_BRACKET) or self._match(DSLTokenType.EOF):
                break

        self._expect(DSLTokenType.RIGHT_BRACKET)
        return arr

    def _parse_json_value(self) -> Any:
        """Parse JSON value."""
        token = self._peek()

        if token.type == DSLTokenType.STRING:
            str_token = self._advance()
            return self._unescape_json_string(str_token.value[1:-1])

        if token.type == DSLTokenType.NUMBER:
            num_token = self._advance()
            return float(num_token.value)

        if token.type == DSLTokenType.BOOLEAN or \
           (token.type == DSLTokenType.KEYWORD and token.value in ('true', 'false')):
            bool_token = self._advance()
            return bool_token.value == 'true'

        if token.type == DSLTokenType.NULL or \
           (token.type == DSLTokenType.KEYWORD and token.value == 'null'):
            self._advance()
            return None

        if token.type == DSLTokenType.IDENTIFIER and token.value == 'undefined':
            self._advance()
            return None

        if token.type == DSLTokenType.LEFT_BRACE:
            return self._parse_object()

        if token.type == DSLTokenType.LEFT_BRACKET:
            return self._parse_array()

        expr = self._parse_expression()
        return {'isExpression': True, 'value': expr.expression_text}

    # ===== Helper Methods =====

    def _peek(self) -> DSLToken:
        """Peek at current token."""
        return self._tokens[self._current]

    def _peek_ahead(self, n: int) -> Optional[DSLToken]:
        """Peek ahead n tokens."""
        idx = self._current + n
        if idx < len(self._tokens):
            return self._tokens[idx]
        return None

    def _advance(self) -> DSLToken:
        """Advance to next token and return current."""
        token = self._tokens[self._current]
        self._current += 1
        return token

    def _match(self, type: DSLTokenType, value: Optional[str] = None) -> bool:
        """Check if current token matches type and optional value."""
        token = self._peek()
        if token is None:
            return False
        if token.type != type:
            return False
        if value is not None and token.value != value:
            return False
        return True

    def _expect(self, type: DSLTokenType, value: Optional[str] = None) -> DSLToken:
        """Expect a specific token type and value, raise error if not found."""
        token = self._peek()

        if not self._match(type, value):
            expected = f"{type.value} ({value})" if value else type.value
            raise DSLParserError(f"Expected {expected}", token.location, [expected], token)

        return self._advance()

    def _expect_identifier(self) -> str:
        """
        Expect an identifier token.
        Also accepts BOOLEAN and KEYWORD tokens as identifiers since they're used
        as nested block names in if/loop statements.
        """
        token = self._peek()
        if token.type == DSLTokenType.IDENTIFIER:
            return self._advance().value
        if token.type == DSLTokenType.BOOLEAN:
            return self._advance().value
        if token.type == DSLTokenType.KEYWORD:
            return self._advance().value
        expected = self._expect(DSLTokenType.IDENTIFIER)
        return expected.value

    def _expect_dotted_identifier(self) -> str:
        """Expect a dotted identifier (e.g., System.Math.Add)."""
        name = self._expect_identifier()

        while self._match(DSLTokenType.DOT):
            self._advance()
            name += '.' + self._expect_identifier()

        return name

    def _expect_step_reference(self) -> str:
        """Expect a step reference (e.g., Steps.create.output)."""
        return self._expect_dotted_identifier()

    @staticmethod
    def _unescape_json_string(s: str) -> str:
        """
        Unescape JSON string escape sequences.
        Handles: \\n, \\r, \\t, \\\\, \\", \\/, \\b, \\f, \\uXXXX
        """
        result = ''
        i = 0
        while i < len(s):
            if s[i] == '\\' and i + 1 < len(s):
                next_ch = s[i + 1]
                if next_ch == 'n':
                    result += '\n'
                    i += 2
                elif next_ch == 'r':
                    result += '\r'
                    i += 2
                elif next_ch == 't':
                    result += '\t'
                    i += 2
                elif next_ch == 'b':
                    result += '\b'
                    i += 2
                elif next_ch == 'f':
                    result += '\f'
                    i += 2
                elif next_ch == '\\':
                    result += '\\'
                    i += 2
                elif next_ch == '"':
                    result += '"'
                    i += 2
                elif next_ch == '/':
                    result += '/'
                    i += 2
                elif next_ch == 'u':
                    if i + 5 < len(s):
                        hex_str = s[i + 2:i + 6]
                        if re.match(r'^[0-9a-fA-F]{4}$', hex_str):
                            result += chr(int(hex_str, 16))
                            i += 6
                            continue
                    result += s[i]
                    i += 1
                else:
                    result += s[i]
                    i += 1
            else:
                result += s[i]
                i += 1
        return result
