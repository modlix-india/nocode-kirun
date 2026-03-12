from __future__ import annotations
from typing import TypeVar, Generic, Optional, List, Protocol, runtime_checkable

T = TypeVar('T')


@runtime_checkable
class Repository(Protocol[T]):
    async def find(self, namespace: str, name: str) -> Optional[T]: ...
    async def filter(self, name: str) -> List[str]: ...
