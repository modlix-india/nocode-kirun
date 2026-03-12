from __future__ import annotations
from typing import Any, Optional


class Position:
    def __init__(self, left: float = 0, top: float = 0):
        self._left = left
        self._top = top

    def get_left(self) -> float:
        return self._left

    def set_left(self, left: float) -> Position:
        self._left = left
        return self

    def get_top(self) -> float:
        return self._top

    def set_top(self, top: float) -> Position:
        self._top = top
        return self

    @staticmethod
    def from_value(json: Any) -> Optional[Position]:
        if not json:
            return None
        return Position(json.get('left', 0), json.get('top', 0))

    def to_json(self) -> dict:
        return {'left': self._left, 'top': self._top}
