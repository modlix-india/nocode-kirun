from __future__ import annotations

from typing import Any, Optional

from kirun_py.dsl.lexer.dsl_token import SourceLocation
from kirun_py.dsl.parser.ast.ast_node import ASTNode


class ExpressionNode(ASTNode):
    """Expression node - wraps KIRun Expression."""

    def __init__(self, expression_text: str, location: SourceLocation) -> None:
        super().__init__('Expression', location)
        self.expression_text = expression_text
        self.parsed_expression: Optional[Any] = None

    def parse(self) -> None:
        """Parse the expression using KIRun Expression parser."""
        if self.parsed_expression is None:
            from kirun_py.runtime.expression.expression import Expression
            self.parsed_expression = Expression(self.expression_text)

    def to_json(self) -> Any:
        return {
            'type': self.type,
            'expressionText': self.expression_text,
        }
