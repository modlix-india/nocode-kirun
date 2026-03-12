from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.json.schema.schema import Schema
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.common import get_datetime

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class IsBetween(AbstractDateFunction):

    PARAMETER_START_TIMESTAMP_NAME: str = 'startTimestamp'
    PARAMETER_END_TIMESTAMP_NAME: str = 'endTimestamp'
    PARAMETER_CHECK_TIMESTAMP_NAME: str = 'checkTimestamp'

    def __init__(self) -> None:
        super().__init__(
            'IsBetween',
            AbstractDateFunction.EVENT_BOOLEAN,
            Parameter.of(
                IsBetween.PARAMETER_START_TIMESTAMP_NAME,
                Schema.of_ref(Namespaces.DATE + '.Timestamp'),
            ),
            Parameter.of(
                IsBetween.PARAMETER_END_TIMESTAMP_NAME,
                Schema.of_ref(Namespaces.DATE + '.Timestamp'),
            ),
            Parameter.of(
                IsBetween.PARAMETER_CHECK_TIMESTAMP_NAME,
                Schema.of_ref(Namespaces.DATE + '.Timestamp'),
            ),
        )

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}

        start_dt = get_datetime(args.get(IsBetween.PARAMETER_START_TIMESTAMP_NAME))
        end_dt = get_datetime(args.get(IsBetween.PARAMETER_END_TIMESTAMP_NAME))
        check_dt = get_datetime(args.get(IsBetween.PARAMETER_CHECK_TIMESTAMP_NAME))

        if start_dt > end_dt:
            start_dt, end_dt = end_dt, start_dt

        result = start_dt <= check_dt <= end_dt

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_RESULT_NAME: result}
            ),
        ])
