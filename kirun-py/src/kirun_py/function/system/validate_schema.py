from __future__ import annotations

from typing import Any, TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.exception.ki_runtime_exception import KIRuntimeException
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


class ValidateSchema(AbstractFunction):

    SOURCE: str = 'source'
    SCHEMA: str = 'schema'
    IS_VALID: str = 'isValid'

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('ValidateSchema')
            .set_namespace(Namespaces.SYSTEM_OBJECT)
            .set_parameters(dict([
                Parameter.of_entry(ValidateSchema.SOURCE, Schema.of_any(ValidateSchema.SCHEMA)),
                Parameter.of_entry(
                    ValidateSchema.SCHEMA,
                    Schema.SCHEMA,
                    False,
                    ParameterType.CONSTANT,
                ),
            ]))
            .set_events(dict([
                Event.output_event_map_entry({
                    ValidateSchema.IS_VALID: Schema.of_boolean(ValidateSchema.IS_VALID),
                }),
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments()
        element: Any = args.get(ValidateSchema.SOURCE) if args else None
        schema_raw = args.get(ValidateSchema.SCHEMA) if args else None
        schema = Schema.from_value(schema_raw) if schema_raw is not None else None

        if schema is None:
            raise KIRuntimeException('Schema is not supplied.')

        return await self._validate_schema(schema, context.get_schema_repository(), element)

    async def _validate_schema(
        self,
        target_schema: Schema,
        target_schema_repository: Repository,
        element: Any,
    ) -> FunctionOutput:
        try:
            await SchemaValidator.validate(
                None, target_schema, target_schema_repository, element
            )
            return FunctionOutput([
                EventResult.output_of({ValidateSchema.IS_VALID: True}),
            ])
        except Exception:
            return FunctionOutput([
                EventResult.output_of({ValidateSchema.IS_VALID: False}),
            ])
