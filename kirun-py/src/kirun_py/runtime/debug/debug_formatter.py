from __future__ import annotations

import time
from typing import Any, Dict, List, Optional

from kirun_py.runtime.debug.types import ExecutionLog, LogEntry


class PerformanceSummary:
    def __init__(
        self,
        total_duration: float,
        step_count: int,
        slowest_steps: List[LogEntry],
        average_duration: float,
    ) -> None:
        self.total_duration = total_duration
        self.step_count = step_count
        self.slowest_steps = slowest_steps
        self.average_duration = average_duration


class DebugFormatter:

    @staticmethod
    def format_as_text(execution: ExecutionLog) -> str:
        lines: List[str] = []
        duration = (
            execution.end_time - execution.start_time
            if execution.end_time is not None
            else (time.time() * 1000) - execution.start_time
        )
        status = '❌' if execution.errored else '✓'

        lines.append(f'{status} Execution: {execution.execution_id}')
        lines.append(f'Duration: {duration:.0f}ms')
        lines.append(f'Steps: {len(DebugFormatter._flatten(execution.logs))}')
        lines.append('')

        DebugFormatter._format_logs(execution.logs, lines, 0)
        return '\n'.join(lines)

    @staticmethod
    def get_timeline(execution: ExecutionLog) -> List[LogEntry]:
        return sorted(
            DebugFormatter._flatten(execution.logs),
            key=lambda l: l.timestamp,
        )

    @staticmethod
    def get_performance_summary(execution: ExecutionLog) -> PerformanceSummary:
        logs = DebugFormatter._flatten(execution.logs)
        total_duration = (
            execution.end_time - execution.start_time
            if execution.end_time is not None
            else (time.time() * 1000) - execution.start_time
        )

        with_duration = [l for l in logs if l.duration is not None]
        slowest = sorted(with_duration, key=lambda l: -(l.duration or 0))[:10]

        return PerformanceSummary(
            total_duration=total_duration,
            step_count=len(logs),
            slowest_steps=slowest,
            average_duration=total_duration / len(logs) if logs else 0,
        )

    @staticmethod
    def _format_logs(logs: List[LogEntry], lines: List[str], depth: int) -> None:
        indent = '  ' * depth
        for log in logs:
            status = '❌' if log.error else '✓'
            name = log.statement_name or '(anonymous)'
            lines.append(
                f'{indent}{status} {name} => {log.function_name} ({log.duration or 0:.0f}ms)'
            )
            if log.error:
                lines.append(f'{indent}  Error: {log.error}')
            if log.children:
                DebugFormatter._format_logs(log.children, lines, depth + 1)

    @staticmethod
    def _flatten(logs: List[LogEntry]) -> List[LogEntry]:
        result: List[LogEntry] = []

        def visit(entries: List[LogEntry]) -> None:
            for log in entries:
                result.append(log)
                if log.children:
                    visit(log.children)

        visit(logs)
        return result
