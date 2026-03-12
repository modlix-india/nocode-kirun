from __future__ import annotations

from typing import Any, Optional, TYPE_CHECKING

from kirun_py.exception.ki_runtime_exception import KIRuntimeException
from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.convertor.enums.conversion_mode import ConversionMode
from kirun_py.json.schema.schema import Schema
from kirun_py.json.schema.validator.schema_validator import SchemaValidator
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.model.parameter_type import ParameterType
from kirun_py.namespaces.namespaces import Namespaces

if TYPE_CHECKING:
    from kirun_py.repository import Repository
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

SOURCE = 'source'
SCHEMA = 'schema'
VALUE = 'value'
CONVERSION_MODE = 'conversionMode'


def _get_conversion_modes() -> list:
    return [e.value for e in ConversionMode]


def _generic_value_of(val: Any) -> Optional[ConversionMode]:
    if val is None:
        return None
    try:
        return ConversionMode(val)
    except (ValueError, KeyError):
        return None


class ObjectConvert(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('ObjectConvert')
            .set_namespace(Namespaces.SYSTEM_OBJECT)
            .set_parameters(
                dict([
                    Parameter.of_entry(SOURCE, Schema.of_any(SCHEMA)),
                    Parameter.of_entry(
                        SCHEMA,
                        Schema.SCHEMA,
                        False,
                        ParameterType.CONSTANT,
                    ),
                    Parameter.of_entry(
                        CONVERSION_MODE,
                        Schema.of_string(CONVERSION_MODE).set_enums(
                            _get_conversion_modes()
                        ),
                    ),
                ])
            )
            .set_events(
                dict([Event.output_event_map_entry({VALUE: Schema.of_any(VALUE)})])
            )
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}
        element = args.get(SOURCE)
        schema_value = args.get(SCHEMA)

        schema: Optional[Schema] = Schema.from_value(schema_value)

        if schema is None:
            raise KIRuntimeException('Schema is not supplied.')

        mode_value = args.get(CONVERSION_MODE)
        mode: ConversionMode = _generic_value_of(mode_value) or ConversionMode.STRICT

        return await self._convert_to_schema(
            schema, context.get_schema_repository(), element, mode
        )

    @staticmethod
    async def _convert_to_schema(
        target_schema: Schema,
        target_schema_repository: Repository,
        element: Any,
        mode: ConversionMode,
    ) -> FunctionOutput:
        try:
            converted = await SchemaValidator.validate(
                [],
                target_schema,
                target_schema_repository,
                element,
            )
            return FunctionOutput([EventResult.output_of({VALUE: converted})])
        except Exception as error:
            raise KIRuntimeException(str(error))
