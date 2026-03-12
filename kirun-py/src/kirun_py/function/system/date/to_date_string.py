from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.json.schema.schema import Schema
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.parameter import Parameter
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.common import get_datetime

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


# Mapping from Luxon-style format tokens to Python strftime tokens.
# This covers the most common tokens; extend as needed.
_LUXON_TO_STRFTIME = {
    'yyyy': '%Y',
    'yy': '%y',
    'MMMM': '%B',
    'MMM': '%b',
    'MM': '%m',
    'M': '%-m',
    'dd': '%d',
    'd': '%-d',
    'EEEE': '%A',
    'EEE': '%a',
    'HH': '%H',
    'H': '%-H',
    'hh': '%I',
    'h': '%-I',
    'mm': '%M',
    'ss': '%S',
    'SSS': '%f',
    'a': '%p',
    'ZZZZ': '%z',
    'ZZ': '%z',
    'Z': '%z',
}


def _convert_luxon_format(fmt: str) -> str:
    """Best-effort conversion of Luxon format string to strftime format."""
    result = []
    i = 0
    while i < len(fmt):
        matched = False
        # Try longest tokens first
        for length in (4, 3, 2, 1):
            token = fmt[i:i + length]
            if token in _LUXON_TO_STRFTIME:
                result.append(_LUXON_TO_STRFTIME[token])
                i += length
                matched = True
                break
        if not matched:
            result.append(fmt[i])
            i += 1
    return ''.join(result)


class ToDateString(AbstractDateFunction):

    PARAMETER_FORMAT_NAME: str = 'format'
    PARAMETER_LOCALE_NAME: str = 'locale'

    def __init__(self) -> None:
        super().__init__(
            'ToDateString',
            AbstractDateFunction.EVENT_STRING,
            AbstractDateFunction.PARAMETER_TIMESTAMP,
            Parameter.of(
                ToDateString.PARAMETER_FORMAT_NAME,
                Schema.of_string(ToDateString.PARAMETER_FORMAT_NAME),
            ),
            Parameter.of(
                ToDateString.PARAMETER_LOCALE_NAME,
                Schema.of_string(ToDateString.PARAMETER_LOCALE_NAME).set_default_value(''),
            ),
        )

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}

        timestamp = args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
        dt = get_datetime(timestamp)

        fmt = args.get(ToDateString.PARAMETER_FORMAT_NAME, '')

        strftime_fmt = _convert_luxon_format(fmt)
        result = dt.strftime(strftime_fmt)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_RESULT_NAME: result}
            ),
        ])
