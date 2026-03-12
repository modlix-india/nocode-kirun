from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any

from kirun_py.exception.execution_exception import ExecutionException
from kirun_py.util.null_check import is_null_value
from kirun_py.util.string.string_formatter import StringFormatter


class UnaryOperator(ABC):

    @abstractmethod
    def apply(self, t: Any) -> Any:
        ...

    def null_check(self, e1: Any, op) -> None:
        if is_null_value(e1):
            raise ExecutionException(
                StringFormatter.format(
                    '$ cannot be applied to a null value',
                    op.get_operator_name(),
                )
            )
