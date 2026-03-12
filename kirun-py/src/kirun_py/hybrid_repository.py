from __future__ import annotations

from typing import Any, List, Optional

from kirun_py.repository import Repository


class HybridRepository:

    def __init__(self, *repos: Repository) -> None:
        self._repos = repos

    async def find(self, namespace: str, name: str) -> Optional[Any]:
        for repo in self._repos:
            s = await repo.find(namespace, name)
            if s is not None:
                return s
        return None

    async def filter(self, name: str) -> List[str]:
        result: set = set()
        for repo in self._repos:
            items = await repo.filter(name)
            result.update(items)
        return list(result)
