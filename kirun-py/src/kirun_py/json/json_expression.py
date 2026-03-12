class JsonExpression:
    def __init__(self, expression: str):
        self._expression = expression

    def get_expression(self) -> str:
        return self._expression
