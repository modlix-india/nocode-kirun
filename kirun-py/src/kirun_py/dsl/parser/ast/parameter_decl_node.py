from __future__ import annotations

from typing import Any

from kirun_py.dsl.lexer.dsl_token import SourceLocation
from kirun_py.dsl.parser.ast.ast_node import ASTNode
from kirun_py.dsl.parser.ast.schema_node import SchemaNode


class ParameterDeclNode(ASTNode):
    """
    Parameter declaration node.
    Example: n AS INTEGER
    """

    def __init__(
        self,
        name: str,
        schema: SchemaNode,
        location: SourceLocation,
    ) -> None:
        super().__init__('ParameterDecl', location)
        self.name = name
        self.schema = schema

    def to_json(self) -> Any:
        return {
            'type': self.type,
            'name': self.name,
            'schema': self.schema.to_json(),
        }
