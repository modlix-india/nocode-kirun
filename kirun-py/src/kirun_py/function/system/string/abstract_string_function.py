from __future__ import annotations

import re
from abc import abstractmethod
from typing import Any, Callable, Dict, List, Optional, Tuple, TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class AbstractStringFunction(AbstractFunction):

    PARAMETER_STRING_NAME: str = 'string'
    PARAMETER_SEARCH_STRING_NAME: str = 'searchString'
    PARAMETER_SECOND_STRING_NAME: str = 'secondString'
    PARAMETER_THIRD_STRING_NAME: str = 'thirdString'
    PARAMETER_INDEX_NAME: str = 'index'
    PARAMETER_SECOND_INDEX_NAME: str = 'secondIndex'
    EVENT_RESULT_NAME: str = 'result'

    PARAMETER_STRING: Parameter = Parameter(
        'string', Schema.of_string('string')
    )
    PARAMETER_SECOND_STRING: Parameter = Parameter(
        'secondString', Schema.of_string('secondString')
    )
    PARAMETER_THIRD_STRING: Parameter = Parameter(
        'thirdString', Schema.of_string('thirdString')
    )
    PARAMETER_INDEX: Parameter = Parameter(
        'index', Schema.of_integer('index')
    )
    PARAMETER_SECOND_INDEX: Parameter = Parameter(
        'secondIndex', Schema.of_integer('secondIndex')
    )
    PARAMETER_SEARCH_STRING: Parameter = Parameter(
        'searchString', Schema.of_string('string')
    )

    EVENT_STRING: Event = Event(
        Event.OUTPUT, {'result': Schema.of_string('result')}
    )
    EVENT_BOOLEAN: Event = Event(
        Event.OUTPUT, {'result': Schema.of_boolean('result')}
    )
    EVENT_INT: Event = Event(
        Event.OUTPUT, {'result': Schema.of_integer('result')}
    )
    EVENT_ARRAY: Event = Event(
        Event.OUTPUT, {'result': Schema.of_array('result')}
    )

    def __init__(
        self,
        namespace: str,
        function_name: str,
        event: Event,
        *parameters: Parameter,
    ) -> None:
        super().__init__()
        param_map: Dict[str, Parameter] = {}
        for p in parameters:
            param_map[p.get_parameter_name()] = p

        self._signature = (
            FunctionSignature(function_name)
            .set_namespace(namespace)
            .set_parameters(param_map)
            .set_events({event.get_name(): event})
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    # ---- Factory helpers that create (name, function) tuples ----

    @staticmethod
    def of_entry_string_and_string_output(
        name: str,
        fun: Callable[[str], str],
    ) -> Tuple[str, AbstractFunction]:

        class _Fn(AbstractStringFunction):
            async def internal_execute(
                self, context: FunctionExecutionParameters
            ) -> FunctionOutput:
                s: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_STRING_NAME
                )
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractStringFunction.EVENT_RESULT_NAME: fun(s)}
                    )
                ])

        return (
            name,
            _Fn(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_STRING,
                AbstractStringFunction.PARAMETER_STRING,
            ),
        )

    @staticmethod
    def of_entry_string_and_boolean_output(
        name: str,
        fun: Callable[[str], bool],
    ) -> Tuple[str, AbstractFunction]:

        class _Fn(AbstractStringFunction):
            async def internal_execute(
                self, context: FunctionExecutionParameters
            ) -> FunctionOutput:
                s: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_STRING_NAME
                )
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractStringFunction.EVENT_RESULT_NAME: fun(s)}
                    )
                ])

        return (
            name,
            _Fn(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_BOOLEAN,
                AbstractStringFunction.PARAMETER_STRING,
            ),
        )

    @staticmethod
    def of_entry_string_and_integer_output(
        name: str,
        fun: Callable[[str], int],
    ) -> Tuple[str, AbstractFunction]:

        class _Fn(AbstractStringFunction):
            async def internal_execute(
                self, context: FunctionExecutionParameters
            ) -> FunctionOutput:
                s: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_STRING_NAME
                )
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractStringFunction.EVENT_RESULT_NAME: fun(s)}
                    )
                ])

        return (
            name,
            _Fn(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_INT,
                AbstractStringFunction.PARAMETER_STRING,
            ),
        )

    @staticmethod
    def of_entry_string_string_and_boolean_output(
        name: str,
        fun: Callable[[str, str], bool],
    ) -> Tuple[str, AbstractFunction]:

        class _Fn(AbstractStringFunction):
            async def internal_execute(
                self, context: FunctionExecutionParameters
            ) -> FunctionOutput:
                s: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_STRING_NAME
                )
                ss: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME
                )
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractStringFunction.EVENT_RESULT_NAME: fun(s, ss)}
                    )
                ])

        return (
            name,
            _Fn(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_BOOLEAN,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_SEARCH_STRING,
            ),
        )

    @staticmethod
    def of_entry_string_string_and_integer_output(
        name: str,
        fun: Callable[[str, str], int],
    ) -> Tuple[str, AbstractFunction]:

        class _Fn(AbstractStringFunction):
            async def internal_execute(
                self, context: FunctionExecutionParameters
            ) -> FunctionOutput:
                s: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_STRING_NAME
                )
                ss: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME
                )
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractStringFunction.EVENT_RESULT_NAME: fun(s, ss)}
                    )
                ])

        return (
            name,
            _Fn(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_INT,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_SEARCH_STRING,
            ),
        )

    @staticmethod
    def of_entry_string_integer_and_string_output(
        name: str,
        fun: Callable[[str, int], str],
    ) -> Tuple[str, AbstractFunction]:

        class _Fn(AbstractStringFunction):
            async def internal_execute(
                self, context: FunctionExecutionParameters
            ) -> FunctionOutput:
                s: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_STRING_NAME
                )
                count: int = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_INDEX_NAME
                )
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractStringFunction.EVENT_RESULT_NAME: fun(s, count)}
                    )
                ])

        return (
            name,
            _Fn(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_STRING,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_INDEX,
            ),
        )

    @staticmethod
    def of_entry_string_string_integer_and_integer_output(
        name: str,
        fun: Callable[[str, str, int], int],
    ) -> Tuple[str, AbstractFunction]:

        class _Fn(AbstractStringFunction):
            async def internal_execute(
                self, context: FunctionExecutionParameters
            ) -> FunctionOutput:
                s: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_STRING_NAME
                )
                ss: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_SEARCH_STRING_NAME
                )
                ind: int = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_INDEX_NAME
                )
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractStringFunction.EVENT_RESULT_NAME: fun(s, ss, ind)}
                    )
                ])

        return (
            name,
            _Fn(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_INT,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_SEARCH_STRING,
                AbstractStringFunction.PARAMETER_INDEX,
            ),
        )

    @staticmethod
    def of_entry_string_integer_integer_and_string_output(
        name: str,
        fun: Callable[[str, int, int], str],
    ) -> Tuple[str, AbstractFunction]:

        class _Fn(AbstractStringFunction):
            async def internal_execute(
                self, context: FunctionExecutionParameters
            ) -> FunctionOutput:
                s: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_STRING_NAME
                )
                ind1: int = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_INDEX_NAME
                )
                ind2: int = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_SECOND_INDEX_NAME
                )
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractStringFunction.EVENT_RESULT_NAME: fun(s, ind1, ind2)}
                    )
                ])

        return (
            name,
            _Fn(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_STRING,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_INDEX,
                AbstractStringFunction.PARAMETER_SECOND_INDEX,
            ),
        )

    @staticmethod
    def of_entry_string_string_string_and_string_output(
        name: str,
        fun: Callable[[str, str, str], str],
    ) -> Tuple[str, AbstractFunction]:

        class _Fn(AbstractStringFunction):
            async def internal_execute(
                self, context: FunctionExecutionParameters
            ) -> FunctionOutput:
                s1: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_STRING_NAME
                )
                s2: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_SECOND_STRING_NAME
                )
                s3: str = context.get_arguments().get(
                    AbstractStringFunction.PARAMETER_THIRD_STRING_NAME
                )
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractStringFunction.EVENT_RESULT_NAME: fun(s1, s2, s3)}
                    )
                ])

        return (
            name,
            _Fn(
                Namespaces.STRING,
                name,
                AbstractStringFunction.EVENT_STRING,
                AbstractStringFunction.PARAMETER_STRING,
                AbstractStringFunction.PARAMETER_SECOND_STRING,
                AbstractStringFunction.PARAMETER_THIRD_STRING,
            ),
        )
