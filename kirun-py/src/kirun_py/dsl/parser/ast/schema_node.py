from __future__ import annotations

from typing import Any, Union

from kirun_py.dsl.lexer.dsl_token import SourceLocation
from kirun_py.dsl.parser.ast.ast_node import ASTNode


class SchemaNode(ASTNode):
    """
    Schema node - represents a schema specification.
    Can be either a simple string (e.g., "INTEGER", "ARRAY OF STRING")
    or a complex JSON Schema object.
    """

    def __init__(self, schema_spec: Union[str, dict], location: SourceLocation) -> None:
        super().__init__('Schema', location)
        self.schema_spec = schema_spec

    def to_json(self) -> Any:
        return {
            'type': self.type,
            'schemaSpec': self.schema_spec,
        }
