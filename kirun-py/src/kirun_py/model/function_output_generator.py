from __future__ import annotations
from typing import Optional, Protocol

from kirun_py.model.event_result import EventResult


class FunctionOutputGenerator(Protocol):
    def next(self) -> Optional[EventResult]:
        ...
