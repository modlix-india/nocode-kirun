from __future__ import annotations

from typing import TYPE_CHECKING

from dateutil import parser as dateutil_parser

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class IsValidISODate(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()

        self._signature = (
            FunctionSignature('IsValidISODate')
            .set_namespace(Namespaces.DATE)
            .set_parameters(dict([
                Parameter.of_entry(
                    AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                    Schema.of_string(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME),
                ),
            ]))
            .set_events(dict([
                Event.output_event_map_entry(
                    {AbstractDateFunction.EVENT_RESULT_NAME: Schema.of_boolean(AbstractDateFunction.EVENT_RESULT_NAME)},
                ),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}
        timestamp = args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME, '')

        is_valid = True
        try:
            dateutil_parser.isoparse(timestamp)
        except (ValueError, TypeError):
            is_valid = False

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_RESULT_NAME: is_valid}
            ),
        ])
