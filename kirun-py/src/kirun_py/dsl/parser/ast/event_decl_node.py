from __future__ import annotations

from typing import Any, List

from kirun_py.dsl.lexer.dsl_token import SourceLocation
from kirun_py.dsl.parser.ast.ast_node import ASTNode
from kirun_py.dsl.parser.ast.parameter_decl_node import ParameterDeclNode


class EventDeclNode(ASTNode):
    """
    Event declaration node.
    Example:
        output
            result AS ARRAY OF INTEGER
    """

    def __init__(
        self,
        name: str,
        parameters: List[ParameterDeclNode],
        location: SourceLocation,
    ) -> None:
        super().__init__('EventDecl', location)
        self.name = name
        self.parameters = parameters

    def to_json(self) -> Any:
        return {
            'type': self.type,
            'name': self.name,
            'parameters': [p.to_json() for p in self.parameters],
        }
