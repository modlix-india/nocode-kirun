from __future__ import annotations

from datetime import datetime, timezone
from typing import TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.function.system.date.abstract_date_function import AbstractDateFunction

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class EpochToTimestamp(AbstractFunction):

    def __init__(self, name: str, is_seconds: bool) -> None:
        super().__init__()
        self._is_seconds = is_seconds
        self._param_name = 'epochSeconds' if is_seconds else 'epochMilliseconds'

        self._signature = (
            FunctionSignature(name)
            .set_namespace(Namespaces.DATE)
            .set_parameters({
                self._param_name: Parameter.of(
                    self._param_name,
                    Schema()
                    .set_name(self._param_name)
                    .set_type(TypeUtil.of(SchemaType.LONG, SchemaType.INTEGER, SchemaType.STRING)),
                ),
            })
            .set_events({
                AbstractDateFunction.EVENT_TIMESTAMP.get_name(): AbstractDateFunction.EVENT_TIMESTAMP,
            })
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}
        raw_epoch = args.get(self._param_name)

        try:
            epoch = int(raw_epoch)
        except (ValueError, TypeError):
            raise ValueError('Please provide a valid value for {}.'.format(self._param_name))

        timestamp_ms = epoch * 1000 if self._is_seconds else epoch
        dt = datetime.fromtimestamp(timestamp_ms / 1000.0, tz=timezone.utc)

        return FunctionOutput([
            EventResult.output_of(
                {AbstractDateFunction.EVENT_TIMESTAMP_NAME: dt.isoformat()}
            ),
        ])
