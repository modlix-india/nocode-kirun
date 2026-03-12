from __future__ import annotations

from typing import Any, Optional

from kirun_py.dsl.lexer.dsl_token import SourceLocation
from kirun_py.dsl.parser.ast.ast_node import ASTNode
from kirun_py.dsl.parser.ast.expression_node import ExpressionNode
from kirun_py.dsl.parser.ast.schema_node import SchemaNode


class SchemaLiteralNode(ASTNode):
    """
    Schema literal node - represents inline schema definitions with default values.
    Example: (ARRAY OF INTEGER) WITH DEFAULT VALUE []
    """

    def __init__(
        self,
        schema: SchemaNode,
        default_value: Optional[ExpressionNode],
        location: SourceLocation,
    ) -> None:
        super().__init__('SchemaLiteral', location)
        self.schema = schema
        self.default_value = default_value

    def to_json(self) -> Any:
        result: dict = {
            'type': self.type,
            'schema': self.schema.to_json(),
        }
        if self.default_value is not None:
            result['defaultValue'] = self.default_value.to_json()
        else:
            result['defaultValue'] = None
        return result
