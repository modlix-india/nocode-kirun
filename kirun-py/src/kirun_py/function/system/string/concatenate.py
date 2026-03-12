from __future__ import annotations

from typing import Any, Dict, List, TYPE_CHECKING

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


class Concatenate(AbstractFunction):

    VALUE: str = 'value'

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('Concatenate')
            .set_namespace(Namespaces.STRING)
            .set_parameters({
                Concatenate.VALUE: Parameter(
                    Concatenate.VALUE,
                    Schema()
                    .set_name(Concatenate.VALUE)
                    .set_type(TypeUtil.of(SchemaType.STRING)),
                ).set_variable_argument(True),
            })
            .set_events(dict([
                Event.output_event_map_entry(
                    {Concatenate.VALUE: Schema.of_string(Concatenate.VALUE)}
                )
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        values: List[str] = context.get_arguments().get(Concatenate.VALUE)

        concatenated: str = ''
        if values:
            for v in values:
                concatenated += str(v) if v is not None else ''

        return FunctionOutput([
            EventResult.output_of({Concatenate.VALUE: concatenated})
        ])
