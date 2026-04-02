from __future__ import annotations

from typing import Any, Dict, List, Optional

from kirun_py.dsl.lexer.dsl_token import SourceLocation
from kirun_py.dsl.parser.ast.ast_node import ASTNode
from kirun_py.dsl.parser.ast.function_call_node import FunctionCallNode


class StatementNode(ASTNode):
    """
    Statement node - represents a single statement in the LOGIC block.
    Example:
        create: System.Context.Create(name = "a") AFTER Steps.prev
            iteration
                if: System.If(condition = true)
    """

    def __init__(
        self,
        statement_name: str,
        function_call: FunctionCallNode,
        after_steps: Optional[List[str]] = None,
        execute_if_steps: Optional[List[str]] = None,
        nested_blocks: Optional[Dict[str, List[StatementNode]]] = None,
        location: Optional[SourceLocation] = None,
        comment: str = '',
    ) -> None:
        super().__init__(
            'Statement',
            location or SourceLocation(1, 1, 0, 0),
        )
        self.statement_name = statement_name
        self.function_call = function_call
        self.after_steps: List[str] = after_steps if after_steps is not None else []
        self.execute_if_steps: List[str] = execute_if_steps if execute_if_steps is not None else []
        self.nested_blocks: Dict[str, List[StatementNode]] = nested_blocks if nested_blocks is not None else {}
        self.comment = comment

    def to_json(self) -> Any:
        return {
            'type': self.type,
            'statementName': self.statement_name,
            'functionCall': self.function_call.to_json(),
            'afterSteps': self.after_steps,
            'executeIfSteps': self.execute_if_steps,
            'nestedBlocks': {
                block_name: [s.to_json() for s in statements]
                for block_name, statements in self.nested_blocks.items()
            },
            'comment': self.comment,
        }
