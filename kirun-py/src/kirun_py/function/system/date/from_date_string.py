from __future__ import annotations

from datetime import datetime
from typing import TYPE_CHECKING

from kirun_py.json.schema.schema import Schema
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.parameter import Parameter
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.to_date_string import _convert_luxon_format

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class FromDateString(AbstractDateFunction):

    PARAMETER_FORMAT_NAME: str = 'format'
    PARAMETER_TIMESTAMP_STRING_NAME: str = 'timestampString'

    def __init__(self) -> None:
        super().__init__(
            'FromDateString',
            AbstractDateFunction.EVENT_TIMESTAMP,
            Parameter.of(
                FromDateString.PARAMETER_TIMESTAMP_STRING_NAME,
                Schema.of_string(FromDateString.PARAMETER_TIMESTAMP_STRING_NAME),
            ),
            Parameter.of(
                FromDateString.PARAMETER_FORMAT_NAME,
                Schema.of_string(FromDateString.PARAMETER_FORMAT_NAME),
            ),
        )

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}

        timestamp_string = args.get(FromDateString.PARAMETER_TIMESTAMP_STRING_NAME, '')
        fmt = args.get(FromDateString.PARAMETER_FORMAT_NAME, '')

        strftime_fmt = _convert_luxon_format(fmt)
        dt = datetime.strptime(timestamp_string, strftime_fmt)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_RESULT_NAME: dt.isoformat()}
            ),
        ])
