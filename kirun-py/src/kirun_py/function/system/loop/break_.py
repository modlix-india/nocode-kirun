from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

STEP_NAME = 'stepName'


class Break(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('Break')
            .set_namespace(Namespaces.SYSTEM_LOOP)
            .set_parameters(dict([
                Parameter.of_entry(STEP_NAME, Schema.of(STEP_NAME, SchemaType.STRING)),
            ]))
            .set_events(dict([
                Event.output_event_map_entry({}),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        step_name: str = args.get(STEP_NAME) if args else None

        context.get_execution_context()[step_name] = True

        return FunctionOutput([EventResult.output_of({})])
