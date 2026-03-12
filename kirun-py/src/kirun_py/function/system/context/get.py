from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.string.string_format import StringFormat
from kirun_py.json.schema.type.schema_type import SchemaType
from kirun_py.json.schema.type.type_util import TypeUtil
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.model.parameter_type import ParameterType
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.util.string.string_formatter import StringFormatter

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

NAME = 'name'
VALUE = 'value'


class Get(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('Get')
            .set_namespace(Namespaces.SYSTEM_CTX)
            .set_parameters(dict([
                Parameter.of_entry(
                    NAME,
                    Schema()
                    .set_name(NAME)
                    .set_type(TypeUtil.of(SchemaType.STRING))
                    .set_min_length(1)
                    .set_format(StringFormat.REGEX)
                    .set_pattern('^[a-zA-Z_$][a-zA-Z_$0-9]*$'),
                    False,
                    ParameterType.CONSTANT,
                ),
            ]))
            .set_events(dict([
                Event.output_event_map_entry({VALUE: Schema.of_any(VALUE)}),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        name: str = args.get(NAME) if args else None

        ctx = context.get_context()
        if ctx is None or name not in ctx:
            raise KIRuntimeException(
                StringFormatter.format("Context don't have an element for '$' ", name)
            )

        return FunctionOutput([
            EventResult.output_of({VALUE: ctx[name].get_element()}),
        ])
