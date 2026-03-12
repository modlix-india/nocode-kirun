from __future__ import annotations

from typing import Any, Optional


class ExpressionHandler:
    """
    Expression Handler.
    Utilities for working with KIRun expressions.
    """

    @staticmethod
    def parse(expression_text: str) -> Any:
        """Parse expression text using KIRun Expression parser."""
        from kirun_py.runtime.expression.expression import Expression
        return Expression(expression_text)

    @staticmethod
    def validate(expression_text: str) -> bool:
        """Validate expression syntax."""
        try:
            from kirun_py.runtime.expression.expression import Expression
            Expression(expression_text)
            return True
        except Exception:
            return False

    @staticmethod
    def is_expression(value: Any) -> bool:
        """Check if a value is an expression (has isExpression flag)."""
        return (
            value is not None
            and isinstance(value, dict)
            and value.get('isExpression') is True
            and isinstance(value.get('value'), str)
        )

    @staticmethod
    def extract_expression_text(value: Any) -> Optional[str]:
        """Extract expression text from value object."""
        if ExpressionHandler.is_expression(value):
            return value['value']
        return None
