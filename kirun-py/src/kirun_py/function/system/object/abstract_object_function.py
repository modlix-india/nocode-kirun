from __future__ import annotations

from abc import ABC

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces

VALUE = 'value'
SOURCE = 'source'


class AbstractObjectFunction(AbstractFunction, ABC):

    def __init__(self, function_name: str, value_schema: Schema) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature(function_name)
            .set_namespace(Namespaces.SYSTEM_OBJECT)
            .set_parameters(dict([Parameter.of_entry(SOURCE, Schema.of_any(SOURCE))]))
            .set_events(
                dict([Event.output_event_map_entry({VALUE: value_schema})])
            )
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature
