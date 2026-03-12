from __future__ import annotations

from typing import Optional, List

from kirun_py.util.linked_list import LinkedList
from kirun_py.util.string.string_builder import StringBuilder
from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.util.string.string_util import StringUtil
from kirun_py.util.tuples import Tuple2
from kirun_py.runtime.expression.exception.expression_evaluation_exception import ExpressionEvaluationException
from kirun_py.runtime.expression.expression_token import ExpressionToken
from kirun_py.runtime.expression.expression_token_value import ExpressionTokenValue
from kirun_py.runtime.expression.operation import Operation


class Expression(ExpressionToken):

    @staticmethod
    def create_ternary(
        condition: ExpressionToken,
        true_expr: ExpressionToken,
        false_expr: ExpressionToken,
    ) -> Expression:
        """Create a ternary expression with condition, true_expr, and false_expr.

        Using push() to maintain compatibility with evaluator (push adds to head).
        Tokens will be in order: [false_expr, true_expr, condition] from head to tail.
        """
        expr = Expression('', skip_parsing=True)
        expr._tokens.push(condition)
        expr._tokens.push(true_expr)
        expr._tokens.push(false_expr)
        expr._ops.push(Operation.CONDITIONAL_TERNARY_OPERATOR)
        return expr

    @staticmethod
    def create_leaf(value: str) -> Expression:
        """Create a leaf expression (identifier/number) without re-parsing.

        Used by the parser for leaf nodes.
        """
        expr = Expression('', skip_parsing=True)
        expr.expression = value
        expr._tokens.push(ExpressionToken(value))
        return expr

    def __init__(
        self,
        expression: Optional[str] = None,
        l: Optional[ExpressionToken] = None,
        r: Optional[ExpressionToken] = None,
        op: Optional[Operation] = None,
        skip_parsing: bool = False,
    ):
        super().__init__(expression if expression else '')

        # Data structure for storing tokens
        self._tokens: LinkedList[ExpressionToken] = LinkedList()
        # Data structure for storing operations
        self._ops: LinkedList[Operation] = LinkedList()

        # Cached arrays for fast evaluation (avoids LinkedList traversal)
        self._cached_tokens_array: Optional[List[ExpressionToken]] = None
        self._cached_ops_array: Optional[List[Operation]] = None

        # If skip_parsing is True, don't do anything (used by create_leaf/create_ternary)
        if skip_parsing:
            return

        # If we have left/right tokens and operation, construct directly (for AST building)
        if l is not None or r is not None or op is not None:
            if op is not None and op.get_operator() == '..':
                if l is None:
                    l = ExpressionTokenValue('', '')
                elif r is None:
                    r = ExpressionTokenValue('', '')
            if l is not None:
                self._tokens.push(l)
            if r is not None:
                self._tokens.push(r)
            if op is not None:
                self._ops.push(op)
            return

        # If we have an expression string, use the new parser
        if expression and expression.strip():
            try:
                from kirun_py.runtime.expression.expression_parser import ExpressionParser
                trimmed_expression = expression.strip()
                parser = ExpressionParser(trimmed_expression)
                parsed = parser.parse()
                self.expression = trimmed_expression
                self._tokens = parsed.get_tokens()
                self._ops = parsed.get_operations()
                self._cached_tokens_array = None
                self._cached_ops_array = None
            except Exception:
                # Fall back to old parser if new one fails
                self._evaluate()
                if (not self._ops.is_empty() and
                        self._ops.peek_last().get_operator() == '..' and
                        self._tokens.length == 1):
                    self._tokens.push(ExpressionToken(''))

    def get_tokens(self) -> LinkedList[ExpressionToken]:
        return self._tokens

    def get_operations(self) -> LinkedList[Operation]:
        return self._ops

    def get_tokens_array(self) -> List[ExpressionToken]:
        """Fast array access for evaluation (cached)."""
        if self._cached_tokens_array is None:
            self._cached_tokens_array = self._tokens.to_array()
        return self._cached_tokens_array

    def get_operations_array(self) -> List[Operation]:
        """Fast array access for evaluation (cached)."""
        if self._cached_ops_array is None:
            self._cached_ops_array = self._ops.to_array()
        return self._cached_ops_array

    # ---- Legacy evaluate (old parser) ----

    def _evaluate(self) -> None:
        length = len(self.expression)
        chr_ = ''

        sb = StringBuilder('')
        buff: Optional[str] = None
        i = 0
        is_prev_op = False

        while i < length:
            chr_ = self.expression[i]
            buff = str(sb)

            if chr_ == ' ':
                is_prev_op = self._process_token_separator(sb, buff, is_prev_op)
            elif chr_ == '(':
                i = self._process_sub_expression(length, sb, buff, i, is_prev_op)
                is_prev_op = False
            elif chr_ == ')':
                raise ExpressionEvaluationException(
                    self.expression,
                    'Extra closing parenthesis found',
                )
            elif chr_ == ']':
                raise ExpressionEvaluationException(
                    self.expression,
                    'Extra closing square bracket found',
                )
            elif chr_ == "'" or chr_ == '"':
                # If inside a bracket (ARRAY_OPERATOR was just added)
                if is_prev_op and self._ops.peek() == Operation.ARRAY_OPERATOR:
                    result = self._process(length, sb, i)
                    i = result.get_t1()
                    is_prev_op = result.get_t2()
                else:
                    result = self._process_string_literal(length, chr_, i)
                    i = result.get_t1()
                    is_prev_op = result.get_t2()
            elif chr_ == '?':
                if (i + 1 < length and
                        self.expression[i + 1] != '?' and
                        i != 0 and
                        self.expression[i - 1] != '?'):
                    i = self._process_ternary_operator(length, sb, buff, i, is_prev_op)
                else:
                    result = self._process_others(chr_, length, sb, buff, i, is_prev_op)
                    i = result.get_t1()
                    is_prev_op = result.get_t2()
                    if is_prev_op and self._ops.peek() == Operation.ARRAY_OPERATOR:
                        result = self._process(length, sb, i)
                        i = result.get_t1()
                        is_prev_op = result.get_t2()
            else:
                result = self._process_others(chr_, length, sb, buff, i, is_prev_op)
                i = result.get_t1()
                is_prev_op = result.get_t2()
                if is_prev_op and self._ops.peek() == Operation.ARRAY_OPERATOR:
                    result = self._process(length, sb, i)
                    i = result.get_t1()
                    is_prev_op = result.get_t2()

            i += 1

        buff = str(sb)
        if not StringUtil.is_null_or_blank(buff):
            if buff in Operation.OPERATORS:
                raise ExpressionEvaluationException(
                    self.expression,
                    'Expression is ending with an operator',
                )
            else:
                self._tokens.push(ExpressionToken(buff))

    def _process_string_literal(self, length: int, chr_: str, i: int) -> Tuple2[int, bool]:
        str_constant = ''

        j = i + 1
        while j < length:
            next_char = self.expression[j]
            if next_char == chr_ and self.expression[j - 1] != '\\':
                break
            str_constant += next_char
            j += 1

        if j == length and self.expression[j - 1] != chr_:
            raise ExpressionEvaluationException(
                self.expression,
                'Missing string ending marker ' + chr_,
            )

        result = Tuple2(j, False)
        self._tokens.push(ExpressionTokenValue(str_constant, str_constant))
        return result

    def _process(self, length: int, sb: StringBuilder, i: int) -> Tuple2[int, bool]:
        cnt = 1
        i += 1
        while i < length and cnt != 0:
            c = self.expression[i]
            if c == ']':
                cnt -= 1
            elif c == '[':
                cnt += 1
            if cnt != 0:
                sb.append(c)
                i += 1

        self._tokens.push(Expression(str(sb)))
        sb.set_length(0)

        return Tuple2(i, False)

    def _process_others(
        self, chr_: str, length: int, sb: StringBuilder,
        buff: str, i: int, is_prev_op: bool,
    ) -> Tuple2[int, bool]:
        start = length - i
        start = start if start < Operation.BIGGEST_OPERATOR_SIZE else Operation.BIGGEST_OPERATOR_SIZE

        for size in range(start, 0, -1):
            op = self.expression[i:i + size]
            if op in Operation.OPERATORS_WITHOUT_SPACE_WRAP:
                if not StringUtil.is_null_or_blank(buff):
                    self._tokens.push(ExpressionToken(buff))
                    is_prev_op = False
                elif op == '..' and self._tokens.is_empty():
                    self._tokens.push(ExpressionToken('0'))
                    is_prev_op = False
                self._check_unary_operator(
                    self._tokens,
                    self._ops,
                    Operation.OPERATION_VALUE_OF.get(op),
                    is_prev_op,
                )
                is_prev_op = True
                sb.set_length(0)
                return Tuple2(i + size - 1, is_prev_op)

        sb.append(chr_)
        return Tuple2(i, False)

    def _process_ternary_operator(
        self, length: int, sb: StringBuilder,
        buff: str, i: int, is_prev_op: bool,
    ) -> int:
        if is_prev_op:
            raise ExpressionEvaluationException(
                self.expression,
                'Ternary operator is followed by an operator',
            )

        if buff.strip() != '':
            self._tokens.push(Expression(buff))
            sb.set_length(0)

        i += 1
        cnt = 1
        in_chr = ''
        start = i
        while i < length and cnt > 0:
            in_chr = self.expression[i]
            if in_chr == '?':
                cnt += 1
            elif in_chr == ':':
                cnt -= 1
            i += 1

        if in_chr != ':':
            raise ExpressionEvaluationException(
                self.expression, "':' operater is missing",
            )

        if i >= length:
            raise ExpressionEvaluationException(
                self.expression,
                'Third part of the ternary expression is missing',
            )

        while (not self._ops.is_empty() and
               self._has_precedence(Operation.CONDITIONAL_TERNARY_OPERATOR, self._ops.peek())):
            prev = self._ops.pop()

            if prev in Operation.UNARY_OPERATORS:
                l = self._tokens.pop()
                self._tokens.push(Expression('', l, None, prev))
            else:
                r = self._tokens.pop()
                l = self._tokens.pop()
                self._tokens.push(Expression('', l, r, prev))

        self._ops.push(Operation.CONDITIONAL_TERNARY_OPERATOR)
        self._tokens.push(Expression(self.expression[start:i - 1]))

        second_exp = self.expression[i:]
        if second_exp.strip() == '':
            raise ExpressionEvaluationException(
                self.expression,
                'Third part of the ternary expression is missing',
            )

        self._tokens.push(Expression(second_exp))

        return length - 1

    def _process_sub_expression(
        self, length: int, sb: StringBuilder,
        buff: str, i: int, is_prev_op: bool,
    ) -> int:
        if buff in Operation.OPERATORS:
            self._check_unary_operator(
                self._tokens,
                self._ops,
                Operation.OPERATION_VALUE_OF.get(buff),
                is_prev_op,
            )
            sb.set_length(0)
        elif not StringUtil.is_null_or_blank(buff):
            raise ExpressionEvaluationException(
                self.expression,
                StringFormatter.format('Unkown token : $ found.', buff),
            )

        cnt = 1
        sub_exp = StringBuilder()
        in_chr = self.expression[i]
        i += 1
        while i < length and cnt > 0:
            in_chr = self.expression[i]
            if in_chr == '(':
                cnt += 1
            elif in_chr == ')':
                cnt -= 1
            if cnt != 0:
                sub_exp.append(in_chr)
                i += 1

        if in_chr != ')':
            raise ExpressionEvaluationException(
                self.expression,
                'Missing a closed parenthesis',
            )

        # Only remove outer parentheses if they actually match
        while sub_exp.length() > 2 and self._has_matching_outer_parentheses(str(sub_exp)):
            sub_exp.delete_char_at(0)
            sub_exp.set_length(sub_exp.length() - 1)

        self._tokens.push(Expression(str(sub_exp).strip()))
        return i

    def _process_token_separator(
        self, sb: StringBuilder, buff: str, is_prev_op: bool,
    ) -> bool:
        if not StringUtil.is_null_or_blank(buff):
            if buff in Operation.OPERATORS:
                self._check_unary_operator(
                    self._tokens,
                    self._ops,
                    Operation.OPERATION_VALUE_OF.get(buff),
                    is_prev_op,
                )
                is_prev_op = True
            else:
                self._tokens.push(ExpressionToken(buff))
                is_prev_op = False
        sb.set_length(0)

        return is_prev_op

    def _check_unary_operator(
        self,
        tokens: LinkedList[ExpressionToken],
        ops: LinkedList[Operation],
        op: Optional[Operation],
        is_prev_op: bool,
    ) -> None:
        if op is None:
            return
        if is_prev_op or tokens.is_empty():
            if op in Operation.UNARY_OPERATORS:
                x = Operation.UNARY_MAP.get(op)
                if x is not None:
                    ops.push(x)
            else:
                raise ExpressionEvaluationException(
                    self.expression,
                    StringFormatter.format('Extra operator $ found.', op),
                )
        else:
            while not ops.is_empty() and self._has_precedence(op, ops.peek()):
                prev = ops.pop()

                if prev in Operation.UNARY_OPERATORS:
                    l = tokens.pop()
                    tokens.push(Expression('', l, None, prev))
                else:
                    r = tokens.pop()
                    l = tokens.pop()
                    tokens.push(Expression('', l, r, prev))
            ops.push(op)

    def _has_precedence(self, op1: Operation, op2: Operation) -> bool:
        pre1 = Operation.OPERATOR_PRIORITY.get(op1)
        pre2 = Operation.OPERATOR_PRIORITY.get(op2)
        if pre1 is None or pre2 is None:
            raise RuntimeError('Unknown operators provided')
        if pre2 == pre1:
            return (op2 != Operation.OBJECT_OPERATOR and
                    op2 != Operation.ARRAY_OPERATOR and
                    op1 != Operation.OBJECT_OPERATOR and
                    op1 != Operation.ARRAY_OPERATOR)
        return pre2 < pre1

    def _has_matching_outer_parentheses(self, s: str) -> bool:
        if len(s) < 2 or s[0] != '(' or s[-1] != ')':
            return False
        level = 0
        for i in range(len(s) - 1):
            ch = s[i]
            if ch == '(':
                level += 1
            elif ch == ')':
                level -= 1
            if level == 0:
                return False
        return level == 1

    # ---- toString ----

    def __str__(self) -> str:
        # Leaf node: no operations, just return the token
        if self._ops.is_empty():
            if self._tokens.size() == 1:
                return self._token_to_string(self._tokens.get(0))
            if self.expression and len(self.expression) > 0:
                return self.expression
            return 'Error: No tokens'

        op = self._ops.get(0)

        # Unary operation
        if op.get_operator().startswith('UN: '):
            return self._format_unary_operation(op)

        # Ternary operation
        if op == Operation.CONDITIONAL_TERNARY_OPERATOR:
            return self._format_ternary_operation()

        # Binary operation
        return self._format_binary_operation(op)

    def _format_unary_operation(self, op: Operation) -> str:
        operand = self._tokens.get(0)
        operand_str = self._token_to_string(operand)
        return '(' + op.get_operator()[4:] + operand_str + ')'

    def _format_ternary_operation(self) -> str:
        # With push() order: get(0)=false_expr, get(1)=true_expr, get(2)=condition
        false_expr = self._tokens.get(0)
        true_expr = self._tokens.get(1)
        condition = self._tokens.get(2)

        condition_str = self._token_to_string(condition)
        true_str = self._token_to_string(true_expr)
        false_str = self._token_to_string(false_expr)

        return '(' + condition_str + '?' + true_str + ':' + false_str + ')'

    def _format_binary_operation(self, op: Operation) -> str:
        # Safety check
        if self._tokens.size() < 2:
            if self.expression and len(self.expression) > 0:
                return self.expression
            if self._tokens.size() == 1:
                return self._token_to_string(self._tokens.get(0))
            return 'Error: Invalid binary expression'

        # With push() order: get(0)=right, get(1)=left
        right = self._tokens.get(0)
        left = self._tokens.get(1)

        # ARRAY_OPERATOR: left[index]
        if op == Operation.ARRAY_OPERATOR:
            left_str = self._token_to_string(left)
            index_str = self._format_array_index(right)
            return left_str + '[' + index_str + ']'

        # OBJECT_OPERATOR: left.right
        if op == Operation.OBJECT_OPERATOR:
            left_str = self._token_to_string(left)
            right_str = self._token_to_string(right)

            if isinstance(right, Expression):
                right_ops = right.get_operations()
                if not right_ops.is_empty():
                    right_op = right_ops.get(0)
                    if right_op == Operation.ARRAY_OPERATOR:
                        return '(' + left_str + '.(' + right_str + '))'
                    else:
                        return '(' + left_str + '.' + right_str + ')'

            return '(' + left_str + '.' + right_str + ')'

        # ARRAY_RANGE_INDEX_OPERATOR: (start..end)
        if op == Operation.ARRAY_RANGE_INDEX_OPERATOR:
            left_str = self._token_to_string(left)
            right_str = self._token_to_string(right)
            return '(' + left_str + '..' + right_str + ')'

        # Other binary operators: (left op right)
        left_str = self._token_to_string(left)
        right_str = self._token_to_string(right)
        return '(' + left_str + op.get_operator() + right_str + ')'

    def _token_to_string(self, token: ExpressionToken) -> str:
        if isinstance(token, Expression):
            return str(token)

        # Check if token is an ExpressionTokenValue
        if isinstance(token, ExpressionTokenValue):
            original_expr = token.get_expression()
            if original_expr and (original_expr.startswith('"') or original_expr.startswith("'")):
                return original_expr

        return str(token)

    def _format_array_index(self, token: ExpressionToken) -> str:
        if isinstance(token, Expression):
            expr = token
            if expr.get_operations().is_empty() and expr.get_tokens().size() == 1:
                inner_token = expr.get_tokens().get(0)
                if isinstance(inner_token, ExpressionTokenValue):
                    original_expr = inner_token.get_expression()
                    if original_expr and (original_expr.startswith('"') or original_expr.startswith("'")):
                        return original_expr
            return str(expr)

        if isinstance(token, ExpressionTokenValue):
            original_expr = token.get_expression()
            if original_expr and (original_expr.startswith('"') or original_expr.startswith("'")):
                return original_expr

        return str(token)

    def _token_has_operations(self, token: ExpressionToken) -> bool:
        if isinstance(token, Expression):
            return not token.get_operations().is_empty()
        return False

    def equals(self, o: Expression) -> bool:
        return self.expression == o.expression

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Expression):
            return NotImplemented
        return self.expression == other.expression

    def __hash__(self) -> int:
        return hash(self.expression)
