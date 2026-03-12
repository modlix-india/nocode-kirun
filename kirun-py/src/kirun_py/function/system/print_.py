from __future__ import annotations

import sys
from typing import Any, List, Optional, TYPE_CHECKING

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


class Print(AbstractFunction):

    VALUES: str = 'values'
    STREAM: str = 'stream'
    LOG: str = 'LOG'
    ERROR: str = 'ERROR'

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('Print')
            .set_namespace(Namespaces.SYSTEM)
            .set_parameters(dict([
                Parameter.of_entry(Print.VALUES, Schema.of_any(Print.VALUES), True),
                Parameter.of_entry(
                    Print.STREAM,
                    Schema.of_string(Print.STREAM)
                    .set_enums([Print.LOG, Print.ERROR])
                    .set_default_value(Print.LOG),
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
        values: Optional[List[Any]] = args.get(Print.VALUES) if args else None
        stream: str = args.get(Print.STREAM, Print.LOG) if args else Print.LOG

        if values is not None:
            output = sys.stderr if stream == Print.ERROR else sys.stdout
            print(*values, file=output)

        return FunctionOutput([EventResult.output_of({})])
