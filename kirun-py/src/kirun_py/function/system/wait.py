from __future__ import annotations

import asyncio
from typing import TYPE_CHECKING

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


class Wait(AbstractFunction):

    MILLIS: str = 'millis'

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('Wait')
            .set_namespace(Namespaces.SYSTEM)
            .set_parameters(dict([
                Parameter.of_entry(
                    Wait.MILLIS,
                    Schema.of_number(Wait.MILLIS).set_minimum(0).set_default_value(0),
                ),
            ]))
            .set_events(dict([
                Event.output_event_map_entry({}),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        millis = args.get(Wait.MILLIS, 0) if args else 0

        if millis and millis > 0:
            await asyncio.sleep(millis / 1000.0)

        return FunctionOutput([EventResult.output_of({})])
