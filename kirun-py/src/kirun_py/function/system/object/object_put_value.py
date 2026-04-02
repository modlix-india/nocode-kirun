from __future__ import annotations

from typing import TYPE_CHECKING

from kirun_py.function.abstract_function import AbstractFunction
from kirun_py.json.schema.schema import Schema
from kirun_py.model.event import Event
from kirun_py.model.event_result import EventResult
from kirun_py.model.function_output import FunctionOutput
from kirun_py.model.function_signature import FunctionSignature
from kirun_py.model.parameter import Parameter
from kirun_py.namespaces.namespaces import Namespaces
from kirun_py.runtime.expression.tokenextractor.object_value_setter_extractor import (
    ObjectValueSetterExtractor,
)

if TYPE_CHECKING:
    from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters

VALUE = 'value'
SOURCE = 'source'
KEY = 'key'
OVERWRITE = 'overwrite'
DELETE_KEY_ON_NULL = 'deleteKeyOnNull'


class ObjectPutValue(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('ObjectPutValue')
            .set_namespace(Namespaces.SYSTEM_OBJECT)
            .set_parameters(
                dict([
                    Parameter.of_entry(SOURCE, Schema.of_object(SOURCE)),
                    Parameter.of_entry(KEY, Schema.of_string(KEY)),
                    Parameter.of_entry(VALUE, Schema.of_any(VALUE)),
                    Parameter.of_entry(
                        OVERWRITE,
                        Schema.of_boolean(OVERWRITE).set_default_value(True),
                    ),
                    Parameter.of_entry(
                        DELETE_KEY_ON_NULL,
                        Schema.of_boolean(DELETE_KEY_ON_NULL).set_default_value(False),
                    ),
                ])
            )
            .set_events(
                dict([Event.output_event_map_entry({VALUE: Schema.of_object(VALUE)})])
            )
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(self, context: FunctionExecutionParameters) -> FunctionOutput:
        args = context.get_arguments() or {}
        source = args.get(SOURCE)
        key = args.get(KEY)
        value = args.get(VALUE)
        overwrite = args.get(OVERWRITE, True)
        delete_key_on_null = args.get(DELETE_KEY_ON_NULL, False)

        ove = ObjectValueSetterExtractor(source, 'Data.')
        ove.set_value(key, value, overwrite, delete_key_on_null)

        return FunctionOutput([EventResult.output_of({VALUE: ove.get_store()})])
