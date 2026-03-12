from __future__ import annotations

from typing import Any, Protocol, Set


class GraphVertexType(Protocol):

    def get_unique_key(self) -> Any:
        ...

    def get_depenedencies(self) -> Set[str]:
        ...
