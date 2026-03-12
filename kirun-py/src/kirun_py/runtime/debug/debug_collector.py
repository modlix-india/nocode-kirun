from __future__ import annotations

import random
import string
import time
from typing import Any, Dict, List, Optional, Set

from kirun_py.runtime.debug.types import DebugEventListener, ExecutionLog, LogEntry


class DebugCollector:
    """Simple debug collector -- single class that handles everything."""

    _instance: Optional[DebugCollector] = None

    def __init__(self) -> None:
        self._executions: Dict[str, ExecutionLog] = {}
        self._execution_order: List[str] = []
        self._step_stacks: Dict[str, List[LogEntry]] = {}
        self._pending_logs: Dict[str, LogEntry] = {}
        self._listeners: Set[DebugEventListener] = set()
        self._enabled: bool = False
        self._max_executions: int = 10

    @classmethod
    def get_instance(cls) -> DebugCollector:
        if cls._instance is None:
            cls._instance = DebugCollector()
        return cls._instance

    def enable(self) -> None:
        self._enabled = True

    def disable(self) -> None:
        self._enabled = False

    def is_enabled(self) -> bool:
        return self._enabled

    # ------------------------------------------------------------------
    # Execution lifecycle
    # ------------------------------------------------------------------

    def start_execution(
        self,
        execution_id: str,
        function_name: str,
        definition: Any = None,
    ) -> None:
        if not self._enabled:
            return

        execution = self._executions.get(execution_id)

        if execution is None:
            execution = ExecutionLog(execution_id, time.time() * 1000)
            self._executions[execution_id] = execution
            self._execution_order.append(execution_id)
            self._step_stacks[execution_id] = []

            # Prune old executions
            while len(self._execution_order) > self._max_executions:
                old_id = self._execution_order.pop(0)
                self._executions.pop(old_id, None)
                self._step_stacks.pop(old_id, None)

            self._emit('executionStart', execution_id, {'functionName': function_name})

        # Store definition (works for both new and nested calls)
        exec_log = self._executions.get(execution_id)
        if exec_log is not None and definition is not None:
            if function_name not in exec_log.definitions:
                exec_log.definitions[function_name] = definition

    def end_execution(self, execution_id: str) -> None:
        if not self._enabled:
            return

        execution = self._executions.get(execution_id)
        if execution is not None:
            execution.end_time = time.time() * 1000
            self._emit('executionEnd', execution_id, {
                'duration': execution.end_time - execution.start_time,
                'errored': execution.errored,
            })

    # ------------------------------------------------------------------
    # Step lifecycle
    # ------------------------------------------------------------------

    def start_step(
        self,
        execution_id: str,
        statement_name: str,
        function_name: str,
        args: Any = None,
        kirun_function_name: Optional[str] = None,
    ) -> Optional[str]:
        if not self._enabled:
            return None

        execution = self._executions.get(execution_id)
        if execution is None:
            return None

        now = time.time() * 1000
        rand_suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
        step_id = f'{int(now)}_{rand_suffix}'
        stack = self._step_stacks.get(execution_id, [])

        log = LogEntry(
            step_id=step_id,
            timestamp=now,
            function_name=function_name,
            statement_name=statement_name,
            kirun_function_name=kirun_function_name,
        )
        log.arguments = self._serialize(args)

        self._pending_logs[step_id] = log
        stack.append(log)
        self._step_stacks[execution_id] = stack

        self._emit('stepStart', execution_id, {
            'stepId': step_id,
            'statementName': statement_name,
            'functionName': function_name,
        })

        return step_id

    def end_step(
        self,
        execution_id: str,
        step_id: str,
        event_name: str,
        result: Any = None,
        error: Optional[str] = None,
    ) -> None:
        if not self._enabled:
            return

        log = self._pending_logs.get(step_id)
        if log is None:
            return

        execution = self._executions.get(execution_id)
        if execution is None:
            return

        # Complete the log
        log.duration = (time.time() * 1000) - log.timestamp
        log.result = self._serialize(result)
        log.event_name = event_name
        log.error = error

        # Remove from stack
        stack = self._step_stacks.get(execution_id, [])
        idx = next((i for i, l in enumerate(stack) if l.step_id == step_id), -1)
        if idx != -1:
            stack.pop(idx)

        # Add to parent's children or to root logs
        if stack:
            stack[-1].children.append(log)
        else:
            execution.logs.append(log)

        self._pending_logs.pop(step_id, None)

        if error:
            execution.errored = True
            self._emit('executionErrored', execution_id)

        self._emit('stepEnd', execution_id, {'log': log})

    def mark_errored(self, execution_id: str) -> None:
        execution = self._executions.get(execution_id)
        if execution is not None:
            execution.errored = True
            self._emit('executionErrored', execution_id)

    # ------------------------------------------------------------------
    # Query methods
    # ------------------------------------------------------------------

    def get_execution(self, execution_id: str) -> Optional[ExecutionLog]:
        return self._executions.get(execution_id)

    def get_last_execution(self) -> Optional[ExecutionLog]:
        if not self._execution_order:
            return None
        last_id = self._execution_order[-1]
        return self._executions.get(last_id)

    def get_definition(self, execution_id: str, key: str) -> Any:
        execution = self._executions.get(execution_id)
        if execution is None:
            return None
        return execution.definitions.get(key)

    def get_flat_logs(self, execution_id: str) -> List[LogEntry]:
        execution = self._executions.get(execution_id)
        if execution is None:
            return []

        result: List[LogEntry] = []

        def flatten(logs: List[LogEntry]) -> None:
            for log in logs:
                result.append(log)
                if log.children:
                    flatten(log.children)

        flatten(execution.logs)
        return result

    def get_all_execution_ids(self) -> List[str]:
        return list(self._execution_order)

    def clear(self) -> None:
        self._executions.clear()
        self._execution_order.clear()
        self._step_stacks.clear()
        self._pending_logs.clear()
        self._listeners.clear()

    # ------------------------------------------------------------------
    # Event listeners
    # ------------------------------------------------------------------

    def add_event_listener(self, listener: DebugEventListener):
        self._listeners.add(listener)

        def unsubscribe() -> None:
            self._listeners.discard(listener)

        return unsubscribe

    def remove_event_listener(self, listener: DebugEventListener) -> None:
        self._listeners.discard(listener)

    # ------------------------------------------------------------------
    # Internal helpers
    # ------------------------------------------------------------------

    def _emit(self, type_: str, execution_id: str, data: Any = None) -> None:
        event = {'type': type_, 'executionId': execution_id, 'data': data}
        for listener in list(self._listeners):
            try:
                listener(event)
            except Exception:
                pass

    def _serialize(self, value: Any) -> Any:
        if value is None:
            return value
        if isinstance(value, dict):
            return {k: self._serialize(v) for k, v in value.items()}
        if isinstance(value, (list, tuple)):
            return [self._serialize(v) for v in value]
        return value
