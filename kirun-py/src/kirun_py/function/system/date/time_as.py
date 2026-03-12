from __future__ import annotations

from typing import Any, Dict, List, Union, TYPE_CHECKING

from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.common import get_datetime

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class TimeAs(AbstractDateFunction):

    EVENT_TIME_OBJECT_NAME: str = 'object'
    EVENT_TIME_ARRAY_NAME: str = 'array'

    def __init__(self, is_array: bool) -> None:
        event_name = TimeAs.EVENT_TIME_ARRAY_NAME if is_array else TimeAs.EVENT_TIME_OBJECT_NAME

        if is_array:
            schema = Schema.of_array(
                TimeAs.EVENT_TIME_ARRAY_NAME,
                Schema.of_integer('timeParts'),
            )
        else:
            schema = Schema.of_ref(Namespaces.DATE + '.TimeObject')

        super().__init__(
            'TimeAsArray' if is_array else 'TimeAsObject',
            Event(Event.OUTPUT, {event_name: schema}),
            AbstractDateFunction.PARAMETER_TIMESTAMP,
        )
        self._is_array = is_array

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}

        timestamp = args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)
        dt = get_datetime(timestamp)

        if self._is_array:
            result_key = TimeAs.EVENT_TIME_ARRAY_NAME
            result_value: Union[List[int], Dict[str, int]] = [
                dt.year,
                dt.month,
                dt.day,
                dt.hour,
                dt.minute,
                dt.second,
                dt.microsecond // 1000,  # milliseconds
            ]
        else:
            result_key = TimeAs.EVENT_TIME_OBJECT_NAME
            result_value = {
                'year': dt.year,
                'month': dt.month,
                'day': dt.day,
                'hour': dt.hour,
                'minute': dt.minute,
                'second': dt.second,
                'millisecond': dt.microsecond // 1000,
            }

        return FunctionOutput([
            EventResult.output_of({result_key: result_value}),
        ])
