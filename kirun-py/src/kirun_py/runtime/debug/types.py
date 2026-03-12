from __future__ import annotations

from typing import Any, Callable, Dict, List, Optional


class LogEntry:
    """Log entry for a single step execution."""

    def __init__(
        self,
        step_id: str,
        timestamp: float,
        function_name: str,
        statement_name: Optional[str] = None,
        kirun_function_name: Optional[str] = None,
    ) -> None:
        self.step_id: str = step_id
        self.timestamp: float = timestamp
        self.function_name: str = function_name
        self.statement_name: Optional[str] = statement_name
        self.kirun_function_name: Optional[str] = kirun_function_name
        self.duration: Optional[float] = None
        self.arguments: Any = None
        self.result: Any = None
        self.event_name: Optional[str] = None
        self.error: Optional[str] = None
        self.children: List[LogEntry] = []


class ExecutionLog:
    """Complete execution log."""

    def __init__(self, execution_id: str, start_time: float) -> None:
        self.execution_id: str = execution_id
        self.start_time: float = start_time
        self.end_time: Optional[float] = None
        self.errored: bool = False
        self.logs: List[LogEntry] = []
        self.definitions: Dict[str, Any] = {}


DebugEventListener = Callable[[Dict[str, Any]], None]
