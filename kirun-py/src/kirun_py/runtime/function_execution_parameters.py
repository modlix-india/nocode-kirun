from __future__ import annotations

from typing import Any, Dict, List, Optional, TYPE_CHECKING

from kirun_py.runtime.context_element import ContextElement
from kirun_py.runtime.expression.tokenextractor.token_value_extractor import TokenValueExtractor
from kirun_py.runtime.statement_execution import StatementExecution
from kirun_py.runtime.tokenextractor.context_token_value_extractor import ContextTokenValueExtractor
from kirun_py.runtime.tokenextractor.output_map_token_value_extractor import OutputMapTokenValueExtractor
from kirun_py.util.uuid_ import uuid

if TYPE_CHECKING:
    from kirun_py.repository import Repository


class FunctionExecutionParameters:

    def __init__(
        self,
        function_repository: Repository,
        schema_repository: Repository,
        execution_id: Optional[str] = None,
    ) -> None:
        self._context: Optional[Dict[str, ContextElement]] = None
        self._args: Optional[Dict[str, Any]] = None
        self._events: Optional[Dict[str, List[Dict[str, Any]]]] = None
        self._statement_execution: Optional[StatementExecution] = None
        self._steps: Optional[Dict[str, Dict[str, Dict[str, Any]]]] = None
        self._count: int = 0
        self._function_repository: Repository = function_repository
        self._schema_repository: Repository = schema_repository
        self._execution_id: str = execution_id if execution_id is not None else uuid()
        self._execution_context: Dict[str, Any] = {}
        self._value_extractors: Dict[str, TokenValueExtractor] = {}

    def get_execution_id(self) -> str:
        return self._execution_id

    def get_context(self) -> Optional[Dict[str, ContextElement]]:
        return self._context

    def set_context(
        self, context: Dict[str, ContextElement]
    ) -> FunctionExecutionParameters:
        self._context = context
        x: TokenValueExtractor = ContextTokenValueExtractor(context)
        self._value_extractors[x.get_prefix()] = x
        return self

    def get_arguments(self) -> Optional[Dict[str, Any]]:
        return self._args

    def set_arguments(self, args: Dict[str, Any]) -> FunctionExecutionParameters:
        self._args = args
        return self

    def get_events(self) -> Optional[Dict[str, List[Dict[str, Any]]]]:
        return self._events

    def set_events(
        self, events: Dict[str, List[Dict[str, Any]]]
    ) -> FunctionExecutionParameters:
        self._events = events
        return self

    def get_statement_execution(self) -> Optional[StatementExecution]:
        return self._statement_execution

    def set_statement_execution(
        self, statement_execution: StatementExecution
    ) -> FunctionExecutionParameters:
        self._statement_execution = statement_execution
        return self

    def get_steps(self) -> Optional[Dict[str, Dict[str, Dict[str, Any]]]]:
        return self._steps

    def set_steps(
        self, steps: Dict[str, Dict[str, Dict[str, Any]]]
    ) -> FunctionExecutionParameters:
        self._steps = steps
        x: TokenValueExtractor = OutputMapTokenValueExtractor(steps)
        self._value_extractors[x.get_prefix()] = x
        return self

    def get_count(self) -> int:
        return self._count

    def set_count(self, count: int) -> FunctionExecutionParameters:
        self._count = count
        return self

    def get_values_map(self) -> Dict[str, TokenValueExtractor]:
        return self._value_extractors

    def get_function_repository(self) -> Repository:
        return self._function_repository

    def set_function_repository(
        self, function_repository: Repository
    ) -> FunctionExecutionParameters:
        self._function_repository = function_repository
        return self

    def get_schema_repository(self) -> Repository:
        return self._schema_repository

    def set_schema_repository(
        self, schema_repository: Repository
    ) -> FunctionExecutionParameters:
        self._schema_repository = schema_repository
        return self

    def add_token_value_extractor(
        self, *extractors: TokenValueExtractor
    ) -> FunctionExecutionParameters:
        for tve in extractors:
            self._value_extractors[tve.get_prefix()] = tve
        return self

    def set_values_map(
        self, values_map: Dict[str, TokenValueExtractor]
    ) -> FunctionExecutionParameters:
        for k, v in values_map.items():
            self._value_extractors[k] = v
        return self

    def set_execution_context(
        self, execution_context: Dict[str, Any]
    ) -> FunctionExecutionParameters:
        self._execution_context = execution_context
        return self

    def get_execution_context(self) -> Dict[str, Any]:
        return self._execution_context
