from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters


class Reverse(AbstractFunction):

    VALUE: str = 'value'

    def __init__(self) -> None:
        super().__init__()

        self._signature = (
            FunctionSignature('Reverse')
            .set_namespace(Namespaces.STRING)
            .set_parameters({
                self.VALUE: Parameter(
                    self.VALUE,
                    Schema.of_string(self.VALUE),
                ).set_variable_argument(False),
            })
            .set_events(dict([
                Event.output_event_map_entry({
                    self.VALUE: Schema()
                    .set_type(TypeUtil.of(SchemaType.STRING))
                    .set_name(self.VALUE),
                })
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        actual_string: str = context.get_arguments().get(self.VALUE)
        reversed_string = actual_string[::-1]

        return FunctionOutput([
            EventResult.output_of({self.VALUE: reversed_string})
        ])
