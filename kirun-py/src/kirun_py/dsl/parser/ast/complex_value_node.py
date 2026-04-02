from __future__ import annotations

from typing import Any

from kirun_py.dsl.lexer.dsl_token import SourceLocation
from kirun_py.dsl.parser.ast.ast_node import ASTNode


class ComplexValueNode(ASTNode):
    """
    Complex value node - represents literal values.
    Used for parameter values that are objects, arrays, or primitives
    (strings, numbers, booleans, null).
    """

    def __init__(self, value: Any, location: SourceLocation) -> None:
        super().__init__('ComplexValue', location)
        self.value = value

    def to_json(self) -> Any:
        return {
            'type': self.type,
            'value': self.value,
        }
