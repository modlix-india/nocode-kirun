from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction
from kirun_py.function.system.date.common import get_datetime, datetime_to_iso

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class LastFirstOf(AbstractFunction):

    def __init__(self, is_last: bool) -> None:
        super().__init__()
        self._is_last = is_last

        self._signature = (
            FunctionSignature('LastOf' if is_last else 'FirstOf')
            .set_namespace(Namespaces.DATE)
            .set_parameters({
                AbstractDateFunction.PARAMETER_TIMESTAMP_NAME: Parameter(
                    AbstractDateFunction.PARAMETER_TIMESTAMP_NAME,
                    Schema.of_ref(Namespaces.DATE + '.Timestamp'),
                ).set_variable_argument(True),
            })
            .set_events({
                AbstractDateFunction.EVENT_TIMESTAMP.get_name(): AbstractDateFunction.EVENT_TIMESTAMP,
            })
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}
        timestamps = args.get(AbstractDateFunction.PARAMETER_TIMESTAMP_NAME)

        if not timestamps:
            raise ValueError('No timestamps provided')

        datetimes = [get_datetime(ts) for ts in timestamps]
        datetimes.sort()

        chosen = datetimes[-1] if self._is_last else datetimes[0]

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_TIMESTAMP_NAME: datetime_to_iso(chosen)}
            ),
        ])
