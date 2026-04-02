from __future__ import annotations

from typing import Any, List, TYPE_CHECKING

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

VALUE = 'source'
DELIMITER = 'delimiter'
OUTPUT = 'result'


class Join(AbstractFunction):

    def __init__(self) -> None:
        super().__init__()
        self._signature = (
            FunctionSignature('Join')
            .set_namespace(Namespaces.SYSTEM_ARRAY)
            .set_parameters({
                VALUE: Parameter(
                    VALUE,
                    Schema.of_array(
                        VALUE,
                        Schema.of(
                            'each',
                            SchemaType.STRING,
                            SchemaType.INTEGER,
                            SchemaType.LONG,
                            SchemaType.DOUBLE,
                            SchemaType.FLOAT,
                            SchemaType.NULL,
                        ),
                    ),
                ),
                DELIMITER: Parameter(
                    DELIMITER,
                    Schema.of_string(DELIMITER).set_default_value(''),
                ),
            })
            .set_events(dict([
                Event.output_event_map_entry(
                    {OUTPUT: Schema.of_string(OUTPUT)}
                )
            ]))
        )

    def get_signature(self) -> FunctionSignature:
        return self._signature

    async def internal_execute(
        self, context: FunctionExecutionParameters
    ) -> FunctionOutput:
        source: List[Any] = context.get_arguments().get(VALUE)
        delimiter: str = context.get_arguments().get(DELIMITER)

        result = delimiter.join(str(x) if x is not None else '' for x in source)

        return FunctionOutput([
            EventResult.output_of({OUTPUT: result})
        ])
