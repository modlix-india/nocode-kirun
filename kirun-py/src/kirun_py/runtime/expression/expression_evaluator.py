from __future__ import annotations
import re
from typing import Any, Dict, List, Optional, Set

from kirun_py.exception.execution_exception import ExecutionException
from kirun_py.util.string.string_formatter import StringFormatter
from kirun_py.util.tuples import Tuple2
from kirun_py.runtime.expression.exception.expression_evaluation_exception import ExpressionEvaluationException
from kirun_py.runtime.expression.expression_token import ExpressionToken
from kirun_py.runtime.expression.expression_token_value import ExpressionTokenValue
from kirun_py.runtime.expression.operation import Operation

from kirun_py.runtime.expression.operators.binary.binary_operator import BinaryOperator
from kirun_py.runtime.expression.operators.binary.arithmetic_addition_operator import ArithmeticAdditionOperator
from kirun_py.runtime.expression.operators.binary.arithmetic_subtraction_operator import ArithmeticSubtractionOperator
from kirun_py.runtime.expression.operators.binary.arithmetic_multiplication_operator import ArithmeticMultiplicationOperator
from kirun_py.runtime.expression.operators.binary.arithmetic_division_operator import ArithmeticDivisionOperator
from kirun_py.runtime.expression.operators.binary.arithmetic_integer_division_operator import ArithmeticIntegerDivisionOperator
from kirun_py.runtime.expression.operators.binary.arithmetic_modulus_operator import ArithmeticModulusOperator
from kirun_py.runtime.expression.operators.binary.bitwise_and_operator import BitwiseAndOperator
from kirun_py.runtime.expression.operators.binary.bitwise_or_operator import BitwiseOrOperator
from kirun_py.runtime.expression.operators.binary.bitwise_xor_operator import BitwiseXorOperator
from kirun_py.runtime.expression.operators.binary.bitwise_left_shift_operator import BitwiseLeftShiftOperator
from kirun_py.runtime.expression.operators.binary.bitwise_right_shift_operator import BitwiseRightShiftOperator
from kirun_py.runtime.expression.operators.binary.bitwise_unsigned_right_shift_operator import BitwiseUnsignedRightShiftOperator
from kirun_py.runtime.expression.operators.binary.logical_and_operator import LogicalAndOperator
from kirun_py.runtime.expression.operators.binary.logical_or_operator import LogicalOrOperator
from kirun_py.runtime.expression.operators.binary.logical_equal_operator import LogicalEqualOperator
from kirun_py.runtime.expression.operators.binary.logical_not_equal_operator import LogicalNotEqualOperator
from kirun_py.runtime.expression.operators.binary.logical_greater_than_operator import LogicalGreaterThanOperator
from kirun_py.runtime.expression.operators.binary.logical_greater_than_equal_operator import LogicalGreaterThanEqualOperator
from kirun_py.runtime.expression.operators.binary.logical_less_than_operator import LogicalLessThanOperator
from kirun_py.runtime.expression.operators.binary.logical_less_than_equal_operator import LogicalLessThanEqualOperator
from kirun_py.runtime.expression.operators.binary.logical_nullish_coalescing_operator import LogicalNullishCoalescingOperator
from kirun_py.runtime.expression.operators.binary.array_operator import ArrayOperator
from kirun_py.runtime.expression.operators.binary.array_range_operator import ArrayRangeOperator
from kirun_py.runtime.expression.operators.binary.object_operator import ObjectOperator

from kirun_py.runtime.expression.operators.unary.unary_operator import UnaryOperator
from kirun_py.runtime.expression.operators.unary.arithmetic_unary_plus_operator import ArithmeticUnaryPlusOperator
from kirun_py.runtime.expression.operators.unary.arithmetic_unary_minus_operator import ArithmeticUnaryMinusOperator
from kirun_py.runtime.expression.operators.unary.logical_not_operator import LogicalNotOperator
from kirun_py.runtime.expression.operators.unary.bitwise_complement_operator import BitwiseComplementOperator

from kirun_py.runtime.expression.operators.ternary.ternary_operator import TernaryOperator
from kirun_py.runtime.expression.operators.ternary.conditional_ternary_operator import ConditionalTernaryOperator

