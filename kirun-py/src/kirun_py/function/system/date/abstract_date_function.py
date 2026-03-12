from __future__ import annotations

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


class AbstractDateFunction(AbstractFunction):

    PARAMETER_TIMESTAMP_NAME: str = 'isoTimeStamp'
    PARAMETER_TIMESTAMP_NAME_ONE: str = 'isoTimeStamp1'
    PARAMETER_TIMESTAMP_NAME_TWO: str = 'isoTimeStamp2'
    PARAMETER_UNIT_NAME: str = 'unit'
    PARAMETER_NUMBER_NAME: str = 'number'

    PARAMETER_TIMESTAMP: Parameter = Parameter(
        PARAMETER_TIMESTAMP_NAME,
        Schema.of_ref(Namespaces.DATE + '.Timestamp'),
    )

    PARAMETER_TIMESTAMP_ONE: Parameter = Parameter(
        PARAMETER_TIMESTAMP_NAME_ONE,
        Schema.of_ref(Namespaces.DATE + '.Timestamp'),
    )

    PARAMETER_TIMESTAMP_TWO: Parameter = Parameter(
        PARAMETER_TIMESTAMP_NAME_TWO,
        Schema.of_ref(Namespaces.DATE + '.Timestamp'),
    )

    PARAMETER_VARIABLE_UNIT: Parameter = Parameter(
        PARAMETER_UNIT_NAME,
        Schema.of_ref(Namespaces.DATE + '.Timeunit'),
    ).set_variable_argument(True)

    PARAMETER_UNIT: Parameter = Parameter(
        PARAMETER_UNIT_NAME,
        Schema.of_ref(Namespaces.DATE + '.Timeunit'),
    )

    PARAMETER_NUMBER: Parameter = Parameter(
        PARAMETER_NUMBER_NAME,
        Schema.of_integer(PARAMETER_NUMBER_NAME),
    )

    EVENT_RESULT_NAME: str = 'result'
    EVENT_TIMESTAMP_NAME: str = 'isoTimeStamp'

    EVENT_INT: Event = Event(
        Event.OUTPUT,
        {EVENT_RESULT_NAME: Schema.of_integer(EVENT_RESULT_NAME)},
    )

    EVENT_STRING: Event = Event(
        Event.OUTPUT,
        {EVENT_RESULT_NAME: Schema.of_string(EVENT_RESULT_NAME)},
    )

    EVENT_LONG: Event = Event(
        Event.OUTPUT,
        {EVENT_RESULT_NAME: Schema.of_long(EVENT_RESULT_NAME)},
    )

    EVENT_BOOLEAN: Event = Event(
        Event.OUTPUT,
        {EVENT_RESULT_NAME: Schema.of_boolean(EVENT_RESULT_NAME)},
    )

    EVENT_TIMESTAMP: Event = Event(
        Event.OUTPUT,
        {EVENT_TIMESTAMP_NAME: Schema.of_ref(Namespaces.DATE + '.Timestamp')},
    )

    def __init__(
        self,
        function_name: str,
        event: Event,
        *parameters: Parameter,
    ) -> None:
        super().__init__()

        self._signature = (
            FunctionSignature(function_name)
            .set_namespace(Namespaces.DATE)
            .set_events({event.get_name(): event})
        )

        if parameters:
            param_map: Dict[str, Parameter] = {}
            for p in parameters:
                param_map[p.get_parameter_name()] = p
            self._signature.set_parameters(param_map)

    def get_signature(self) -> FunctionSignature:
        return self._signature

    # ----------------------------------------------------------------
    # Factory helpers that create concrete function instances inline
    # ----------------------------------------------------------------

    @staticmethod
    def of_entry_timestamp_and_integer_output(
        name: str,
        fun: Callable[[str], int],
    ) -> Tuple[str, AbstractFunction]:
        class _Fn(AbstractDateFunction):
            async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
                args = context.get_arguments() or {}
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractDateFunction.EVENT_RESULT_NAME: fun(args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME))}
                    ),
                ])
        return (name, _Fn(name, AbstractDateFunction.EVENT_INT, AbstractDateFunction.PARAMETER_TIMESTAMP))

    @staticmethod
    def of_entry_timestamp_and_boolean_output(
        name: str,
        fun: Callable[[str], bool],
    ) -> Tuple[str, AbstractFunction]:
        class _Fn(AbstractDateFunction):
            async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
                args = context.get_arguments() or {}
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractDateFunction.EVENT_RESULT_NAME: fun(args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME))}
                    ),
                ])
        return (name, _Fn(name, AbstractDateFunction.EVENT_BOOLEAN, AbstractDateFunction.PARAMETER_TIMESTAMP))

    @staticmethod
    def of_entry_timestamp_and_string_output(
        name: str,
        fun: Callable[[str], str],
    ) -> Tuple[str, AbstractFunction]:
        class _Fn(AbstractDateFunction):
            async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
                args = context.get_arguments() or {}
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractDateFunction.EVENT_RESULT_NAME: fun(args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME))}
                    ),
                ])
        return (name, _Fn(name, AbstractDateFunction.EVENT_STRING, AbstractDateFunction.PARAMETER_TIMESTAMP))

    @staticmethod
    def of_entry_timestamp_integer_and_timestamp_output(
        name: str,
        fun: Callable[[str, int], str],
    ) -> Tuple[str, AbstractFunction]:
        class _Fn(AbstractDateFunction):
            async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
                args = context.get_arguments() or {}
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractDateFunction.EVENT_RESULT_NAME: fun(
                            args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME),
                            args.get(AbstractDateFunction.PARAMETER_NUMBER_NAME),
                        )}
                    ),
                ])
        return (
            name,
            _Fn(
                name,
                AbstractDateFunction.EVENT_TIMESTAMP,
                AbstractDateFunction.PARAMETER_TIMESTAMP,
                AbstractDateFunction.PARAMETER_NUMBER,
            ),
        )

    @staticmethod
    def of_entry_timestamp_timestamp_and_t_output(
        name: str,
        event: Event,
        fun: Callable[..., Any],
        *parameters: Parameter,
    ) -> Tuple[str, AbstractFunction]:
        extra_params = list(parameters)

        class _Fn(AbstractDateFunction):
            async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
                args = context.get_arguments() or {}
                extra_args: List[Any] = []
                if extra_params:
                    extra_args = [args.get(p.get_parameter_name()) for p in extra_params]
                return FunctionOutput([
                    EventResult.output_of(
                        {AbstractDateFunction.EVENT_RESULT_NAME: fun(
                            args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_ONE),
                            args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME_TWO),
                            extra_args,
                        )}
                    ),
                ])

        all_params = [
            AbstractDateFunction.PARAMETER_TIMESTAMP_ONE,
            AbstractDateFunction.PARAMETER_TIMESTAMP_TWO,
        ] + extra_params

        return (name, _Fn(name, event, *all_params))
