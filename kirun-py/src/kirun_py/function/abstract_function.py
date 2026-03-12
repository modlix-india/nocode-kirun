from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any, Dict, List, Optional, Tuple, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.validator.schema_validator import SchemaValidator
from kirun_py.model.event import Event
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.util.error_message_formatter import ErrorMessageFormatter
from kirun_py.util.null_check import is_null_value
from kirun_py.util.tuples import Tuple2

if TYPE_CHECKING:
    from kirun_py.repository import Repository
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
    from kirun_py.runtime.statement_execution import StatementExecution


class AbstractFunction(ABC):

    async def validate_arguments(
        self,
        args: Dict[str, Any],
        schema_repository: Repository,
        statement_execution: Optional[StatementExecution],
    ) -> Dict[str, Any]:
        retmap: Dict[str, Any] = {}

        for key, param in self.get_signature().get_parameters().items():
            try:
                tup = await self._validate_argument(
                    args, schema_repository, key, param
                )
                retmap[tup.get_t1()] = tup.get_t2()
            except Exception as err:
                signature = self.get_signature()
                function_name = ErrorMessageFormatter.format_function_name(
                    signature.get_namespace(), signature.get_name()
                )
                statement_name = ErrorMessageFormatter.format_statement_name(
                    statement_execution.get_statement().get_statement_name()
                    if statement_execution is not None
                    else None
                )
                error_message = ErrorMessageFormatter.format_error_message(err)
                raise KIRuntimeException(
                    ErrorMessageFormatter.build_function_execution_error(
                        function_name,
                        statement_name,
                        error_message,
                        param.get_parameter_name(),
                        param.get_schema(),
                    )
                )

        return retmap

    async def _validate_argument(
        self,
        args: Dict[str, Any],
        schema_repository: Repository,
        key: str,
        param: Parameter,
    ) -> Tuple2[str, Any]:
        json_element: Any = args.get(key)

        if is_null_value(json_element) and not param.is_variable_argument():
            return Tuple2(
                key,
                await SchemaValidator.validate(
                    None, param.get_schema(), schema_repository, None
                ),
            )

        if not param.is_variable_argument():
            return Tuple2(
                key,
                await SchemaValidator.validate(
                    None, param.get_schema(), schema_repository, json_element
                ),
            )

        array: Optional[List[Any]] = None

        if isinstance(json_element, list):
            array = list(json_element)
        else:
            array = []
            if not is_null_value(json_element):
                array.append(json_element)
            elif not is_null_value(param.get_schema().get_default_value()):
                array.append(param.get_schema().get_default_value())

        for i in range(len(array)):
            array[i] = await SchemaValidator.validate(
                None, param.get_schema(), schema_repository, array[i]
            )

        return Tuple2(key, array)

    async def execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args: Dict[str, Any] = await self.validate_arguments(
            context.get_arguments() if context.get_arguments() is not None else {},
            context.get_schema_repository(),
            context.get_statement_execution(),
        )
        context.set_arguments(args)
        try:
            return await self.internal_execute(context)
        except Exception as err:
            signature = self.get_signature()
            function_name = ErrorMessageFormatter.format_function_name(
                signature.get_namespace(), signature.get_name()
            )
            statement_name = ErrorMessageFormatter.format_statement_name(
                context.get_statement_execution().get_statement().get_statement_name()
                if context.get_statement_execution() is not None
                else None
            )
            error_message = ErrorMessageFormatter.format_error_message(err)
            raise KIRuntimeException(
                ErrorMessageFormatter.build_function_execution_error(
                    function_name,
                    statement_name,
                    error_message,
                )
            )

    def get_probable_event_signature(
        self, probable_parameters: Dict[str, List[Schema]]
    ) -> Dict[str, Event]:
        return self.get_signature().get_events()

    @abstractmethod
    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        ...

    @abstractmethod
    def get_signature(self) -> FunctionSignature:
        ...
