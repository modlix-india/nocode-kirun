from __future__ import annotations

import json
from abc import ABC, abstractmethod
from typing import Any

from kirun_py.dsl.lexer.dsl_token import SourceLocation


class ASTNode(ABC):
    """Base class for all AST nodes."""

    def __init__(self, type: str, location: SourceLocation) -> None:
        self.type = type
        self.location = location

    @abstractmethod
    def to_json(self) -> Any:
        """Convert AST node to JSON (for debugging/inspection)."""
        ...

    def __str__(self) -> str:
        """Pretty-print the AST node."""
        return json.dumps(self.to_json(), indent=2)

    def __repr__(self) -> str:
        return self.__str__()
