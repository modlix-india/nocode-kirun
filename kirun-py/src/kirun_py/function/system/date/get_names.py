from __future__ import annotations

import calendar
from typing import List, TYPE_CHECKING

try:
    from zoneinfo import available_timezones
except ImportError:
    from backports.zoneinfo import available_timezones  # type: ignore[no-redef]

from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.parameter import Parameter
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class GetNames(AbstractDateFunction):

    EVENT_NAMES_NAME: str = 'names'
    PARAMETER_UNIT_NAME: str = 'unit'
    PARAMETER_LOCALE_NAME: str = 'locale'

    def __init__(self) -> None:
        super().__init__(
            'GetNames',
            Event(
                GetNames.EVENT_NAMES_NAME,
                {
                    GetNames.EVENT_NAMES_NAME: Schema.of_array(
                        GetNames.EVENT_NAMES_NAME,
                        Schema.of_string(GetNames.EVENT_NAMES_NAME),
                    ),
                },
            ),
            Parameter(
                GetNames.PARAMETER_UNIT_NAME,
                Schema.of_string(GetNames.PARAMETER_UNIT_NAME).set_enums([
                    'TIMEZONES',
                    'MONTHS',
                    'WEEKDAYS',
                ]),
            ),
            Parameter(
                GetNames.PARAMETER_LOCALE_NAME,
                Schema.of_string(GetNames.PARAMETER_LOCALE_NAME).set_default_value('system'),
            ),
        )

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}
        unit = args.get(GetNames.PARAMETER_UNIT_NAME, '')
        # locale parameter is accepted but Python calendar uses system locale
        return FunctionOutput([
            EventResult.output_of(
                {GetNames.EVENT_NAMES_NAME: self._get_names(unit)}
            ),
        ])

    @staticmethod
    def _get_names(unit: str) -> List[str]:
        if unit == 'TIMEZONES':
            return sorted(available_timezones())

        if unit == 'MONTHS':
            # calendar.month_name[1..12]
            return [calendar.month_name[i] for i in range(1, 13)]

        if unit == 'WEEKDAYS':
            # Monday..Sunday
            return [calendar.day_name[i] for i in range(7)]

        return []
