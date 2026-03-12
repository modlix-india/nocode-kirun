from __future__ import annotations
from typing import Optional

from kirun_py.model.position import Position


class AbstractStatement:
    def __init__(self, ast: Optional[AbstractStatement] = None):
        self._comment: Optional[str] = None
        self._description: Optional[str] = None
        self._position: Optional[Position] = None
        self._override: bool = False

        if ast is not None:
            self._comment = ast._comment
            self._description = ast._description
            self._position = (
                Position(ast._position.get_left(), ast._position.get_top())
                if ast._position else None
            )
            self._override = ast._override

    def get_comment(self) -> Optional[str]:
        return self._comment

    def set_comment(self, comment: str) -> AbstractStatement:
        self._comment = comment
        return self

    def is_override(self) -> bool:
        return self._override

    def set_override(self, override: bool) -> AbstractStatement:
        self._override = override
        return self

    def get_description(self) -> Optional[str]:
        return self._description

    def set_description(self, description: str) -> AbstractStatement:
        self._description = description
        return self

    def get_position(self) -> Optional[Position]:
        return self._position

    def set_position(self, position: Optional[Position]) -> AbstractStatement:
        self._position = position
        return self
