from __future__ import annotations

from typing import Optional, Dict, Set


class Operation:

    # Static instances - initialized below class body
    MULTIPLICATION: Operation
    DIVISION: Operation
    INTEGER_DIVISION: Operation
    MOD: Operation
    ADDITION: Operation
    SUBTRACTION: Operation

    NOT: Operation
    AND: Operation
    OR: Operation
    LESS_THAN: Operation
    LESS_THAN_EQUAL: Operation
    GREATER_THAN: Operation
    GREATER_THAN_EQUAL: Operation
    EQUAL: Operation
    NOT_EQUAL: Operation

    BITWISE_AND: Operation
    BITWISE_OR: Operation
    BITWISE_XOR: Operation
    BITWISE_COMPLEMENT: Operation
    BITWISE_LEFT_SHIFT: Operation
    BITWISE_RIGHT_SHIFT: Operation
    BITWISE_UNSIGNED_RIGHT_SHIFT: Operation

    UNARY_PLUS: Operation
    UNARY_MINUS: Operation
    UNARY_LOGICAL_NOT: Operation
    UNARY_BITWISE_COMPLEMENT: Operation

    ARRAY_OPERATOR: Operation
    ARRAY_RANGE_INDEX_OPERATOR: Operation
    OBJECT_OPERATOR: Operation

    NULLISH_COALESCING_OPERATOR: Operation

    CONDITIONAL_TERNARY_OPERATOR: Operation

    # Static collections - initialized by _init()
    UNARY_OPERATORS: Set[Operation]
    ARITHMETIC_OPERATORS: Set[Operation]
    LOGICAL_OPERATORS: Set[Operation]
    BITWISE_OPERATORS: Set[Operation]
    CONDITIONAL_OPERATORS: Set[Operation]
    OPERATOR_PRIORITY: Dict[Operation, int]
    OPERATORS: Set[str]
    OPERATORS_WITHOUT_SPACE_WRAP: Set[str]
    OPERATION_VALUE_OF: Dict[str, Operation]
    UNARY_MAP: Dict[Operation, Operation]
    BIGGEST_OPERATOR_SIZE: int

    def __init__(self, operator: str, operator_name: Optional[str] = None,
                 should_be_wrapped_in_space: bool = False):
        self._operator = operator
        self._operator_name = operator_name if operator_name is not None else operator
        self._should_be_wrapped_in_space = should_be_wrapped_in_space

    def get_operator(self) -> str:
        return self._operator

    def get_operator_name(self) -> str:
        return self._operator_name

    def should_be_wrapped_in_space(self) -> bool:
        return self._should_be_wrapped_in_space

    def value_of(self, s: str) -> Optional[Operation]:
        return Operation._VALUE_OF.get(s)

    def __str__(self) -> str:
        return self._operator

    def __repr__(self) -> str:
        return f'Operation({self._operator!r})'

    def __hash__(self) -> int:
        return hash(self._operator)

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Operation):
            return NotImplemented
        return self._operator == other._operator

    @classmethod
    def _init(cls) -> None:
        # Static instances
        cls.MULTIPLICATION = Operation('*')
        cls.DIVISION = Operation('/')
        cls.INTEGER_DIVISION = Operation('//')
        cls.MOD = Operation('%')
        cls.ADDITION = Operation('+')
        cls.SUBTRACTION = Operation('-')

        cls.NOT = Operation('not', None, True)
        cls.AND = Operation('and', None, True)
        cls.OR = Operation('or', None, True)
        cls.LESS_THAN = Operation('<')
        cls.LESS_THAN_EQUAL = Operation('<=')
        cls.GREATER_THAN = Operation('>')
        cls.GREATER_THAN_EQUAL = Operation('>=')
        cls.EQUAL = Operation('=')
        cls.NOT_EQUAL = Operation('!=')

        cls.BITWISE_AND = Operation('&')
        cls.BITWISE_OR = Operation('|')
        cls.BITWISE_XOR = Operation('^')
        cls.BITWISE_COMPLEMENT = Operation('~')
        cls.BITWISE_LEFT_SHIFT = Operation('<<')
        cls.BITWISE_RIGHT_SHIFT = Operation('>>')
        cls.BITWISE_UNSIGNED_RIGHT_SHIFT = Operation('>>>')

        cls.UNARY_PLUS = Operation('UN: +', '+')
        cls.UNARY_MINUS = Operation('UN: -', '-')
        cls.UNARY_LOGICAL_NOT = Operation('UN: not', 'not')
        cls.UNARY_BITWISE_COMPLEMENT = Operation('UN: ~', '~')

        cls.ARRAY_OPERATOR = Operation('[')
        cls.ARRAY_RANGE_INDEX_OPERATOR = Operation('..')
        cls.OBJECT_OPERATOR = Operation('.')

        cls.NULLISH_COALESCING_OPERATOR = Operation('??')

        cls.CONDITIONAL_TERNARY_OPERATOR = Operation('?')

        # VALUE_OF map (name -> Operation)
        cls._VALUE_OF: Dict[str, Operation] = {
            'MULTIPLICATION': cls.MULTIPLICATION,
            'DIVISION': cls.DIVISION,
            'INTEGER_DIVISION': cls.INTEGER_DIVISION,
            'MOD': cls.MOD,
            'ADDITION': cls.ADDITION,
            'SUBTRACTION': cls.SUBTRACTION,
            'NOT': cls.NOT,
            'AND': cls.AND,
            'OR': cls.OR,
            'LESS_THAN': cls.LESS_THAN,
            'LESS_THAN_EQUAL': cls.LESS_THAN_EQUAL,
            'GREATER_THAN': cls.GREATER_THAN,
            'GREATER_THAN_EQUAL': cls.GREATER_THAN_EQUAL,
            'EQUAL': cls.EQUAL,
            'NOT_EQUAL': cls.NOT_EQUAL,
            'BITWISE_AND': cls.BITWISE_AND,
            'BITWISE_OR': cls.BITWISE_OR,
            'BITWISE_XOR': cls.BITWISE_XOR,
            'BITWISE_COMPLEMENT': cls.BITWISE_COMPLEMENT,
            'BITWISE_LEFT_SHIFT': cls.BITWISE_LEFT_SHIFT,
            'BITWISE_RIGHT_SHIFT': cls.BITWISE_RIGHT_SHIFT,
            'BITWISE_UNSIGNED_RIGHT_SHIFT': cls.BITWISE_UNSIGNED_RIGHT_SHIFT,
            'UNARY_PLUS': cls.UNARY_PLUS,
            'UNARY_MINUS': cls.UNARY_MINUS,
            'UNARY_LOGICAL_NOT': cls.UNARY_LOGICAL_NOT,
            'UNARY_BITWISE_COMPLEMENT': cls.UNARY_BITWISE_COMPLEMENT,
            'ARRAY_OPERATOR': cls.ARRAY_OPERATOR,
            'ARRAY_RANGE_INDEX_OPERATOR': cls.ARRAY_RANGE_INDEX_OPERATOR,
            'OBJECT_OPERATOR': cls.OBJECT_OPERATOR,
            'NULLISH_COALESCING_OPERATOR': cls.NULLISH_COALESCING_OPERATOR,
            'CONDITIONAL_TERNARY_OPERATOR': cls.CONDITIONAL_TERNARY_OPERATOR,
        }

        # Operator sets
        cls.UNARY_OPERATORS = {
            cls.ADDITION,
            cls.SUBTRACTION,
            cls.NOT,
            cls.BITWISE_COMPLEMENT,
            cls.UNARY_PLUS,
            cls.UNARY_MINUS,
            cls.UNARY_LOGICAL_NOT,
            cls.UNARY_BITWISE_COMPLEMENT,
        }

        cls.ARITHMETIC_OPERATORS = {
            cls.MULTIPLICATION,
            cls.DIVISION,
            cls.INTEGER_DIVISION,
            cls.MOD,
            cls.ADDITION,
            cls.SUBTRACTION,
        }

        cls.LOGICAL_OPERATORS = {
            cls.NOT,
            cls.AND,
            cls.OR,
            cls.LESS_THAN,
            cls.LESS_THAN_EQUAL,
            cls.GREATER_THAN,
            cls.GREATER_THAN_EQUAL,
            cls.EQUAL,
            cls.NOT_EQUAL,
            cls.NULLISH_COALESCING_OPERATOR,
        }

        cls.BITWISE_OPERATORS = {
            cls.BITWISE_AND,
            cls.BITWISE_COMPLEMENT,
            cls.BITWISE_LEFT_SHIFT,
            cls.BITWISE_OR,
            cls.BITWISE_RIGHT_SHIFT,
            cls.BITWISE_UNSIGNED_RIGHT_SHIFT,
            cls.BITWISE_XOR,
        }

        cls.CONDITIONAL_OPERATORS = {
            cls.CONDITIONAL_TERNARY_OPERATOR,
        }

        # Operator priority
        cls.OPERATOR_PRIORITY = {
            cls.UNARY_PLUS: 1,
            cls.UNARY_MINUS: 1,
            cls.UNARY_LOGICAL_NOT: 1,
            cls.UNARY_BITWISE_COMPLEMENT: 1,
            cls.ARRAY_OPERATOR: 1,
            cls.OBJECT_OPERATOR: 1,
            cls.ARRAY_RANGE_INDEX_OPERATOR: 2,
            cls.MULTIPLICATION: 2,
            cls.DIVISION: 2,
            cls.INTEGER_DIVISION: 2,
            cls.MOD: 2,
            cls.ADDITION: 3,
            cls.SUBTRACTION: 3,
            cls.BITWISE_LEFT_SHIFT: 4,
            cls.BITWISE_RIGHT_SHIFT: 4,
            cls.BITWISE_UNSIGNED_RIGHT_SHIFT: 4,
            cls.LESS_THAN: 5,
            cls.LESS_THAN_EQUAL: 5,
            cls.GREATER_THAN: 5,
            cls.GREATER_THAN_EQUAL: 5,
            cls.EQUAL: 6,
            cls.NOT_EQUAL: 6,
            cls.BITWISE_AND: 7,
            cls.BITWISE_XOR: 8,
            cls.BITWISE_OR: 9,
            cls.NOT: 10,
            cls.AND: 10,
            cls.OR: 11,
            cls.NULLISH_COALESCING_OPERATOR: 11,
            cls.CONDITIONAL_TERNARY_OPERATOR: 12,
        }

        # All operator strings
        all_ops = list(cls.ARITHMETIC_OPERATORS) + list(cls.LOGICAL_OPERATORS) + \
            list(cls.BITWISE_OPERATORS) + [
                cls.ARRAY_OPERATOR,
                cls.ARRAY_RANGE_INDEX_OPERATOR,
                cls.OBJECT_OPERATOR,
            ] + list(cls.CONDITIONAL_OPERATORS)

        cls.OPERATORS = {e.get_operator() for e in all_ops}

        cls.OPERATORS_WITHOUT_SPACE_WRAP = {
            e.get_operator() for e in all_ops if not e.should_be_wrapped_in_space()
        }

        # Operator string -> Operation mapping
        cls.OPERATION_VALUE_OF = {
            o.get_operator(): o for o in cls._VALUE_OF.values()
        }

        # Unary map
        cls.UNARY_MAP = {
            cls.ADDITION: cls.UNARY_PLUS,
            cls.SUBTRACTION: cls.UNARY_MINUS,
            cls.NOT: cls.UNARY_LOGICAL_NOT,
            cls.BITWISE_COMPLEMENT: cls.UNARY_BITWISE_COMPLEMENT,
            cls.UNARY_PLUS: cls.UNARY_PLUS,
            cls.UNARY_MINUS: cls.UNARY_MINUS,
            cls.UNARY_LOGICAL_NOT: cls.UNARY_LOGICAL_NOT,
            cls.UNARY_BITWISE_COMPLEMENT: cls.UNARY_BITWISE_COMPLEMENT,
        }

        # Biggest operator size (excluding UN: prefixed)
        cls.BIGGEST_OPERATOR_SIZE = max(
            (len(o.get_operator()) for o in cls._VALUE_OF.values()
             if not o.get_operator().startswith('UN: ')),
            default=0
        )


Operation._init()
