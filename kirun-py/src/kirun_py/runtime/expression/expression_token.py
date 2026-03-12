from __future__ import annotations


class ExpressionToken:

    def __init__(self, expression: str):
        self.expression = expression

    def get_expression(self) -> str:
        return self.expression

    def __str__(self) -> str:
        return self.expression
