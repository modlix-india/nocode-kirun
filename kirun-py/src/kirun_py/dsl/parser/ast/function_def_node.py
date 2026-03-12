from __future__ import annotations

from typing import Any, List, Optional

from kirun_py.dsl.lexer.dsl_token import SourceLocation
from kirun_py.dsl.parser.ast.ast_node import ASTNode
from kirun_py.dsl.parser.ast.event_decl_node import EventDeclNode
from kirun_py.dsl.parser.ast.parameter_decl_node import ParameterDeclNode
from kirun_py.dsl.parser.ast.statement_node import StatementNode


class FunctionDefNode(ASTNode):
    """
    Function definition node - root AST node.
    Represents the entire function definition.
    """

    def __init__(
        self,
        name: str,
        namespace: Optional[str] = None,
        parameters: Optional[List[ParameterDeclNode]] = None,
        events: Optional[List[EventDeclNode]] = None,
        logic: Optional[List[StatementNode]] = None,
        location: Optional[SourceLocation] = None,
    ) -> None:
        super().__init__(
            'FunctionDefinition',
            location or SourceLocation(1, 1, 0, 0),
        )
        self.name = name
        self.namespace = namespace
        self.parameters: List[ParameterDeclNode] = parameters if parameters is not None else []
        self.events: List[EventDeclNode] = events if events is not None else []
        self.logic: List[StatementNode] = logic if logic is not None else []

    def to_json(self) -> Any:
        return {
            'type': self.type,
            'name': self.name,
            'namespace': self.namespace,
            'parameters': [p.to_json() for p in self.parameters],
            'events': [e.to_json() for e in self.events],
            'logic': [s.to_json() for s in self.logic],
        }
