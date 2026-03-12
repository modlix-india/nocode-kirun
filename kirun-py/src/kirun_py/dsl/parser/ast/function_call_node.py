from __future__ import annotations

from typing import Any, Dict

from kirun_py.dsl.lexer.dsl_token import SourceLocation
from kirun_py.dsl.parser.ast.argument_node import ArgumentNode
from kirun_py.dsl.parser.ast.ast_node import ASTNode


class FunctionCallNode(ASTNode):
    """
    Function call node.
    Example: System.Array.InsertLast(source = Context.a, element = 10)
    """

    def __init__(
        self,
        namespace: str,
        name: str,
        arguments_map: Dict[str, ArgumentNode],
        location: SourceLocation,
    ) -> None:
        super().__init__('FunctionCall', location)
        self.namespace = namespace
        self.name = name
        self.arguments_map = arguments_map

    def to_json(self) -> Any:
        return {
            'type': self.type,
            'namespace': self.namespace,
            'name': self.name,
            'arguments': {key: arg.to_json() for key, arg in self.arguments_map.items()},
        }
