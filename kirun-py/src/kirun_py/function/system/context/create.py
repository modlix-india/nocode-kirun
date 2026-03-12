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
from kirun_py.runtime.context_element import ContextElement
from kirun_py.util.null_check import is_null_value
from kirun_py.util.string.string_formatter import StringFormatter

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

NAME = 'name'
SCHEMA = 'schema'


class Create(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('Create')
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
                Parameter.of_entry(SCHEMA, Schema.SCHEMA, False, ParameterType.CONSTANT),
            ]))
            .set_events(dict([
                Event.output_event_map_entry({}),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        name: str = args.get(NAME) if args else None

        ctx = context.get_context()
        if ctx is not None and name in ctx:
            raise KIRuntimeException(
                StringFormatter.format("Context already has an element for '$' ", name)
            )

        schema_raw = args.get(SCHEMA) if args else None
        s = Schema.from_value(schema_raw) if schema_raw is not None else None

        if s is None:
            raise KIRuntimeException('Schema is not supplied.')

        default_val = s.get_default_value() if not is_null_value(s.get_default_value()) else None

        if ctx is not None:
            ctx[name] = ContextElement(element=default_val)

        return FunctionOutput([EventResult.output_of({})])
