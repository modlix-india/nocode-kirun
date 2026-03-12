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
SOURCE = 'source'


class JSONStringify(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('JSONStringify')
            .set_namespace(Namespaces.SYSTEM_JSON)
            .set_parameters(dict([
                Parameter.of_entry(SOURCE, Schema.of_any(SOURCE)),
            ]))
            .set_events(dict([
                Event.output_event_map_entry({VALUE: Schema.of_string(VALUE)}),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        source: Any = args.get(SOURCE) if args else None

        result = json.dumps(source if source is not None else None)

        return FunctionOutput([EventResult.output_of({VALUE: result})])