from kirun_py.runtime.expression.tokenextractor.literal_token_value_extractor import LiteralTokenValueExtractor
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor
from kirun_py.runtime.expression.tokenextractor.expression_internal_value_extractor import ExpressionInternalValueExtractor


class ExpressionEvaluator:
    _expression_cache: Dict[str, Any] = {}
    _logged_error_keys: Set[str] = set()
    _key_counter: int = 0
    _pattern_cache: Dict[str, int] = {}

    PATTERN_UNKNOWN = 0
    PATTERN_LITERAL = 1
    PATTERN_SIMPLE_PATH = 2
    PATTERN_SIMPLE_ARRAY_ACCESS = 3
    PATTERN_SIMPLE_COMPARISON = 4
    PATTERN_SIMPLE_TERNARY = 5

    NUMBER_REGEX = re.compile(r'^-?\d+(\.\d+)?$')
    SINGLE_QUOTE_STRING_REGEX = re.compile(r"^'([^'\\]|\\.)*'$")
    DOUBLE_QUOTE_STRING_REGEX = re.compile(r'^"([^"\\]|\\.)*"$')

    UNARY_OPERATORS_MAP: Dict[Operation, UnaryOperator] = {}
    TERNARY_OPERATORS_MAP: Dict[Operation, TernaryOperator] = {}
    BINARY_OPERATORS_MAP: Dict[Operation, BinaryOperator] = {}
    UNARY_OPERATORS_MAP_KEY_SET: Set[Operation] = set()

    _initialized = False

    @classmethod
    def _init_maps(cls):
        if cls._initialized:
            return
        cls._initialized = True

        cls.UNARY_OPERATORS_MAP = {
            Operation.UNARY_BITWISE_COMPLEMENT: BitwiseComplementOperator(),
            Operation.UNARY_LOGICAL_NOT: LogicalNotOperator(),
            Operation.UNARY_MINUS: ArithmeticUnaryMinusOperator(),
            Operation.UNARY_PLUS: ArithmeticUnaryPlusOperator(),
        }
        cls.TERNARY_OPERATORS_MAP = {
            Operation.CONDITIONAL_TERNARY_OPERATOR: ConditionalTernaryOperator(),
        }
        cls.BINARY_OPERATORS_MAP = {
            Operation.ADDITION: ArithmeticAdditionOperator(),
            Operation.DIVISION: ArithmeticDivisionOperator(),
            Operation.INTEGER_DIVISION: ArithmeticIntegerDivisionOperator(),
            Operation.MOD: ArithmeticModulusOperator(),
            Operation.MULTIPLICATION: ArithmeticMultiplicationOperator(),
            Operation.SUBTRACTION: ArithmeticSubtractionOperator(),
            Operation.BITWISE_AND: BitwiseAndOperator(),
            Operation.BITWISE_LEFT_SHIFT: BitwiseLeftShiftOperator(),
            Operation.BITWISE_OR: BitwiseOrOperator(),
            Operation.BITWISE_RIGHT_SHIFT: BitwiseRightShiftOperator(),
            Operation.BITWISE_UNSIGNED_RIGHT_SHIFT: BitwiseUnsignedRightShiftOperator(),
            Operation.BITWISE_XOR: BitwiseXorOperator(),
            Operation.AND: LogicalAndOperator(),
            Operation.EQUAL: LogicalEqualOperator(),
            Operation.GREATER_THAN: LogicalGreaterThanOperator(),
            Operation.GREATER_THAN_EQUAL: LogicalGreaterThanEqualOperator(),
            Operation.LESS_THAN: LogicalLessThanOperator(),
            Operation.LESS_THAN_EQUAL: LogicalLessThanEqualOperator(),
            Operation.OR: LogicalOrOperator(),
            Operation.NOT_EQUAL: LogicalNotEqualOperator(),
            Operation.NULLISH_COALESCING_OPERATOR: LogicalNullishCoalescingOperator(),
            Operation.ARRAY_OPERATOR: ArrayOperator(),
            Operation.ARRAY_RANGE_INDEX_OPERATOR: ArrayRangeOperator(),
            Operation.OBJECT_OPERATOR: ObjectOperator(),
        }
        cls.UNARY_OPERATORS_MAP_KEY_SET = set(cls.UNARY_OPERATORS_MAP.keys())

    @staticmethod
    def _get_cached_expression(expression_string: str):
        from kirun_py.runtime.expression.expression import Expression
        exp = ExpressionEvaluator._expression_cache.get(expression_string)
        if exp is None:
            exp = Expression(expression_string)
            ExpressionEvaluator._expression_cache[expression_string] = exp
        return exp

    @staticmethod
    def _detect_pattern(exp) -> int:
        exp_str = exp.get_expression()
        cached = ExpressionEvaluator._pattern_cache.get(exp_str)
        if cached is not None:
            return cached

        pattern = ExpressionEvaluator.PATTERN_UNKNOWN

        if (exp_str == 'true' or exp_str == 'false' or
                exp_str == 'null' or exp_str == 'undefined' or
                ExpressionEvaluator.NUMBER_REGEX.match(exp_str) or
                ExpressionEvaluator.SINGLE_QUOTE_STRING_REGEX.match(exp_str) or
                ExpressionEvaluator.DOUBLE_QUOTE_STRING_REGEX.match(exp_str)):
            pattern = ExpressionEvaluator.PATTERN_LITERAL
        elif '.' in exp_str and '{{' not in exp_str and '..' not in exp_str:
            from kirun_py.runtime.expression.expression import Expression
            ops = exp.get_operations_array()
            tokens = exp.get_tokens_array()
            is_simple_path = len(ops) > 0
            for op in ops:
                if op is not Operation.OBJECT_OPERATOR and op is not Operation.ARRAY_OPERATOR:
                    is_simple_path = False
                    break
            if is_simple_path:
                for token in tokens:
                    if isinstance(token, Expression):
                        is_simple_path = False
                        break
            if is_simple_path:
                pattern = ExpressionEvaluator.PATTERN_SIMPLE_PATH
            else:
                pattern = ExpressionEvaluator._detect_ternary_or_comparison(exp, ops)
        elif '..' in exp_str:
            pattern = ExpressionEvaluator.PATTERN_UNKNOWN
        elif '{{' not in exp_str:
            ops = exp.get_operations_array()
            pattern = ExpressionEvaluator._detect_ternary_or_comparison(exp, ops)

        ExpressionEvaluator._pattern_cache[exp_str] = pattern
        return pattern

    @staticmethod
    def _detect_ternary_or_comparison(exp, ops: list) -> int:
        from kirun_py.runtime.expression.expression import Expression
        tokens = exp.get_tokens_array()
        for token in tokens:
            if isinstance(token, Expression):
                return ExpressionEvaluator.PATTERN_UNKNOWN
        if len(ops) == 1 and ops[0] is Operation.CONDITIONAL_TERNARY_OPERATOR:
            return ExpressionEvaluator.PATTERN_SIMPLE_TERNARY
        if len(ops) == 1 and (ops[0] is Operation.EQUAL or ops[0] is Operation.NOT_EQUAL):
            return ExpressionEvaluator.PATTERN_SIMPLE_COMPARISON
        return ExpressionEvaluator.PATTERN_UNKNOWN

    @staticmethod
    def _evaluate_literal(exp_str: str) -> Any:
        if exp_str == 'true':
            return True
        if exp_str == 'false':
            return False
        if exp_str == 'null':
            return None
        if exp_str == 'undefined':
            return None
        if ExpressionEvaluator.NUMBER_REGEX.match(exp_str):
            return float(exp_str) if '.' in exp_str else int(exp_str)
        if ExpressionEvaluator.SINGLE_QUOTE_STRING_REGEX.match(exp_str):
            return exp_str[1:-1]
        if ExpressionEvaluator.DOUBLE_QUOTE_STRING_REGEX.match(exp_str):
            return exp_str[1:-1]
        return None

    def __init__(self, exp):
        ExpressionEvaluator._init_maps()
        from kirun_py.runtime.expression.expression import Expression
        if isinstance(exp, Expression):
            self._exp = exp
            self._expression = self._exp.get_expression()
        else:
            self._expression = exp
            self._exp = None
        self._internal_token_value_extractor = ExpressionInternalValueExtractor()

    def evaluate(self, values_map: Dict[str, TokenValueExtractor]) -> Any:
        try:
            for extractor in values_map.values():
                extractor.set_values_map(values_map)

            tup = self._process_nesting_expression(self._expression, values_map)
            expanded_expression = tup.get_t1()
            expanded_exp = tup.get_t2()

            pattern = ExpressionEvaluator._detect_pattern(expanded_exp)

            if pattern == ExpressionEvaluator.PATTERN_LITERAL:
                return ExpressionEvaluator._evaluate_literal(expanded_expression)

            if pattern == ExpressionEvaluator.PATTERN_SIMPLE_PATH:
                return self._evaluate_simple_path(expanded_exp, values_map)

            if pattern == ExpressionEvaluator.PATTERN_SIMPLE_COMPARISON:
                return self._evaluate_simple_comparison(expanded_exp, values_map)

            if pattern == ExpressionEvaluator.PATTERN_SIMPLE_TERNARY:
                return self._evaluate_simple_ternary(expanded_exp, values_map)

            vm = dict(values_map)
            vm[self._internal_token_value_extractor.get_prefix()] = self._internal_token_value_extractor
            self._internal_token_value_extractor.set_values_map(vm)

            return self._evaluate_expression(expanded_exp, vm)
        except Exception as err:
            raise

    def _evaluate_simple_path(self, exp, values_map: dict) -> Any:
        path_str = exp.get_expression()
        dot_idx = path_str.index('.') if '.' in path_str else -1
        if dot_idx == -1:
            return None
        prefix = path_str[:dot_idx + 1]
        extractor = values_map.get(prefix)
        if not extractor:
            return None
        return extractor.get_value(path_str)

    def _evaluate_simple_comparison(self, exp, values_map: dict) -> Any:
        ops = exp.get_operations_array()
        tokens = exp.get_tokens_array()
        if len(tokens) != 2 or len(ops) != 1:
            return None
        v1 = self._get_token_value(tokens[1], values_map)
        v2 = self._get_token_value(tokens[0], values_map)
        if ops[0] is Operation.EQUAL:
            return v1 == v2
        elif ops[0] is Operation.NOT_EQUAL:
            return v1 != v2
        return None

    def _evaluate_simple_ternary(self, exp, values_map: dict) -> Any:
        tokens = exp.get_tokens_array()
        if len(tokens) != 3:
            return None
        condition = self._get_token_value(tokens[2], values_map)
        true_value = self._get_token_value(tokens[1], values_map)
        false_value = self._get_token_value(tokens[0], values_map)
        return true_value if condition else false_value

    def _get_token_value(self, token: ExpressionToken, values_map: dict) -> Any:
        if isinstance(token, ExpressionTokenValue):
            return token.get_element()
        token_str = token.get_expression()
        literal_val = ExpressionEvaluator._evaluate_literal(token_str)
        if literal_val is not None or token_str in ('undefined', 'null'):
            return literal_val
        if '.' in token_str:
            dot_idx = token_str.index('.')
            prefix = token_str[:dot_idx + 1]
            extractor = values_map.get(prefix)
            if extractor:
                return extractor.get_value(token_str)
        return LiteralTokenValueExtractor.INSTANCE.get_value(token_str)

    def _process_nesting_expression(self, expression: str, values_map: dict) -> Tuple2:
        current = expression
        while '{{' in current:
            innermost = self._find_innermost_pair(current)
            if not innermost:
                break
            current = self._replace_one_nesting(current, innermost, values_map)
        return Tuple2(current, ExpressionEvaluator._get_cached_expression(current))

    def _find_innermost_pair(self, expr: str):
        i = 0
        length = len(expr)
        while i < length - 1:
            if expr[i] != '{' or expr[i + 1] != '{':
                i += 1
                continue
            open_pos = i
            i += 2
            depth = 1
            while i < length - 1 and depth > 0:
                if expr[i] == '{' and expr[i + 1] == '{':
                    depth += 1
                    i += 2
                    continue
                if expr[i] == '}' and expr[i + 1] == '}':
                    depth -= 1
                    if depth == 0:
                        close_pos = i + 2
                        content = expr[open_pos + 2:i]
                        if '{{' not in content:
                            return {'start': open_pos, 'end': close_pos, 'content': content}
                        inner = self._find_innermost_pair(content)
                        if not inner:
                            i += 2
                            continue
                        return {
                            'start': open_pos + 2 + inner['start'],
                            'end': open_pos + 2 + inner['end'],
                            'content': inner['content'],
                        }
                    i += 2
                    continue
                i += 1
            i = open_pos + 1
        return None

    def _replace_one_nesting(self, expression: str, innermost: dict, values_map: dict) -> str:
        start_pos = innermost['start']
        end_pos = innermost['end']
        inner_expr = innermost['content']

        nested_evaluator = ExpressionEvaluator(inner_expr)
        nested_evaluator._internal_token_value_extractor = self._internal_token_value_extractor
        evaluated_value = nested_evaluator.evaluate(values_map)

        return expression[:start_pos] + str(evaluated_value) + expression[end_pos:]

    def get_expression(self):
        if self._exp is None:
            self._exp = ExpressionEvaluator._get_cached_expression(self._expression)
        return self._exp

    def get_expression_string(self) -> str:
        return self._expression

    def _evaluate_expression(self, exp, values_map: dict) -> Any:
        from kirun_py.runtime.expression.expression import Expression

        ops_array = exp.get_operations_array()
        tokens_source = exp.get_tokens_array()
        working_stack: List[ExpressionToken] = []

        ctx = {'op_idx': 0, 'src_idx': 0}

        def pop_token() -> ExpressionToken:
            if working_stack:
                return working_stack.pop()
            if ctx['src_idx'] >= len(tokens_source):
                if ctx['op_idx'] < len(ops_array):
                    raise ExpressionEvaluationException(
                        exp.get_expression(), 'Not enough tokens to evaluate expression')
                raise ExpressionEvaluationException(
                    exp.get_expression(), 'Expression evaluation incomplete: missing token')
            t = tokens_source[ctx['src_idx']]
            ctx['src_idx'] += 1
            return t

        def pop_op():
            if ctx['op_idx'] >= len(ops_array):
                return None
            o = ops_array[ctx['op_idx']]
            ctx['op_idx'] += 1
            return o

        def peek_op():
            if ctx['op_idx'] >= len(ops_array):
                return None
            return ops_array[ctx['op_idx']]

        while ctx['op_idx'] < len(ops_array):
            operator = pop_op()
            token = pop_token()

            if operator in ExpressionEvaluator.UNARY_OPERATORS_MAP_KEY_SET:
                working_stack.append(
                    self._apply_unary_operation(operator, self._get_value_from_token(values_map, token)))
            elif operator is Operation.OBJECT_OPERATOR or operator is Operation.ARRAY_OPERATOR:
                self._process_object_or_array_operator_indexed(
                    values_map, ops_array, tokens_source, working_stack, ctx,
                    operator, token, pop_token, pop_op, peek_op)
            elif operator is Operation.CONDITIONAL_TERNARY_OPERATOR:
                token2 = pop_token()
                token3 = pop_token()
                v1 = self._get_value_from_token(values_map, token3)
                v2 = self._get_value_from_token(values_map, token2)
                v3 = self._get_value_from_token(values_map, token)
                working_stack.append(self._apply_ternary_operation(operator, v1, v2, v3))
            else:
                token2 = pop_token()
                v1 = self._get_value_from_token(values_map, token2)
                v2 = self._get_value_from_token(values_map, token)
                working_stack.append(self._apply_binary_operation(operator, v1, v2, values_map))

        while ctx['src_idx'] < len(tokens_source):
            working_stack.append(tokens_source[ctx['src_idx']])
            ctx['src_idx'] += 1

        if len(working_stack) == 0:
            raise ExecutionException(
                StringFormatter.format('Expression : $ evaluated to null', exp))

        if len(working_stack) != 1:
            raise ExecutionException(
                StringFormatter.format('Expression : $ evaluated multiple values $', exp, working_stack))

        token = working_stack[0]
        if isinstance(token, ExpressionTokenValue):
            return token.get_element()
        if isinstance(token, Expression):
            return self._evaluate_expression(token, values_map)
        return self._get_value_from_token(values_map, token)

    def _process_object_or_array_operator_indexed(
        self, values_map, ops_array, tokens_source, working_stack, ctx,
        operator, token, pop_token, pop_op, peek_op
    ):
        from kirun_py.runtime.expression.expression import Expression

        obj_tokens: List[ExpressionToken] = []
        obj_operations: List[Operation] = []

        if operator is None or token is None:
            return

        while True:
            obj_operations.append(operator)
            if isinstance(token, Expression):
                should_evaluate = (
                    operator is Operation.ARRAY_OPERATOR or not self._is_path_expression(token))
                if should_evaluate:
                    evaluated_value = self._evaluate_expression(token, values_map)
                    obj_tokens.append(ExpressionTokenValue(str(token), evaluated_value))
                else:
                    token_str = self._build_path_string(token)
                    obj_tokens.append(ExpressionToken(token_str))
            elif token is not None:
                obj_tokens.append(token)

            if working_stack:
                token = working_stack.pop()
            elif ctx['src_idx'] < len(tokens_source):
                token = tokens_source[ctx['src_idx']]
                ctx['src_idx'] += 1
            else:
                token = None
            operator = pop_op()

            if operator is not Operation.OBJECT_OPERATOR and operator is not Operation.ARRAY_OPERATOR:
                break

        if token is not None:
            if isinstance(token, Expression):
                if self._is_path_expression(token):
                    token_str = self._build_path_string(token)
                    obj_tokens.append(ExpressionToken(token_str))
                else:
                    obj_tokens.append(ExpressionTokenValue(
                        str(token), self._evaluate_expression(token, values_map)))
            else:
                obj_tokens.append(token)

        if operator is not None:
            ctx['op_idx'] -= 1

        obj_token_idx = len(obj_tokens) - 1
        obj_op_idx = len(obj_operations) - 1

        obj_token = obj_tokens[obj_token_idx]
        obj_token_idx -= 1

        if (isinstance(obj_token, ExpressionTokenValue) and
                isinstance(obj_token.get_token_value(), (dict, list))):
            key = '_k' + str(ExpressionEvaluator._key_counter)
            ExpressionEvaluator._key_counter += 1
            self._internal_token_value_extractor.add_value(key, obj_token.get_token_value())
            obj_token = ExpressionToken(ExpressionInternalValueExtractor.PREFIX + key)

        if isinstance(obj_token, ExpressionTokenValue):
            original_expr = obj_token.get_expression()
            evaluated_value = obj_token.get_token_value()
            if (original_expr and len(original_expr) > 0 and
                    (original_expr[0] == '"' or original_expr[0] == "'") and
                    isinstance(evaluated_value, str) and '.' in evaluated_value):
                s = original_expr
            else:
                s = str(evaluated_value) if not isinstance(evaluated_value, str) else evaluated_value
        else:
            s = str(obj_token)

        while obj_token_idx >= 0:
            obj_token = obj_tokens[obj_token_idx]
            obj_token_idx -= 1
            op = obj_operations[obj_op_idx]
            obj_op_idx -= 1

            if isinstance(obj_token, ExpressionTokenValue):
                original_expr = obj_token.get_expression()
                evaluated_value = obj_token.get_token_value()
                if (op is Operation.ARRAY_OPERATOR and original_expr and len(original_expr) > 0 and
                        (original_expr[0] == '"' or original_expr[0] == "'") and
                        isinstance(evaluated_value, str) and '.' in evaluated_value):
                    token_val = original_expr
                else:
                    token_val = str(evaluated_value) if not isinstance(evaluated_value, str) else evaluated_value
            else:
                token_val = str(obj_token)

            s = s + op.get_operator() + token_val + (']' if op is Operation.ARRAY_OPERATOR else '')

        dot_idx = s.find('.')
        key = s[:dot_idx + 1] if dot_idx >= 0 else ''
        if len(key) > 2 and key in values_map:
            working_stack.append(ExpressionTokenValue(s, self._get_value(s, values_map)))
        else:
            try:
                v = LiteralTokenValueExtractor.INSTANCE.get_value(s)
            except Exception:
                v = self._evaluate_literal_property_access(s)
            working_stack.append(ExpressionTokenValue(s, v))

    def _evaluate_literal_property_access(self, s: str) -> Any:
        dot_idx = s.find('.')
        if dot_idx == -1:
            return s
        base_part = s[:dot_idx]
        prop_part = s[dot_idx + 1:]
        try:
            base_value = LiteralTokenValueExtractor.INSTANCE.get_value(base_part)
        except Exception:
            return s
        if base_value is None:
            return None
        prop_parts = prop_part.split('.')
        result = base_value
        for prop in prop_parts:
            if result is None:
                return None
            if isinstance(result, dict):
                result = result.get(prop)
            elif isinstance(result, (list, str)):
                if prop == 'length':
                    return len(result)
                try:
                    result = result[int(prop)]
                except (ValueError, IndexError):
                    return None
            else:
                return None
        return result

    def _apply_ternary_operation(self, operator: Operation, v1, v2, v3) -> ExpressionToken:
        op = ExpressionEvaluator.TERNARY_OPERATORS_MAP.get(operator)
        if not op:
            raise ExpressionEvaluationException(
                self._expression,
                StringFormatter.format('No operator found to evaluate $', self.get_expression()))
        return ExpressionTokenValue(str(operator), op.apply(v1, v2, v3))

    def _apply_binary_operation(self, operator: Operation, v1, v2, values_map=None) -> ExpressionToken:
        typv1 = type(v1).__name__
        typv2 = type(v2).__name__

        op = ExpressionEvaluator.BINARY_OPERATORS_MAP.get(operator)

        if ((isinstance(v1, (dict, list)) or isinstance(v2, (dict, list))) and
                operator is not Operation.EQUAL and
                operator is not Operation.NOT_EQUAL and
                operator is not Operation.NULLISH_COALESCING_OPERATOR and
                operator is not Operation.AND and
                operator is not Operation.OR):
            raise ExpressionEvaluationException(
                self._expression,
                StringFormatter.format('Cannot evaluate expression $ $ $', v1, operator.get_operator(), v2))

        if not op:
            raise ExpressionEvaluationException(
                self._expression,
                StringFormatter.format('No operator found to evaluate $ $ $', v1, operator.get_operator(), v2))

        result = op.apply(v1, v2)

        if (operator is Operation.NULLISH_COALESCING_OPERATOR and
                isinstance(result, str) and values_map and
                result.strip() and self._looks_like_expression(result)):
            try:
                result = ExpressionEvaluator(result).evaluate(values_map)
            except Exception:
                pass

        return ExpressionTokenValue(str(operator), result)

    def _looks_like_expression(self, s: str) -> bool:
        trimmed = s.strip()
        if not trimmed:
            return False
        if re.search(r'[+\-*/%=<>!&|?:]', trimmed):
            return True
        prefixes = ['Store.', 'Context.', 'Arguments.', 'Steps.', 'Page.', 'Parent.']
        return any(p in trimmed for p in prefixes)

    def _apply_unary_operation(self, operator: Operation, value) -> ExpressionToken:
        if (operator.get_operator() != Operation.NOT.get_operator() and
                operator.get_operator() != Operation.UNARY_LOGICAL_NOT.get_operator() and
                isinstance(value, (dict, list))):
            raise ExpressionEvaluationException(
                self._expression,
                StringFormatter.format('The operator $ cannot be applied to $', operator.get_operator(), value))

        op = ExpressionEvaluator.UNARY_OPERATORS_MAP.get(operator)
        if not op:
            raise ExpressionEvaluationException(
                self._expression,
                StringFormatter.format('No Unary operator $ is found to apply on $', operator.get_operator(), value))

        return ExpressionTokenValue(str(operator), op.apply(value))

    def _get_value_from_token(self, values_map: dict, token: ExpressionToken) -> Any:
        from kirun_py.runtime.expression.expression import Expression
        if isinstance(token, Expression):
            return self._evaluate_expression(token, values_map)
        elif isinstance(token, ExpressionTokenValue):
            return token.get_element()
        return self._get_value(token.get_expression(), values_map)

    def _get_value(self, path: str, values_map: dict) -> Any:
        dot_idx = path.find('.')
        path_prefix = path[:dot_idx + 1] if dot_idx >= 0 else ''
        if path_prefix in values_map:
            return values_map[path_prefix].get_value(path)
        return LiteralTokenValueExtractor.INSTANCE.get_value_from_extractors(path, values_map)

    def _build_path_string(self, expr) -> str:
        from kirun_py.runtime.expression.expression import Expression
        ops = expr.get_operations_array()
        tokens = expr.get_tokens_array()

        if len(ops) == 0:
            if len(tokens) == 1:
                token = tokens[0]
                if isinstance(token, Expression):
                    return self._build_path_string(token)
                return self._get_token_expression_string(token)
            return expr.get_expression() or ''

        if len(tokens) >= 2 and len(ops) >= 1:
            right = tokens[0]
            left = tokens[1]
            op = ops[0]

            from kirun_py.runtime.expression.expression import Expression
            left_str = self._build_path_string(left) if isinstance(left, Expression) else self._get_token_expression_string(left)
            right_str = self._build_path_string(right) if isinstance(right, Expression) else self._get_token_expression_string(right)

            if op is Operation.OBJECT_OPERATOR:
                return left_str + '.' + right_str
            elif op is Operation.ARRAY_OPERATOR:
                return left_str + '[' + right_str + ']'
            elif op is Operation.ARRAY_RANGE_INDEX_OPERATOR:
                return left_str + '..' + right_str

        return self._strip_outer_parens(str(expr))

    def _get_token_expression_string(self, token: ExpressionToken) -> str:
        return token.get_expression()

    def _strip_outer_parens(self, s: str) -> str:
        if len(s) >= 2 and s[0] == '(' and s[-1] == ')':
            depth = 0
            for i in range(len(s)):
                if s[i] == '(':
                    depth += 1
                elif s[i] == ')':
                    depth -= 1
                if depth == 0 and i < len(s) - 1:
                    return s
            return s[1:-1]
        return s

    def _is_path_expression(self, expr) -> bool:
        from kirun_py.runtime.expression.expression import Expression
        ops = expr.get_operations_array()
        tokens = expr.get_tokens_array()

        if len(ops) == 0:
            return True

        for i, op in enumerate(ops):
            if not self._is_path_operator(op):
                return False
            if op is Operation.ARRAY_OPERATOR and len(tokens) > 0 and not self._is_static_array_index(tokens[0]):
                return False

        return all(
            not isinstance(token, Expression) or self._is_path_expression(token)
            for token in tokens
        )

    def _is_path_operator(self, op: Operation) -> bool:
        return (op is Operation.OBJECT_OPERATOR or
                op is Operation.ARRAY_OPERATOR or
                op is Operation.ARRAY_RANGE_INDEX_OPERATOR)

    def _is_static_array_index(self, token: ExpressionToken) -> bool:
        from kirun_py.runtime.expression.expression import Expression
        if isinstance(token, Expression):
            return self._is_static_array_index_expression(token)
        return self._is_static_literal(token.get_expression())

    def _is_static_array_index_expression(self, expr) -> bool:
        ops = expr.get_operations_array()
        tokens = expr.get_tokens_array()
        if len(ops) == 0 and len(tokens) == 1:
            return self._is_static_literal(tokens[0].get_expression())
        if len(ops) == 1 and ops[0] is Operation.ARRAY_RANGE_INDEX_OPERATOR:
            return all(self._is_static_array_index(t) for t in tokens)
        return False

    def _is_static_literal(self, s: str) -> bool:
        if re.match(r'^-?\d+(\.\d+)?$', s):
            return True
        if ((s.startswith('"') and s.endswith('"')) or
                (s.startswith("'") and s.endswith("'"))):
            return True
        return False
