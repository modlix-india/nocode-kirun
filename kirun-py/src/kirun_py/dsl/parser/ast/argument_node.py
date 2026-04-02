from __future__ import annotations

from typing import Any, List, Union

from kirun_py.dsl.lexer.dsl_token import SourceLocation
from kirun_py.dsl.parser.ast.ast_node import ASTNode
from kirun_py.dsl.parser.ast.complex_value_node import ComplexValueNode
from kirun_py.dsl.parser.ast.expression_node import ExpressionNode
from kirun_py.dsl.parser.ast.schema_literal_node import SchemaLiteralNode

ArgumentValue = Union[ExpressionNode, ComplexValueNode, SchemaLiteralNode]


class ArgumentNode(ASTNode):
    """
    Argument node - represents a function argument.
    Can be a single value or multiple values (multi-value parameter).
    Each value can be an expression, complex value (object/array), or schema literal.
    """

    def __init__(
        self,
        key: str,
        value: Union[ArgumentValue, List[ArgumentValue]],
        location: SourceLocation,
    ) -> None:
        super().__init__('Argument', location)
        self.key = key
        self.values: List[ArgumentValue] = value if isinstance(value, list) else [value]

    @property
    def value(self) -> ArgumentValue:
        """Get the first (or only) value - for backwards compatibility."""
        return self.values[0]

    def is_multi_value(self) -> bool:
        """Check if this is a multi-value parameter."""
        return len(self.values) > 1

    def to_json(self) -> Any:
        return {
            'type': self.type,
            'key': self.key,
            'values': [v.to_json() for v in self.values],
        }
