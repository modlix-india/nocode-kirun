from __future__ import annotations

import json
from typing import Any, Optional, TYPE_CHECKING

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

VALUE = 'value'
ERROR = 'error'
ERROR_MESSAGE = 'errorMessage'
SOURCE = 'source'


class JSONParse(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('JSONParse')
            .set_namespace(Namespaces.SYSTEM_JSON)
            .set_parameters(dict([
                Parameter.of_entry(SOURCE, Schema.of_string(SOURCE)),
            ]))
            .set_events(dict([
                Event.event_map_entry(
                    ERROR,
                    {ERROR_MESSAGE: Schema.of_string(ERROR_MESSAGE)},
                ),
                Event.output_event_map_entry({VALUE: Schema.of_any(VALUE)}),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        source: Optional[str] = args.get(SOURCE) if args else None

        try:
            value = json.loads(source) if source else None
        except Exception as err:
            return FunctionOutput([
                EventResult.of(
                    ERROR,
                    {ERROR_MESSAGE: str(err) or 'Unknown Error parsing JSON'},
                ),
                EventResult.output_of({VALUE: None}),
            ])

        return FunctionOutput([EventResult.output_of({VALUE: value})])
